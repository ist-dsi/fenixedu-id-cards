package org.fenixedu.idcards.tasks;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.exception.SantanderCardNoPermissionException;
import org.fenixedu.idcards.notifications.CardNotifications;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderMissingInformationException;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import java.util.List;
import java.util.Locale;

@Task(englishTitle = "Update state and renew users santander cards", readOnly = true)
public class UpdateAndRenewSantanderCardsTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(UpdateAndRenewSantanderCardsTask.class);
    private final SantanderIdCardsService cardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);

    private final int waitTime = 500; // Wait time between santander requests
    private static boolean waitFlag = false; // If the webservice is invoked we must wait calling before calling it again

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream()
                .filter(u -> u.getCurrentSantanderEntry() != null && canRequestCard(u))
                .forEach(this::requestCard);
    }

    private boolean canRequestCard(final User user) {
        // Updates card state
        final SantanderCardState oldCardState = user.getCurrentSantanderEntry().getState();
        final List<RegisterAction> availableActions = cardsService.getPersonAvailableActions(user);

        switch (oldCardState) {
            case IGNORED:
            case ISSUED:
            case EXPIRED:
            case DELIVERED:
            case WAITING_INFO:
                break;
            default:
                // If getRegister webservice is called we must wait
                sleep();
        }

        return availableActions.contains(RegisterAction.RENU);
    }

    private void requestCard(final User user) {
        waitFlag = false;
        FenixFramework.atomic(() -> {
            try {
                final SantanderEntry entry = cardsService.createRegister(user, RegisterAction.RENU, "Automatic task request");
                cardsService.sendRegister(user, entry);

                logger.debug("Requested card for user {} (current SantanderEntry: {})", user.getUsername(),
                        user.getCurrentSantanderEntry().getExternalId());
                waitFlag = true;
            } catch (final SantanderCardNoPermissionException e) {
                logger.debug("No permission to request card for user {}", user.getUsername());
            } catch (final SantanderMissingInformationException smie) {
                logger.debug("User {} has missing information: {}", user.getUsername(), smie.getMessage());
                notifyMissingInformation(user, smie.getMessage());
            } catch (final SantanderValidationException sve) {
                logger.debug("Error generating card for {} (current SantanderEntry: {}): {}", user.getUsername(),
                        user.getCurrentSantanderEntry() == null ? "null" : user.getCurrentSantanderEntry().getExternalId(),
                        sve.getMessage());
                waitFlag = true;
            } catch (final Throwable t) {
                logger.error(String.format("Failed for user %s (current SantanderEntry: %s)", user.getUsername(),
                        user.getCurrentSantanderEntry() == null ? "null" : user.getCurrentSantanderEntry().getExternalId()), t);
                waitFlag = true;
            }
        });

        if (waitFlag) {
            sleep();
        }
    }

    private void notifyMissingInformation(final User user, final String errors) {
        final Locale locale = user.getProfile().getPreferredLocale();
        CardNotifications.notifyMissingInformation(user, cardsService.getErrorMessage(locale, errors));
    }

    private void sleep() {
        try {
            Thread.sleep(waitTime);
        } catch (final InterruptedException ignored) {
        }
    }
}
