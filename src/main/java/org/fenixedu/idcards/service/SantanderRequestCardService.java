package org.fenixedu.idcards.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.idcards.IdCardsConfiguration;
import org.fenixedu.idcards.domain.RegisterAction;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderPhotoEntry;
import org.fenixedu.idcards.utils.Action;
import org.fenixedu.idcards.utils.SantanderCardState;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import pt.ist.fenixframework.Atomic;
import pt.sibscartoes.portal.wcf.register.info.IRegisterInfoService;
import pt.sibscartoes.portal.wcf.register.info.dto.RegisterData;
import pt.sibscartoes.portal.wcf.tui.ITUIDetailService;
import pt.sibscartoes.portal.wcf.tui.dto.TUIResponseData;
import pt.sibscartoes.portal.wcf.tui.dto.TuiPhotoRegisterData;
import pt.sibscartoes.portal.wcf.tui.dto.TuiSignatureRegisterData;

public class SantanderRequestCardService {

    private static String NO_RESULT = "NoResult";

    private static Logger logger = LoggerFactory.getLogger(SantanderRequestCardService.class);

    public static List<RegisterAction> getPersonAvailableActions(Person person) {

        List<RegisterAction> actions = new LinkedList<>();
        SantanderEntryNew personEntry = person.getCurrentSantanderEntry();

        /* If card is in canceled state
         * Or the person has no successful request
         * => Can only use ACTION_NEW
         */
        if (personEntry == null || personEntry.getState().canRegisterNew()) {
            actions.add(RegisterAction.NOVO);
            return actions;
        }

        SantanderCardState cardState = personEntry.getState();

        /**
         * If current state is pending
         * we need to know if it was saved
         * on sibs side and update accordingly
         */
        if (cardState == SantanderCardState.PENDING) {
            RegisterData registerData = getRegister(person);
            String status = registerData.getStatus().getValue();

            // It wasn't saved on sibs side
            if (NO_RESULT.equals(status)) {
                actions.add(RegisterAction.NOVO);
                return actions;
            }

            // TODO: set new state accordingly, and save the data
        }

        /* If the card is issued
         * => Can use ACTION_ATUA and ACTION_CANC
         * => ACTION_RENU can be used if there are 60 days or less until the expiration date
         * => ACTION_REMI can be used otherwise
         */
        if (cardState == SantanderCardState.ISSUED) {
            actions.add(RegisterAction.REMI);

            if (personEntry.isExpiring()) {
                actions.add(RegisterAction.RENU);
            }

            actions.add(RegisterAction.ATUA);
            actions.add(RegisterAction.CANC);
        }

        // Entry is in successful state and we need to update it's status

        RegisterData registerData = getRegister(person);
        String status = registerData.getStatus().getValue();

        // updateEntry

        /* If card is in production state
         * => Can only use ACTION_CANC
         */
        if (cardState == SantanderCardState.PRODUCTION) {
            actions.add(RegisterAction.CANC);
            return actions;
        }

        /* If the card is in any of this states
         * => Can only use ACTION_ATUA and ACTION_CANC
         */
        if (specialCases(cardState)) {
            actions.add(RegisterAction.ATUA);
            actions.add(RegisterAction.CANC);
            return actions;
        }

        return actions;
    }

    private static boolean specialCases(SantanderCardState cardState) {

        // TODO: Add correct verifications
        /*if (cardState == SantanderCardState. || status.equals(READY_FOR_PRODUCTION) || status.equals(REMI_REQUEST)
                || status.equals(RENU_REQUEST) || status.equals(REJECTED_REQUEST)) {
            return true;
        }*/

        return false;
    }

    public static RegisterData getRegister(Person person) {
        List<String> result = new ArrayList<>();

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        factory.setServiceClass(IRegisterInfoService.class);
        factory.setAddress("https://portal.sibscartoes.pt/tstwcfv2/services/RegisterInfoService.svc");
        factory.setBindingId("http://schemas.xmlsoap.org/wsdl/soap12/");
        factory.getFeatures().add(new WSAddressingFeature());

        //Add loggers to request
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());

        IRegisterInfoService port = (IRegisterInfoService) factory.create();

