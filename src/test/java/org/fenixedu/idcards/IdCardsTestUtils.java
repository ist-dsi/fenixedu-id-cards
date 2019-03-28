package org.fenixedu.idcards;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;

import java.util.Locale;

public class IdCardsTestUtils {

    public static Person createPerson(String username) {
        UserProfile userProfile = new UserProfile("test", "test", "test test", "test@test.com", Locale.getDefault());
        User user = new User(username, userProfile);

        return new Person(userProfile);
    }
}
