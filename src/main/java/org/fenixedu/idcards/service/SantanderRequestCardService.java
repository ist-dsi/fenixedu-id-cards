package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Service
public class SantanderRequestCardService {

    private SantanderCardService santanderCardService;
    private IUserInfoService userInfoService;

    @Autowired
    public SantanderRequestCardService(SantanderCardService santanderCardService, IUserInfoService userInfoService) {
        this.santanderCardService = santanderCardService;
        this.userInfoService = userInfoService;
    }

    private Logger logger = LoggerFactory.getLogger(SantanderRequestCardService.class);

    public List<RegisterAction> getPersonAvailableActions(User user) {

        List<RegisterAction> actions = new LinkedList<>();
        SantanderEntryNew personEntry = getOrUpdateState(user);

        if (personEntry == null || personEntry.canRegisterNew()) {
            actions.add(RegisterAction.NOVO);
            return actions;
        }

        if (personEntry.canReemitCard()) {
            actions.add(RegisterAction.REMI);
        }

        if (personEntry.canRenovateCard()) {
            actions.add(RegisterAction.RENU);
        }

        return actions;
    }

    public SantanderEntryNew getOrUpdateState(User user) {
        SantanderEntryNew entryNew = user.getCurrentSantanderEntry();

        if (entryNew == null) {
            return null;
        }

        SantanderCardState cardState = entryNew.getState();

        switch (cardState) {
            case IGNORED:
            case ISSUED:
                return entryNew;
            case PENDING:
                return synchronizeFenixAndSantanderStates(user, entryNew);
            case REJECTED:
            case NEW:
                return checkAndUpdateState(entryNew);
            default:
                logger.debug("SantanderEntryNew " + entryNew.getExternalId() + " has unknown state (" + cardState.getName() + ")");
                throw new RuntimeException();
        }
    }

    private SantanderEntryNew checkAndUpdateState(SantanderEntryNew entry) {
        GetRegisterResponse registerData = getRegister(entry.getUser());
        return checkAndUpdateState(entry, registerData);
    }

    private SantanderEntryNew checkAndUpdateState(SantanderEntryNew entry, GetRegisterResponse registerData) {
        if (registerData == null) {
            return entry;
        }

        GetRegisterStatus status = registerData.getStatus();

        switch (status) {
            case REJECTED_REQUEST:
                entry.updateState(SantanderCardState.REJECTED);
                return entry;

            case READY_FOR_PRODUCTION:
            case REMI_REQUEST:
            case RENU_REQUEST:
            case PRODUCTION:
                entry.updateState(SantanderCardState.NEW);
                break;

            case ISSUED:
                entry.update(registerData);
                break;

            case NO_RESULT:
                // syncing problem between both services
                if (!entry.wasRegisterSuccessful()) {
                    entry.updateState(SantanderCardState.IGNORED);
                }
                break;

            default:
                logger.debug("Not supported status:  " + status);
        }

        return entry;
    }

    private SantanderEntryNew synchronizeFenixAndSantanderStates(User user, SantanderEntryNew entry) {
        GetRegisterResponse registerData = getRegister(user);
        GetRegisterStatus status = registerData.getStatus();

        SantanderEntryNew previousEntry = entry.getPrevious();

        if (previousEntry == null) {
            if (status.equals(GetRegisterStatus.NO_RESULT)) {
                entry.updateState(SantanderCardState.IGNORED);
                return entry;
            } else {
                return checkAndUpdateState(entry, registerData);
            }
        }

        String newMifare = registerData.getMifare();
        String oldMifare =
                previousEntry.getSantanderCardInfo() != null ? previousEntry.getSantanderCardInfo().getMifareNumber() : null;

        if (Strings.isNullOrEmpty(newMifare) || Strings.isNullOrEmpty(oldMifare) || !newMifare.equals(oldMifare)) {
            return checkAndUpdateState(entry, registerData);
        } else {
            entry.updateState(SantanderCardState.IGNORED);
            return entry;
        }
    }

    private GetRegisterResponse getRegister(User user) {
        
        logger.debug("Entering getRegister");

        final String userName = user.getUsername();

        try {
            GetRegisterResponse statusInformation = santanderCardService.getRegister(userName);
            logger.debug("Result: " + statusInformation.getStatus());
            return statusInformation;

        } catch (Throwable t) {
            logger.debug("failed trying to communicate with santander");
            t.printStackTrace();
            return null;
        }
    }

    public void createRegister(User user, RegisterAction action) {
        if (!getPersonAvailableActions(user).contains(action)) {
            throw new RuntimeException(
                    "Action (" + action.getLocalizedName() + ") not available for user " + user.getUsername());
        }

        SantanderEntryNew entry = createOrResetEntry(user);
        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(action);

        CreateRegisterResponse response = santanderCardService.createRegister(createRegisterRequest);

        entry.update(response);
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntryNew createOrResetEntry(User user) {
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntryNew(user);
        }

        SantanderCardState cardState = entryNew.getState();

        switch (cardState) {
        case IGNORED:
            entry.reset();
            return entry;
        case REJECTED:
        case ISSUED:
            new SantanderEntryNew(user);
        default:
            //should be impossible to reach;
            throw new RuntimeException();
        }
    }
}
