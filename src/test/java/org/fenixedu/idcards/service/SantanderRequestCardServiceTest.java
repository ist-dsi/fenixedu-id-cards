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

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    @Mock
    private SantanderCardService mockedService;

    private static final String MIFARE1 = "123456789";

    private static final String MIFARE2 = "987654321";

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    private CreateRegisterResponse successResponse() {
        CreateRegisterResponse sucessResponse = new CreateRegisterResponse();
        sucessResponse.setRegisterSuccessful(true);
        sucessResponse.setResponseLine("response");
        sucessResponse.setRequestLine("entry");
        return sucessResponse;
    }

    private CreateRegisterResponse errorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setRegisterSuccessful(false);
        failWithErrorResponse.setErrorDescription("error");
        failWithErrorResponse.setRequestLine("entry");
        return failWithErrorResponse;
    }

    private CreateRegisterResponse communicationErrorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setRegisterSuccessful(false);
        failWithErrorResponse.setResponseLine("error response");
        failWithErrorResponse.setErrorDescription("communication error");
        failWithErrorResponse.setRequestLine("entry");
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
    public void createRegister_noPreviousEntry_success() {
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterSuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError() {
        
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailWithError");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
    }


    @Test
    public void createRegister_noPreviousEntry_failCommunication() {
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(communicationErrorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunication");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }
    
    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_readyForProduction() {
        // ##### Arrange #####
        
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterReadyForProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncNew");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO); //createRegister fails with communicayiom error
        service.getOrUpdateState(user); //getRegister is called and state is synched with santander
        
        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(1)).createRegister(any(CreateRegisterRequest.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getSantanderCardInfo());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_issued() {
        // ##### Arrange #####
        CreateRegisterResponse expired = communicationErrorResponse();
        String TUI_ENTRY = expiredEntry();
        expired.setRequestLine(TUI_ENTRY);

        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(expired);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncIssued");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));

        service.createRegister(user, RegisterAction.NOVO); //createRegister fails with communication error
        service.getOrUpdateState(user); //getRegister is called and state is synched with santander
        
        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called
        verify(mockedService, times(1)).createRegister(any(CreateRegisterRequest.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(TUI_ENTRY, entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_and_retry_success() {
        
        // Fail response
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorAndRetrySuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);

        // Success response
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse());
        service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));


        service.createRegister(user, RegisterAction.NOVO);

        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_twice() {
        
        // Fail response
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorTwice");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);


        CreateRegisterResponse registerResponse2 = errorResponse();
        registerResponse2.setRequestLine("entry2");
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(registerResponse2);


        // Fail response 2
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry2", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(availableActions.size(), 1);
    }

    @Test(expected = Exception.class)
    public void createRegister_noPreviousEntry_failCommunication_and_retryWithoutSynchronize() {
        
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenThrow(WebServiceException.class);


        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }

    @Test
    public void createRegister_withPreviousEntry_reemission_success() {
        CreateRegisterResponse expired = successResponse();
        String expiredEntry = expiredEntry();
        expired.setRequestLine(expiredEntry);
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(expired);


        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(expiredEntry, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));


        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse());

        // Success response 2
        service.createRegister(user, RegisterAction.NOVO);

        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithError() {
        CreateRegisterResponse expired = successResponse();
        String expiredEntry = expiredEntry();
        expired.setRequestLine(expiredEntry);
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(expired);


        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailError");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(expiredEntry, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));


        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


        // Error response
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(errorResponse());
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication() {
        CreateRegisterResponse expired = successResponse();
        String expiredEntry = expiredEntry();
        expired.setRequestLine(expiredEntry);
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(expired);


        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunication");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(expiredEntry, entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));


        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


        // Fail communication response
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(communicationErrorResponse());
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_readyForProduction() {
        // ##### Arrange #####
        CreateRegisterResponse communicationResponse = communicationErrorResponse();
        communicationResponse.setRequestLine("entry2");
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse()).thenReturn(communicationResponse);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterReadyForProduction(),
                getRegisterReadyForProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncNew");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO); //success
        service.getOrUpdateState(user); //first card is issued
        service.createRegister(user, RegisterAction.NOVO); //new card with communication error
        service.getOrUpdateState(user); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(CreateRegisterRequest.class));
        verify(mockedService, times(3)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals("entry2", newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, newEntry.getState());
        assertNull(newEntry.getSantanderCardInfo());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals("entry", oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_issued() {
        // ##### Arrange #####
        CreateRegisterResponse communicationResponse = communicationErrorResponse();
        String expiredEntry = expiredEntry();
        communicationResponse.setRequestLine(expiredEntry);
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse())
                .thenReturn(communicationResponse);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterIssued(MIFARE2));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncIssued");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO); //success
        service.getOrUpdateState(user); //first card is issued
        service.createRegister(user, RegisterAction.NOVO); //new card with communication error
        service.getOrUpdateState(user); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(CreateRegisterRequest.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(expiredEntry, newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, newEntry.getState());
        assertNotNull(newEntry.getSantanderCardInfo());
        assertEquals(MIFARE2, newEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals("entry", oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 2);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_oldCard() {
        // ##### Arrange #####
        CreateRegisterResponse communicationResponse = communicationErrorResponse();
        String expiredEntry = expiredEntry();
        communicationResponse.setRequestLine(expiredEntry);
        when(mockedService.createRegister(any(CreateRegisterRequest.class))).thenReturn(successResponse())
                .thenReturn(communicationResponse);
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncOldCard");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, mock(IUserInfoService.class));
        service.createRegister(user, RegisterAction.NOVO); //success
        service.getOrUpdateState(user); //first card is issued
        service.createRegister(user, RegisterAction.NOVO); //new card with communication error
        service.getOrUpdateState(user); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(CreateRegisterRequest.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(expiredEntry, newEntry.getRequestLine());
        assertNotNull(newEntry.getErrorDescription());
        assertEquals("Erro ao comunicar com o Santander", newEntry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, newEntry.getState());
        assertNull(newEntry.getSantanderCardInfo());
        assertNotNull(newEntry.getPrevious());

        SantanderEntryNew oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals("entry", oldEntry.getRequestLine());
        assertNotNull(oldEntry.getErrorDescription());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertNotNull(oldEntry.getSantanderCardInfo());
        assertEquals(MIFARE1, oldEntry.getSantanderCardInfo().getMifareNumber());
        assertNotNull(oldEntry.getNext());
        assertEquals(newEntry, oldEntry.getNext());

        assertEquals(SantanderEntryNew.getSantanderCardHistory(user).size(), 1);
        assertEquals(SantanderEntryNew.getSantanderEntryHistory(user).size(), 2);
    }
}
