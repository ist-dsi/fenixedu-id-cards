package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.dto.SantanderCardDto;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderSdkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@Service
public class SantanderIdCardsService {

    private SantanderSdkService santanderCardService;
    private IUserInfoService userInfoService;

    @Autowired
    public SantanderIdCardsService(SantanderSdkService santanderCardService, IUserInfoService userInfoService) {
        this.santanderCardService = santanderCardService;
        this.userInfoService = userInfoService;
    }

    private Logger logger = LoggerFactory.getLogger(SantanderIdCardsService.class);

    public List<SantanderCardDto> getUserSantanderCards(User user) {

        return SantanderEntry.getSantanderCardHistory(user)
                .stream().map(SantanderCardDto::new)
                .collect(Collectors.toList());
    }

    public List<RegisterAction> getPersonAvailableActions(User user) {
        SantanderEntry personEntry = getOrUpdateState(user);
        return getPersonAvailableActions(personEntry);
    }

    public List<RegisterAction> getPersonAvailableActions(SantanderEntry personEntry) {

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

    public SantanderEntry getOrUpdateState(User user) {
         SantanderEntry entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return null;
        }

        SantanderCardState cardState = entry.getState();

        switch (cardState) {
            case IGNORED:
            case ISSUED:
                return entry;
            case PENDING:
                return synchronizeFenixAndSantanderStates(user, entry);
            case REJECTED:
            case NEW:
            case READY_FOR_PRODUCTION:
            case PRODUCTION:
                return checkAndUpdateState(entry);
            default:
                logger.debug("SantanderEntry " + entry.getExternalId() + " has unknown state (" + cardState.name() + ")");
                throw new RuntimeException();
        }
    }

    private SantanderEntry checkAndUpdateState(SantanderEntry entry) {
        GetRegisterResponse registerData = getRegister(entry.getUser());
        return checkAndUpdateState(entry, registerData);
    }

    private SantanderEntry checkAndUpdateState(SantanderEntry entry, GetRegisterResponse registerData) {
        if (registerData == null) {
            return entry;
        }

        GetRegisterStatus status = registerData.getStatus();

        switch (status) {
            case REJECTED_REQUEST:
                entry.updateState(SantanderCardState.REJECTED);
                return entry;

            case REMI_REQUEST:
            case RENU_REQUEST:
                entry.updateState(SantanderCardState.NEW);
                return entry;

            case READY_FOR_PRODUCTION:
                entry.updateState(SantanderCardState.READY_FOR_PRODUCTION);
                return entry;
            case PRODUCTION:
                entry.updateState(SantanderCardState.PRODUCTION);
                return entry;

            case ISSUED:
                if (!SantanderEntry.hasMifare(entry.getUser(), registerData.getMifare())) {
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

    private SantanderEntry synchronizeFenixAndSantanderStates(User user, SantanderEntry entry) {
        GetRegisterResponse registerData = getRegister(user);
        GetRegisterStatus status = registerData.getStatus();

        SantanderEntry previousEntry = entry.getPrevious();

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

        if (Strings.isNullOrEmpty(newMifare) || !SantanderEntry.hasMifare(user, newMifare)) {

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
                    "Action (" + action.name() + ") not available for user " + user.getUsername());
        }

        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(action);

        try {
            CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
            SantanderEntry entry = createOrResetEntry(user, cardPreviewBean);
            CreateRegisterResponse response = santanderCardService.createRegister(cardPreviewBean);

            entry.saveResponse(response);
        } catch (SantanderValidationException sve) {
            //TODO send proper error
            throw new RuntimeException(sve.getMessage());
        } catch (RuntimeException rte) {
            return;
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntry createOrResetEntry(User user, CardPreviewBean cardPreviewBean) {
        SantanderEntry entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntry(user, cardPreviewBean);
        }

        SantanderCardState cardState = entry.getState();

        switch (cardState) {
        case IGNORED:
            entry.reset(cardPreviewBean);
            return entry;
        case REJECTED:
        case ISSUED:
            return new SantanderEntry(user, cardPreviewBean);
        default:
            //should be impossible to reach;
            throw new RuntimeException();
        }
    }
}
