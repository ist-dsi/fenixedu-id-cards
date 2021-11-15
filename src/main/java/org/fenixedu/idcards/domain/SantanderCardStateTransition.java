package org.fenixedu.idcards.domain;

import org.joda.time.DateTime;

import java.util.Comparator;

public class SantanderCardStateTransition extends SantanderCardStateTransition_Base {

    public static Comparator<SantanderCardStateTransition> COMPARATOR_BY_TRANSITION_DATE = (p1, p2) -> {
        DateTime date1 = p1.getTransitionDate();
        DateTime date2 = p2.getTransitionDate();
        return date1.compareTo(date2);
    };

    public static Comparator<SantanderCardStateTransition> REVERSED_COMPARATOR_BY_TRANSITION_DATE = COMPARATOR_BY_TRANSITION_DATE.reversed();

    public SantanderCardStateTransition() {
        super();
    }

    public SantanderCardStateTransition(final SantanderCardInfo card, final SantanderCardState state, final DateTime transitionDate) {
        setSantanderCard(card);
        setState(state);
        setTransitionDate(transitionDate);
    }
}
