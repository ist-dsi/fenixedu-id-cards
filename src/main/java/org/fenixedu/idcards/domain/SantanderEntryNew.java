package org.fenixedu.idcards.domain;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.idcards.utils.SantanderEntryUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.LinkedList;
import java.util.List;

public class SantanderEntryNew extends SantanderEntryNew_Base {

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

    public DateTime getExpiryDate() {
        String requestLine = getRequestLine();

        String expiryDateString = SantanderEntryUtils.getValue(requestLine, 18);

        return DateTime.parse("20" + expiryDateString, DateTimeFormat.forPattern("yyMM"));
    }
}