        /*define WSDL policy*/
        Client client = ClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) client.getConduit();

        //Add username and password properties
        http.getAuthorization().setUserName(IdCardsConfiguration.getConfiguration().sibsWebServiceUsername());
        http.getAuthorization().setPassword(IdCardsConfiguration.getConfiguration().sibsWebServicePassword());

        final String userName = person.getUsername();

        RegisterData statusInformation = port.getRegister(userName);

        result.add(statusInformation.getStatus().getValue());
        result.add(statusInformation.getStatusDate().getValue());
        result.add(statusInformation.getStatusDescription().getValue());

        logger.debug("Result: " + result.get(1) + " : " + result.get(0) + " - " + result.get(2));

        return statusInformation;
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public static void createRegister(String tuiEntry, Person person) throws SantanderCardMissingDataException {

        if (tuiEntry == null) {
            logger.debug("Null tuiEntry for user " + person.getUsername());
            return;
        }

        logger.debug("Entry: " + tuiEntry);
        logger.debug("Entry size: " + tuiEntry.length());
        
        TuiPhotoRegisterData photo = getOrCreateSantanderPhoto(person);
        TuiSignatureRegisterData signature = new TuiSignatureRegisterData();

        /*
         * If there was an error on the previous entry update it
         * Else create a new entry
         */
        SantanderEntryNew entry = createOrResetEntry(person, tuiEntry);

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        factory.setServiceClass(ITUIDetailService.class);
        factory.setAddress("https://portal.sibscartoes.pt/tstwcfv2/services/TUIDetailService.svc");
        factory.setBindingId("http://schemas.xmlsoap.org/wsdl/soap12/");
        factory.getFeatures().add(new WSAddressingFeature());

        //Add loggers to request
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());

        ITUIDetailService port = (ITUIDetailService) factory.create();

        /*define WSDL policy*/
        Client client = ClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) client.getConduit();
        //Add username and password properties
        http.getAuthorization().setUserName(IdCardsConfiguration.getConfiguration().sibsWebServiceUsername());
        http.getAuthorization().setPassword(IdCardsConfiguration.getConfiguration().sibsWebServicePassword());

        TUIResponseData tuiResponse;

        try {
            tuiResponse = port.saveRegister(tuiEntry, photo, signature);
        } catch (Throwable t) {
            entry.saveWithError("Erro ao comunicar com o Santander", SantanderCardState.RESPONSE_ERROR);
            return;
        }

        // Update entry with the response
        saveResponse(entry, tuiResponse);
    }

    private static TuiPhotoRegisterData getOrCreateSantanderPhoto(Person person) throws SantanderCardMissingDataException {
        final QName FILE_NAME =
                new QName("http://schemas.datacontract.org/2004/07/SibsCards.Wcf.Services.DataContracts", "FileName");
        final QName FILE_EXTENSION =
                new QName("http://schemas.datacontract.org/2004/07/SibsCards.Wcf.Services.DataContracts", "Extension");
        final QName FILE_CONTENTS =
                new QName("http://schemas.datacontract.org/2004/07/SibsCards.Wcf.Services.DataContracts", "FileContents");
        final QName FILE_SIZE = new QName("http://schemas.datacontract.org/2004/07/SibsCards.Wcf.Services.DataContracts", "Size");

        final String EXTENSION = ".jpeg";

        TuiPhotoRegisterData photo = new TuiPhotoRegisterData();

        SantanderPhotoEntry photoEntry;

        try {
            photoEntry = SantanderPhotoEntry.getOrCreatePhotoEntryForPerson(person);
        } catch (Throwable t) {
            throw new SantanderCardMissingDataException("Missing photo");
        }

        byte[] photo_contents = photoEntry.getPhotoAsByteArray();

        photo.setFileContents(new JAXBElement<>(FILE_CONTENTS, byte[].class, photo_contents));
        photo.setSize(new JAXBElement<>(FILE_SIZE, String.class, Integer.toString(photo_contents.length)));
        photo.setExtension(new JAXBElement<>(FILE_EXTENSION, String.class, EXTENSION));
        photo.setFileName(new JAXBElement<>(FILE_NAME, String.class, "foto")); //TODO

        return photo;
    }

    private static SantanderEntryNew createOrResetEntry(Person person, String request) {
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntryNew(person, request);
        }

        SantanderCardState cardState = entry.getState();

        // In error state, overwrite entry
        if (cardState == SantanderCardState.RESPONSE_ERROR || cardState == SantanderCardState.REJECTED) {
            entry.reset(request);
        }

        return entry;
    }

    private static void saveResponse(SantanderEntryNew entry, TUIResponseData response) {
        String status = response.getStatus() == null || response.getStatus().getValue() == null ? "" : response
                .getStatus().getValue().trim();
        String errorDescription = response.getStatusDescription() == null
                || response.getStatusDescription().getValue() == null ? "" : response.getStatusDescription().getValue()
                .trim();
        String responseLine = response.getTuiResponseLine() == null
                || response.getTuiResponseLine().getValue() == null ? "" : response.getTuiResponseLine().getValue().trim();

        boolean registerSuccessful = !status.isEmpty() && !status.toLowerCase().equals("error");

        if (registerSuccessful) {
            entry.saveSuccessful(responseLine);
        } else {
            entry.saveWithError(errorDescription, SantanderCardState.REJECTED);
        }
    }
}
