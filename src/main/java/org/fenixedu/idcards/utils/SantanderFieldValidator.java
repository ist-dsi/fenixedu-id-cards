package org.fenixedu.idcards.utils;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;

public class SantanderFieldValidator {

    private boolean required;
    private boolean numeric;
    private int size;
    private String field;

    public SantanderFieldValidator(String field, boolean numeric, int size, boolean required) {
        this.field = field;
        this.numeric = numeric;
        this.size = size;
        this.required = required;
    }

    public void validate(String s) throws SantanderValidationException {
        if (Strings.isNullOrEmpty(s)) {
            if (required) {
                throw new SantanderValidationException("field " + field + " is missing");
            } else {
                return;
            }
        }

        if (s.length() > size) {
            String template = "field %s (%s) has to many characters (max characters: %d)";
            String error = String.format(template, field, s, size);
            throw new SantanderValidationException(error);
        }

        if (numeric && !StringUtils.isNumeric(s)) {
            String template = "field %s (%s) can only contain numbers";
            String error = String.format(template, field, s);
            throw new SantanderValidationException(error);
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
