package org.fenixedu.idcards;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;

import java.util.Locale;
import java.util.UUID;

public class IdCardsTestUtils {

    public static User createPerson(String username) {
        UserProfile userProfile = new UserProfile("test", "test", "test test", "test@test.com", Locale.getDefault());

        return new User(username, userProfile);
    }

    public static User createUser(String givenNames, String familyNames) {
        UserProfile userProfile = new UserProfile(givenNames, familyNames, givenNames + " " + familyNames, "test@test.com", Locale.getDefault());

        return new User(UUID.randomUUID().toString(), userProfile);
    }
}
