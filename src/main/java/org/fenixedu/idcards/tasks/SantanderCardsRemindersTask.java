package org.fenixedu.idcards.tasks;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.notifications.CardNotifications;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Remind users with expiring cards and cards to pickup", readOnly = true)
public class SantanderCardsRemindersTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(SantanderCardsRemindersTask.class);
    private final int DAYS_TO_EXPIRE = 90;

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream()
                .filter(u -> u.getCurrentSantanderEntry() != null
                        && (SantanderCardState.ISSUED.equals(u.getCurrentSantanderEntry().getState())
                                || SantanderCardState.DELIVERED.equals(u.getCurrentSantanderEntry().getState())))
                .forEach(this::remindUser);
    }

    private void remindUser(User user) {
        SantanderEntry entry = user.getCurrentSantanderEntry();
        SantanderCardState state = entry.getState();

        if (SantanderCardState.DELIVERED.equals(state) && !entry.getWasExpiringNotified()
                && DateTime.now().isAfter(entry.getSantanderCardInfo().getExpiryDate().minusDays(DAYS_TO_EXPIRE))) {
            FenixFramework.atomic(() -> {
                entry.setWasExpiringNotified(true);
                CardNotifications.notifyCardExpiring(user);
                logger.debug("Notifying user for expiring card: {}", user.getUsername());
            });
        } else if (SantanderCardState.ISSUED.equals(entry.getState()) && !entry.getWasPickupNotified()
                && DateTime.now().isAfter(entry.getLastUpdate().plusDays(15))
                && DateTime.now().isBefore(entry.getSantanderCardInfo().getExpiryDate())) {
            FenixFramework.atomic(() -> {
                entry.setWasPickupNotified(true);
                CardNotifications.notifyCardPickup(user);
                logger.debug("Notifying user to pickup card: {}", user.getUsername());
            });
        }
    }
}
