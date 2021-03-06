package org.fenixedu.idcards.domain;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(FenixFrameworkRunner.class)
public class SantanderUserInfoTest {

    @Test
    public void isCardNameValid_fail_empty() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, null));
        assertFalse(SantanderUserInfo.isCardNameValid(user, ""));
    }

    @Test
    public void isCardNameValid_fail_nonPresentNames() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste fail Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Etset fail Xpto Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Etset - Xpto Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Etset- Xpto Asd"));
    }

    @Test
    public void isCardNameValid_fail_nonMatchingNameCount() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");
        final User user2 = IdCardsTestUtils.createUser("Teste Teste", "Asd Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Teste Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Asd Asd"));

        assertFalse(SantanderUserInfo.isCardNameValid(user2, "Teste Teste Asd Asd Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user2, "Teste Teste Teste Asd Asd"));
    }

    @Test
    public void isCardNameValid_fail_noGivenNamePresent() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Asd Xpto"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Xpto"));
    }

    @Test
    public void isCardNameValid_fail_noFamilyNamePresent() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Etset"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Etset"));
    }

    @Test
    public void isCardNameValid_fail_nameWith41Characters() {
        final String givenNames = StringUtils.repeat("a", 20);
        final String familyNames = StringUtils.repeat("a", 20);

        final User user = IdCardsTestUtils.createUser(givenNames, familyNames);

        assertFalse(SantanderUserInfo.isCardNameValid(user, givenNames + " " + familyNames));
    }

    @Test
    public void isCardNameValid_fail_wrongNameOrder() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");
        final User user2 = IdCardsTestUtils.createUser("Teste Etset Teste Xpto123", "Xpto Asd dsa Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Xpto Teste"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Etset Teste Asd"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Etset Asd Xpto"));

        assertFalse(SantanderUserInfo.isCardNameValid(user2, "Teste Etset Asd Xpto"));
        assertFalse(SantanderUserInfo.isCardNameValid(user2, "Teste Xpto123 Etset Asd"));
    }

    @Test
    public void isCardNameValid_fail_withExcludedNames() {
        final User user = IdCardsTestUtils.createUser("Teste de Etset", "Xpto Asd");
        final User user2 = IdCardsTestUtils.createUser("Teste Etset", "Xpto de Asd");
        final User user3 = IdCardsTestUtils.createUser("De Etset", "Xpto Das Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "de Etset"));
        assertFalse(SantanderUserInfo.isCardNameValid(user2, "Teste de"));
        assertFalse(SantanderUserInfo.isCardNameValid(user3, "de das"));
    }

    @Test
    public void isCardNameValid_fail_withUserWithRepeatedNames() {
        final User user = IdCardsTestUtils.createUser("Teste Xpto", "Teste Asd");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste Xpto"));
    }

    @Test
    public void isCardNameValid_success_withUserWithRepeatedNames() {
        final User user = IdCardsTestUtils.createUser("Teste Xpto", "Teste Asd");
        final User user2 = IdCardsTestUtils.createUser("Teste Xpto Teste", "Teste Xpto Asd Asd");

        assertTrue(SantanderUserInfo.isCardNameValid(user, "Teste Teste"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Xpto ASd"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Teste ASd"));

        assertTrue(SantanderUserInfo.isCardNameValid(user2, "Teste Teste"));
        assertTrue(SantanderUserInfo.isCardNameValid(user2, "Teste Teste Asd Asd"));
        assertTrue(SantanderUserInfo.isCardNameValid(user2, "Xpto Teste"));
        assertTrue(SantanderUserInfo.isCardNameValid(user2, "Xpto Asd"));
        assertTrue(SantanderUserInfo.isCardNameValid(user2, "Teste Asd"));
    }

    @Test
    public void isCardNameValid_success_nameWith40Characters() {
        final String givenNames = StringUtils.repeat("a", 19);
        final String familyNames = StringUtils.repeat("a", 20);

        final User user = IdCardsTestUtils.createUser(givenNames, familyNames);

        assertTrue(SantanderUserInfo.isCardNameValid(user, givenNames + " " + familyNames));
    }

    @Test
    public void isCardNameValid_success_simpleName() {
        final User user = IdCardsTestUtils.createUser("Teste Etset", "Xpto Asd");

        assertTrue(SantanderUserInfo.isCardNameValid(user, "teste Xpto"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Etset Asd"));
    }

    @Test
    public void isCardNameValid_fail_nameWithDashes() {
        final User user = IdCardsTestUtils.createUser("Teste-Teste Ola", "Xpto-Asd xpto");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Teste-Teste Xpto"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Ola Xpto-Asd"));
    }

    @Test
    public void isCardNameValid_success_nameWithDashes() {
        final User user = IdCardsTestUtils.createUser("Teste-Teste Ola", "Xpto-Asd xpto");

        assertTrue(SantanderUserInfo.isCardNameValid(user, "Teste Teste Xpto"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Ola Xpto Asd"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Ola Asd"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Ola xpto"));
    }

    @Test
    public void isCardValid_fail_nameWithApostrophes() {
        final User user = IdCardsTestUtils.createUser("Qwerty's Teste", "Xpto Qwerty");

        assertFalse(SantanderUserInfo.isCardNameValid(user, "Qwerty's Xpto"));
        assertFalse(SantanderUserInfo.isCardNameValid(user, "Qwerty's Teste Xpto"));
    }

    @Test
    public void isCardValid_success_nameWithApostrophes() {
        final User user = IdCardsTestUtils.createUser("Qwerty's Teste", "Xpto Qwerty");

        assertTrue(SantanderUserInfo.isCardNameValid(user, "Qwertys Qwerty"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Qwertys Xpto"));
    }

    @Test
    public void isCardNameValid_success_withRightRepeatedNames() {
        final User user = IdCardsTestUtils.createUser("Teste Teste Teste", "Ola Ola Ola");

        assertTrue(SantanderUserInfo.isCardNameValid(user, "Teste Teste Teste Ola"));
        assertTrue(SantanderUserInfo.isCardNameValid(user, "Teste Ola Ola Ola"));
    }
}
