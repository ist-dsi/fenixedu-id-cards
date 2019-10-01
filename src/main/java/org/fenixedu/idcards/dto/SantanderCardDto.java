package org.fenixedu.idcards.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.fenixedu.santandersdk.dto.PickupAddress;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.io.BaseEncoding;

public class SantanderCardDto {

    // New cards are requested 60 days before previous expiry date.
    private static final int DAYS_TO_EXPIRE = 60;
    // Cards take between 8 and 15 days to arrive the pickup location
    static final int DAYS_TO_ARRIVE = 15;

    public String cardId;
    public String istId;
    public String role;
    public String expiryDate;
    public Integer daysBeforeRequest;
    public String name;
    public String photo;
    public String serialNumber;
    public SantanderCardState currentState;
    public PickupAddress pickupAddress;
    public List<SantanderStateDto> history;

    public SantanderCardDto(SantanderCardInfo cardInfo) {
        DateTime expiryDate = cardInfo.getExpiryDate();
        this.currentState = cardInfo.getCurrentState();
        if (expiryDate != null) {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy/MM");
            this.expiryDate = dateFormatter.print(expiryDate);
            expiryDate = expiryDate.withTimeAtStartOfDay();
            DateTime today = DateTime.now().withTimeAtStartOfDay();

            if (today.isAfter(expiryDate.minusDays(DAYS_TO_EXPIRE + 31)) && today
                    .isBefore(expiryDate.minusDays(DAYS_TO_EXPIRE - 1))) {
                this.daysBeforeRequest = Days.daysBetween(today, expiryDate.minusDays(DAYS_TO_EXPIRE)).getDays();
            }
        }
        this.cardId = cardInfo.getExternalId();
        this.istId = cardInfo.getIdentificationNumber();
        this.name = cardInfo.getCardName();
        this.role = cardInfo.getRole();

        if (cardInfo.getPhoto() != null) {
            this.photo = BaseEncoding.base64().encode(cardInfo.getPhoto());
        }

        this.serialNumber = cardInfo.getSerialNumber();
        this.history = cardInfo.getOrderedTransitions().stream()
                .filter(t -> !SantanderCardState.ISSUED.equals(t.getState()) || DateTime.now()
                        .isAfter(t.getTransitionDate().plusDays(DAYS_TO_ARRIVE)))
                .map(SantanderStateDto::new)
                .collect(Collectors.toList());
        this.pickupAddress = cardInfo.getPickupLocation().toPickupAddress();
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
