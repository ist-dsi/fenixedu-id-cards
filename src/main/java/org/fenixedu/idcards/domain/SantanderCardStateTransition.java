package org.fenixedu.idcards.domain;

import org.joda.time.DateTime;

import java.util.Comparator;

public class SantanderCardStateTransition extends SantanderCardStateTransition_Base {

    public static Comparator<SantanderCardStateTransition> COMPARATOR_BY_TRANSITION_DATE = (p1, p2) -> {
        DateTime date1 = p1.getTransitionDate();
        DateTime date2 = p2.getTransitionDate();
        return date2.compareTo(date1);
    };

    public SantanderCardStateTransition() {
        super();
    }

    public SantanderCardStateTransition(SantanderCardInfo card, SantanderCardState state, DateTime transitionDate) {
        setSantanderCard(card);
        setState(state);
        setTransitionDate(transitionDate);
    }
}
