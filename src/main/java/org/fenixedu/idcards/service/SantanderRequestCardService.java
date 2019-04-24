package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.dto.SantanderCardInfoDto;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
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

    public List<SantanderCardInfoDto> getUserSantanderCards(String username) {
        User user = User.findByUsername(username);

        return SantanderEntryNew.getSantanderCardHistory(user)
                .stream().map(SantanderCardInfoDto::new)
                .collect(Collectors.toList());
    }

    public List<RegisterAction> getPersonAvailableActions(User user) {
        SantanderEntryNew personEntry = getOrUpdateState(user);
        return getPersonAvailableActions(personEntry);
    }

    public List<RegisterAction> getPersonAvailableActions(SantanderEntryNew personEntry) {

        List<RegisterAction> actions = new LinkedList<>();

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
                return entry;

            case ISSUED:
                if (!SantanderEntryNew.hasMifare(entry.getUser(), registerData.getMifare())) {
                    entry.updateIssued(registerData);
                }
                return entry;

            case NO_RESULT:
                // syncing problem between both services
                if (!entry.wasRegisterSuccessful()) {
                    entry.updateState(SantanderCardState.IGNORED);
                }
                return entry;

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
            // TODO: check synchronization between the 2 webservices
            if (status.equals(GetRegisterStatus.NO_RESULT)) {
                entry.updateState(SantanderCardState.IGNORED);
                return entry;
            } else {
                return checkAndUpdateState(entry, registerData);
            }
        }

        String newMifare = registerData.getMifare();

        if (Strings.isNullOrEmpty(newMifare) || !SantanderEntryNew.hasMifare(user, newMifare)) {

            return checkAndUpdateState(entry, registerData);
        } else {
            entry.updateState(SantanderCardState.IGNORED);
            return entry;
        }
    }

    private GetRegisterResponse getRegister(User user) {
        final String userName = user.getUsername();

        try {
            GetRegisterResponse statusInformation = santanderCardService.getRegister(userName);
            return statusInformation;

        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public void createRegister(User user, RegisterAction action) {
        if (!getPersonAvailableActions(user.getCurrentSantanderEntry()).contains(action)) {
            throw new RuntimeException(
                    "Action (" + action.getLocalizedName() + ") not available for user " + user.getUsername());
        }

        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(action);

        try {
            CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
            SantanderEntryNew entry = createOrResetEntry(user, cardPreviewBean);
            CreateRegisterResponse response = santanderCardService.createRegister(cardPreviewBean);

            entry.update(response);
        } catch (SantanderValidationException sve) {
            //TODO send proper error
            throw new RuntimeException(sve.getMessage());
        } catch (RuntimeException rte) {
            return;
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntryNew createOrResetEntry(User user, CardPreviewBean cardPreviewBean) {
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntryNew(user, cardPreviewBean);
        }

        SantanderCardState cardState = entry.getState();

        switch (cardState) {
        case IGNORED:
            entry.reset(cardPreviewBean);
            return entry;
        case REJECTED:
        case ISSUED:
            return new SantanderEntryNew(user, cardPreviewBean);
        default:
            //should be impossible to reach;
            throw new RuntimeException();
        }
    }
}
