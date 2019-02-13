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

import com.google.gson.JsonObject;

public class SantanderEntryNew extends SantanderEntryNew_Base {

    static public Comparator<SantanderEntryNew> COMPARATOR_BY_CREATED_DATE = new Comparator<SantanderEntryNew>() {
        @Override
        public int compare(final SantanderEntryNew p1, final SantanderEntryNew p2) {
            DateTime date1 = p1.getCreatedAt();
            DateTime date2 = p2.getCreatedAt();
            return date1.compareTo(date2);
        }
    };

    static public Comparator<SantanderEntryNew> REVERSE_COMPARATOR_BY_CREATED_DATE = new Comparator<SantanderEntryNew>() {
        @Override
        public int compare(final SantanderEntryNew p1, final SantanderEntryNew p2) {
            DateTime date1 = p1.getCreatedAt();
            DateTime date2 = p2.getCreatedAt();
            return date2.compareTo(date1);
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
        if (!getRegisterSuccessful()) {
            return "";
        }

        try {
            return getResponseLine().substring(18, 20);
        } catch (StringIndexOutOfBoundsException soobe) {
            return "-1";
        }
    }

    public String getErrorDescriptionMessage() {
        return getErrorCode().isEmpty() ? getErrorDescription() : getErrorCode() + " - " + getErrorDescription();
    }

    public DateTime getExpiryDate() {
        String requestLine = getRequestLine();

        String expiryDateString = SantanderEntryUtils.getValue(requestLine, 18);

        return DateTime.parse("20" + expiryDateString, DateTimeFormat.forPattern("yyyyMM"));
    }

    public JsonObject getResponseAsJson() {
        JsonObject response = new JsonObject();

        if (!getRegisterSuccessful()) {
            response.addProperty("status", "Error");
            response.addProperty("errorCode", getErrorCode());
            response.addProperty("errorDescription", getErrorDescription());
        } else {
            response.addProperty("status", "Ok");
            response.addProperty("errorCode", "");
            response.addProperty("errorDescription", "");
        }
        return response;
    }

    public JsonObject getRequestAsJson() {
        return SantanderEntryUtils.getRequestAsJson(getRequestLine());
    }
}
