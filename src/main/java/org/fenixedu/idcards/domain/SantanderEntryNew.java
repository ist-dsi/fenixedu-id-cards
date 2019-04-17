package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse.ErrorType;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class SantanderEntryNew extends SantanderEntryNew_Base {

    static public Comparator<SantanderEntryNew> COMPARATOR_BY_CREATED_DATE = (p1, p2) -> {
        DateTime date1 = p1.getLastUpdate();
        DateTime date2 = p2.getLastUpdate();
        return date1.compareTo(date2);
    };

    static public Comparator<SantanderEntryNew> REVERSE_COMPARATOR_BY_CREATED_DATE = COMPARATOR_BY_CREATED_DATE.reversed();

    public SantanderEntryNew(User user, CardPreviewBean cardPreviewBean) {
        setRootDomainObject(Bennu.getInstance());
        SantanderEntryNew currentEntry = user.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        setUser(user);
        reset(cardPreviewBean);
    }
    
    @Atomic(mode = TxMode.WRITE)
    public void update(CreateRegisterResponse response) {

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

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateIssued(GetRegisterResponse registerData) {
        updateState(SantanderCardState.ISSUED);

        SantanderCardInfo cardInfo = getSantanderCardInfo();
        cardInfo.setMifareNumber(registerData.getMifare());
        setLastUpdate(DateTime.now());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateState(SantanderCardState state) {
        if (state != SantanderCardState.IGNORED && state != SantanderCardState.REJECTED) {
            SantanderCardInfo cardInfo = getSantanderCardInfo();

            if (cardInfo.getCurrentState() == SantanderCardState.PENDING && state != SantanderCardState.PENDING) {
                cardInfo.deleteTransitions();
            }

            if (cardInfo.getCurrentState() != state) {
                new SantanderCardStateTransition(getSantanderCardInfo(), state, DateTime.now());
            }
        } else {
            setState(state);
        }
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void update(SantanderCardState state, CreateRegisterResponse response) {
        updateState(state);
        setResponseLine(Strings.isNullOrEmpty(response.getResponseLine()) ? "" : response.getResponseLine());
        setErrorDescription(Strings.isNullOrEmpty(response.getErrorDescription()) ? "" : response.getErrorDescription());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void reset(CardPreviewBean cardPreviewBean) {
        setLastUpdate(DateTime.now());
        setState(SantanderCardState.PENDING);
        setRequestLine(cardPreviewBean.getRequestLine());
        setSantanderCardInfo(new SantanderCardInfo(cardPreviewBean));
        setResponseLine("");
        setErrorDescription("");
    }
    
    public static List<SantanderEntryNew> getSantanderEntryHistory(User user) {
        LinkedList<SantanderEntryNew> history = new LinkedList<>();

        for(SantanderEntryNew entry = user.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            history.addFirst(entry);
        }

        return history;
    }

    public static List<SantanderCardInfo> getSantanderCardHistory(User user) {
        return getSantanderEntryHistory(user).stream()
                .filter(e -> e.getSantanderCardInfo() != null)
                .sorted(REVERSE_COMPARATOR_BY_CREATED_DATE)
                .map(SantanderEntryNew::getSantanderCardInfo)
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
        SantanderEntryNew previous = getPrevious();
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
                && Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), expiryDate.withTimeAtStartOfDay()).getDays() < 60;
    }

    @Override
    public SantanderCardState getState() {
        SantanderCardInfo cardInfo = getSantanderCardInfo();

        if (cardInfo == null) {
            return super.getState();
        }

        return getSantanderCardInfo().getCurrentState();
    }
}
