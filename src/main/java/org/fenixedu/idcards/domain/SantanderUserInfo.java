package org.fenixedu.idcards.domain;

import com.google.common.base.Strings;
import org.fenixedu.bennu.core.domain.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SantanderUserInfo extends SantanderUserInfo_Base {
    private static List<String> excludedNames = new ArrayList<String>(){{
        add("da");
        add("das");
        add("do");
        add("dos");
        add("de");
        add("e");
    }};

    public SantanderUserInfo() {
        super();
    }

    @Override
    public void setCardName(String cardName) {
        cardName = cardName.toUpperCase().trim();
        cardName = cardName.length() > 40 ? cardName.substring(0, 40) : cardName;
        super.setCardName(cardName);
    }

    public static boolean isCardNameValid(User user, String cardName) {

        if (Strings.isNullOrEmpty(cardName)) {
            return false;
        }

        List<String> fullNameParts = new ArrayList<>(Arrays.asList(user.getProfile().getFullName().toLowerCase().trim().split(" ")));
        List<String> cardNameParts = new ArrayList<>(Arrays.asList(cardName.toLowerCase().split(" ")));

        // All card names must be in fullName
        if (!fullNameParts.containsAll(cardNameParts)) {
            return false;
        }

        // Card names must not appear in higher frequency that in fullName
        for(String name: new HashSet<>(cardNameParts)) {
            if (Collections.frequency(fullNameParts, name) < Collections.frequency(cardNameParts, name)) {
                return false;
            }
        }

        // Names must appear in the same order as in fullName
        for (int i = 0; i < cardNameParts.size() - 1; i++) {
            String currentCardName = cardNameParts.get(i);
            String nextCardName = cardNameParts.get(i + 1);
            boolean currentFound = false;
            boolean orderSatisfied = false;

            for (String currentName : fullNameParts) {
                if (currentName.equals(currentCardName)) {
                    currentFound = true;
                }

                if (currentFound && currentName.equals(nextCardName)) {
                    orderSatisfied = true;
                }
            }

            if (!orderSatisfied) {
                return false;
            }
        }

        List<String> givenNames =  Arrays.asList(user.getProfile().getGivenNames().toLowerCase().trim().split(" "));
        List<String> familyNames =  Arrays.asList(user.getProfile().getFamilyNames().toLowerCase().trim().split(" "));

        // At least one given name must be present
        boolean givenNamePresent = givenNames.stream()
                .filter(n -> !excludedNames.contains(n))
                .anyMatch(n -> cardNameParts.contains(n) && cardNameParts.remove(n)); // Remove so the same name isnt matched in family name

        // At least one family name must be present
        boolean familyNamePresent = familyNames.stream()
                .filter(n -> !excludedNames.contains(n))
                .anyMatch(cardNameParts::contains);

        // Card name must have a maximum of 40 characters
        return givenNamePresent && familyNamePresent && cardName.length() <= 40;
    }
}
