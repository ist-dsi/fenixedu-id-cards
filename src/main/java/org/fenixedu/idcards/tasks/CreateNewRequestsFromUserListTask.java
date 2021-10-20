package org.fenixedu.idcards.tasks;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.custom.ReadCustomTask;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.exception.SantanderCardNoPermissionException;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderMissingInformationException;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderSdkService;
import pt.ist.fenixframework.Atomic;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tiago Pinho
 */
public class CreateNewRequestsFromUserListTask extends ReadCustomTask {

    private static final SantanderIdCardsService cardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);
    private static final SantanderSdkService sdkService = BennuSpringContextHelper.getBean(SantanderSdkService.class);

    private static final AtomicInteger totalSuccessful = new AtomicInteger(0);
    private static final AtomicInteger totalFailed = new AtomicInteger(0);

    /**
     * Time in ms between santander webservice requests.
     * Necessary to avoid too many followed requests, causing an error in the invoked webservice.
     */
    private static final int WEBSERVICE_WAIT_TIME = 500;

    private static boolean webserviceWaitFlag = false;

    @Override
    public void runTask() throws Exception {
        // TODO: Replace this with the correct list of new users to be target for a new card request.
        final List<User> users = Stream.of(User.findByUsername(null)).collect(Collectors.toList());
        users.stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getCurrentSantanderEntry() == null)
                .forEach(this::request);

        taskLog("\n====================================\n");
        taskLog("Total processed: %s%n", totalProcessed());
        taskLog("Total successful: %s%n", totalSuccessful.get());
        taskLog("Total failed: %s%n%n", totalFailed.get());
        taskLog("======================================");
    }

    @Atomic
    private void request(final User user) {
        webserviceWaitFlag = false;
        final String username = user.getUsername();
        try {
            taskLog("Creating santander entry request for user -> %s ...%n", username);
            final SantanderEntry entry = cardsService.createRegister(user, RegisterAction.NOVO,
                    "First card automatic request made by custom task.");
            final CardPreviewBean bean = entry.getCardPreviewBean();
            final CreateRegisterResponse response = sdkService.createRegister(bean);

            if (response.wasRegisterSuccessful()) {
                entry.saveResponse(response);
                taskLog("Request for user %s was successful! Response -> %s%n%n", username, response.getResponseLine());
                totalSuccessful.getAndIncrement();
            } else {
                handleError("Request for user %s failed! -> Error: %s%n%n", username, response.getErrorDescription());
            }
            webserviceWaitFlag = true;
        } catch (final SantanderCardNoPermissionException scnpe) {
            handleError("Error: User %s has no permission to request card.%n%n", username);
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

    private void handleError(final String format, final Object... params) {
        taskLog(format, params);
        totalFailed.getAndIncrement();
    }

    private int totalProcessed() {
        return totalSuccessful.get() + totalFailed.get();
    }

    private void sleep() {
        try {
            Thread.sleep(WEBSERVICE_WAIT_TIME);
        } catch (final InterruptedException ignored) {
        }
    }

}
