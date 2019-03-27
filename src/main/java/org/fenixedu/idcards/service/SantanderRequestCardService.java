package org.fenixedu.idcards.service;

import java.util.LinkedList;
import java.util.List;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.idcards.domain.RegisterAction;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.domain.SantanderPhotoEntry;
import org.fenixedu.idcards.utils.SantanderCardState;
import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterResponse;
import org.fenixedu.santandersdk.dto.GetRegisterStatus;
import org.fenixedu.santandersdk.service.SantanderCardService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class SantanderRequestCardService {

    private static Logger logger = LoggerFactory.getLogger(SantanderRequestCardService.class);

    public static List<RegisterAction> getPersonAvailableActions(Person person) {

        List<RegisterAction> actions = new LinkedList<>();
        SantanderEntryNew personEntry = getOrUpdateState(person);

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

    public static SantanderEntryNew getOrUpdateState(Person person) {
        SantanderEntryNew entryNew = person.getCurrentSantanderEntry();

        if (entryNew == null) {
            return null;
        }

        SantanderCardState cardState = entryNew.getState();

        switch (cardState) {
            case IGNORED:
            case ISSUED:
                return entryNew;
            case PENDING:
                return synchronizeFenixAndSantanderStates(person, entryNew);
            case NEW:
                return checkAndUpdateState(entryNew);
            default:
                logger.debug("SantanderEntryNew " + entryNew.getExternalId() + " has unknown state (" + cardState.getName() + ")");
                throw new RuntimeException();
        }
    }

    private static SantanderEntryNew checkAndUpdateState(SantanderEntryNew entryNew) {
        GetRegisterResponse registerData = getRegister(entryNew.getPerson());
        return checkAndUpdateState(entryNew, registerData);
    }

    private static SantanderEntryNew checkAndUpdateState(SantanderEntryNew entryNew, GetRegisterResponse registerData) {
        if (registerData == null) {
            return entryNew;
        }

        GetRegisterStatus status = registerData.getStatus();

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
                // May not be processed yet, do nothing
                // throw new RuntimeException(); //TODO throw decent exception
                break;

            default:
                logger.debug("Not supported status:  " + status); //When can this happen?
        }

        return entryNew;
    }

    private static SantanderEntryNew synchronizeFenixAndSantanderStates(Person person, SantanderEntryNew entryNew) {
        GetRegisterResponse registerData = getRegister(person);
        GetRegisterStatus status = registerData.getStatus();

        SantanderEntryNew previousEntry = entryNew.getPrevious();

        if (previousEntry == null) {
            if (status.equals(GetRegisterStatus.NO_RESULT)) {
                entryNew.updateState(SantanderCardState.IGNORED);
                return entryNew;
            } else {
                return checkAndUpdateState(entryNew, registerData);
            }
        }

        DateTime expiryDate = registerData.getExpiryDate();

        if (expiryDate == null) {
            throw new RuntimeException(); //TODO registerData is incomplete
        }

        if (expiryDate.equals(entryNew.getExpiryDate())) {
            return checkAndUpdateState(entryNew, registerData);
        } else if (expiryDate.equals(previousEntry.getExpiryDate())) {
            entryNew.updateState(SantanderCardState.IGNORED);
            return entryNew;
        } else {
            throw new RuntimeException(); //TODO should not be possible 
        }
    }

    private static GetRegisterResponse getRegister(Person person) {
        
        logger.debug("Entering getRegister");

        final String userName = person.getUsername();

        // Change to autowired
        SantanderCardService santanderCardService = new SantanderCardService();

        try {

            //TODO use getRegister only when synchronizing and card is issued
            //Otherwise use getRegisterStatus
            GetRegisterResponse statusInformation = santanderCardService.getRegister(userName);

            logger.debug("Result: " + statusInformation.getStatus());

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

        /*
         * If there was an error on the previous entry update it
         * Else create a new entry
         */
        SantanderEntryNew entry = createOrResetEntry(person, tuiEntry);

        CreateRegisterResponse response;

        SantanderCardService santanderCardService = new SantanderCardService();

        try {
            response = santanderCardService.createRegister(tuiEntry, getOrCreateSantanderPhoto(person));
            logger.debug("saveRegister result: %s" + response.getResponseLine());
        } catch (Throwable t) {
            entry.saveWithError("Erro ao comunicar com o Santander", SantanderCardState.PENDING);
            logger.debug("Error connecting with santander");
            t.printStackTrace();
            return;
        }

        saveResponse(entry, response);
    }

    private static byte[] getOrCreateSantanderPhoto(Person person) throws SantanderCardMissingDataException {
        try {
            SantanderPhotoEntry photoEntry = SantanderPhotoEntry.getOrCreatePhotoEntryForPerson(person);
            byte[] photo_contents = photoEntry.getPhotoAsByteArray();

            return photo_contents;
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


    private static void saveResponse(SantanderEntryNew entry, CreateRegisterResponse response) {
        if (response.wasRegisterSuccessful()) {
            entry.saveSuccessful(response.getResponseLine());
        } else {
            entry.saveWithError(response.getErrorDescription(), SantanderCardState.IGNORED);
        }
    }
}
