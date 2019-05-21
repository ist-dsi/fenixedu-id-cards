package org.fenixedu.idcards.controller;

import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.idcards.dto.SantanderCardDto;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/idcards")
public class IdCardsController {

    private SantanderIdCardsService cardService;

    @Autowired
    public IdCardsController(SantanderIdCardsService cardService) {
        this.cardService = cardService;
    }

    @RequestMapping(value = "/getUserCards", method = RequestMethod.GET)
    public List<SantanderCardDto> getUserCards() {
        return cardService.getUserSantanderCards(Authenticate.getUser());
    }
}
