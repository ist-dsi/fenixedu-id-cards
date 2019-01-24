package org.fenixedu.idcards.utils;

import org.apache.commons.lang.StringUtils;

public class Validator {

    private boolean required;
    private boolean numeric;
    private int size;

    public Validator(boolean required, boolean numeric, int size) {
        this.required = required;
        this.numeric = numeric;
        this.size = size;
    }

    public void validate(String s) {
        if (s == null && required) {
            throw new ValidationException();
        } else if (s == null) {
            return;
        }

        if (s.length() > size) {
            throw new ValidationException();
        }

        if (numeric && !StringUtils.isNumeric(s)) {
            throw new ValidationException();
        }
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public int getSize() {
        return size;
    }
}
