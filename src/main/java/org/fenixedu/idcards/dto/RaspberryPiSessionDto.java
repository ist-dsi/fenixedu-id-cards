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

    public static RaspberryPiSessionDto create(RaspberryPiSession raspberryPiSession) {
        RaspberryPiSessionDto dto = new RaspberryPiSessionDto();

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm dd/MM/YYYY");
        dto.setCreatedAt(dateFormatter.print(raspberryPiSession.getCreatedAt()));

        dto.setUserMifare(raspberryPiSession.getUserMifare());

        SantanderCardInfo cardInfo = raspberryPiSession.getUserCardInfo();
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

    public void setUserMifare(String userMifare) {
        this.userMifare = userMifare;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserIstId() {
        return userIstId;
    }

    public void setUserIstId(String userIstId) {
        this.userIstId = userIstId;
    }
}
