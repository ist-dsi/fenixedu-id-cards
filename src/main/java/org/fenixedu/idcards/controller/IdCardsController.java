package org.fenixedu.idcards.controller;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import pt.ist.fenixframework.FenixFramework;

@RestController
@RequestMapping("/idcards")
public class IdCardsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdCardsController.class);

    private SantanderIdCardsService cardService;

    @Autowired
    public IdCardsController(SantanderIdCardsService cardService) {
        this.cardService = cardService;
    }

    @RequestMapping(value = "/getUserCards", method = RequestMethod.GET)
    public ResponseEntity<?> getUserCards() {
        User user = Authenticate.getUser();

        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        cardService.getOrUpdateState(user);
        return ResponseEntity.ok(cardService.getUserSantanderCards(Authenticate.getUser()));
    }

    @RequestMapping(value = "/getUserCards/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserCards(@PathVariable String username) {
        if (!isCardsAdmin(Authenticate.getUser())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = User.findByUsername(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        cardService.getOrUpdateState(user);
        return ResponseEntity.ok(cardService.getUserSantanderCards(user));
    }

    @RequestMapping(value = "/isCardsAdmin", method = RequestMethod.GET)
    public boolean isCardsAdmin() {
        return isCardsAdmin(Authenticate.getUser());
    }

    @SkipCSRF
    @RequestMapping(value = "/requestCard", method = RequestMethod.POST)
    public ResponseEntity<?> requestCard() {
        User user = Authenticate.getUser();

        try {
            SantanderEntry entry = cardService.createRegister(user);
            cardService.sendRegister(user, entry);
        } catch (SantanderValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/previewCard", method = RequestMethod.GET)
    public ResponseEntity<?> previewCard() {
        try {
            return ResponseEntity.ok(cardService.generateCardPreview(Authenticate.getUser()));
        } catch (SantanderValidationException e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(error.toString());
        }
    }

    @SkipCSRF
    @RequestMapping(value = "/deliver/{mifare}", method = RequestMethod.PUT)
    public ResponseEntity<?> deliver(@PathVariable String mifare) {
        try {
            final Long mifareNumber = Long.parseLong(mifare);
            return Bennu.getInstance().getSantanderCardInfoSet().stream()
                    .filter(card -> !Strings.isNullOrEmpty(card.getMifareNumber())).filter(card -> {
                        try {
                            final Long cardMifareNumber = Long.parseLong(card.getMifareNumber());
                            return cardMifareNumber.equals(mifareNumber);
                        } catch (NumberFormatException nfe) {
                            LOGGER.error("Error parsing >{}< for card {}", card.getMifareNumber(), card.getExternalId());
                            return false;
                        }
                    }).findAny().map(card -> {
                        FenixFramework.atomic(() -> {
                            card.getSantanderEntry().updateState(SantanderCardState.DELIVERED);
                        });
                        return ResponseEntity.ok().build();
                    }).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (NumberFormatException nfe) {
            LOGGER.error("Error parsing number", nfe);
            JsonObject error = new JsonObject();
            error.addProperty("error", nfe.getMessage());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(error.toString());
        }
    }

    private boolean isCardsAdmin(User user) {
        Group group = Group.dynamic("idCardManager");
        return group.isMember(user);
    }
}
