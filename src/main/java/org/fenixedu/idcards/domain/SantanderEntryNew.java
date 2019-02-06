package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.idcards.utils.SantanderEntryUtils;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

public class SantanderEntryNew extends SantanderEntryNew_Base {

    static public Comparator<SantanderEntryNew> COMPARATOR_BY_CREATED_DATE = new Comparator<SantanderEntryNew>() {
        @Override
        public int compare(final SantanderEntryNew p1, final SantanderEntryNew p2) {
            DateTime date1 = p1.getCreatedAt();
            DateTime date2 = p2.getCreatedAt();
            return date1.compareTo(date2);
        }
    };

    public SantanderEntryNew(Person person, String requestLine, String responseLine, boolean registerSuccessful, String errorDescription) {
        super();
        setRootDomainObject(Bennu.getInstance());
        setCreatedAt(DateTime.now());
        setCardIssued(false);
        if (person.getCurrentSantanderEntry() != null) {
            setPrevious(person.getCurrentSantanderEntry());
        }
        person.setCurrentSantanderEntry(this);
        setPhotograph(person.getPersonalPhoto());
        setRequestLine(requestLine);
        setResponseLine(responseLine);
        setRegisterSuccessful(registerSuccessful);
        setErrorDescription(errorDescription);
    }

    public static List<SantanderEntryNew> getSantanderEntryHistory(Person person) {
        LinkedList<SantanderEntryNew> history = new LinkedList<>();

        for(SantanderEntryNew entry = person.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            history.addFirst(entry);
        }

        return history;
    }

    public static List<SantanderEntryNew> getSantanderEntryHistory(ExecutionYear executionYear) {
        return null;
    }

    public ExecutionYear getExecutionYear() {
        DateTime dateTime = getCreatedAt();
        YearMonthDay yearMonthDay = new YearMonthDay(dateTime.getMillis());

        return ExecutionYear.getExecutionYearByDate(yearMonthDay);
    }

    public String getIdentificationNumber() {
        return SantanderEntryUtils.getValue(getRequestLine(), 1);
    }

    public String getName() {
        String firstName = SantanderEntryUtils.getValue(getRequestLine(), 2);
        String surname = SantanderEntryUtils.getValue(getRequestLine(), 3);
        return firstName.trim() + " " + surname.trim();
    }

    public String getErrorCode() {
        return getResponseLine().substring(18, 20);
    }

    public DateTime getExpiryDate() {
        String requestLine = getRequestLine();

        String expiryDateString = SantanderEntryUtils.getValue(requestLine, 18);

        return DateTime.parse(expiryDateString, DateTimeFormat.forPattern("yyMM"));
    }
}
