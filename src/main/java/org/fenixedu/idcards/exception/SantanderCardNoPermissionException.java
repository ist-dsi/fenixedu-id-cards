package org.fenixedu.idcards.exception;

import org.fenixedu.santandersdk.exception.SantanderValidationException;

public class SantanderCardNoPermissionException extends SantanderValidationException {

    public SantanderCardNoPermissionException() {
        super();
    }

    public SantanderCardNoPermissionException(final String msg) {
        super(msg);
    }
}
