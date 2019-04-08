package org.fenixedu.idcards.service;

import com.google.common.io.BaseEncoding;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.PhotoType;
import org.fenixedu.academic.domain.Photograph;
import org.fenixedu.academic.util.ContentType;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.RegisterAction;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.utils.SantanderCardState;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;

import javax.xml.ws.WebServiceException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    private static final String PHOTO_ENCODED = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    private CreateRegisterResponse successResponse() {
        CreateRegisterResponse sucessResponse = new CreateRegisterResponse();
        sucessResponse.setRegisterSuccessful(true);
        sucessResponse.setResponseLine("response");
        return sucessResponse;
    }

    private CreateRegisterResponse errorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setRegisterSuccessful(false);
        failWithErrorResponse.setErrorDescription("error");
        return failWithErrorResponse;
    }

    private GetRegisterResponse getRegisterIssued() {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.ISSUED);

        return getRegisterResponse;
    }

    private String expiredEntry() {
        DateFormat yearFormat = new SimpleDateFormat("yyyy");
        DateFormat monthFormat = new SimpleDateFormat("MM");

        String requestLine = String.format("%" + 248 + "s", yearFormat.format(Calendar.getInstance().getTime())).replace(' ', 'x');
        requestLine = String.format("%-" + 326 + "s", requestLine).replace(' ', 'x');
        requestLine = requestLine + monthFormat.format(Calendar.getInstance().getTime());

        return requestLine;
    }

    @Test
    public void createRegister_noPreviousEntry_success() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterSuccess");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);

        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());


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


        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(errorResponse());


        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
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

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_and_retry_success() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterFailErrorAndRetrySuccess");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);

        // Fail response
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(errorResponse());
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);

        // Success response
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());
        service = new SantanderRequestCardService(mockedService);
        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_twice() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterFailErrorTwice");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);

        // Fail response
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(errorResponse());
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);


        // Fail response 2
        service.createRegister("entry2", person);

        assertNotNull(person.getCurrentSantanderEntry());
        entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry2", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);
    }

    @Test(expected = Exception.class)
    public void createRegister_noPreviousEntry_failCommunication_and_retryWithoutSynchronize() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);

        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenThrow(WebServiceException.class);

        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        service.createRegister("entry", person);
        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        service.createRegister("entry", person);
    }

    @Test
    public void createRegister_withPreviousEntry_reemission_success() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterSuccessReemission");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        String requestLine = expiredEntry();

        // Success response
        service.createRegister(requestLine, person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(requestLine, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());


        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());

        // Success response 2
        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithError() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailError");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        String requestLine = expiredEntry();

        // Success response
        service.createRegister(requestLine, person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(requestLine, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());

        // Error response
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(errorResponse());
        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication() throws SantanderCardMissingDataException {
        Person person = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunication");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);

        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse());
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);

        String requestLine = expiredEntry();

        // Success response
        service.createRegister(requestLine, person);

        assertNotNull(person.getCurrentSantanderEntry());
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(requestLine, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());

        // Fail communication response
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenThrow(WebServiceException.class);
        service.createRegister("entry", person);

        assertNotNull(person.getCurrentSantanderEntry());
        entry = person.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }
}
