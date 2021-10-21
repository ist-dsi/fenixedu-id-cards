package org.fenixedu.idcards.tasks;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.service.SantanderIdCardsService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tiago Pinho
 */
@Task(englishTitle = "Update users santander entry state cron task", readOnly = true)
public class UpdateSantanderEntriesStateTask extends CronTask {

    private final SantanderIdCardsService service = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);

    /**
     * Time in ms between santander webservice requests.
     * Necessary to avoid too many followed requests, causing an error in the invoked webservice.
     */
    private final int WEBSERVICE_WAIT_TIME = 500;

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void runTask() throws Exception {
        Bennu.getInstance().getUserSet().stream()
                .filter(user -> user.getCurrentSantanderEntry() != null)
                .forEach(this::updateSantanderEntryState);
        taskLog("Total entries updated: %s", counter.get());
    }

    private void updateSantanderEntryState(final User user) {
        final SantanderEntry entry = user.getCurrentSantanderEntry();
        final SantanderCardState state = entry.getState();

        switch (state) {
            case IGNORED:
            case ISSUED:
            case EXPIRED:
            case DELIVERED:
            case WAITING_INFO:
                // Skip these states, there is nothing to update to.
                break;
            default:
                // Update user entry state.
                service.getOrUpdateState(user);
                // Increment counter
                counter.getAndIncrement();
                // Sleep using the webservice wait time.
                sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(WEBSERVICE_WAIT_TIME);
        } catch (final InterruptedException ignored) {
        }
    }

}
