package org.fenixedu.idcards.domain;

import org.fenixedu.santandersdk.dto.CardPreviewBean;

import pt.ist.fenixframework.Atomic;

public class SantanderCardInfo extends SantanderCardInfo_Base {
    
    public SantanderCardInfo() {
        super();
    }
    
    public SantanderCardInfo(CardPreviewBean cardPreviewBean) {
        update(cardPreviewBean);
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void update(CardPreviewBean cardPreviewBean) {
        setIdentificationNumber(cardPreviewBean.getIdentificationNumber());
        setCardName(cardPreviewBean.getCardName());
        setExpiryDate(cardPreviewBean.getExpiryDate());
        setPhoto(cardPreviewBean.getPhoto());
    }
    public SantanderCardState getCurrentState() {
        SantanderCardStateTransition stateTransition = getSantanderCardStateTransitionsSet().stream()
                .min(SantanderCardStateTransition.REVERSED_COMPARATOR_BY_TRANSITION_DATE)
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
