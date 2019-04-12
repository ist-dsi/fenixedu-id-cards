package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse.ErrorType;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.YearMonthDay;

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

    public SantanderEntryNew(User user) {
        setRootDomainObject(Bennu.getInstance());
        SantanderEntryNew currentEntry = user.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        setUser(user);
        setLastUpdate(DateTime.now());
        setState(SantanderCardState.PENDING);
        setSantanderCardInfo(new SantanderCardInfo());
        setRequestLine("");
        setResponseLine("");
        setErrorDescription("");
    }
    
    @Atomic(mode = TxMode.WRITE)
    public void update(CreateRegisterResponse response) {
        ErrorType errorType = response.getErrorType();
        switch (errorType) {
        case REQUEST_REFUSED:
        case INVALID_INFORMATION:
            update(SantanderCardState.IGNORED, response);
        case SANTANDER_COMMUNICATION:
            update(SantanderCardState.PENDING, response);
            break;
        case NONE:
            update(SantanderCardState.NEW, response);
            break;
        default:
            break;
        }

        updateCardInfo(response);
        setLastUpdate(DateTime.now());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void update(GetRegisterResponse registerData) {
        SantanderCardInfo cardInfo = getSantanderCardInfo();
        cardInfo.setMifareNumber(registerData.getMifare());
        setState(SantanderCardState.ISSUED);
        setLastUpdate(DateTime.now());
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void updateState(SantanderCardState state) {        
        setState(state);
        setLastUpdate(DateTime.now());
    }

    public void updateCardInfo(CreateRegisterResponse response) {
        ErrorType errorType = response.getErrorType();
        if (errorType != ErrorType.INVALID_INFORMATION) {
            SantanderCardInfo cardInfo =
                    new SantanderCardInfo(response.getCardName(), response.getCardExpiryDate(), response.getPhoto());
            setSantanderCardInfo(cardInfo);
        }
    }

    public void update(SantanderCardState state, CreateRegisterResponse response) {
        updateState(state);
        setRequestLine(Strings.isNullOrEmpty(response.getRequestLine()) ? "" : response.getRequestLine());
        setResponseLine(Strings.isNullOrEmpty(response.getResponseLine()) ? "" : response.getResponseLine());
        setErrorDescription(Strings.isNullOrEmpty(response.getErrorDescription()) ? "" : response.getErrorDescription());
    }

    public void reset() {
        setLastUpdate(DateTime.now());
        setState(SantanderCardState.PENDING);
    }
    
    public static List<SantanderEntryNew> getSantanderEntryHistory(User user) {
        LinkedList<SantanderEntryNew> history = new LinkedList<>();

        for(SantanderEntryNew entry = user.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            history.addFirst(entry);
        }

        return history;
    }

    public static List<SantanderEntryNew> getSantanderCardHistory(User user) {
        return getSantanderEntryHistory(user).stream()
                .filter(SantanderEntryNew::wasRegisterSuccessful)
                .sorted(REVERSE_COMPARATOR_BY_CREATED_DATE)
                .collect(Collectors.toList());
    }

    public static List<SantanderEntryNew> getSantanderEntryHistory(ExecutionYear executionYear) {
        return Bennu.getInstance().getSantanderEntriesNewSet().stream()
                .filter(sen -> sen.getExecutionYear().equals(executionYear))
                .sorted(SantanderEntryNew.REVERSE_COMPARATOR_BY_CREATED_DATE).collect(Collectors.toList());
    }

    public ExecutionYear getExecutionYear() {
        DateTime dateTime = getLastUpdate();
        YearMonthDay yearMonthDay = new YearMonthDay(dateTime.getMillis());

        return ExecutionYear.getExecutionYearByDate(yearMonthDay);
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
        DateTime expiryDate = getSantanderCardInfo().getExpiryDate();
        return canReemitCard() && expiryDate != null
                && Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), expiryDate.withTimeAtStartOfDay()).getDays() < 60;
    }
}
