package org.fenixedu.idcards.tasks;

import com.google.common.base.Strings;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.custom.ReadCustomTask;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.idcards.exception.SantanderCardNoPermissionException;
import org.fenixedu.idcards.service.IUserInfoService;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderMissingInformationException;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderEntryValidator;
import org.fenixedu.santandersdk.service.SantanderSdkService;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import pt.ist.fenixframework.Atomic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author Tiago Pinho
 */
public class FixRequestsWithIncorrectActionTask extends ReadCustomTask {

    private static final String SIBS_CARDS_DATA_CSV_FILENAME = "/absolute/path/to/sibs/cards-data.csv";

    private static final String ERROR_DESCRIPTION = "Indicador de ação inválido";
    private static final String RESPONSE_LINE = "000011070Indicador de ação inválido";

    private static final HashMap<String, HashSet<SibsEntry>> sibsEntriesMap = new HashMap<>();

    private static final SantanderIdCardsService cardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);
    private static final IUserInfoService userInfoService = BennuSpringContextHelper.getBean(IUserInfoService.class);
    private static final SantanderSdkService sdkService = BennuSpringContextHelper.getBean(SantanderSdkService.class);
    private static final SantanderEntryValidator santanderEntryValidator = new SantanderEntryValidator();

    private static final AtomicInteger totalSuccessfulFixes = new AtomicInteger(0);
    private static final AtomicInteger totalFailedFixes = new AtomicInteger(0);

    private static final int REGISTER_ACTION_FIELD_INDEX = 20;

    // Constants containing the line parts index for each field in sibs entry.
    private static final int GIVEN_NAME_LINE_PART = 0;
    private static final int FAMILY_NAME_LINE_PART = 1;
    private static final int USERNAME_LINE_PART = 4;
    private static final int LAST_UPDATE_LINE_PART = 7;
    private static final int EXPIRY_DATE_LINE_PART = 8;
    private static final int ISSUE_DATE_LINE_PART = 9;

    /**
     * Time in ms between santander webservice requests.
     * Necessary to avoid too many followed requests, causing an error in the invoked webservice.
     */
    private static final int WEBSERVICE_WAIT_TIME = 500;

    private static boolean webserviceWaitFlag = false;

    @Override
    public void runTask() throws Exception {
        loadSibsEntriesMap();
//        logEveryEntry();
        Bennu.getInstance().getUserSet().stream()
                .map(User::getCurrentSantanderEntry)
                .filter(Objects::nonNull)
                // Filter entries with ignored state.
                .filter(entry -> entry.getState() != null && entry.getState().equals(SantanderCardState.IGNORED))
                // Filter entries that match the invalid register action error response.
                .filter(entry -> entry.getErrorDescription() != null && entry.getErrorDescription().contains(ERROR_DESCRIPTION))
                .filter(entry -> entry.getErrorDescription().contains(ERROR_DESCRIPTION) || entry.getResponseLine().contains(RESPONSE_LINE))
                .forEach(this::fixEntry);

        taskLog("\n====================================\n");
        taskLog("Total processed entries: %s%n", totalProcessedEntries());
        taskLog("Total successful fixes: %s%n", totalSuccessfulFixes.get());
        taskLog("Total failed fixes: %s%n%n", totalFailedFixes.get());
        taskLog("======================================");
    }

    private void logEveryEntry() {
        sibsEntriesMap.forEach((username, entrySet) -> {
            taskLog("Entries for %s:%n", username);
            entrySet.forEach(entry -> taskLog("%s%n", entry.toString()));
            taskLog("=================\n");
        });
    }

    private void fixEntry(final SantanderEntry entry) {
        if (entry != null) {
            final User user = entry.getUser();
            if (user != null) {
                final RegisterAction action = parseAction(entry);
                if (action.equals(RegisterAction.RENU)) {
                    fixRenuEntry(entry, user);
                } else if (action.equals(RegisterAction.REMI)) {
                    fixRemiEntry(entry, user);
                } else if (action.equals(RegisterAction.NOVO)) {
                    fixNovoEntry(entry, user);
                }
            }
        }
    }

    private boolean isPreviousNotExpired(final SantanderEntry entry) {
        return entry.getPrevious() != null && entry.getPrevious().getSantanderCardInfo() != null
                && entry.getPrevious().getSantanderCardInfo().getExpiryDate() != null
                && entry.getPrevious().getSantanderCardInfo().getExpiryDate().isAfterNow();
    }

    private boolean isNotExpired(final SibsEntry sibsEntry) {
        return sibsEntry != null && sibsEntry.getExpiryDate() != null && sibsEntry.getExpiryDate().isAfterNow();
    }

    private boolean isPreviousNotExpired(final SantanderEntry santanderEntry, final SibsEntry sibsEntry) {
        return isPreviousNotExpired(santanderEntry) || isNotExpired(sibsEntry);
    }

    private boolean isPreviousExpired(final SantanderEntry entry) {
        return entry.getPrevious() != null && entry.getPrevious().getSantanderCardInfo() != null
                && entry.getPrevious().getSantanderCardInfo().getExpiryDate() != null
                && entry.getPrevious().getSantanderCardInfo().getExpiryDate().isBeforeNow();
    }

    private boolean isExpired(final SibsEntry sibsEntry) {
        if (sibsEntry.getExpiryDate() == null && sibsEntry.getIssueDate() != null) {
            return sibsEntry.getIssueDate().isBefore(DateTime.now().minusDays(DateTime.now().getDayOfMonth()).minusYears(3));
        }
        return sibsEntry.getExpiryDate() != null && sibsEntry.getExpiryDate().isBeforeNow();
    }

    private boolean isPreviousExpired(final SantanderEntry santanderEntry, final SibsEntry sibsEntry) {
        return isPreviousExpired(santanderEntry) || (sibsEntry != null && isExpired(sibsEntry));
    }

    private void fixRenuEntry(final SantanderEntry entry, final User user) {
        if (!findEntriesForUser(user).findAny().isPresent()) {
            // No entries found on sibs for this user.
            // We can now retry the request using the NOVO request action.
            retryEntryRequest(entry, RegisterAction.NOVO);
        } else if (isPreviousNotExpired(entry, getMostRecentEntryForUser(user))) {
            // Has at least one previous entry that is not yet expired, in this case, we want to retry
            // the request, using the REMI request action.
            retryEntryRequest(entry, RegisterAction.REMI);
        }
    }

    private void fixRemiEntry(final SantanderEntry entry, final User user) {
        if (!findEntriesForUser(user).findAny().isPresent()) {
            // No entries found on sibs for this user.
            // We can now retry the request using the NOVO request action.
            retryEntryRequest(entry, RegisterAction.NOVO);
        } else if (isPreviousExpired(entry, getMostRecentEntryForUser(user))) {
            // The previous entry is expired, in this case, we want to retry the request,
            // using the RENU request action.
            retryEntryRequest(entry, RegisterAction.RENU);
        }
    }

    private void fixNovoEntry(final SantanderEntry entry, final User user) {
        final SibsEntry mostRecentEntry = getMostRecentEntryForUser(user);
        if (mostRecentEntry != null) {
            // Found at least one entry on sibs data.
            // Check whether that entry is expired or not.
            if (isExpired(mostRecentEntry)) {
                // SibsEntry is expired, we can retry with the RENU request action.
                retryEntryRequest(entry, RegisterAction.RENU);
            } else if (isNotExpired(mostRecentEntry)) {
                // SibsEntry is not expired, we can retry with the REMI request action.
                retryEntryRequest(entry, RegisterAction.REMI);
            }
        }
    }

    private SibsEntry getMostRecentEntryForUser(final User user) {
        if (user != null) {
            final String username = user.getUsername();
            return getMostRecentEntryForUser(username);
        }
        return null;
    }

    private SibsEntry getMostRecentEntryForUser(final String username) {
        if (!Strings.isNullOrEmpty(username)) {
            return findEntriesForUser(username).findFirst().orElse(null);
        }
        return null;
    }

    private Stream<SibsEntry> findEntriesForUser(final User user) {
        if (user != null) {
            final String username = user.getUsername();
            return findEntriesForUser(username);
        }
        return Stream.empty();
    }

    private Stream<SibsEntry> findEntriesForUser(final String username) {
        if (!Strings.isNullOrEmpty(username)) {
            final HashSet<SibsEntry> entries = sibsEntriesMap.get(username);
            if (entries != null) {
                return entries.stream().sorted(SibsEntry.COMPARATOR_BY_EXPIRY_DATE);
            }
        }
        return Stream.empty();
    }

    private RegisterAction parseAction(final SantanderEntry entry) {
        return RegisterAction.valueOf(santanderEntryValidator.getValue(entry.getRequestLine(), REGISTER_ACTION_FIELD_INDEX));
    }

    @Atomic
    private void retryEntryRequest(final SantanderEntry entry, final RegisterAction action) {
        webserviceWaitFlag = false;
        if (entry != null) {
            final User user = entry.getUser();
            if (user != null) {
                final String username = user.getUsername();
                try {
                    taskLog("Retrying santander entry request for user -> %s , with previous action -> %s and new action -> %s ...%n",
                            username, parseAction(entry).name(), action.name());
                    final SantanderUser santanderUser = new SantanderUser(user, userInfoService);
                    final CreateRegisterRequest request = santanderUser.toCreateRegisterRequest(action);
                    final CardPreviewBean bean = sdkService.generateCardRequest(request);
                    final CreateRegisterResponse response = sdkService.createRegister(bean);

//                    taskLog("Card preview: %nName: %s%nPhoto: %s%nRole: %s%nCampus: %s%nPickup location: %s%n%n",
//                            bean.getCardName(), Base64.getEncoder().encodeToString(bean.getPhoto()),
//                            bean.getRole(), santanderUser.getCampus(),
//                            santanderUser.getUserPickupLocation().getPickupLocation());

                    if (response.wasRegisterSuccessful()) {
                        try {
                            entry.reset(bean, santanderUser.getUserPickupLocation(),"Automatic task to fix previous request action.");
                            entry.saveResponse(response);
                            taskLog("Request for user %s was successful! Response -> %s%n%n", username, response.getResponseLine());
                            totalSuccessfulFixes.getAndIncrement();
                            webserviceWaitFlag = true;
                        } catch (final SantanderCardNoPermissionException e) {
                            handleError("Error: User %s has no permission to request card.%n%n", username);
                        }
                    } else {
                        handleError("Request for user %s failed! -> Error: %s%n%n", username, response.getErrorDescription());
                        webserviceWaitFlag = true;
                    }
                } catch (final SantanderMissingInformationException smie) {
                    handleError("Error: User %s has missing information -> %s%n%n", username,
                            cardsService.getErrorMessage(user.getProfile().getPreferredLocale(), smie.getMessage()));
                } catch (final SantanderValidationException sve) {
                    handleError("Error: Validation exception for user %s -> %s%n%n", username, sve.getMessage());
                    webserviceWaitFlag = true;
                } finally {
                    if (webserviceWaitFlag) {
                        sleep();
                    }
                }
            }
        }
    }

    private void handleError(final String format, final Object... params) {
        taskLog(format, params);
        totalFailedFixes.getAndIncrement();
    }

    private void loadSibsEntriesMap() {
        try {
            final File file = new File(SIBS_CARDS_DATA_CSV_FILENAME);
            Files.readAllLines(file.toPath()).forEach(line -> {
                final String[] parts = line.split(";");
                final String username = parts[USERNAME_LINE_PART].trim();
                sibsEntriesMap.computeIfAbsent(username, e -> new HashSet<>()).add(SibsEntry.fromLine(line));
            });
        } catch (final IOException e) {
            taskLog("Error loading sibs entries map:%n%n -> IOException: %s%n", e.getMessage());
        }
    }

    private int totalProcessedEntries() {
        return totalSuccessfulFixes.get() + totalFailedFixes.get();
    }

    private void sleep() {
        try {
            Thread.sleep(WEBSERVICE_WAIT_TIME);
        } catch (final InterruptedException ignored) {
        }
    }

    private static class SibsEntry {
        private final String name;
        private final String username;
        private final DateTime lastUpdate;
        private final DateTime expiryDate;
        private final DateTime issueDate;

        // String to be used as a matching reference when the data from sibs csv file shows no expiry date for this line.
        private static final String NO_EXPIRY_DATE = "Sem data de expiração";
        // String to be used as a matching reference when the data from sibs csv file shows no issue date for this line.
        private static final String NO_ISSUE_DATE = "Sem data de expedição";

        private static final Comparator<SibsEntry> COMPARATOR_BY_EXPIRY_DATE = (e1, e2) -> {
            final DateTime expiryDate1 = e1.getExpiryDate();
            final DateTime expiryDate2 = e2.getExpiryDate();
            if (expiryDate1 != null && expiryDate2 != null) {
                return expiryDate2.compareTo(expiryDate1);
            }
            return expiryDate1 == null ? 1 : -1;
        };

        private SibsEntry(final String name, final String username, final DateTime lastUpdate, final DateTime expiryDate,
                         final DateTime issueDate) {
            this.name = name;
            this.username = username;
            this.lastUpdate = lastUpdate;
            this.expiryDate = expiryDate;
            this.issueDate = issueDate;
        }

        private static SibsEntry fromLine(final String line) {
            final String[] parts = line.split(";");
            final String name = parts[GIVEN_NAME_LINE_PART].trim() + " " + parts[FAMILY_NAME_LINE_PART].trim();
            final String username = parts[USERNAME_LINE_PART].trim();
            final String lastUpdate = parts[LAST_UPDATE_LINE_PART];
            String expiryDate = parts[EXPIRY_DATE_LINE_PART];
            String issueDate = parts[ISSUE_DATE_LINE_PART];
            if (isInvalidDate(expiryDate, NO_EXPIRY_DATE)) {
                expiryDate = null;
            }
            if (isInvalidDate(issueDate, NO_ISSUE_DATE)) {
                issueDate = null;
            }
            return new SibsEntry(name, username, parseDate(lastUpdate), parseDate(expiryDate), parseDate(issueDate));
        }

        private static boolean isInvalidDate(final String date, final String comparable) {
            return Strings.isNullOrEmpty(date) || date.trim().equalsIgnoreCase(comparable);
        }

        private static DateTime parseDate(final String date) {
            if (date != null) {
                return ISODateTimeFormat.date().parseDateTime(date.trim());
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public DateTime getLastUpdate() {
            return lastUpdate;
        }

        public DateTime getExpiryDate() {
            return expiryDate;
        }

        public DateTime getIssueDate() {
            return issueDate;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SibsEntry{ ");
            sb.append("name -> ").append(name).append(", ");
            sb.append("username -> ").append(username).append(", ");
            sb.append("last updated -> ").append(lastUpdate.toString(ISODateTimeFormat.date())).append(", ");
            sb.append("expiry date -> ").append(expiryDate != null ? expiryDate.toString(ISODateTimeFormat.date()) : "null");
            sb.append(", ");
            sb.append("issue date -> ").append(issueDate != null ? issueDate.toString(ISODateTimeFormat.date()) : "null");
            sb.append(" }");
            return sb.toString();
        }
    }

}
