package org.fenixedu.idcards.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;
import org.fenixedu.idcards.domain.SantanderEntryNew;
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

import com.google.common.io.BaseEncoding;

@RunWith(FenixFrameworkRunner.class)
public class SantanderRequestCardServiceTest {

    @Mock
    private SantanderCardService mockedService;

    @Mock
    private IUserInfoService userInfoService;

    private static final String IDENTIFICATION_NUMBER = "ist112345";

    private static final String CARD_NAME = "JOEL";

    private static final DateTime EXPIRY_DATE_EXPIRED = DateTime.now().minusYears(10);

    private static final DateTime EXPIRY_DATE_NOT_EXPIRED = DateTime.now();

    private static final String REQUEST_LINE1 = "Request Line 1";

    private static final String REQUEST_LINE2 = "Request Line 2";

    private static final String MIFARE1 = "123456789";

    private static final String MIFARE2 = "987654321";

    private static final String PHOTO_ENCODED = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    private static final byte[] PHOTO_DECODED = BaseEncoding.base64().decode(PHOTO_ENCODED);

    private static final String SUCCESS_RESPONSE = "Success Response";

    private static final String SUCCESS_ERROR_DESCPRITION = "No Error";

    private static final String REFUSED_REQUEST_ERROR_RESPONSE_LINE = "Invalid Request Error Response";

    private static final String REFUSED_REQUEST_ERROR_DESCRIPTION = "Invalid Request Error Description";

    private static final String COMMUNICATION_ERROR_RESPONSE_LINE = "Communication Error Response";

    private static final String COMMUNICATION_ERROR_DESCRIPTION = "Communication Error Description";


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BufferedImage image = mock(BufferedImage.class);
        when(image.getWidth()).thenReturn(100);
        when(image.getHeight()).thenReturn(100);

