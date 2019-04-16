package org.fenixedu.idcards.domain;

import org.fenixedu.santandersdk.dto.CreateRegisterResponse;
import org.joda.time.DateTime;
import pt.ist.fenixframework.Atomic;

public class SantanderCardInfo extends SantanderCardInfo_Base {
    
    public SantanderCardInfo() {
        super();
    }
    
    public SantanderCardInfo(String cardName, DateTime expiryDate, byte[] photo) {
        setCardName(cardName);
        setExpiryDate(expiryDate);
        setPhoto(photo);
    }

    public void update(CreateRegisterResponse response) {
        setCardName(response.getCardName());
        setExpiryDate(response.getCardExpiryDate());
        setPhoto(response.getPhoto());
    }
    public SantanderCardState getCurrentState() {
        SantanderCardStateTransition stateTransition = getSantanderCardStateTransitionsSet().stream()
                .min(SantanderCardStateTransition.COMPARATOR_BY_TRANSITION_DATE)
                .orElse(null);

        if (stateTransition == null) {
            return null;
        }

        return stateTransition.getState();
    }

    @Atomic
    public void deleteTransitions() {
        for (SantanderCardStateTransition transition: getSantanderCardStateTransitionsSet()) {
            transition.setSantanderCard(null);
        }
    }
}
