package org.fenixedu.idcards.controller;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.spring.BaseController;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.idcards.domain.RaspberryPiSession;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.domain.SantanderUserInfo;
import org.fenixedu.idcards.dto.DeliverSessionMifareRequest;
import org.fenixedu.idcards.dto.RaspberryPiSessionDto;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.fenixedu.santandersdk.exception.SantanderValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/idcards")
public class IdCardsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdCardsController.class);

    private final SantanderIdCardsService cardService;

    @Autowired
    public IdCardsController(final SantanderIdCardsService cardService) {
        this.cardService = cardService;
    }

    @RequestMapping(value = "{username}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserCards(final @PathVariable String username, final User user) {
        final User usernameUser = User.findByUsername(username);
        if (usernameUser == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isIdCardManager(user) && user != usernameUser) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return getUserCardsResponse(usernameUser);
    }

    private ResponseEntity<?> getUserCardsResponse(final User user) {
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        cardService.getOrUpdateState(user);
        return ResponseEntity.ok(cardService.getUserSantanderCards(user));
    }

    @RequestMapping(value = "user-info", method = RequestMethod.GET)
    public ResponseEntity<?> userInfo(final User user) {
        final JsonObject response = new JsonObject();
        response.addProperty("admin", isIdCardManager(user));
        response.addProperty("canRequestCard", cardService.canRequestCard(user));
        response.addProperty("language", I18N.getLocale().getLanguage());
        return ResponseEntity.ok(response.toString());
    }

    @RequestMapping(value = "user-names", method = RequestMethod.GET)
    public ResponseEntity<?> userNames(final User user) {
        final JsonObject response = cardService.getUserNames(user);
        return ResponseEntity.ok(response.toString());
    }

    @SkipCSRF
    @RequestMapping(value = "change-card-name", method = RequestMethod.POST)
    public ResponseEntity<?> changeCardName(final User user, final @RequestBody String cardName) {
        if (updateCardName(user, cardName)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @SkipCSRF
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> requestCard(final @RequestHeader("X-Requested-With") String requestedWith,
                                         final User user, final @RequestBody(required = false) String requestReason) {

        if (!cardService.canRequestCard(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            final SantanderEntry entry = cardService.createRegister(user, requestReason);
            cardService.sendRegister(user, entry);
        } catch (final SantanderValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(cardService.getErrorMessage(user.getProfile().getPreferredLocale(), e.getMessage()));
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "preview", method = RequestMethod.GET)
    public ResponseEntity<?> previewCard(final User user) {
        try {
            return ResponseEntity.ok(cardService.generateCardPreview(user));
        } catch (final SantanderValidationException e) {
            final JsonObject error = new JsonObject();
            error.addProperty("error", cardService.getErrorMessage(user.getProfile().getPreferredLocale(), e.getMessage()));
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(error.toString());
        }
    }

    @RequestMapping(value = "deliver/admin-session", method = RequestMethod.GET)
    public ResponseEntity<?> getAdminSession(final User user) {
        if (isIdCardManager(user)) {
            final RaspberryPiSession raspberryPiSession = user.getRaspberryPiSession();
            if (raspberryPiSession == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(RaspberryPiSessionDto.create(raspberryPiSession));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @SkipCSRF
    @RequestMapping(value = "deliver/admin-session", method = RequestMethod.PUT)
    public ResponseEntity<?> deliverMifare(final @RequestHeader("X-Requested-With") String requestedWith,
                                           final @RequestBody DeliverSessionMifareRequest request, final User user) {
        if (isIdCardManager(user)) {
            final User userToDeliver = User.findByUsername(request.getIstId());
            if (userToDeliver == null) {
                return ResponseEntity.notFound().build();
            }
            FenixFramework.atomic(() -> {
                final SantanderCardInfo card = userToDeliver.getCurrentSantanderEntry().getSantanderCardInfo();
                card.setMifareNumber(request.getMifare());
                card.getSantanderEntry().updateState(SantanderCardState.DELIVERED);
                registerMifareSession(user.getRaspberryPiSession().getIpAddress(), request.getMifare(), card);
            });
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @SkipCSRF
    @RequestMapping(value = "deliver/{mifare}", method = RequestMethod.PUT)
    public ResponseEntity<?> deliver(final @RequestHeader("X-Requested-With") String requestedWith,
                                     final @PathVariable String mifare, final HttpServletRequest request) {
        try {
            final Long mifareNumber = Long.parseLong(mifare);
            return Bennu.getInstance().getSantanderCardInfoSet().stream()
                    .filter(card -> !Strings.isNullOrEmpty(card.getMifareNumber())).filter(card -> {
                        try {
                            final Long cardMifareNumber = Long.parseLong(card.getMifareNumber());
                            return cardMifareNumber.equals(mifareNumber);
                        } catch (final NumberFormatException nfe) {
                            LOGGER.error("Error parsing >{}< for card {}", card.getMifareNumber(), card.getExternalId());
                            return false;
                        }
                    })
                    .findAny()
                    .map(card -> {
                        FenixFramework.atomic(() -> {
                            final User user = User.findByUsername(card.getIdentificationNumber());
                                if (isIdCardManager(user) && card.getSantanderCardStateTransitionsSet().stream()
                                    .anyMatch(t -> t.getState().equals(SantanderCardState.DELIVERED))) {
                                RaspberryPiSession.init(request.getRemoteAddr(), user);
                            } else {
                                registerMifareSession(request.getRemoteAddr(), mifare, card);
                                card.getSantanderEntry().updateState(SantanderCardState.DELIVERED);
                            }
                        });
                        return ResponseEntity.ok().build();
                    })
                    .orElseGet(() -> {
                        registerMifareSession(request.getRemoteAddr(), mifare, null);
                        return ResponseEntity.notFound().build();
                    });
        } catch (final NumberFormatException nfe) {
            LOGGER.error("Error parsing number", nfe);
            final JsonObject error = new JsonObject();
            error.addProperty("error", nfe.getMessage());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(error.toString());
        }
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    private void registerMifareSession(final String ipAddress, final String mifare, final SantanderCardInfo cardInfo) {
        final RaspberryPiSession raspberryPiSession = RaspberryPiSession.getSessionByIpAddress(ipAddress);
        if (raspberryPiSession != null) {
            raspberryPiSession.setUserMifare(mifare);
            raspberryPiSession.setUserCardInfo(cardInfo);
        }
    }

    @SkipCSRF
    @RequestMapping(value = "/{card}/deliver", method = RequestMethod.PUT)
    public ResponseEntity<?> deliverCard(final @PathVariable SantanderCardInfo card, final User user,
                                         final @RequestHeader("X-Requested-With") String requestedWith) {
        if (isIdCardManager(user)) {
            FenixFramework.atomic(() -> {
                card.getSantanderEntry().updateState(SantanderCardState.DELIVERED);
            });
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Atomic
    private boolean updateCardName(final User user, final String cardName) {
        SantanderUserInfo userInfo = user.getSantanderUserInfo();
        if (SantanderUserInfo.isCardNameValid(user, cardName)) {
            if (userInfo == null) {
                userInfo = new SantanderUserInfo();
            }
            userInfo.setCardName(cardName);
            return true;
        }
        return false;
    }

    private boolean isIdCardManager(final User user) {
        return Group.dynamic("idCardManager").isMember(user);
    }
}
