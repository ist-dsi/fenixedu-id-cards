package org.fenixedu.idcards.domain;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.idcards.utils.SantanderCardState;
import org.fenixedu.idcards.utils.SantanderEntryUtils;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.google.gson.JsonObject;

public class SantanderEntryNew extends SantanderEntryNew_Base {

    static public Comparator<SantanderEntryNew> COMPARATOR_BY_CREATED_DATE = new Comparator<SantanderEntryNew>() {
        @Override
        public int compare(final SantanderEntryNew p1, final SantanderEntryNew p2) {
            DateTime date1 = p1.getLastUpdate();
            DateTime date2 = p2.getLastUpdate();
            return date1.compareTo(date2);
        }
    };

    static public Comparator<SantanderEntryNew> REVERSE_COMPARATOR_BY_CREATED_DATE = new Comparator<SantanderEntryNew>() {
        @Override
        public int compare(final SantanderEntryNew p1, final SantanderEntryNew p2) {
            DateTime date1 = p1.getLastUpdate();
            DateTime date2 = p2.getLastUpdate();
            return date2.compareTo(date1);
        }
    };

    public SantanderEntryNew(Person person, String requestLine) {
        setRootDomainObject(Bennu.getInstance());
        SantanderEntryNew currentEntry = person.getCurrentSantanderEntry();
        if (currentEntry != null) {
            setPrevious(currentEntry);
            currentEntry.setNext(this);
        }
        person.setCurrentSantanderEntry(this);
        setRequestLine(requestLine);
        setLastUpdate(DateTime.now());

        // No response from server yet
        setState(SantanderCardState.FENIX_ERROR);
    }

    public void reset(String requestLine) {
        setLastUpdate(DateTime.now());
        setState(SantanderCardState.FENIX_ERROR);
        setRequestLine(requestLine);
    }

    public void saveSuccessful(String responseLine) {
        setLastUpdate(DateTime.now());
        setResponseLine(responseLine);
        setState(SantanderCardState.NEW);
    }

    public void saveWithError(String errorDescription, SantanderCardState state) {
        // TODO: Check the correct error state
        setLastUpdate(DateTime.now());
        setErrorDescription(errorDescription);
        setState(state);
    }

    /*public void reset(Person person) {
        updateEntry(person, SantanderCardState.FENIX_ERROR, DateTime.now(), "", "", false, "Erro no fenix");
    }

    public void updateEntry(Person person, SantanderCardState state, DateTime lastUpdated, String requestLine,
            String responseLine,
            boolean registerSuccessful, String errorDescription) {

        setPhotograph(person.getPersonalPhoto());
        setLastUpdate(lastUpdated);
        setState(state);
        setRequestLine(requestLine);
        setResponseLine(responseLine);
        setRegisterSuccessful(registerSuccessful);
        setErrorDescription(errorDescription);
    }*/

    public static SantanderEntryNew getLastSuccessfulEntry(Person person) {

        for (SantanderEntryNew entry = person.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            if (entry.getRegisterSuccessful()) {
                return entry;
            }
        }

        return null;
    }

    public static List<SantanderEntryNew> getSantanderEntryHistory(Person person) {
        LinkedList<SantanderEntryNew> history = new LinkedList<>();

        for(SantanderEntryNew entry = person.getCurrentSantanderEntry(); entry != null; entry = entry.getPrevious()) {
            history.addFirst(entry);
        }

        return history;
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

    public String getIdentificationNumber() {
        return SantanderEntryUtils.getValue(getRequestLine(), 1);
    }

    public String getName() {
        String firstName = SantanderEntryUtils.getValue(getRequestLine(), 2);
        String surname = SantanderEntryUtils.getValue(getRequestLine(), 3);
        return firstName.trim() + " " + surname.trim();
    }

    public String getErrorCode() {
        if (getRegisterSuccessful()) {
            return "";
        }

        try {
            return getResponseLine().substring(18, 20);
        } catch (StringIndexOutOfBoundsException soobe) {
            return "-1";
        }
    }

    public String getErrorDescriptionMessage() {
        if (getRegisterSuccessful()) {
            return "";
        }
        return getErrorCode() + " - " + getErrorDescription();
    }

    public DateTime getExpiryDate() {
        String requestLine = getRequestLine();

        String expiryMonth = SantanderEntryUtils.getValue(requestLine, 18).substring(2);
        String expiryDateYear = SantanderEntryUtils.getValue(requestLine, 11).substring(5);
        String expiryDateString = expiryMonth + expiryDateYear;

        System.out.println(expiryDateString);

        return DateTime.parse(expiryDateString, DateTimeFormat.forPattern("MMyyyy"));
    }

    public JsonObject getResponseAsJson() {
        //TODO
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
