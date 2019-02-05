package org.fenixedu.idcards.utils;

import java.util.ArrayList;
import java.util.List;

import org.fenixedu.academic.domain.exceptions.DomainException;

import com.google.common.base.Joiner;

public class SantanderEntryUtils {

    private static List<SantanderFieldValidator> validators = new ArrayList<SantanderFieldValidator>() {{
        add(new SantanderFieldValidator("record type", true, 1, true)); // 0: record type
        add(new SantanderFieldValidator("id number", false, 10, true)); // 1: id number
        add(new SantanderFieldValidator("first name", false, 15, true)); // 2: first name
        add(new SantanderFieldValidator("surname", false, 15, true)); // 3: surname
        add(new SantanderFieldValidator("middle names", false, 40, false)); // 4: middle names
        add(new SantanderFieldValidator("address 1", false, 50, true)); // 5: address 1
        add(new SantanderFieldValidator("address 2", false, 50, false)); // 6: address 2
        add(new SantanderFieldValidator("zip code", false, 8, true)); // 7: zip code
        add(new SantanderFieldValidator("town", false, 30, true)); // 8: town
        add(new SantanderFieldValidator("home country", false, 10, false)); // 9: home country
        add(new SantanderFieldValidator("residence country", false, 10, true)); // 10: residence country
        add(new SantanderFieldValidator("expire date", false, 9, false)); // 11: expire date
        add(new SantanderFieldValidator("degree code", false, 16, false)); // 12: degree code
        add(new SantanderFieldValidator("back number", true, 10, true)); // 13: back number
        add(new SantanderFieldValidator("curricular year", true, 2, false)); // 14: curricular year
        add(new SantanderFieldValidator("execution year", true, 8, false)); // 15: execution year
        add(new SantanderFieldValidator("unit", false, 30, false)); // 16: unit
        add(new SantanderFieldValidator("access control", false, 10, false)); // 17: access control
        add(new SantanderFieldValidator("expire date", false, 4, true)); // 18: expire date
        add(new SantanderFieldValidator("template code", false, 10, false)); // 19: template code
        add(new SantanderFieldValidator("action code", false, 4, true)); // 20: action code
        add(new SantanderFieldValidator("role code", true, 2, true)); // 21: role code
        add(new SantanderFieldValidator("role desc", false, 20, false)); // 22: role desc
        add(new SantanderFieldValidator("id document type", true, 1, true)); // 23: id document type
        add(new SantanderFieldValidator("check digit", true, 1, false)); // 24: check digit
        add(new SantanderFieldValidator("card type", false, 2, true)); // 25: card type
        add(new SantanderFieldValidator("expedition code", false, 2, true)); // 26: expedition code
        add(new SantanderFieldValidator("detour address 1", false, 50, false)); // 27: detour address 1
        add(new SantanderFieldValidator("detour address 2", false, 50, false)); // 28: detour address 2
        add(new SantanderFieldValidator("detour address 3", false, 50, false)); // 29: detour address 3
        add(new SantanderFieldValidator("detour zip code", false, 8, false)); // 30: detour zip code
        add(new SantanderFieldValidator("detour town", false, 30, false)); // 31: detour town
        add(new SantanderFieldValidator("additional data", true, 1, true)); // 32: additional data
        add(new SantanderFieldValidator("card name", false, 40, false)); // 33: card name
        add(new SantanderFieldValidator("email", false, 100, false)); // 34: email
        add(new SantanderFieldValidator("phone", false, 20, false)); // 35: phone
        add(new SantanderFieldValidator("photo flag", true, 1, false)); // 36: photo flag
        add(new SantanderFieldValidator("photo ref", false, 32, false)); // 37: photo ref
        add(new SantanderFieldValidator("signature flag", true, 1, false)); // 38: signature flag
        add(new SantanderFieldValidator("signature ref", false, 32, false)); // 39: signature ref
        add(new SantanderFieldValidator("dig certificate flag", true, 1, false)); // 40: dig certificate flag
        add(new SantanderFieldValidator("dig certificate ref", false, 32, false)); // 41: dig certificate ref
        add(new SantanderFieldValidator("filler", false, 681, false)); // 42: filler
        add(new SantanderFieldValidator("end flag", false, 1, true)); // 43: end flag
    }};

    public static String generateLine(List<String> values) {
        List<String> errors = new ArrayList<>();
        StringBuilder strBuilder = new StringBuilder(1500);
        int i = 0;

        for (String value: values) {
            try {
                strBuilder.append(makeStringBlock(value, validators.get(i)));
            } catch (SantanderValidationException sve) {
                errors.add(sve.getMessage());
            }
            i++;
        }

        if (!errors.isEmpty()) {
            String errors_message = Joiner.on("\n").join(errors);
            throw new DomainException(errors_message); //TODO
        }

        return strBuilder.toString();
    }

    private static String makeStringBlock(String value, SantanderFieldValidator validator) throws SantanderValidationException {
        validator.validate(value);

        int size = validator.getSize();
        int fillerLength = size - value.length();

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
        int endIndex = validators.get(i).getSize() + beginIndex;

        return line.substring(beginIndex, endIndex).trim();
    }
}
