package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse.ErrorType;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class SantanderEntry extends SantanderEntry_Base {

    public static final String STATE_CHANGED = "fenixedu.idcards.domain.santanderEntry.stateChanged";

    public static Comparator<SantanderEntry> COMPARATOR_BY_CREATED_DATE = (p1, p2) -> {
        DateTime date1 = p1.getLastUpdate();
        DateTime date2 = p2.getLastUpdate();
        return date1.compareTo(date2);
    };

    public static Comparator<SantanderEntry> REVERSE_COMPARATOR_BY_CREATED_DATE = COMPARATOR_BY_CREATED_DATE.reversed();

    public SantanderEntry(User user, CardPreviewBean cardPreviewBean) {
        setRootDomainObject(Bennu.getInstance());
        SantanderEntry currentEntry = user.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        setUser(user);

        reset(cardPreviewBean);
    }

    public void reset(CardPreviewBean cardPreviewBean) {
        setRequestLine(cardPreviewBean.getRequestLine());
        setResponseLine("");
        setErrorDescription("");
        setSantanderCardInfo(new SantanderCardInfo(cardPreviewBean));
        updateState(SantanderCardState.PENDING);
    }
    
    @Atomic(mode = TxMode.WRITE)
    public void saveResponse(CreateRegisterResponse response) {

        if (response.wasRegisterSuccessful()) {
            update(SantanderCardState.NEW, response);
            return;
        }

        ErrorType errorType = response.getErrorType();
        switch (errorType) {
            case REQUEST_REFUSED:
                update(SantanderCardState.IGNORED, response);
                break;
            case SANTANDER_COMMUNICATION:
                update(SantanderCardState.PENDING, response);
                break;
            default:
                break;
        }
        setLastUpdate(DateTime.now());
    }

    private void update(SantanderCardState state, CreateRegisterResponse response) {
        updateState(state);
        setResponseLine(Strings.isNullOrEmpty(response.getResponseLine()) ? "" : response.getResponseLine());
        setErrorDescription(Strings.isNullOrEmpty(response.getErrorDescription()) ? "" : response.getErrorDescription());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateIssued(GetRegisterResponse registerData) {
        SantanderCardInfo cardInfo = getSantanderCardInfo();
        cardInfo.setMifareNumber(registerData.getMifare());
        cardInfo.setSerialNumber(registerData.getSerialNumber());

        updateState(SantanderCardState.ISSUED);
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateState(SantanderCardState state) {
        DateTime now = DateTime.now();

        if (getState() != state) {
            createSantanderCardStateTransition(state, now);
            setState(state);
            Signal.emit(STATE_CHANGED, this);
        }
        setLastUpdate(now);
    }

    public void createSantanderCardStateTransition(SantanderCardState state, DateTime date) {
        SantanderCardInfo cardInfo = getSantanderCardInfo();
        SantanderCardStateTransition transation = cardInfo.getLastTransition();
        DateTime lastTransactionTime = transation != null ? transation.getTransitionDate() : null;

        if (lastTransactionTime != null && lastTransactionTime.compareTo(date) >= 0) {
            new SantanderCardStateTransition(getSantanderCardInfo(), state, lastTransactionTime.plusMillis(1));
        } else {
            new SantanderCardStateTransition(getSantanderCardInfo(), state, date);
        }

    }

    public static List<SantanderEntry> getSantanderEntryHistory(User user) {
        LinkedList<SantanderEntry> history = new LinkedList<>();

        for(SantanderEntry entry = user.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            history.addFirst(entry);
        }

        return history;
    }

    public static List<SantanderCardInfo> getSantanderCardHistory(User user) {
        return getSantanderEntryHistory(user).stream()
                .filter(e -> e.getSantanderCardInfo() != null && e.getState() != SantanderCardState.IGNORED && e.getState() != SantanderCardState.PENDING)
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

    public boolean canRegisterNew() {
        SantanderCardState state = getState();
        return state == SantanderCardState.IGNORED && getPrevious() == null;
    }
    
    public boolean canReemitCard() {
        SantanderCardState state = getState();
        SantanderEntry previous = getPrevious();
        return state == SantanderCardState.ISSUED
                || (previous != null && previous.getState() == SantanderCardState.ISSUED && state == SantanderCardState.IGNORED);
    }

    public boolean canRenovateCard() {
        SantanderCardInfo cardInfo = getSantanderCardInfo();

        if (cardInfo == null) {
            return false;
        }

        DateTime expiryDate = getSantanderCardInfo().getExpiryDate();
        return canReemitCard() && expiryDate != null
                && Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), expiryDate.withTimeAtStartOfDay()).getDays() <= 60;
    }

    public static boolean hasMifare(User user, String mifare) {
        if (Strings.isNullOrEmpty(mifare)) {
            return false;
        }

        return getSantanderCardHistory(user).stream()
                .anyMatch(e -> e.getMifareNumber() != null && e.getMifareNumber().equals(mifare));
    }
}
