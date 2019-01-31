package org.fenixedu.idcards.domain;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

public class SantanderEntryNew extends SantanderEntryNew_Base {

    public SantanderEntryNew() {
        super();
        setRootDomainObject(Bennu.getInstance());
        setCreatedAt(DateTime.now());
        setCardIssued(false);
    }

    public SantanderEntryNew(Person person, String requestLine, String responseLine) {
        setPerson(person);
        setRequestLine(requestLine);
        setResponseLine(responseLine);
        setPhotograph(person.getPersonalPhoto());
    }

}
