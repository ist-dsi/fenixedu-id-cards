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

import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Remind users with expiring cards and cards to pickup", readOnly = true)
public class SantanderCardsRemindersTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(SantanderCardsRemindersTask.class);
    private final int DAYS_TO_EXPIRE = 90;

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream()
                .filter(u -> u.getCurrentSantanderEntry() != null && canBeNotified(u.getCurrentSantanderEntry()))
                .forEach(this::remindUser);
    }

    private boolean canBeNotified(final SantanderEntry entry) {
        return entry != null && (canBePickupNotified(entry) || canBeExpiringNotified(entry));
    }

    private void remindUser(final User user) {
        final SantanderEntry entry = user.getCurrentSantanderEntry();
        if (canBeExpiringNotified(entry)) {
            FenixFramework.atomic(() -> {
                CardNotifications.notifyCardExpiring(user);
                entry.setWasExpiringNotified(true);
                logger.debug("Notifying user for expiring card: {}", user.getUsername());
            });
        } else if (canBePickupNotified(entry)) {
            FenixFramework.atomic(() -> {
                CardNotifications.notifyCardPickup(user);
                entry.setWasPickupNotified(true);
                logger.debug("Notifying user to pickup card: {}", user.getUsername());
            });
        }
    }

    private boolean canBePickupNotified(final SantanderEntry entry) {
        final SantanderCardState state = entry.getState();
        return SantanderCardState.ISSUED.equals(state) && !entry.getWasPickupNotified()
                && DateTime.now().isAfter(entry.getLastUpdate().plusDays(15))
                && DateTime.now().isBefore(entry.getSantanderCardInfo().getExpiryDate());
    }

    private boolean canBeExpiringNotified(final SantanderEntry entry) {
        final SantanderCardState state = entry.getState();
        return SantanderCardState.DELIVERED.equals(state) && !entry.getWasExpiringNotified()
                && DateTime.now().isAfter(entry.getSantanderCardInfo().getExpiryDate().minusDays(DAYS_TO_EXPIRE));
    }

}
