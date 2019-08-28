package org.fenixedu.idcards.tasks;

import java.util.Locale;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
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

@Task(englishTitle = "Requests users first IST cards", readOnly = true)
public class SantanderCardsRequestTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(SantanderCardsRequestTask.class);
    private SantanderIdCardsService idCardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);
    private final int waitTime = 500;   // TODO: check santander request rate
    private static boolean waitFlag = false; //If the webservice is invoked we must wait calling before  it again

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream()
                .filter(u -> u.getCurrentSantanderEntry() == null).forEach(this::requestCard);
    }

    public void requestCard(User user) {
        waitFlag = false;
        FenixFramework.atomic(() -> {
            try {
                SantanderEntry createRegister =
                        idCardsService.createRegister(user, RegisterAction.NOVO, "First card automatic request");
                idCardsService.sendRegister(user, createRegister);
                logger.debug("Created card with success for user {} (current SantanderEntry: {})", user.getUsername(),
                        user.getCurrentSantanderEntry().getExternalId());
                waitFlag = true;
            } catch (SantanderCardNoPermissionException scnpe) {
                logger.debug("No permission to request card for user {}", user.getUsername());
            } catch (SantanderMissingInformationException smie) {
                logger.debug("User {} has missing information: {}", user.getUsername(), smie.getMessage());
                notifyMissingInformation(user, smie.getMessage());
            } catch (SantanderValidationException sve) {
                logger.debug("Error generating card for {} (current SantanderEntry: {}): {}", user.getUsername(),
                        user.getCurrentSantanderEntry() == null ? "null" : user.getCurrentSantanderEntry().getExternalId(),
                        sve.getMessage());
                waitFlag = true;
            } catch (Throwable t) {
                logger.error(String.format("Failed for user %s (current SantanderEntry: %s)", user.getUsername(),
                        user.getCurrentSantanderEntry() == null ? "null" : user.getCurrentSantanderEntry().getExternalId()), t);
                waitFlag = true;
            }
        });
        if (waitFlag) {
            sleep();
        }
    }

    private void notifyMissingInformation(User user, String errors) {
        Locale locale = user.getProfile().getPreferredLocale();
        CardNotifications.notifyMissingInformation(user, idCardsService.getErrorMessage(locale, errors));
    }

    private void sleep() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
    }
}
