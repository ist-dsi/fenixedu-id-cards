package org.fenixedu.idcards.tasks;

import java.util.List;
import java.util.Locale;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.notifications.CardNotifications;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderNoRoleAvailableException;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Update users cards", readOnly = true)
public class UpdateSantanderCardsStateTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(UpdateSantanderCardsStateTask.class);
    private SantanderIdCardsService cardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);
    private final int waitTime = 100;   // TODO: check santander request rate

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream().filter(u -> u.getCurrentSantanderEntry() != null && canRequestCard(u))
                .forEach(this::requestCard);
    }

    private boolean canRequestCard(User user) {
        // Updates card state
        SantanderCardState oldCardState = user.getCurrentSantanderEntry().getState();
        List<RegisterAction> availableActions = cardsService.getPersonAvailableActions(user);

        switch (oldCardState) {
            case IGNORED:
            case ISSUED:

            case EXPIRED:
            case DELIVERED:
                break;
            default:
                //If getRegister endpoint is called we must wait
                sleep();
        }

        return availableActions.contains(RegisterAction.NOVO) || availableActions.contains(RegisterAction.RENU);
    }

    private void requestCard(User user) {
        FenixFramework.atomic(() -> {
            try {
                List<RegisterAction> availableActions = cardsService.getPersonAvailableActions(user.getCurrentSantanderEntry());
                RegisterAction action = RegisterAction.RENU;
                if (availableActions.contains(RegisterAction.NOVO)) {
                    action = RegisterAction.NOVO;
                }
                SantanderEntry entry = cardsService.createRegister(user, action,  "Automatic task request");
                cardsService.sendRegister(user, entry);
                taskLog("Requested card for user %s%n", user.getUsername());
            } catch (SantanderNoRoleAvailableException e) {
                taskLog("No role available for %s%n", user.getUsername());
            } catch (SantanderValidationException sve) {
                taskLog("Error generating card for %s: %s%n", user.getUsername(), sve.getMessage());
                notifyMissingInformation(user, sve.getMessage());
            } catch (Exception oe) {
                taskLog("Failed for user %s: %s%n", user.getUsername(), oe);
            }
        });

        sleep();
    }

    private void notifyMissingInformation(User user, String errors) {
        Locale locale = user.getProfile().getPreferredLocale();
        CardNotifications.notifyMissingInformation(user, cardsService.getErrorMessage(locale, errors));
    }

    private void sleep() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
    }
}
