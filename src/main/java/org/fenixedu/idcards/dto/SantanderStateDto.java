package org.fenixedu.idcards.dto;

import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SantanderStateDto {
    public SantanderCardState state;
    public String when;

    public SantanderStateDto(SantanderCardStateTransition transition) {
        this.state = transition.getState();
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        this.when = dateFormatter.print(transition.getTransitionDate());
    }
}
