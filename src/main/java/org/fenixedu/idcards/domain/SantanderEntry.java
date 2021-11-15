package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.idcards.dto.RequestedCardBean;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse.ErrorType;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.service.SantanderEntryValidator;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class SantanderEntry extends SantanderEntry_Base {

    public static final String STATE_CHANGED = "fenixedu.idcards.domain.santanderEntry.stateChanged";

    public static Comparator<SantanderEntry> COMPARATOR_BY_CREATED_DATE = (p1, p2) -> {
        DateTime date1 = p1.getCreationDate();
        DateTime date2 = p2.getCreationDate();
        return date1.compareTo(date2);
    };

    public static Comparator<SantanderEntry> REVERSE_COMPARATOR_BY_CREATED_DATE = COMPARATOR_BY_CREATED_DATE.reversed();

    @Atomic(mode = TxMode.WRITE)
    public static SantanderEntry importEntry(final User user, final RequestedCardBean requestedCardBean) {
        if (hasMifare(user, requestedCardBean.getMifare()))
            return null;

        return new SantanderEntry(user, requestedCardBean);
    }

    private SantanderEntry(final User user, final RequestedCardBean requestedCardBean) {
        setBennu(Bennu.getInstance());
        final SantanderEntry currentEntry = user.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        setUser(user);

        setRequestLine(requestedCardBean.getRequestLine());
        setResponseLine("");
        setErrorDescription("");
        setWasPickupNotified(true);

        final SantanderCardInfo cardInfo = new SantanderCardInfo();
        cardInfo.setIdentificationNumber(requestedCardBean.getIdentificationNumber());
        cardInfo.setCardName(requestedCardBean.getCardName());
        cardInfo.setMifareNumber(requestedCardBean.getMifare());
        cardInfo.setRole(requestedCardBean.getRole());
        cardInfo.setSerialNumber(requestedCardBean.getCardSerialNumber());
        cardInfo.setPickupLocation(PickupLocation.ALAMEDA_SANTANDER);

        final DateTime cardExpiryTime = requestedCardBean.getExpiryDate();
        cardInfo.setExpiryDate(cardExpiryTime);

        final String photo = requestedCardBean.getPhoto();
        if (photo != null && photo.length() > 0)
            cardInfo.setPhoto(BaseEncoding.base64().decode(photo));

        setSantanderCardInfo(cardInfo);

        final DateTime requestDate = requestedCardBean.getRequestDate();

        updateState(SantanderCardState.PENDING, requestDate);
        updateState(SantanderCardState.NEW, requestDate);
        updateState(SantanderCardState.ISSUED, requestedCardBean.getProductionDate());

        if (DateTime.now().isAfter(cardExpiryTime)) {
            updateState(SantanderCardState.EXPIRED, cardExpiryTime);
            setWasExpiringNotified(true);
        } else {
            setWasExpiringNotified(false);
        }
    }

    public SantanderEntry(final User user, final CardPreviewBean cardPreviewBean, final PickupLocation pickupLocation,
                          final String requestReason) {
        setBennu(Bennu.getInstance());
        final SantanderEntry currentEntry = user.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        setUser(user);
        reset(cardPreviewBean, pickupLocation, requestReason);
    }

    public SantanderCardState getState() {
        return getSantanderCardInfo().getCurrentState();
    }

    public CardPreviewBean getCardPreviewBean() {
        final CardPreviewBean cardPreviewBean = new CardPreviewBean();
        final SantanderCardInfo card = getSantanderCardInfo();
        cardPreviewBean.setCardName(card.getCardName());
        cardPreviewBean.setExpiryDate(card.getExpiryDate());
        cardPreviewBean.setRole(card.getRole());
        cardPreviewBean.setIdentificationNumber(card.getIdentificationNumber());
        cardPreviewBean.setPhoto(card.getPhoto());
        cardPreviewBean.setRequestLine(getRequestLine());
        return cardPreviewBean;
    }

    public DateTime getCreationDate() {
        return getSantanderCardInfo().getFirstTransitionDate();
    }

    public void reset(final CardPreviewBean cardPreviewBean, final PickupLocation pickupLocation, final String requestReason) {
        setRequestReason(requestReason);
        setRequestLine(cardPreviewBean.getRequestLine());
        setResponseLine("");
        setErrorDescription("");
        setSantanderCardInfo(new SantanderCardInfo(cardPreviewBean, pickupLocation));
        updateState(SantanderCardState.PENDING);
        setWasExpiringNotified(false);
        setWasPickupNotified(false);
    }
    
    @Atomic(mode = TxMode.WRITE)
    public void saveResponse(final CreateRegisterResponse response) {
        if (response.wasRegisterSuccessful()) {
            update(SantanderCardState.NEW, response, true);
            return;
        }

        final ErrorType errorType = response.getErrorType();
        switch (errorType) {
            case REQUEST_REFUSED:
                update(SantanderCardState.IGNORED, response, false);
                break;
            case SANTANDER_COMMUNICATION:
                update(SantanderCardState.PENDING, response, false);
                break;
            default:
                break;
        }
        setLastUpdate(DateTime.now());
    }

    private void update(final SantanderCardState state, final CreateRegisterResponse response, final boolean notify) {
        updateState(state, DateTime.now(), notify);
        setResponseLine(Strings.isNullOrEmpty(response.getResponseLine()) ? "" : response.getResponseLine());
        setErrorDescription(Strings.isNullOrEmpty(response.getErrorDescription()) ? "" : response.getErrorDescription());
    }

    @Atomic(mode = TxMode.WRITE)
    public void updateIssued(final GetRegisterResponse registerData) {
        final SantanderCardInfo cardInfo = getSantanderCardInfo();
        final DateTime expeditionDate = registerData.getExpeditionDate() == null ? DateTime.now() : registerData.getExpeditionDate();

        cardInfo.setMifareNumber(registerData.getMifare());
        cardInfo.setSerialNumber(registerData.getSerialNumber());

        if (cardInfo.getExpiryDate() == null) {
            cardInfo.setExpiryDate(registerData.getExpiryDate());
        }

        updateState(SantanderCardState.ISSUED, expeditionDate);
    }

    @Atomic(mode = TxMode.WRITE)
    public void updateState(final SantanderCardState state) {
        updateState(state, DateTime.now(), false);
    }


    @Atomic(mode = TxMode.WRITE)
    private void updateState(final SantanderCardState state, final DateTime time) {
        updateState(state, time, false);
    }

    private void updateState(final SantanderCardState state, final DateTime time, final boolean notify) {
        if (getSantanderCardInfo().getSantanderCardStateTransitionsSet().stream()
                .noneMatch(t -> state.equals(t.getState()))) {
            createSantanderCardStateTransition(state, time);
            if (notify) {
                Signal.emit(STATE_CHANGED, this);
            }
        }
        setLastUpdate(time);
    }

    private void createSantanderCardStateTransition(final SantanderCardState state, final DateTime date) {
        final SantanderCardInfo cardInfo = getSantanderCardInfo();
        final SantanderCardStateTransition lastTransition = cardInfo.getLastTransition();
        final DateTime lastTransitionTime = lastTransition != null ? lastTransition.getTransitionDate() : null;

        if (lastTransition != null && SantanderCardState.EXPIRED.equals(lastTransition.getState()) &&
                SantanderCardState.DELIVERED.equals(state)) {
            new SantanderCardStateTransition(getSantanderCardInfo(), state, lastTransitionTime.minusSeconds(1));
        } else {
            if (lastTransitionTime != null && lastTransitionTime.compareTo(date) >= 0) {
                new SantanderCardStateTransition(getSantanderCardInfo(), state, lastTransitionTime.plusSeconds(1));
            } else {
                new SantanderCardStateTransition(getSantanderCardInfo(), state, date);
            }
        }
    }

    public static List<SantanderEntry> getSantanderEntryHistory(final User user) {
        final LinkedList<SantanderEntry> history = new LinkedList<>();
        for(SantanderEntry entry = user.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            if (entry.getState() != SantanderCardState.WAITING_INFO) {
                history.add(entry);
            }
        }
        return history;
    }

    @Deprecated
    public static List<SantanderCardInfo> getSantanderCardHistory(final User user) {
        return getSantanderEntryHistory(user).stream()
                .filter(e -> e.getSantanderCardInfo() != null && e.getState() != SantanderCardState.IGNORED
                        && e.getState() != SantanderCardState.PENDING)
                .sorted(REVERSE_COMPARATOR_BY_CREATED_DATE)
                .map(SantanderEntry::getSantanderCardInfo)
                .collect(Collectors.toList());
    }

    public boolean wasRegisterSuccessful() {
        return getState() != SantanderCardState.IGNORED && getState() != SantanderCardState.PENDING;
    }

    @Override
    public String getErrorDescription() {
        if (wasRegisterSuccessful()) {
            return "";
        } else {
            return super.getErrorDescription();
        }
    }

    public boolean isCardIssued() {
        final SantanderCardState state = getState();
        return state == SantanderCardState.ISSUED || state == SantanderCardState.DELIVERED || state == SantanderCardState.EXPIRED;
    }

    public boolean canRegisterNew() {
        final SantanderCardState state = getState();
        return (state == SantanderCardState.WAITING_INFO) || (state == SantanderCardState.IGNORED && getPrevious() == null);
    }
    
    public boolean canReemitCard() {
        final SantanderCardState state = getState();
        final SantanderEntry previous = getPrevious();
        return isCardIssued() || (state == SantanderCardState.IGNORED && previous != null && previous.isCardIssued());
    }

    public boolean canRenovateCard() {
        if (getState() == SantanderCardState.IGNORED && getPrevious() == null)
            return false;

        final DateTime expiryDate = getState() == SantanderCardState.IGNORED ? getPrevious().getSantanderCardInfo()
                .getExpiryDate() : getSantanderCardInfo().getExpiryDate();

        return canReemitCard() && expiryDate != null
                && Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), expiryDate.withTimeAtStartOfDay()).getDays() <= 60;
    }

    public static boolean hasMifare(final User user, final String mifare) {
        if (Strings.isNullOrEmpty(mifare)) {
            return false;
        }

        return getSantanderEntryHistory(user).stream().anyMatch(e -> e.getSantanderCardInfo().getMifareNumber() != null
                && e.getSantanderCardInfo().getMifareNumber().equals(mifare));
    }

    public static String getLastMifareNumber(final User user) {
        return SantanderEntry.getSantanderEntryHistory(user).stream()
                .filter(e -> e.getSantanderCardInfo().getMifareNumber() != null
                        && e.getSantanderCardInfo().isDelivered())
                .min(REVERSE_COMPARATOR_BY_CREATED_DATE)
                .map(e -> e.getSantanderCardInfo().getMifareNumber())
                .orElse(null);
    }

    public static SantanderEntry readByUsernameAndRoleCode(final String username, final String roleCode) {
        final User user = User.findByUsername(username);
        if (user == null) {
            return null;
        }

        final SantanderEntryValidator entryValidator = new SantanderEntryValidator();
        return SantanderEntry.getSantanderEntryHistory(user).stream()
                .filter(e -> e.getSantanderCardInfo().isDelivered() &&
                        roleCode.equals(entryValidator.getValue(e.getRequestLine(), 21)))
                .min(REVERSE_COMPARATOR_BY_CREATED_DATE)
                .orElse(null);
    }

}
