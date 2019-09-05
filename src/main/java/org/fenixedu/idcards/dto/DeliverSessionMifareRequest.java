package org.fenixedu.idcards.dto;

public class DeliverSessionMifareRequest {
    private String mifare;
    private String istId;

    public String getMifare() {
        return mifare;
    }

    public void setMifare(String mifare) {
        this.mifare = mifare;
    }

    public String getIstId() {
        return istId;
    }

    public void setIstId(String istId) {
        this.istId = istId;
    }
}
