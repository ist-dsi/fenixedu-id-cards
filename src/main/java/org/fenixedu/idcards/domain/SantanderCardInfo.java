package org.fenixedu.idcards.domain;

import org.joda.time.DateTime;

public class SantanderCardInfo extends SantanderCardInfo_Base {
    
    public SantanderCardInfo() {
        super();
    }
    
    public SantanderCardInfo(String cardName, DateTime expiryDate, byte[] photo) {
        setCardName(cardName);
        setExpiryDate(expiryDate);
        setPhoto(photo);
    }

}
