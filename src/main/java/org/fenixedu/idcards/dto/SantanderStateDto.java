package org.fenixedu.idcards.dto;

import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;

public class SantanderStateDto {
    public SantanderCardState state;
    public String transitionDate;

    public SantanderStateDto(SantanderCardStateTransition transition) {
        this.state = transition.getState();
        this.transitionDate = transition.getTransitionDate().toString();
    }
}
