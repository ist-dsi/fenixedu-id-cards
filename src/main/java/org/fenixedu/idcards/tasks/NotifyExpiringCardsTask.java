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

@Task(englishTitle = "Notify users with expiring cards", readOnly = true)
public class NotifyExpiringCardsTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(NotifyExpiringCardsTask.class);
    private final int DAYS_TO_EXPIRE = 30;   // TODO: check santander request rate

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() {
        Bennu.getInstance().getUserSet().stream().filter(u -> SantanderCardState.ISSUED.equals(u.getCurrentSantanderEntry().getState()))
                .forEach(this::notifyExpiringCard);
    }

    public void notifyExpiringCard(User user) {
        SantanderEntry entry = user.getCurrentSantanderEntry();
        SantanderCardState newState = entry.getState();
        if (SantanderCardState.ISSUED.equals(newState) && DateTime.now().isBefore(entry.getSantanderCardInfo()
                .getLastTransition().getTransitionDate().plusDays(DAYS_TO_EXPIRE)) && entry.getWasExpiringNotified()) {
            entry.setWasExpiringNotified(true);
            CardNotifications.notifyCardExpiring(user);
            taskLog("Notifying user: %s%n", user.getUsername());
        }
    }
}
