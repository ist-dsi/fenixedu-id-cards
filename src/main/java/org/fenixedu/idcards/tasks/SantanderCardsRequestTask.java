package org.fenixedu.idcards.tasks;

import java.util.List;
import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderNoRoleAvailableException;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@DeclareMessageTemplate(
        id = "message.template.santander.card.automatic.generation.first.card.error.missing.information",
        description = "error while trying to request user first card notification",
        subject = "IST Card Error",
        text = "user has missing information: {{ errorDescription }}",
        parameters = {
                @TemplateParameter(id = "errorDescription", description = "parameter.errorDescription"),
        }
)

@Task(englishTitle = "Requests users first IST cards", readOnly = true)
public class SantanderCardsRequestTask extends CronTask {

    private static final Logger logger = LoggerFactory.getLogger(SantanderCardsRequestTask.class);
    private SantanderIdCardsService idCardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() throws Exception {
        Bennu.getInstance().getUserSet().stream().filter(u -> u.getCurrentSantanderEntry() == null)
                .forEach(user -> requestCard(user));
    }

    public void requestCard(User user) {
        FenixFramework.atomic(() -> {
            try {   
                SantanderEntry createRegister =
                        idCardsService.createRegister(user, RegisterAction.NOVO, "First Card Automatic Request");
                idCardsService.sendRegister(user, createRegister);
            } catch (SantanderNoRoleAvailableException e) {
                logger.debug("No role available for {}", user.getUsername());
            } catch (SantanderValidationException sve) {
                logger.error(String.format("error generating card for %s%n", user.getUsername()), sve);
                notifyMissingInformation(user, sve.getMessage());
            }
        });
        //TODO Santander requests rate?
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

    public void notifyMissingInformation(User user, String errorLabels) {
        String errorDescription = idCardsService.getErrorMessage(user.getProfile().getPreferredLocale(), errorLabels);

        Message.fromSystem()
                .to(Group.users(user))
                .template("message.template.santander.card.automatic.generation.first.card.error.missing.information")
                .parameter("errorDescription", errorDescription)
                .and().wrapped().send();

    }
}