        when(userInfoService.getUserPhoto(any(User.class))).thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
    }

    private CardPreviewBean createCardPreview() {
        return createCardPreview(REQUEST_LINE1, EXPIRY_DATE_NOT_EXPIRED);
    }

    private CardPreviewBean createCardPreview(String requestLine, DateTime expiryDate) {
        CardPreviewBean cardPreview = new CardPreviewBean();
        cardPreview.setCardName(CARD_NAME);
        cardPreview.setExpiryDate(expiryDate);
        cardPreview.setIdentificationNumber(IDENTIFICATION_NUMBER);
        cardPreview.setLine(requestLine);
        cardPreview.setPhoto(PHOTO_DECODED);

        return cardPreview;
    }

    private CreateRegisterResponse successResponse() {
        CreateRegisterResponse sucessResponse = new CreateRegisterResponse();
        sucessResponse.setResponseLine(SUCCESS_RESPONSE);
        sucessResponse.setErrorDescription(SUCCESS_ERROR_DESCPRITION);
        return sucessResponse;
    }

    private CreateRegisterResponse errorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setErrorType(CreateRegisterResponse.ErrorType.REQUEST_REFUSED);
        failWithErrorResponse.setResponseLine(REFUSED_REQUEST_ERROR_RESPONSE_LINE);
        failWithErrorResponse.setErrorDescription(REFUSED_REQUEST_ERROR_DESCRIPTION);
        return failWithErrorResponse;
    }

    private CreateRegisterResponse communicationErrorResponse() {
        CreateRegisterResponse failWithErrorResponse = new CreateRegisterResponse();
        failWithErrorResponse.setErrorType(CreateRegisterResponse.ErrorType.SANTANDER_COMMUNICATION);
        failWithErrorResponse.setResponseLine(COMMUNICATION_ERROR_RESPONSE_LINE);
        failWithErrorResponse.setErrorDescription(COMMUNICATION_ERROR_DESCRIPTION);
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

    private GetRegisterResponse getRegisterNoResult() {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.NO_RESULT);
        return getRegisterResponse;
    }

    //TODO Tests
    //Make tests where the createRegister fails and then try different possibilities with getRegister and getAvailableActions
    //Make test where createRegister receives wrong action

    @Test
    public void createRegister_noPreviousEntry_success() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterSuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);        
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntryNew.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailWithError");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(0, SantanderEntryNew.getSantanderCardHistory(user).size());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunication");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(0, SantanderEntryNew.getSantanderCardHistory(user).size());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(1, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
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
        service.createRegister(user, RegisterAction.NOVO);
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should be called

        // ##### Assert #####        
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntryNew.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
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
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####        
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertEquals(MIFARE1, cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntryNew.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.ISSUED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_and_retry_success() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse()).thenReturn(successResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorAndRetrySuccess");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO); //fail response
        service.createRegister(user, RegisterAction.NOVO); //success response

        // ##### Assert #####
        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(1, SantanderEntryNew.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntryNew.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_twice() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview())
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorTwice");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO); //Fail response
        service.createRegister(user, RegisterAction.NOVO); //Fail response

        // ##### Assert #####
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, entry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(0, SantanderEntryNew.getSantanderCardHistory(user).size());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_and_retryWithoutSynchronize() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.createRegister(user, RegisterAction.NOVO);  //Receives fail communication error
        try {
            service.createRegister(user, RegisterAction.NOVO);  //Exception must be thrown because action is not valid
            fail();
        } catch (RuntimeException rte) {}

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntryNew entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntryNew.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntryNew.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntriesNew());
        assertEquals(0, SantanderEntryNew.getSantanderCardHistory(user).size());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(1, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
    }
/*
@Test
public void createRegister_withPreviousEntry_reemission_success() throws SantanderValidationException, InterruptedException {
    when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
    when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());


    SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
    User user = IdCardsTestUtils.createPerson("createRegister_withPreviousEntry_reemission_success");
    service.createRegister(user, RegisterAction.NOVO);

    assertNotNull(user.getCurrentSantanderEntry());
    SantanderEntryNew entry = user.getCurrentSantanderEntry();

    assertTrue(entry.wasRegisterSuccessful());
    assertEquals("entry", entry.getRequestLine());
    assertEquals("response", entry.getResponseLine());
    assertEquals(SantanderCardState.NEW, entry.getState());

    // Finalize transition to ISSUED
    when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));
    Thread.sleep(1);

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
    Thread.sleep(1);
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
    Thread.sleep(1);

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

@Test
public void createRegister_withPreviousEntry_failWithCommunication_getRegister_readyForProduction() throws SantanderValidationException, InterruptedException {
    // ##### Arrange #####
    when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
    when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse()).thenReturn(communicationErrorResponse());
    when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterReadyForProduction(),
            getRegisterReadyForProduction());

    // ##### Act #####
    User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncNew");
    SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
    service.createRegister(user, RegisterAction.NOVO); //success
    Thread.sleep(1);
    service.getOrUpdateState(user); //first card is issued

    CardPreviewBean cardPreview2 = createCardPreview();
    cardPreview2.setLine("entry2");
    when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(cardPreview2); // expired

    service.createRegister(user, RegisterAction.REMI); //new card with communication error
    service.getOrUpdateState(user); //sync card state with santander

    // ##### Assert #####
    List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
    verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
    verify(mockedService, times(3)).getRegister(any(String.class));
    assertTrue(availableActions.isEmpty());

    SantanderEntryNew newEntry = user.getCurrentSantanderEntry();
    assertNotNull(newEntry);
    assertTrue(newEntry.wasRegisterSuccessful());
    assertEquals("entry2", newEntry.getRequestLine());
    assertNotNull(newEntry.getErrorDescription());
    assertTrue(newEntry.getErrorDescription().isEmpty());
    assertEquals(SantanderCardState.NEW, newEntry.getState());
    assertNotNull(newEntry.getSantanderCardInfo());
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
public void createRegister_withPreviousEntry_failWithCommunication_getRegister_issued() throws SantanderValidationException, InterruptedException {
    // ##### Arrange #####
    when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
    when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
            .thenReturn(communicationErrorResponse());
    when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterIssued(MIFARE2));

    // ##### Act #####
    User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncIssued");
    SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
    service.createRegister(user, RegisterAction.NOVO); //success
    Thread.sleep(1);
    service.getOrUpdateState(user); //first card is issued
    service.createRegister(user, RegisterAction.REMI); //new card with communication error
    service.getOrUpdateState(user); //sync card state with santander

    // ##### Assert #####
    List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
    verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
    verify(mockedService, times(2)).getRegister(any(String.class));
    assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
    assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
    assertEquals(availableActions.size(), 2);

    SantanderEntryNew newEntry = user.getCurrentSantanderEntry();
    assertNotNull(newEntry);
    assertTrue(newEntry.wasRegisterSuccessful());
    assertEquals("entry", newEntry.getRequestLine());
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
d
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