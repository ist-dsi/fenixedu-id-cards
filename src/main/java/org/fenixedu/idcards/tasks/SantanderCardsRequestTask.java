package org.fenixedu.idcards.tasks;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;

import java.util.List;
import java.util.Set;

public class SantanderCardsRequestTask extends CustomTask {
    private static final Logger logger = LoggerFactory.getLogger(SantanderCardsRequestTask.class);

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.WRITE;
    }

    @Override
    public void runTask() throws Exception {
        SantanderIdCardsService idCardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);
        Set<User> users = Bennu.getInstance().getUserSet();

        for (User user: users) {
            List<RegisterAction> userActions = idCardsService.getPersonAvailableActions(user);
            try {
                if (userActions.contains(RegisterAction.NOVO)) {
                    idCardsService.createRegister(user, RegisterAction.NOVO);
                } else if (userActions.contains(RegisterAction.RENU)) {
                    idCardsService.createRegister(user, RegisterAction.RENU);
                }
                taskLog("Requested card for user %s%n", user.getUsername());
                Thread.sleep(5000);
            } catch (SantanderValidationException se) {
                taskLog("User %s cant request a card because: %s%n", user.getUsername(), se.getMessage());
            } catch (RuntimeException e) {
                taskLog("Couldn't request card for user %s%n", user.getUsername());
                Thread.sleep(5000);
            }
        }
    }
}
