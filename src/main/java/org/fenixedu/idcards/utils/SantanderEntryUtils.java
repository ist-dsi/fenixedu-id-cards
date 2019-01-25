package org.fenixedu.idcards.utils;

import org.fenixedu.academic.domain.exceptions.DomainException;

import java.util.ArrayList;
import java.util.List;

public class SantanderEntryUtils {

    private static List<SantanderFieldValidator> validators = new ArrayList<SantanderFieldValidator>() {{
        add(new SantanderFieldValidator(true, 1, true)); // 0: record type
        add(new SantanderFieldValidator(false, 10, true)); // 1: id number
        add(new SantanderFieldValidator(false, 15, true)); // 2: first name
        add(new SantanderFieldValidator(false, 15, true)); // 3: surname
        add(new SantanderFieldValidator(false, 40, false)); // 4: middle names
        add(new SantanderFieldValidator(false, 50, true)); // 5: address 1
        add(new SantanderFieldValidator(false, 50, false)); // 6: address 2
        add(new SantanderFieldValidator(false, 8, true)); // 7: zip code
        add(new SantanderFieldValidator(false, 30, true)); // 8: town
        add(new SantanderFieldValidator(false, 10, false)); // 9: home country
        add(new SantanderFieldValidator(false, 10, true)); // 10: residence country
        add(new SantanderFieldValidator(false, 9, false)); // 11: expire date
        add(new SantanderFieldValidator(false, 16, false)); // 12: degree code
        add(new SantanderFieldValidator(true, 10, true)); // 13: back number
        add(new SantanderFieldValidator(true, 2, false)); // 14: curricular year
        add(new SantanderFieldValidator(true, 8, false)); // 15: execution year
        add(new SantanderFieldValidator(false, 30, false)); // 16: unit
        add(new SantanderFieldValidator(false, 10, false)); // 17: access control
        add(new SantanderFieldValidator(false, 4, true)); // 18: expire date
        add(new SantanderFieldValidator(false, 10, false)); // 19: template code
        add(new SantanderFieldValidator(false, 4, true)); // 20: action code
        add(new SantanderFieldValidator(true, 2, true)); // 21: role code
        add(new SantanderFieldValidator(false, 20, false)); // 22: role desc
        add(new SantanderFieldValidator(true, 1, true)); // 23: id document type
        add(new SantanderFieldValidator(true, 1, false)); // 24: check digit
        add(new SantanderFieldValidator(false, 2, true)); // 25: card type
        add(new SantanderFieldValidator(false, 2, true)); // 26: expedition code
        add(new SantanderFieldValidator(false, 50, false)); // 27: detour address 1
        add(new SantanderFieldValidator(false, 50, false)); // 28: detour address 2
        add(new SantanderFieldValidator(false, 50, false)); // 29: detour address 3
        add(new SantanderFieldValidator(false, 8, false)); // 30: detour zip code
        add(new SantanderFieldValidator(false, 30, false)); // 31: detour town
        add(new SantanderFieldValidator(true, 1, true)); // 32: additional data
        add(new SantanderFieldValidator(false, 40, false)); // 33: card name
        add(new SantanderFieldValidator(false, 100, false)); // 34: email
        add(new SantanderFieldValidator(false, 20, false)); // 35: phone
        add(new SantanderFieldValidator(true, 1, false)); // 36: photo flag
        add(new SantanderFieldValidator(false, 32, false)); // 37: photo ref
        add(new SantanderFieldValidator(true, 1, false)); // 38: signature flag
        add(new SantanderFieldValidator(false, 32, false)); // 39: signature ref
        add(new SantanderFieldValidator(true, 1, false)); // 40: dig certificate flag
        add(new SantanderFieldValidator(false, 32, false)); // 41: dig certificate ref
        add(new SantanderFieldValidator(false, 682, false)); // 42: filler
    }};

    public static String generateLine(List<String> values) {
        int i = 0;
        StringBuilder strBuilder = new StringBuilder(1500);

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
