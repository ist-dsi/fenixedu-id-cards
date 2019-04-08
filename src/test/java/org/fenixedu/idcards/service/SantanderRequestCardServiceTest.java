package org.fenixedu.idcards.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.xml.ws.WebServiceException;

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

import com.google.common.io.BaseEncoding;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    private static final String PHOTO_ENCODED = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    private static final String MIFARE1 = "123456789";

    private static final String MIFARE2 = "987654321";

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

    private GetRegisterResponse getRegisterIssued(String mifare) {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.ISSUED);
        getRegisterResponse.setMifare(mifare);
        return getRegisterResponse;
    }
    
    private GetRegisterResponse getRegisterReadyForProduction() {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.PRODUCTION);
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
    public void createRegister_noPreviousEntry_failCommunication_getRegister_readyForProduction() throws SantanderCardMissingDataException {
        // ##### Arrange #####
        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenThrow(WebServiceException.class);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterReadyForProduction());

        // ##### Act #####
        Person person = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncNew");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG,
                BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        String TUI_ENTRY = "entry";
        service.createRegister(TUI_ENTRY, person); //createRegister fails with communicayiom error
        service.getOrUpdateState(person); //getRegister is called and state is synched with santander
        
        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(person); //getRegister is called
        verify(mockedService, times(1)).createRegister(any(String.class), any(byte[].class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew entry = person.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(TUI_ENTRY, entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getSantanderCardInfo());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_issued() throws SantanderCardMissingDataException {
        // ##### Arrange #####
        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenThrow(WebServiceException.class);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        Person person = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncIssued");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG, BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        String TUI_ENTRY = expiredEntry();
        service.createRegister(TUI_ENTRY, person); //createRegister fails with communicayiom error
        service.getOrUpdateState(person); //getRegister is called and state is synched with santander
        
        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(person); //getRegister should not be called
        verify(mockedService, times(1)).createRegister(any(String.class), any(byte[].class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew entry = person.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(TUI_ENTRY, entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());
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
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


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
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());

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
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(person);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = person.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());

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

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_readyForProduction()
            throws SantanderCardMissingDataException {
        // ##### Arrange #####
        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse())
                .thenThrow(WebServiceException.class);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterReadyForProduction(),
                getRegisterReadyForProduction());

        // ##### Act #####
        Person person = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncNew");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG, BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);
        String requestLine1 = "entry1";
        String requestLine2 = "entry2";
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        service.createRegister(requestLine1, person); //success
        service.getOrUpdateState(person); //first card is issued
        service.createRegister(requestLine2, person); //new card with communication error
        service.getOrUpdateState(person); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(person); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(String.class), any(byte[].class));
        verify(mockedService, times(3)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew newEntry = person.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(requestLine2, newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, newEntry.getState());
        assertNull(newEntry.getSantanderCardInfo());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(requestLine1, oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_issued()
            throws SantanderCardMissingDataException {
        // ##### Arrange #####
        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse())
                .thenThrow(WebServiceException.class);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterIssued(MIFARE2));

        // ##### Act #####
        Person person = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncIssued");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG, BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);
        String requestLine1 = "entry1";
        String requestLine2 = expiredEntry();
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        service.createRegister(requestLine1, person); //success
        service.getOrUpdateState(person); //first card is issued
        service.createRegister(requestLine2, person); //new card with communication error
        service.getOrUpdateState(person); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(person); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(String.class), any(byte[].class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew newEntry = person.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(requestLine2, newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, newEntry.getState());
        assertNotNull(newEntry.getSantanderCardInfo());
        assertEquals(MIFARE2, newEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(requestLine1, oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_oldCard()
            throws SantanderCardMissingDataException {
        // ##### Arrange #####
        SantanderCardService mockedService = mock(SantanderCardService.class);
        when(mockedService.createRegister(any(String.class), any(byte[].class))).thenReturn(successResponse())
                .thenThrow(WebServiceException.class);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        Person person = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncOldCard");
        Photograph photo = new Photograph(PhotoType.INSTITUTIONAL, ContentType.PNG, BaseEncoding.base64().decode(PHOTO_ENCODED));
        person.setPersonalPhoto(photo);
        String requestLine1 = "entry1";
        String requestLine2 = expiredEntry();
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService);
        service.createRegister(requestLine1, person); //success
        service.getOrUpdateState(person); //first card is issued
        service.createRegister(requestLine2, person); //new card with communication error
        service.getOrUpdateState(person); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(person); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(String.class), any(byte[].class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew newEntry = person.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(requestLine2, newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertEquals("Erro ao comunicar com o Santander", newEntry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, newEntry.getState());
        assertNull(newEntry.getSantanderCardInfo());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(requestLine1, oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(person).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(person).size(), 2);
    }
}
