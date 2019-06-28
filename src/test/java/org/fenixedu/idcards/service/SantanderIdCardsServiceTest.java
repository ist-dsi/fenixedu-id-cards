package org.fenixedu.idcards.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.Collections;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.IdCardsTestUtils;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.fenixedu.santandersdk.service.SantanderSdkService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.io.BaseEncoding;

@RunWith(FenixFrameworkRunner.class)
public class SantanderIdCardsServiceTest {

    @Mock
    private SantanderSdkService mockedService;

    @Mock
    private IUserInfoService userInfoService;

    private static final String IDENTIFICATION_NUMBER = "ist112345";

    private static final String CARD_NAME = "JOEL";

    private static final DateTime EXPIRY_DATE_EXPIRED = DateTime.now().minusYears(10);

    private static final DateTime EXPIRY_DATE_NOT_EXPIRED = DateTime.now();

    private static final DateTime EXPEDITION_DATE1 = DateTime.now().minusYears(3);

    private static final DateTime EXPEDITION_DATE2 = DateTime.now();

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
        when(userInfoService.getUserRoles(any(User.class))).thenReturn(Collections.singletonList("STUDENT"));
        when(userInfoService.getCampus(any(User.class))).thenReturn("Alameda");
    }

    private CardPreviewBean createCardPreview() {
        return createCardPreview(REQUEST_LINE1, EXPIRY_DATE_NOT_EXPIRED);
    }

    private CardPreviewBean createCardPreview(String requestLine, DateTime expiryDate) {
        CardPreviewBean cardPreview = new CardPreviewBean();
        cardPreview.setCardName(CARD_NAME);
        cardPreview.setExpiryDate(expiryDate);
        cardPreview.setIdentificationNumber(IDENTIFICATION_NUMBER);
        cardPreview.setRequestLine(requestLine);
        cardPreview.setPhoto(PHOTO_DECODED);
        cardPreview.setRole("STUDENT");

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
        getRegisterResponse.setExpeditionDate(DateTime.now());
        return getRegisterResponse;
    }

    private GetRegisterResponse getRegisterIssued(String mifare, DateTime expeditionDate) {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.ISSUED);
        getRegisterResponse.setMifare(mifare);
        getRegisterResponse.setExpeditionDate(expeditionDate);
        return getRegisterResponse;
    }
    
    private GetRegisterResponse getRegisterInProduction() {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.PRODUCTION);
        return getRegisterResponse;
    }

    private GetRegisterResponse getRegisterNoResult() {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(GetRegisterStatus.NO_RESULT);
        return getRegisterResponse;
    }

    private GetRegisterResponse getRegister(GetRegisterStatus status) {
        GetRegisterResponse getRegisterResponse = new GetRegisterResponse();
        getRegisterResponse.setStatus(status);
        return getRegisterResponse;
    }

    //TODO Tests
    //Make tests where the createRegister fails and then try different possibilities with getRegister and getAvailableActions
    //Make test where createRegister receives wrong action

    @Test
    public void createRegister_noCards_canRegister_NEW() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_noCards_canRegister_NEW");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
    }

    @Test
    public void createRegister_previousCard_canRegister_REMI() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_previousCard_canRegister_REMI");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(2, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));
        assertEquals(RegisterAction.RENU, availableActions.get(1));

        service.sendRegister(user, service.createRegister(user, RegisterAction.REMI));
    }

    @Test
    public void createRegister_previousCard_canRegister_RENU_60Days() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview(REQUEST_LINE1, DateTime.now().plusDays(60)));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_previousCard_canRegister_RENU_60Days");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(2, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));
        assertEquals(RegisterAction.RENU, availableActions.get(1));

        service.sendRegister(user, service.createRegister(user, RegisterAction.RENU));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_previousCard_cantRegister_RENU_61Days() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview(REQUEST_LINE1, DateTime.now().plusDays(61)));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_previousCard_cantRegister_RENU_61Days");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(1, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));

        service.sendRegister(user, service.createRegister(user, RegisterAction.RENU));
    }

    @Test
    public void createRegister_previousCard_canRegister_RENU_59Days() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview(REQUEST_LINE1, DateTime.now().plusDays(59)));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_previousCard_canRegister_RENU_59Days");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(2, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));
        assertEquals(RegisterAction.RENU, availableActions.get(1));

        service.sendRegister(user, service.createRegister(user, RegisterAction.RENU));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_hasntExpired_RENU() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview(REQUEST_LINE1, DateTime.now().plusDays(61)));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_withWrongAction_hasntExpired_RENU");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(1, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));

        service.sendRegister(user, service.createRegister(user, RegisterAction.RENU));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_hasPreviousCard_andIgnored_NEW() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse()).thenReturn(errorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_withWrongAction_hasPreviousCard_andIgnored_NEW");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(2, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));
        assertEquals(RegisterAction.RENU, availableActions.get(1));

        service.sendRegister(user, service.createRegister(user, RegisterAction.REMI));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertEquals(SantanderCardState.IGNORED, entry.getState());

        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_noCard_RENU() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterNoResult());

        User user = IdCardsTestUtils.createPerson("createRegister_WrongAction_noCard_RENU");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.RENU));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_notIssued_NOVO() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterNoResult());

        User user = IdCardsTestUtils.createPerson("createRegister_WrongAction_notIssued_NOVO");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(0, availableActions.size());

        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_issued_NOVO() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_WrongAction_issued_NOVO");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        assertEquals(2, availableActions.size());
        assertEquals(RegisterAction.REMI, availableActions.get(0));
        assertEquals(RegisterAction.RENU, availableActions.get(1));

        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
    }

    @Test(expected = SantanderValidationException.class)
    public void createRegister_withWrongAction_noCard_REMI() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        User user = IdCardsTestUtils.createPerson("createRegister_WrongAction_issued_REMI");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.REMI));
    }

    @Test
    public void createRegister_fails_getRegister_noResult() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterNoResult());

        User user = IdCardsTestUtils.createPerson("createRegisterRejected");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }

        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertFalse(entry.wasRegisterSuccessful());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        verify(mockedService, times(1)).getRegister(any(String.class));

        assertEquals(1, availableActions.size());
        assertEquals(RegisterAction.NOVO, availableActions.get(0));
        assertEquals(SantanderCardState.IGNORED, user.getCurrentSantanderEntry().getState());
        assertFalse(entry.wasRegisterSuccessful());
    }

    @Test
    public void createRegister_fails_getRegister_unknown() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegister(GetRegisterStatus.UNKNOWN));

        User user = IdCardsTestUtils.createPerson("createRegisterUnknown");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }

        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertFalse(entry.wasRegisterSuccessful());
        assertEquals(SantanderCardState.PENDING, entry.getState());

        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);

        verify(mockedService, times(1)).getRegister(any(String.class));

        // TODO: should this be the correct behaviour when state is unknown?
        assertEquals(0, availableActions.size());
        assertEquals(SantanderCardState.PENDING, user.getCurrentSantanderEntry().getState());
        assertFalse(entry.wasRegisterSuccessful());
    }


    // TODO: check transition to PRODUCTION... we are always transitioning to NEW
    /*@Test
    public void createRegister_fails_getRegister_production() throws SantanderValidationException {
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegister(GetRegisterStatus.PRODUCTION));
    
        User user = IdCardsTestUtils.createPerson("createRegisterProduction");
        SantanderRequestCardService service = new SantanderRequestCardService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user,  RegisterAction.NOVO));
    
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
    
        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertFalse(entry.wasRegisterSuccessful());
        assertEquals(SantanderCardState.PENDING, entry.getState());
    
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user);
    
        verify(mockedService, times(1)).getRegister(any(String.class));
    
        assertEquals(0, availableActions.size());
        assertEquals(SantanderCardState., user.getCurrentSantanderEntry().getState());
        assertFalse(entry.wasRegisterSuccessful());
    }
    */

    @Test
    public void createRegister_noPreviousEntry_success() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterSuccess");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_success_getRegisterReadyForProduction() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterInProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterSuccessgetRegisterReadyForProduction");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should be called

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);        
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.PRODUCTION, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PRODUCTION, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(3, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
        assertEquals(SantanderCardState.PRODUCTION, transitions.get(2).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_success_getRegisterIssued() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterSuccessgetRegisterIssued");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(2, availableActions.size());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertEquals(MIFARE1, cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(3, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, transitions.get(2).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_success_normalWorkflow() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterInProduction())
                .thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterSuccessgetNormalWorkflow");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
        service.getOrUpdateState(user);
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(2, availableActions.size());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertEquals(MIFARE1, cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(4, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, transitions.get(1).getState());
        assertEquals(SantanderCardState.PRODUCTION, transitions.get(2).getState());
        assertEquals(SantanderCardState.ISSUED, transitions.get(3).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailWithError");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_getRegisterNoResult() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterNoResult());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailWithErrorGetRegisterNoResult");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(0)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(1, availableActions.size());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failWithError_and_retry_success() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(errorResponse()).thenReturn(successResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailErrorAndRetrySuccess");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //fail response
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //success response

        // ##### Assert #####
        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

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
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //Fail response
            fail();
        } catch (SantanderValidationException sve) {

        }

        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //Fail response
            fail();
        } catch (SantanderValidationException sve) {

        }

        // ##### Assert #####
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, entry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

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
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(1, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_noResult() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterNoResult());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndGetRegisterNoResult");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####        
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.NOVO)));
        assertEquals(1, availableActions.size());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_readyForProduction() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview());
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterInProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncNew");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.getOrUpdateState(user);
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should be called

        // ##### Assert #####        
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.PRODUCTION, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PRODUCTION, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.PRODUCTION, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_getRegister_issued() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterFailCommunicationAndSyncIssued");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //createRegister fails with communication error
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.getOrUpdateState(user); //getRegister is called and state is synched with santander
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####        
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertTrue(entry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertEquals(MIFARE1, cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(cardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(2, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
        assertEquals(SantanderCardState.ISSUED, transitions.get(1).getState());
    }

    @Test
    public void createRegister_noPreviousEntry_failCommunication_and_retryWithoutSynchronize() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class))).thenReturn(createCardPreview()); // expired
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(communicationErrorResponse());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("failCommunication_and_retryWithoutSynchronize");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));  //Receives fail communication error
            fail();
        } catch (SantanderValidationException sve) {
            // TODO: handle exception
        }
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO));  //Exception must be thrown because action is not valid
            fail("Action is not valid");
        } catch (SantanderValidationException sve) {
        }

        // ##### Assert #####
        verify(mockedService, times(1)).createRegister(any(CardPreviewBean.class));

        SantanderEntry entry = user.getCurrentSantanderEntry();
        assertNotNull(entry);
        assertTrue(!entry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, entry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, entry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, entry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, entry.getState());
        assertNull(entry.getNext());
        assertNull(entry.getPrevious());
        assertEquals(1, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(entry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        SantanderCardInfo cardInfo = entry.getSantanderCardInfo();
        assertNotNull(cardInfo);
        assertEquals(IDENTIFICATION_NUMBER, cardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, cardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, cardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, cardInfo.getPhoto());
        assertNull(cardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, cardInfo.getCurrentState());
        assertEquals(entry, cardInfo.getSantanderEntry());
        assertTrue(SantanderEntry.getSantanderCardHistory(user).isEmpty());

        List<SantanderCardStateTransition> transitions = cardInfo.getOrderedTransitions();
        assertEquals(1, cardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, transitions.get(0).getState());
    }

    @Test
    public void createRegister_withPreviousEntry_reemission_success() throws SantanderValidationException {
        // ##### Arrange #####        
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        User user = IdCardsTestUtils.createPerson("createRegister_withPreviousEntry_reemission_success");
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //create first card
        service.getOrUpdateState(user); //get issued response
        service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //create a re emission

        // ##### Assert #####
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, newEntry.getResponseLine());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.NEW, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.NEW, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(2, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, newTransitions.get(1).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(2, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(newCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(1));
    }

    @Test
    public void createRegister_remi_withPreviousEntry_failWithError() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse()).thenReturn(errorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterRemiWithPreviousFailError");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //create a new card
        service.getOrUpdateState(user); //get issued status
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //re emission is refused by santander
            fail("SantanderValidationException must be thrown!");
        } catch (SantanderValidationException sce) {

        }

        // ##### Assert #####
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(REFUSED_REQUEST_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertEquals(REFUSED_REQUEST_ERROR_DESCRIPTION, newEntry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(2, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, newTransitions.get(1).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));

    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication() throws SantanderValidationException {
        // ##### Arrange #####        
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));

        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
                .thenReturn(communicationErrorResponse());

        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunication");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //create a new card
        service.getOrUpdateState(user); //get issued status
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //re emission fails because of problems with the communication
        } catch (SantanderValidationException sve) {
        }

        // ##### Assert #####        
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(1)).getRegister(any(String.class));

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, newEntry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(1, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_readyForProduction()
            throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));

        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
                .thenReturn(communicationErrorResponse());

        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1), getRegisterInProduction(),
                getRegisterInProduction());

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncNew");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //success
        service.getOrUpdateState(user); //first card is issued
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //new card with communication error
            fail("SantanderValidationException must be thrown!");
        } catch (SantanderValidationException sve) {

        }
        service.getOrUpdateState(user); //sync card state with santander
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should be called

        // ##### Assert #####
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(3)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.PRODUCTION, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PRODUCTION, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(2, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());
        assertEquals(SantanderCardState.PRODUCTION, newTransitions.get(1).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(2, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(newCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(1));
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_issued() throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
                .thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1, EXPEDITION_DATE1),
                getRegisterIssued(MIFARE2, EXPEDITION_DATE2));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncIssued");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //success
        service.getOrUpdateState(user); //first card is issued
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //new card with communication error
            fail();
        } catch (SantanderValidationException sve) {
        }
        service.getOrUpdateState(user); //sync card state with santander
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister should not be called

        // ##### Assert #####        
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertTrue(newEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertEquals(MIFARE2, newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        //TODO should NEW state be skiped?
        assertEquals(2, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());
        assertEquals(SantanderCardState.ISSUED, newTransitions.get(1).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(2, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(newCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(1));
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_oldCard_after_1_day()
            throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
                .thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncOldCardAfter1Day");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //success
        service.getOrUpdateState(user); //first card is issued
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //new card with communication error
            fail();
        } catch (SantanderValidationException sve) {

        }
        user.getCurrentSantanderEntry().setLastUpdate(DateTime.now().minusDays(1).minusMinutes(1));
        service.getOrUpdateState(user); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(2)).getRegister(any(String.class));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.REMI)));
        assertTrue(availableActions.stream().anyMatch(a -> a.equals(RegisterAction.RENU)));
        assertEquals(availableActions.size(), 2);

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, newEntry.getErrorDescription());
        assertEquals(SantanderCardState.IGNORED, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.IGNORED, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(2, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());
        assertEquals(SantanderCardState.IGNORED, newTransitions.get(1).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
    }

    @Test
    public void createRegister_withPreviousEntry_failWithCommunication_getRegister_oldCard_same_day()
            throws SantanderValidationException {
        // ##### Arrange #####
        when(mockedService.generateCardRequest(any(CreateRegisterRequest.class)))
                .thenReturn(createCardPreview(REQUEST_LINE1, EXPIRY_DATE_EXPIRED))
                .thenReturn(createCardPreview(REQUEST_LINE2, EXPIRY_DATE_NOT_EXPIRED));
        when(mockedService.createRegister(any(CardPreviewBean.class))).thenReturn(successResponse())
                .thenReturn(communicationErrorResponse());
        when(mockedService.getRegister(any(String.class))).thenReturn(getRegisterIssued(MIFARE1));

        // ##### Act #####
        User user = IdCardsTestUtils.createPerson("createRegisterWithPreviousFailCommunicationAndSyncOldCardSameDay");
        SantanderIdCardsService service = new SantanderIdCardsService(mockedService, userInfoService);
        service.sendRegister(user, service.createRegister(user, RegisterAction.NOVO)); //success
        service.getOrUpdateState(user); //first card is issued
        try {
            service.sendRegister(user, service.createRegister(user, RegisterAction.REMI)); //new card with communication error
            fail();
        } catch (SantanderValidationException sve) {

        }
        service.getOrUpdateState(user); //sync card state with santander

        // ##### Assert #####
        List<RegisterAction> availableActions = service.getPersonAvailableActions(user); //getRegister is called
        verify(mockedService, times(2)).createRegister(any(CardPreviewBean.class));
        verify(mockedService, times(3)).getRegister(any(String.class));
        assertTrue(availableActions.isEmpty());

        SantanderEntry newEntry = user.getCurrentSantanderEntry();
        assertNotNull(newEntry);
        assertTrue(!newEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE2, newEntry.getRequestLine());
        assertEquals(COMMUNICATION_ERROR_RESPONSE_LINE, newEntry.getResponseLine());
        assertEquals(COMMUNICATION_ERROR_DESCRIPTION, newEntry.getErrorDescription());
        assertEquals(SantanderCardState.PENDING, newEntry.getState());
        assertNull(newEntry.getNext());

        SantanderCardInfo newCardInfo = newEntry.getSantanderCardInfo();
        assertNotNull(newCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, newCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, newCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_NOT_EXPIRED, newCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, newCardInfo.getPhoto());
        assertNull(newCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.PENDING, newCardInfo.getCurrentState());
        assertEquals(newEntry, newCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> newTransitions = newCardInfo.getOrderedTransitions();
        assertEquals(1, newCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, newTransitions.get(0).getState());

        SantanderEntry oldEntry = newEntry.getPrevious();
        assertNotNull(oldEntry);
        assertTrue(oldEntry.wasRegisterSuccessful());
        assertEquals(REQUEST_LINE1, oldEntry.getRequestLine());
        assertEquals(SUCCESS_RESPONSE, oldEntry.getResponseLine());
        assertTrue(oldEntry.getErrorDescription().isEmpty());
        assertEquals(SantanderCardState.ISSUED, oldEntry.getState());
        assertEquals(newEntry, oldEntry.getNext());
        assertNull(oldEntry.getPrevious());

        SantanderCardInfo oldCardInfo = oldEntry.getSantanderCardInfo();
        assertNotNull(oldCardInfo);
        assertEquals(IDENTIFICATION_NUMBER, oldCardInfo.getIdentificationNumber());
        assertEquals(CARD_NAME, oldCardInfo.getCardName());
        assertEquals(EXPIRY_DATE_EXPIRED, oldCardInfo.getExpiryDate());
        assertEquals(PHOTO_DECODED, oldCardInfo.getPhoto());
        assertEquals(MIFARE1, oldCardInfo.getMifareNumber());
        assertEquals(SantanderCardState.ISSUED, oldCardInfo.getCurrentState());
        assertEquals(oldEntry, oldCardInfo.getSantanderEntry());

        List<SantanderCardStateTransition> oldTransitions = oldCardInfo.getOrderedTransitions();
        assertEquals(3, oldCardInfo.getSantanderCardStateTransitionsSet().size());
        assertEquals(SantanderCardState.PENDING, oldTransitions.get(0).getState());
        assertEquals(SantanderCardState.NEW, oldTransitions.get(1).getState());
        assertEquals(SantanderCardState.ISSUED, oldTransitions.get(2).getState());

        assertEquals(2, SantanderEntry.getSantanderEntryHistory(user).size());
        assertEquals(oldEntry, SantanderEntry.getSantanderEntryHistory(user).get(1));
        assertEquals(newEntry, SantanderEntry.getSantanderEntryHistory(user).get(0));

        assertEquals(1, SantanderEntry.getSantanderCardHistory(user).size());
        assertEquals(oldCardInfo, SantanderEntry.getSantanderCardHistory(user).get(0));
    }
}