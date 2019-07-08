package org.fenixedu.idcards.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.santandersdk.dto.CardPreviewBean;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class SantanderCardInfo extends SantanderCardInfo_Base {
    
    public SantanderCardInfo() {
        super();
    }
    
    public SantanderCardInfo(CardPreviewBean cardPreviewBean, PickupLocation pickupLocation) {
        setBennu(Bennu.getInstance());
        setIdentificationNumber(cardPreviewBean.getIdentificationNumber());
        setCardName(cardPreviewBean.getCardName());
        setRole(cardPreviewBean.getRole());
        setExpiryDate(cardPreviewBean.getExpiryDate());
        setPhoto(cardPreviewBean.getPhoto());
        setPickupLocation(pickupLocation);
    }

    public DateTime getFirstTransitionDate() {
        SantanderCardStateTransition stateTransition = getSantanderCardStateTransitionsSet().stream()
                .min(SantanderCardStateTransition.COMPARATOR_BY_TRANSITION_DATE).orElse(null);
        return stateTransition == null ? null : stateTransition.getTransitionDate();
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

    public boolean isDelivered() {
        return getSantanderCardStateTransitionsSet().stream()
                .anyMatch(t -> SantanderCardState.DELIVERED.equals(t.getState()));
    }

    @Atomic
    public void deleteTransitions() {
        for (SantanderCardStateTransition transition: getSantanderCardStateTransitionsSet()) {
            transition.setSantanderCard(null);
        }
    }
}
