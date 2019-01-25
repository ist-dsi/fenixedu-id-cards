package org.fenixedu.idcards.utils;

import org.apache.commons.lang.StringUtils;

public class SantanderFieldValidator {

    private boolean required;
    private boolean numeric;
    private int size;

    public SantanderFieldValidator(boolean numeric, int size, boolean required) {
        this.numeric = numeric;
        this.size = size;
        this.required = required;
    }

    public void validate(String s) {
        if (s == null && required) {
            throw new SantanderValidationException();
        } else if (s == null) {
            return;
        }

        if (s.length() > size) {
            throw new SantanderValidationException();
        }

        if (numeric && !StringUtils.isNumeric(s)) {
            throw new SantanderValidationException();
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
