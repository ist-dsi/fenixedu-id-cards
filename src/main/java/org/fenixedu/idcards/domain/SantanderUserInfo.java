package org.fenixedu.idcards.domain;

import com.google.common.base.Strings;
import org.fenixedu.bennu.core.domain.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SantanderUserInfo extends SantanderUserInfo_Base {

    private static final List<String> excludedNames = new ArrayList<String>(){{
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

    public static boolean isCardNameValid(final User user, final String cardName) {

        if (Strings.isNullOrEmpty(cardName)) {
            return false;
        }

        final String normalizedFullName = getNormalizedSantanderUserFullName(user);

        final List<String> normalizedFullNameParts = new ArrayList<>(Arrays.asList(normalizedFullName.toLowerCase().trim().split(" ")));
        final List<String> cardNameParts = new ArrayList<>(Arrays.asList(cardName.toLowerCase().split(" ")));

        // All card names must be in normalized fullName
        if (!normalizedFullNameParts.containsAll(cardNameParts)) {
            return false;
        }

        // Card names must not appear in higher frequency that in fullName
        for(final String name: new HashSet<>(cardNameParts)) {
            if (Collections.frequency(normalizedFullNameParts, name) < Collections.frequency(cardNameParts, name)) {
                return false;
            }
        }

        // Names must appear in the same order as in fullName
        for (int i = 0; i < cardNameParts.size() - 1; i++) {
            final String currentCardName = cardNameParts.get(i);
            final String nextCardName = cardNameParts.get(i + 1);
            boolean currentFound = false;
            boolean orderSatisfied = false;

            for (final String currentName : normalizedFullNameParts) {
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

        final String normalizedGivenNames = getNormalizedSantanderUserGivenNames(user);
        final String normalizedFamilyNames = getNormalizedSantanderUserFamilyNames(user);

        final List<String> givenNames =  Arrays.asList(normalizedGivenNames.toLowerCase().trim().split(" "));
        final List<String> familyNames =  Arrays.asList(normalizedFamilyNames.toLowerCase().trim().split(" "));

        // At least one given name must be present
        final boolean givenNamePresent = givenNames.stream()
                .filter(n -> !excludedNames.contains(n))
                .anyMatch(n -> cardNameParts.contains(n) && cardNameParts.remove(n)); // Remove so the same name isnt matched in family name

        // At least one family name must be present
        final boolean familyNamePresent = familyNames.stream()
                .filter(n -> !excludedNames.contains(n))
                .anyMatch(cardNameParts::contains);

        // Card name must have a maximum of 40 characters
        return givenNamePresent && familyNamePresent && cardName.length() <= 40;
    }

    public static String getNormalizedSantanderUserGivenNames(final User user) {
        return normalizeSantanderUserName(user.getProfile().getGivenNames());
    }

    public static String getNormalizedSantanderUserFamilyNames(final User user) {
        return normalizeSantanderUserName(user.getProfile().getFamilyNames());
    }

    public static String getNormalizedSantanderUserFullName(final User user) {
        return normalizeSantanderUserName(user.getProfile().getFullName());
    }

    private static String normalizeSantanderUserName(String name) {
        final Map<String, String> replacementMap = new HashMap<String, String>() {{
            put("-", " ");
            put("'", "");
        }};

        for (final String replacement : replacementMap.keySet()) {
            name = name.replaceAll(replacement, replacementMap.get(replacement));
        }

        return name;
    }

}
