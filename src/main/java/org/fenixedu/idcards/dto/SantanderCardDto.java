package org.fenixedu.idcards.dto;

import com.google.common.io.BaseEncoding;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class SantanderCardDto {
    public String cardId;
    public String istId;
    public String role;
    public String expiryDate;
    public String name;
    public String photo;
    public String serialNumber;
    public SantanderCardState currentState;
    public List<SantanderStateDto> history;

    public SantanderCardDto(SantanderCardInfo cardInfo) {
        DateTime expiryDate = cardInfo.getExpiryDate();
        this.currentState = cardInfo.getCurrentState();
        if (expiryDate != null) {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy/MM");
            this.expiryDate = dateFormatter.print(expiryDate);
            if (DateTime.now().isAfter(expiryDate.toInstant())) {
                this.currentState = SantanderCardState.EXPIRED;
            }
        }
        this.cardId = cardInfo.getExternalId();
        this.istId = cardInfo.getIdentificationNumber();
        this.name = cardInfo.getCardName();
        this.role = cardInfo.getRole();
        this.photo = BaseEncoding.base64().encode(cardInfo.getPhoto());
        this.serialNumber = cardInfo.getSerialNumber();
        this.history = cardInfo.getSantanderCardStateTransitionsSet()
                .stream()
                .map(SantanderStateDto::new)
                .collect(Collectors.toList());
    }

    public SantanderCardDto(CardPreviewBean cardPreviewBean) {
        this.istId = cardPreviewBean.getIdentificationNumber();
        this.role = cardPreviewBean.getRole();
        DateTime expiryDate = cardPreviewBean.getExpiryDate();
        if (expiryDate != null) {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy/MM");
            this.expiryDate = dateFormatter.print(expiryDate);
        }
        this.name = cardPreviewBean.getCardName();
        this.photo = BaseEncoding.base64().encode(cardPreviewBean.getPhoto());
    }
}
