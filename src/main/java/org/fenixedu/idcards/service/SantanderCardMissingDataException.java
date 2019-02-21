package org.fenixedu.idcards.service;

public class SantanderCardMissingDataException extends Exception {

    public SantanderCardMissingDataException() {
        super();
    }

    public SantanderCardMissingDataException(String msg) {
        super(msg);
    }
}
