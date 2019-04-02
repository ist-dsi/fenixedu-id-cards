package org.fenixedu.idcards.service;

import com.google.common.io.BaseEncoding;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.PhotoType;
import org.fenixedu.academic.domain.Photograph;
import org.fenixedu.academic.util.ContentType;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.utils.SantanderCardState;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;

import javax.xml.ws.WebServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    private static final String PHOTO_ENCODED = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @Test
    public void createRegister_noPreviousEntry_success() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterSuccess");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);
        CreateRegisterResponse sucessResponse = new CreateRegisterResponse();
        sucessResponse.setRegisterSuccessful(true);
        sucessResponse.setResponseLine("response");

        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(sucessResponse);


        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterFailWithError");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setRegisterSuccessful(false);
        failWithErrorResponse.setErrorDescription("error");

        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(failWithErrorResponse);


        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertFalse(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterFailCommunication");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);

        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenThrow(WebServiceException.class);

        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);
        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertFalse(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }
}
