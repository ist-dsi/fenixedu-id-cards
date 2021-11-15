package org.fenixedu.idcards.dto;

import com.google.common.io.BaseEncoding;
import org.fenixedu.idcards.domain.RaspberryPiSession;
import org.fenixedu.idcards.domain.SantanderCardInfo;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class RaspberryPiSessionDto {

    private String ipAddress;
    private String createdAt;
    private String userMifare;
    private String userPhoto;
    private String userIstId;

    private RaspberryPiSessionDto() {
    }

    public static RaspberryPiSessionDto create(final RaspberryPiSession raspberryPiSession) {
        final RaspberryPiSessionDto dto = new RaspberryPiSessionDto();
        final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm dd/MM/YYYY");

        dto.setCreatedAt(dateFormatter.print(raspberryPiSession.getCreatedAt()));
        dto.setUserMifare(raspberryPiSession.getUserMifare());

        final SantanderCardInfo cardInfo = raspberryPiSession.getUserCardInfo();
        if (cardInfo != null) {
            dto.setUserIstId(cardInfo.getIdentificationNumber());
            dto.setUserPhoto(BaseEncoding.base64().encode(cardInfo.getPhoto()));
        }

        dto.setIpAddress(raspberryPiSession.getIpAddress());
        return dto;
    }

    public String getUserMifare() {
        return userMifare;
    }

    public void setUserMifare(final String userMifare) {
        this.userMifare = userMifare;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(final String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserIstId() {
        return userIstId;
    }

    public void setUserIstId(final String userIstId) {
        this.userIstId = userIstId;
    }

}
