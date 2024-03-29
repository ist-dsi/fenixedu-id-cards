package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.idcards.domain.PickupLocation;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.idcards.domain.SantanderUserInfo;
import org.fenixedu.idcards.dto.SantanderCardDto;
import org.fenixedu.idcards.notifications.CardNotifications;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderSdkService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Service
public class SantanderIdCardsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SantanderIdCardsService.class);
    /*
     * Sometimes santander webservice is not synchronized
     * e.g. request new card with success -> getCardState can return the old card information
     * this variable represents the number of days that the cardservice waits until it is sure that the services are in sync
     * it is used only when there were problems communicating with santander
     */
    private static final int SANTANDER_SYNC_DAYS = 1;

    private final SantanderSdkService santanderCardService;
    private final IUserInfoService userInfoService;

    @Autowired
    public SantanderIdCardsService(final SantanderSdkService santanderCardService, final IUserInfoService userInfoService) {
        this.santanderCardService = santanderCardService;
        this.userInfoService = userInfoService;

        Signal.register(SantanderEntry.STATE_CHANGED, CardNotifications::notifyStateTransition);
    }

    public SantanderCardDto generateCardPreview(final User user) throws SantanderValidationException {
        final SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        // Action doesn't matter
        final CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(RegisterAction.NOVO);
        final CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
        return new SantanderCardDto(cardPreviewBean);
    }

    public List<SantanderCardDto> getUserSantanderCards(final User user) {
        return SantanderEntry.getSantanderEntryHistory(user).stream()
                .map(entry -> new SantanderCardDto(entry.getSantanderCardInfo()))
                .collect(Collectors.toList());
    }

    public List<RegisterAction> getPersonAvailableActions(final User user) {
        final SantanderEntry personEntry = getOrUpdateState(user);
        return getPersonAvailableActions(personEntry);
    }

    public List<RegisterAction> getPersonAvailableActions(final SantanderEntry personEntry) {
        final List<RegisterAction> actions = new LinkedList<>();

        if (personEntry == null || personEntry.canRegisterNew()) {
            actions.add(RegisterAction.NOVO);
            return actions;
        }

        if (personEntry.canReemitCard()) {
            actions.add(RegisterAction.REMI);
        }

        if (personEntry.canRenovateCard()) {
            actions.add(RegisterAction.RENU);
        }

        return actions;
    }

    public SantanderEntry getOrUpdateState(final User user) {
        final SantanderEntry entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return null;
        }

        final SantanderCardState cardState = entry.getState();
        switch (cardState) {
            case IGNORED:
            case EXPIRED:
            case WAITING_INFO:
                return entry;
            case ISSUED:
            case DELIVERED:
                if (entry.getSantanderCardInfo().getExpiryDate().isBefore(DateTime.now())) {
                    entry.updateState(SantanderCardState.EXPIRED);
                }
                return entry;
            case PENDING:
                return synchronizeFenixAndSantanderStates(user, entry);
            case REJECTED:
            case NEW:
            case READY_FOR_PRODUCTION:
            case PRODUCTION:
                return checkAndUpdateState(entry);
            default:
                LOGGER.debug("SantanderEntry " + entry.getExternalId() + " has unknown state (" + cardState.name() + ")");
                return entry;
        }
    }

    private SantanderEntry checkAndUpdateState(final SantanderEntry entry) {
        final GetRegisterResponse registerData = getRegister(entry.getUser());
        return checkAndUpdateState(entry, registerData);
    }

    private SantanderEntry checkAndUpdateState(final SantanderEntry entry, final GetRegisterResponse registerData) {
        if (registerData == null) {
            return entry;
        }

        final GetRegisterStatus status = registerData.getStatus();

        switch (status) {
            case REJECTED_REQUEST:
                entry.updateState(SantanderCardState.REJECTED);
                return entry;

            case REMI_REQUEST:
            case RENU_REQUEST:
                entry.updateState(SantanderCardState.NEW);
                return entry;

            case READY_FOR_PRODUCTION:
                entry.updateState(SantanderCardState.READY_FOR_PRODUCTION);
                return entry;
            case PRODUCTION:
                entry.updateState(SantanderCardState.PRODUCTION);
                return entry;

            case ISSUED:
                if (!SantanderEntry.hasMifare(entry.getUser(), registerData.getMifare())) {
                    entry.updateIssued(registerData);
                }
                return entry;

            case NO_RESULT:
                // syncing problem between both services
                if (!entry.wasRegisterSuccessful()) {
                    entry.updateState(SantanderCardState.IGNORED);
                }
                return entry;
            case UNKNOWN:
            default:
                LOGGER.debug("Not supported status:  " + status);
        }

        return entry;
    }

    private SantanderEntry synchronizeFenixAndSantanderStates(final User user, final SantanderEntry entry) {
        final GetRegisterResponse registerData = getRegister(user);

        if (registerData == null) {
            return entry;
        }

        final GetRegisterStatus status = registerData.getStatus();
        final SantanderEntry previousEntry = entry.getPrevious();

        if (previousEntry == null) {
            if (status.equals(GetRegisterStatus.NO_RESULT) && entry.getLastUpdate().plusDays(SANTANDER_SYNC_DAYS).isBeforeNow()) {
                entry.updateState(SantanderCardState.IGNORED);
                return entry;
            } else {
                return checkAndUpdateState(entry, registerData);
            }
        }

        final String newMifare = registerData.getMifare();
        if (Strings.isNullOrEmpty(newMifare) || !SantanderEntry.hasMifare(user, newMifare)) {
            return checkAndUpdateState(entry, registerData);
        } else if (entry.getLastUpdate().plusDays(SANTANDER_SYNC_DAYS).isBeforeNow()) {
            entry.updateState(SantanderCardState.IGNORED);
        }

        return entry;
    }

    private GetRegisterResponse getRegister(final User user) {
        final String userName = user.getUsername();
        try {
            return santanderCardService.getRegister(userName);
        } catch (final Throwable t) {
            LOGGER.error(String.format("Something went wrong getting info of user %s", user.getUsername()), t);
            return null;
        }
    }

    public SantanderEntry createRegister(final User user, final String requestReason) throws SantanderValidationException {
        final List<RegisterAction> availableActions = getPersonAvailableActions(user);
        if (availableActions.contains(RegisterAction.NOVO)) {
            return createRegister(user, RegisterAction.NOVO, requestReason);
        } else if (availableActions.contains(RegisterAction.RENU)) {
            return createRegister(user, RegisterAction.RENU, requestReason);
        } else if (availableActions.contains(RegisterAction.REMI)) {
            return createRegister(user, RegisterAction.REMI, requestReason);
        } else {
            throw new SantanderValidationException("santander.id.cards.error.user.cannot.request.card");
        }
    }

    public SantanderEntry createRegister(final User user, final RegisterAction action, final String requestReason)
                                         throws SantanderValidationException {
        if (!getPersonAvailableActions(user.getCurrentSantanderEntry()).contains(action)) {
            LOGGER.debug("Action (" + action.name() + ") not available for user " + user.getUsername());
            throw new SantanderValidationException("santander.id.cards.error.wrong.request.action");
        }

        final SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        final CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(action);
        final CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
        return createOrResetEntry(user, cardPreviewBean, santanderUser.getUserPickupLocation(), requestReason);
    }

    @Atomic(mode = TxMode.READ)
    public void sendRegister(final User user, final SantanderEntry santanderEntry) throws SantanderValidationException {
        final CardPreviewBean cardPreviewBean = santanderEntry.getCardPreviewBean();
        final CreateRegisterResponse response = santanderCardService.createRegister(cardPreviewBean);

        santanderEntry.saveResponse(response);

        if (response.getErrorType() != null) {
            throw new SantanderValidationException(response.getErrorType().getErrorMessage());
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntry createOrResetEntry(final User user, final CardPreviewBean cardPreviewBean,
                                              final PickupLocation pickupLocation, final String requestReason)
                                              throws SantanderValidationException {
        final SantanderEntry entry = user.getCurrentSantanderEntry();
        if (entry == null) {
            return new SantanderEntry(user, cardPreviewBean, pickupLocation, requestReason);
        }

        final SantanderCardState cardState = entry.getState();
        switch (cardState) {
            case IGNORED:
            case WAITING_INFO:
                entry.reset(cardPreviewBean, pickupLocation, requestReason);
                return entry;
            case REJECTED:
            case ISSUED:
            case DELIVERED:
            case EXPIRED:
                return new SantanderEntry(user, cardPreviewBean, pickupLocation, requestReason);
            default:
                //should be impossible to reach;
                throw new SantanderValidationException("santander.id.cards.error.santander.entry.invalid.state");
        }
    }

    public boolean canRequestCard(final User user) {
        if (user == null) {
            return false;
        }
        final SantanderEntry currentSantanderEntry = user.getCurrentSantanderEntry();
        if (currentSantanderEntry == null) {
            return true;
        }
        return !getPersonAvailableActions(currentSantanderEntry).isEmpty();
    }

    public String getErrorMessage(Locale locale, final String errorLabels) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String[] errorMessages = errorLabels.split("\n");
        final StringBuilder errorDescription = new StringBuilder();
        for (final String errorMessage : errorMessages) {
            errorDescription.append(BundleUtil.getString("resources.CardGenerationResources", locale, errorMessage));
        }
        return errorDescription.toString();
    }

    public JsonObject getUserNames(final User user) {
        final String normalizedGivenNames = SantanderUserInfo.getNormalizedSantanderUserGivenNames(user);
        final String normalizedFamilyNames = SantanderUserInfo.getNormalizedSantanderUserFamilyNames(user);
        final JsonObject response = new JsonObject();
        final JsonObject userNames = new JsonObject();

        userNames.addProperty("givenNames", normalizedGivenNames);
        userNames.addProperty("familyNames", normalizedFamilyNames);
        response.add("userNames", userNames);
        response.addProperty("wasNameReplaced",
                !normalizedGivenNames.equals(user.getProfile().getGivenNames()) || !normalizedFamilyNames
                        .equals(user.getProfile().getFamilyNames()));

        return response;
    }

}
