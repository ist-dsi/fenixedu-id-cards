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

    public RequestedCardBean(final String requestLine, final String identificationNumber, final String cardName,
                             final DateTime expiryDate, final String role, final String photo, final String mifare,
                             final DateTime productionDate, final DateTime requestDate, final String cardSerialNumber) {
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

    public void setRequestLine(final String requestLine) {
        this.requestLine = requestLine;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(final String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(final String cardName) {
        this.cardName = cardName;
    }

    public DateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final DateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(final String photo) {
        this.photo = photo;
    }

    public String getMifare() {
        return mifare;
    }

    public void setMifare(final String mifare) {
        this.mifare = mifare;
    }

    public DateTime getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(final DateTime productionDate) {
        this.productionDate = productionDate;
    }

    public DateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(final DateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(final String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public String toString() {
        final String template = "requestLine: %s| identificationNumber: %s| cardName: %s| expiryDate: %s| role: %s| photo: %s| "
                + "mifare: %s| productionDate: %s| requestDate: %s| cardSerialNumber %s";
        return String.format(template, getRequestLine(), getIdentificationNumber(), getCardName(), getExpiryDate(), getRole(),
                getPhoto(), getMifare(), getProductionDate(), getRequestDate(), getCardSerialNumber());
    }
}
