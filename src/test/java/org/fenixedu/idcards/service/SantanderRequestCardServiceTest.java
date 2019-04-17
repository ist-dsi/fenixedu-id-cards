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

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.google.common.io.BaseEncoding;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sun.misc.BASE64Decoder;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    @Mock
    private SantanderCardService mockedService;

    @Mock
    private IUserInfoService userInfoService;

    private static final String MIFARE1 = "123456789";

    private static final String MIFARE2 = "987654321";

    private static final String PHOTO_ENCODED = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        BufferedImage image = mock(BufferedImage.class);
        when(image.getWidth()).thenReturn(100);
        when(image.getHeight()).thenReturn(100);

        when(userInfoService.getUserPhoto(any(User.class))).thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
    }

    private CardPreviewBean createCardPreview() {
        CardPreviewBean cardPreview = new CardPreviewBean();
        cardPreview.setCardName("test");
        cardPreview.setExpiryDate(DateTime.now());
        cardPreview.setIdentificationNumber("123");
        cardPreview.setLine("entry");
        cardPreview.setPhoto(BaseEncoding.base64().decode(PHOTO_ENCODED));

        return cardPreview;
    }

    private CreateRegisterResponse successResponse() {
        CreateRegisterResponse sucessResponse = new CreateRegisterResponse();
        sucessResponse.setResponseLine("response");
        return sucessResponse;
    }

    private CreateRegisterResponse errorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setErrorType(CreateRegisterResponse.ErrorType.REQUEST_REFUSED);
        failWithErrorResponse.setErrorDescription("error");
        return failWithErrorResponse;
    }

    private CreateRegisterResponse communicationErrorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setErrorType(CreateRegisterResponse.ErrorType.SANTANDER_COMMUNICATION);
        failWithErrorResponse.setResponseLine("error response");
        failWithErrorResponse.setErrorDescription("communication error");
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
    public void createRegister_noPreviousEntry_success() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterSuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailWithError");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
    }


    @Test
    public void createRegister_noPreviousEntry_failCommunication() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunication");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_readyForProduction() throws SantanderValidationException {
        // ##### Arrange #####

        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterReadyForProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncNew");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO); //createRegister fails with communication error
        service.getOrUpdateState(user); //getRegister is called and state is synched with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(SantanderCardState.NEW, entry.getSantanderCardInfo().getCurrentState());
        assertEquals(entry.getSantanderCardInfo().getSantanderCardStateTransitionsSet().size(), 1);
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_issued() throws SantanderValidationException {
        // ##### Arrange #####

        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncIssued");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);

        service.createRegister(user, RegisterAction.NOVO); //createRegister fails with communication error
        service.getOrUpdateState(user); //getRegister is called and state is synched with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_and_retry_success() throws SantanderValidationException {

        // Fail response
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorAndRetrySuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
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
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        service = new SantanderRequestCardService(mockedService, userInfoService);


        service.createRegister(user, RegisterAction.NOVO);

        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_twice() throws SantanderValidationException {

        // Fail response
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorTwice");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
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


        CardPreviewBean cardPreview = createCardPreview();
        cardPreview.setLine("entry2");
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(cardPreview); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());


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
    public void createRegister_noPreviousEntry_failCommunication_and_retryWithoutSynchronize() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenThrow(WebServiceException.class);


        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    }

    @Test
    public void createRegister_withPreviousEntry_reemission_success() throws SantanderValidationException, InterruptedException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());


        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));
        Thread.sleep(1000);

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());

        // Success response 2
        service.createRegister(user, RegisterAction.REMI);

        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());

        assertEquals(2, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(2, SantanderEntryNew.getSantanderEntryHistory(user).size());
    }

    @Test
    public void createRegister_withPreviousEntry_failWithError() throws SantanderValidationException, InterruptedException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailError");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // We need to wait, to ensure that the state transition for both transitions is at the same time
        // giving inconsistent ordering in the getCurrentState method... TODO: ?
        Thread.sleep(1000);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());

        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);


        // Error response
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());
        service.createRegister(user, RegisterAction.REMI);


        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("error", entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(2, SantanderEntryNew.getSantanderEntryHistory(user).size());
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication() throws SantanderValidationException, InterruptedException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());


        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunication");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);


        assertNotNull(user.getCurrentSantanderEntry());
        SantanderEntryNew entry = user.getCurrentSantanderEntry();

        assertTrue(entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertEquals("response", entry.getResponseLine());
        assertEquals(SantanderCardState.NEW, entry.getState());


        // Finalize transition to ISSUED
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));
        Thread.sleep(1000);

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        entry = user.getCurrentSantanderEntry();

        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNotNull(entry.getSantanderCardInfo());
        assertEquals(MIFARE1, entry.getSantanderCardInfo().getMifareNumber());


        // Fail communication response
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        service.createRegister(user, RegisterAction.REMI);


        assertNotNull(user.getCurrentSantanderEntry());
        entry = user.getCurrentSantanderEntry();

        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals("entry", entry.getRequestLine());
        assertNotNull(entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(2, SantanderEntryNew.getSantanderEntryHistory(user).size());
    }

    /*@Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_readyForProduction() {
        // ##### Arrange #####
        CreateRegisterResponse communicationResponse = communicationErrorResponse();
        communicationResponse.setRequestLine("entry2");
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse()).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterReadyForProduction(),
                getRegisterReadyForProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncNew");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO); //success
        service.getOrUpdateState(user); //first card is issued
        service.createRegister(user, RegisterAction.REMI); //new card with communication error
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

    /*@Test
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
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO); //success
        service.getOrUpdateState(user); //first card is issued
        service.createRegister(user, RegisterAction.REMI); //new card with communication error
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
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
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
    }*/
}
