package org.fenixedu.idcards.dto;

import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

public class SantanderCardInfoDto {
    public String expiryDate;
    public String cardName;
    public String identificationNumber;
    public byte[] photo;
    public List<SantanderStateDto> stateHistory;

    public SantanderCardInfoDto(SantanderCardInfo cardInfo) {
        DateTime expiryDate = cardInfo.getExpiryDate();
        if (expiryDate != null) {
            this.expiryDate = expiryDate.toString();
        }
        this.cardName = cardInfo.getCardName();
        this.identificationNumber = cardInfo.getIdentificationNumber();
        this.photo = cardInfo.getPhoto();
        this.stateHistory = cardInfo.getSantanderCardStateTransitionsSet()
                .stream()
                .map(SantanderStateDto::new)
                .collect(Collectors.toList());
    }
}
