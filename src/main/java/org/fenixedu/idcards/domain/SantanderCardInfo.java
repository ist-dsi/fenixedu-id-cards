package org.fenixedu.idcards.domain;

import java.util.List;
import java.util.stream.Collectors;

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
        setRole(cardPreviewBean.getRole());
        setExpiryDate(cardPreviewBean.getExpiryDate());
        setPhoto(cardPreviewBean.getPhoto());
    }

    public List<SantanderCardStateTransition> getOrderedTransitions() {
        return getSantanderCardStateTransitionsSet().stream()
                .sorted(SantanderCardStateTransition.COMPARATOR_BY_TRANSITION_DATE).collect(Collectors.toList());
    }

    public SantanderCardStateTransition getLastTransition() {
        return getSantanderCardStateTransitionsSet().stream()
                .min(SantanderCardStateTransition.REVERSED_COMPARATOR_BY_TRANSITION_DATE).orElse(null);
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
