package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.dto.SantanderUser;
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
        SantanderEntryNew entryNew = user.getCurrentSantanderEntryNew();

        if (entryNew == null) {
            return null;
        }

        SantanderCardState cardState = entryNew.getState();

        switch (cardState) {
            case IGNORED:
            case ISSUED:
            case REJECTED:
                return entryNew;
            case PENDING:
                return synchronizeFenixAndSantanderStates(user, entryNew);
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
            //TODO use getRegister only when synchronizing and card is issued
            //Otherwise use getRegisterStatus
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
        SantanderUser santanderUser = new SantanderUser(user, userInfoService);

        /*
         * If there was an error on the previous entry update it
         * Else create a new entry
         */
        SantanderEntryNew entry = createOrResetEntry(user);


        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest();
        createRegisterRequest.setAction(action);

        CreateRegisterResponse response = santanderCardService.createRegister(createRegisterRequest);

        saveResponse(entry, response);
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntryNew createOrResetEntry(User user) {
        SantanderEntryNew entryNew = user.getCurrentSantanderEntryNew();
        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        if (entryNew == null) {
            return new SantanderEntryNew(santanderUser);
        }

        SantanderCardState cardState = entryNew.getState();

        switch (cardState) {
            case IGNORED:
                entryNew.reset(santanderUser);
                return entryNew;
            case ISSUED:
                return new SantanderEntryNew(santanderUser);
            default:
                throw new RuntimeException(); //TODO throw decent exception
        }
    }


    private void saveResponse(SantanderEntryNew entry, CreateRegisterResponse response) {
        if (response.wasRegisterSuccessful()) {
            entry.saveSuccessful(response.getRequestLine(), response.getResponseLine());
        }
        // TODO: Change this
        else if ("communication error".equals(response.getErrorDescription())) {
            entry.saveWithError(response.getRequestLine(), "Erro ao comunicar com o Santander", SantanderCardState.PENDING);
        }
        else {
            entry.saveWithError(response.getRequestLine(), response.getErrorDescription(), SantanderCardState.IGNORED);
        }
    }
}
