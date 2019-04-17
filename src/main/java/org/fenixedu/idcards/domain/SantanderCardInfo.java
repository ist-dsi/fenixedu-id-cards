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

    public void update(CardPreviewBean cardPreviewBean) {
        setIdentificationNumber(cardPreviewBean.getIdentificationNumber());
        setCardName(cardPreviewBean.getCardName());
        setExpiryDate(cardPreviewBean.getExpiryDate());
        setPhoto(cardPreviewBean.getPhoto());
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

    public boolean isNotPendingOrIgnored() {
        SantanderCardState state = getCurrentState();
        return state != SantanderCardState.PENDING && state != SantanderCardState.IGNORED && state != SantanderCardState.REJECTED;
    }

    @Atomic
    public void deleteTransitions() {
        for (SantanderCardStateTransition transition: getSantanderCardStateTransitionsSet()) {
            transition.setSantanderCard(null);
        }
    }
}
