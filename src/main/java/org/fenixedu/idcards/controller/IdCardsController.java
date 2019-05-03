package org.fenixedu.idcards.controller;

import org.fenixedu.idcards.dto.SantanderCardInfoDto;
import org.fenixedu.idcards.service.SantanderIdCardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(value = "/getUserCards/{username}", method = RequestMethod.GET)
    public List<SantanderCardInfoDto> getUserCards(@PathVariable String username) {
        return cardService.getUserSantanderCards(username);
    }
}
