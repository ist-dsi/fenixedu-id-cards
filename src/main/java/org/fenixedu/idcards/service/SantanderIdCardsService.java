package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.idcards.domain.PickupLocation;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.domain.SantanderUser;
import org.fenixedu.idcards.dto.SantanderCardDto;
import org.fenixedu.idcards.notifications.CardStateTransitionNotifications;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SantanderIdCardsService.class);
    /*
     * Sometimes santander webservice is not synchronized
     * e.g. request new card with success -> getCardState can return the old card information
     * this variable represents the number of days that the cardservice waits until it is sure that the services are in sync
     * it is used only when there were problems communicating with santander
     */
    private static final int SANTANDER_SYNC_DAYS = 1;

    private SantanderSdkService santanderCardService;
    private IUserInfoService userInfoService;

    @Autowired
    public SantanderIdCardsService(SantanderSdkService santanderCardService, IUserInfoService userInfoService) {
        this.santanderCardService = santanderCardService;
        this.userInfoService = userInfoService;

        Signal.register(SantanderEntry.STATE_CHANGED, CardStateTransitionNotifications::notifyUser);
    }

    public SantanderCardDto generateCardPreview(User user) throws SantanderValidationException {
        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        // Action doesnt matter
        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(RegisterAction.NOVO);
        CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
        return new SantanderCardDto(cardPreviewBean);
    }

    public List<SantanderCardDto> getUserSantanderCards(User user) {

        return SantanderEntry.getSantanderEntryHistory(user).stream()
                .map(entry -> new SantanderCardDto(entry.getSantanderCardInfo())).collect(Collectors.toList());
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
        case EXPIRED:
        case DELIVERED:
            return entry;
        case PENDING:
            return synchronizeFenixAndSantanderStates(user, entry);
        case REJECTED:
        case NEW:
        case READY_FOR_PRODUCTION:
        case PRODUCTION:
            return checkAndUpdateState(entry);
        default:
            LOGGER.debug("SantanderEntry " + entry.getExternalId() + " has unknown state (" + cardState.name() + ")");
            return entry;
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
            entry.updateStateAndNotify(SantanderCardState.REJECTED);
            return entry;

        case REMI_REQUEST:
        case RENU_REQUEST:
            entry.updateStateAndNotify(SantanderCardState.NEW);
            return entry;

        case READY_FOR_PRODUCTION:
            entry.updateStateAndNotify(SantanderCardState.READY_FOR_PRODUCTION);
            return entry;
        case PRODUCTION:
            entry.updateStateAndNotify(SantanderCardState.PRODUCTION);
            return entry;

        case ISSUED:
            if (!SantanderEntry.hasMifare(entry.getUser(), registerData.getMifare())) {
                entry.updateIssued(registerData);
            }
            return entry;

        case NO_RESULT:
            // syncing problem between both services
            if (!entry.wasRegisterSuccessful()) {
                entry.updateStateAndNotify(SantanderCardState.IGNORED);
            }
            return entry;
        case UNKNOWN:
            LOGGER.debug("Card has unkown state:  " + status);
        default:
            LOGGER.debug("Not supported status:  " + status);
        }

        return entry;
    }

    private SantanderEntry synchronizeFenixAndSantanderStates(User user, SantanderEntry entry) {
        GetRegisterResponse registerData = getRegister(user);

        if (registerData == null) {
            return entry;
        }

        GetRegisterStatus status = registerData.getStatus();

        SantanderEntry previousEntry = entry.getPrevious();

        if (previousEntry == null) {
            if (status.equals(GetRegisterStatus.NO_RESULT) && entry.getLastUpdate().plusDays(SANTANDER_SYNC_DAYS).isBeforeNow()) {
                entry.updateStateAndNotify(SantanderCardState.IGNORED);
                return entry;
            } else {
                return checkAndUpdateState(entry, registerData);
            }
        }

        String newMifare = registerData.getMifare();

        if (Strings.isNullOrEmpty(newMifare) || !SantanderEntry.hasMifare(user, newMifare)) {

            return checkAndUpdateState(entry, registerData);
        } else if (entry.getLastUpdate().plusDays(SANTANDER_SYNC_DAYS).isBeforeNow()) {
            entry.updateStateAndNotify(SantanderCardState.IGNORED);
        }
        return entry;
    }

    private GetRegisterResponse getRegister(User user) {
        final String userName = user.getUsername();

        try {
            GetRegisterResponse statusInformation = santanderCardService.getRegister(userName);
            return statusInformation;

        } catch (Throwable t) {
            LOGGER.error(String.format("Something went wrong getting info of user %s", user.getUsername()), t);
            return null;
        }
    }

    public SantanderEntry createRegister(User user) throws SantanderValidationException {
        List<RegisterAction> availableActions = getPersonAvailableActions(user);

        if (availableActions.contains(RegisterAction.NOVO)) {
            return createRegister(user, RegisterAction.NOVO);
        } else if (availableActions.contains(RegisterAction.RENU)) {
            return createRegister(user, RegisterAction.RENU);
        } else if (availableActions.contains(RegisterAction.REMI)) {
            return createRegister(user, RegisterAction.REMI);
        } else {
            throw new SantanderValidationException("santander.id.cards.error.user.cannot.request.card");
        }

    }

    public SantanderEntry createRegister(User user, RegisterAction action) throws SantanderValidationException {
        if (!getPersonAvailableActions(user.getCurrentSantanderEntry()).contains(action)) {
            LOGGER.debug("Action (" + action.name() + ") not available for user " + user.getUsername());
            throw new SantanderValidationException("santander.id.cards.error.wrong.request.action");
        }

        SantanderUser santanderUser = new SantanderUser(user, userInfoService);
        CreateRegisterRequest createRegisterRequest = santanderUser.toCreateRegisterRequest(action);

        CardPreviewBean cardPreviewBean = santanderCardService.generateCardRequest(createRegisterRequest);
        return createOrResetEntry(user, cardPreviewBean, santanderUser.getUserPickupLocation());
    }

    @Atomic(mode = TxMode.READ)
    public void sendRegister(User user, SantanderEntry santanderEntry) throws SantanderValidationException {

        CardPreviewBean cardPreviewBean = santanderEntry.getCardPreviewBean();
        CreateRegisterResponse response = santanderCardService.createRegister(cardPreviewBean);

        santanderEntry.saveResponse(response);

        if (response.getErrorType() != null) {
            throw new SantanderValidationException(response.getErrorType().getErrorMessage());
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private SantanderEntry createOrResetEntry(User user, CardPreviewBean cardPreviewBean, PickupLocation pickupLocation)
            throws SantanderValidationException {
        SantanderEntry entry = user.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntry(user, cardPreviewBean, pickupLocation);
        }

        SantanderCardState cardState = entry.getState();

        switch (cardState) {
        case IGNORED:
            entry.reset(cardPreviewBean, pickupLocation);
            return entry;
        case REJECTED:
        case ISSUED:
        case DELIVERED:
        case EXPIRED:
            return new SantanderEntry(user, cardPreviewBean, pickupLocation);
        default:
            //should be impossible to reach;
            throw new SantanderValidationException("santander.id.cards.error.santander.entry.invalid.state");
        }
    }

    public boolean canRequestCard(final User user) {
        if (user == null) {
            return false;
        }

        SantanderEntry currentSantanderEntry = user.getCurrentSantanderEntry();

        if (currentSantanderEntry == null) {
            return true;
        }

        return !getPersonAvailableActions(currentSantanderEntry).isEmpty();
    }

    public String getErrorMessage(Locale locale, String errorLabels) {
        String[] errorMessages = errorLabels.split("\n");
        String errorDescription = "";
        for (String errorMessage : errorMessages)
            errorDescription += BundleUtil.getString("resources.CardGenerationResources", locale, errorMessage);
        return errorDescription;
    }
}
