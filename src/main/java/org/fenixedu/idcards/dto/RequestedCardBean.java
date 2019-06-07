package org.fenixedu.idcards.dto;

import org.joda.time.DateTime;

public class RequestedCardBean {

    private String requestLine;
    private String identificationNumber;
    private String cardName;
    private DateTime expiryDate;
    private String role;
    private String photo;
    private String mifare;
    private DateTime productionDate;
    private DateTime requestDate;
    private String cardSerialNumber;

    public RequestedCardBean(String requestLine, String identificationNumber, String cardName, DateTime expiryDate, String role,
            String photo, String mifare, DateTime productionDate, DateTime requestDate, String cardSerialNumber) {

        this.requestLine = requestLine;
        this.identificationNumber = identificationNumber;
        this.cardName = cardName;
        this.expiryDate = expiryDate;
        this.role = role;
        this.photo = photo;
        this.mifare = mifare;
        this.productionDate = productionDate;
        this.requestDate = requestDate;
        this.cardSerialNumber = cardSerialNumber;
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public DateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(DateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMifare() {
        return mifare;
    }

    public void setMifare(String mifare) {
        this.mifare = mifare;
    }

    public DateTime getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(DateTime productionDate) {
        this.productionDate = productionDate;
    }

    public DateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(DateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public String toString() {
        String template =
                "requestLine: %s| identificationNumber: %s| cardName: %s| expiryDate: %s| role: %s| photo: %s| mifare: %s| productionDate: %s| requestDate: %s| cardSerialNumber %s";

        return String.format(template, getRequestLine(), getIdentificationNumber(), getCardName(), getExpiryDate(), getRole(),
                getPhoto(), getMifare(), getProductionDate(), getRequestDate(), getCardSerialNumber());
    }
}
