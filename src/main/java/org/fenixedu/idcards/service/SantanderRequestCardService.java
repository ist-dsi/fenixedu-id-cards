package org.fenixedu.idcards.service;

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
import org.fenixedu.idcards.utils.SantanderCardState;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.sibscartoes.portal.wcf.register.info.IRegisterInfoService;
import pt.sibscartoes.portal.wcf.register.info.dto.RegisterData;
import pt.sibscartoes.portal.wcf.tui.ITUIDetailService;
import pt.sibscartoes.portal.wcf.tui.dto.TUIResponseData;
import pt.sibscartoes.portal.wcf.tui.dto.TuiPhotoRegisterData;
import pt.sibscartoes.portal.wcf.tui.dto.TuiSignatureRegisterData;

public class SantanderRequestCardService {

    private static final String READY_FOR_PRODUCTION = "Preparado para Produção";
    private static final String PRODUCTION = "Em Produção";
    private static final String REMI_REQUEST = "Pedido de Reemissão";
    private static final String RENU_REQUEST = "Pedido de Renovação";
    private static final String REJECTED_REQUEST = "Emissão Rejeitada";
    private static final String ISSUED = "Expedido";
    private static final String NO_RESULT = "NoResult";

    private static Logger logger = LoggerFactory.getLogger(SantanderRequestCardService.class);

    public static List<RegisterAction> getPersonAvailableActions(Person person) {

        List<RegisterAction> actions = new LinkedList<>();
        SantanderEntryNew personEntry = person.getCurrentSantanderEntry();

        if (personEntry == null || personEntry.canRegisterNew()) {
            actions.add(RegisterAction.NOVO);
            return actions;
        }

        if (personEntry.canReemitCard()) {
            actions.add(RegisterAction.REMI);
        }

        if (personEntry.canRenovateCard()) {
            actions.add(RegisterAction.RENU);
        }

        return actions;
    }

    public static SantanderEntryNew updateState(Person person) {

        SantanderEntryNew entryNew = person.getCurrentSantanderEntry();

        if (entryNew == null) {
            return null;
        }

        SantanderCardState cardState = entryNew.getState();

        if (cardState == null) {
            entryNew.updateState(SantanderCardState.NEW);
            cardState = SantanderCardState.NEW;
        }

        switch (cardState) {
        case IGNORED:
        case ISSUED:
            return entryNew;
        case PENDING:
            return synchronizeFenixAndSantanderStates(person, entryNew);
        case NEW:
            return checkState(person, entryNew);
        default:
            logger.debug("SantanderEntryNew " + entryNew.getExternalId() + " has unknown state (" + cardState.getName() + ")");
            throw new RuntimeException();
        }
    }

    private static SantanderEntryNew checkState(Person person, SantanderEntryNew entryNew) {
        RegisterData registerData = getRegister(person);
        return checkState(entryNew, registerData);
    }

    private static SantanderEntryNew checkState(SantanderEntryNew entryNew, RegisterData registerData) {
        if (registerData == null) {
            return entryNew;
        }

        String status = registerData.getStatus().getValue();

        switch (status) {
        case REJECTED_REQUEST:
            //TODO Create new state
            return entryNew;

        case READY_FOR_PRODUCTION:
        case REMI_REQUEST:
        case RENU_REQUEST:
        case PRODUCTION:
            entryNew.updateState(SantanderCardState.NEW);
            break;

        case ISSUED:
            entryNew.updateState(SantanderCardState.ISSUED);
            entryNew.update(registerData); //TODO implement update
            break;

        case NO_RESULT:
            throw new RuntimeException(); //TODO throw decent exception

        default:
            logger.debug("Not supported status:  " + status); //When can this happen?
        }

        return entryNew;
    }

    private static SantanderEntryNew synchronizeFenixAndSantanderStates(Person person, SantanderEntryNew entryNew) {
        RegisterData registerData = getRegister(person);

        String status = registerData.getStatus().getValue();

        SantanderEntryNew previousEntry = entryNew.getPrevious();

        if (previousEntry == null) {
            if (status.equals(NO_RESULT)) {

                entryNew.updateState(SantanderCardState.IGNORED);
                return entryNew;
            } else {
                return checkState(entryNew, registerData);
            }
        }

        if (registerData.getExpiryDate() == null || registerData.getExpiryDate().getValue() == null) {
            throw new RuntimeException(); //TODO registerData is incomplete
        }

        DateTime registerDataExpiryDate =
                DateTime.parse(registerData.getExpiryDate().getValue(), DateTimeFormat.forPattern("dd-MM-yyyy"));

        if (registerDataExpiryDate.equals(entryNew.getExpiryDate())) {
            return checkState(entryNew, registerData);
        } else if (registerDataExpiryDate.equals(previousEntry.getExpiryDate())) {
            entryNew.updateState(SantanderCardState.IGNORED);
            return entryNew;
        } else {
            throw new RuntimeException(); //TODO should not be possible 
        }
    }

    private static RegisterData getRegister(Person person) {
        
        logger.debug("Entering getRegister");
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

        try {

            //TODO use getRegister only when synchronizing and card is issued
            //Otherwise use getRegisterStatus
            RegisterData statusInformation = port.getRegister(userName);

            logger.debug("Result: " + statusInformation.getStatus().getValue() + " - "
                    + statusInformation.getStatusDescription().getValue());

            return statusInformation;

        } catch (Throwable t) {
            logger.debug("failed trying to communicate with santander");
            t.printStackTrace();
            return null;
        }
    }

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
            logger.debug("saveRegister result: %s" + tuiResponse.getTuiResponseLine().getValue());
        } catch (Throwable t) {
            entry.saveWithError("Erro ao comunicar com o Santander", SantanderCardState.PENDING);
            logger.debug("Error connecting with santander");
            t.printStackTrace();
            return;
        }

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

        try {
            TuiPhotoRegisterData photo = new TuiPhotoRegisterData();

            SantanderPhotoEntry photoEntry;
            photoEntry = SantanderPhotoEntry.getOrCreatePhotoEntryForPerson(person);
            byte[] photo_contents = photoEntry.getPhotoAsByteArray();

            photo.setFileContents(new JAXBElement<>(FILE_CONTENTS, byte[].class, photo_contents));
            photo.setSize(new JAXBElement<>(FILE_SIZE, String.class, Integer.toString(photo_contents.length)));
            photo.setExtension(new JAXBElement<>(FILE_EXTENSION, String.class, EXTENSION));
            photo.setFileName(new JAXBElement<>(FILE_NAME, String.class, "foto")); //TODO

            return photo;
        } catch (Throwable t) {
            throw new SantanderCardMissingDataException("Missing photo");
        }

    }

    @Atomic(mode = TxMode.WRITE)
    private static SantanderEntryNew createOrResetEntry(Person person, String request) {
        SantanderEntryNew entry = person.getCurrentSantanderEntry();

        if (entry == null) {
            return new SantanderEntryNew(person, request);
        }

        SantanderCardState cardState = entry.getState();

        switch (cardState) {
        case IGNORED:
            entry.reset(person, request);
            return entry;
        case ISSUED:
            return new SantanderEntryNew(person, request);
        default:
            throw new RuntimeException(); //TODO throw decent exception 
        }
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
            entry.saveWithError(errorDescription, SantanderCardState.IGNORED);
        }
    }
}
