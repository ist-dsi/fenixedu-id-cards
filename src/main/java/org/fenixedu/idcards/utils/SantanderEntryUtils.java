package org.fenixedu.idcards.utils;

import org.fenixedu.academic.domain.exceptions.DomainException;

import java.util.ArrayList;
import java.util.List;

public class SantanderEntryUtils {

    private static List<SantanderFieldValidator> validators = new ArrayList<SantanderFieldValidator>() {{
        add(new SantanderFieldValidator(true, true, 1));
        add(new SantanderFieldValidator(true, false, 10));
        add(new SantanderFieldValidator(true, false, 15));
        add(new SantanderFieldValidator(false, false, 40));
    }};

    public static String generateLine(List<String> values) {
        int i = 0;
        StringBuilder strBuilder = new StringBuilder(1505);

        for (String value: values) {
            strBuilder.append(makeStringBlock(value, validators.get(i)));
            i++;
        }

        return strBuilder.toString();
    }

    private static String makeStringBlock(String value, SantanderFieldValidator validator) {
        validator.validate(value);

        int size = validator.getSize();

        int fillerLength = size - value.length();
        if (fillerLength < 0) {
            throw new DomainException("Content is bigger than string block.");
        }
        StringBuilder blockBuilder = new StringBuilder(size);
        blockBuilder.append(value);

        for (int i = 0; i < fillerLength; i++) {
            blockBuilder.append(" ");
        }

        return blockBuilder.toString();
    }

    public static String getValue(String line, int fieldIndex) {
        int i = 0;
        int beginIndex = 0;

        for (; i < fieldIndex; i++) {
            SantanderFieldValidator validator = validators.get(i);
            beginIndex += validator.getSize();
        }
        int endIndex = validators.get(i).getSize();

        return line.substring(beginIndex, endIndex).trim();
    }
}
