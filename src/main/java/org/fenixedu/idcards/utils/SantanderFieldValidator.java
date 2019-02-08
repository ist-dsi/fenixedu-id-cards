package org.fenixedu.idcards.utils;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;

public class SantanderFieldValidator {

    private boolean required;
    private boolean numeric;
    private int size;
    private String fieldName;

    public SantanderFieldValidator(String fieldName, boolean numeric, int size, boolean required) {
        this.fieldName = fieldName;
        this.numeric = numeric;
        this.size = size;
        this.required = required;
    }

    public void validate(String s) throws SantanderValidationException {
        if (Strings.isNullOrEmpty(s)) {
            if (required) {
                throw new SantanderValidationException("property " + fieldName + " is missing");
            } else {
                return;
            }
        }

        if (s.length() > size) {
            String template = "property %s (%s) has to many characters (max characters: %d)";
            String error = String.format(template, fieldName, s, size);
            throw new SantanderValidationException(error);
        }

        if (numeric && !StringUtils.isNumeric(s)) {
            String template = "property %s (%s) can only contain numbers";
            String error = String.format(template, fieldName, s);
            throw new SantanderValidationException(error);
        }
    }

    public String getfieldName() {
        return fieldName;
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
