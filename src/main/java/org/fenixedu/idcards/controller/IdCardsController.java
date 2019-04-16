package org.fenixedu.idcards.controller;

import org.fenixedu.dto.SantanderCardInfoDto;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderEntryNew;
import org.fenixedu.idcards.service.SantanderRequestCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/idcards")
public class IdCardsController {

    private SantanderRequestCardService cardService;

    @Autowired
    public IdCardsController(SantanderRequestCardService cardService) {
        this.cardService = cardService;
    }

    @RequestMapping(value = "/getUserCards/{username}", method = RequestMethod.GET)
    public List<SantanderCardInfoDto> getUserCards(@PathVariable String username) {
        return cardService.getUserSantanderCards(username);
    }
}
