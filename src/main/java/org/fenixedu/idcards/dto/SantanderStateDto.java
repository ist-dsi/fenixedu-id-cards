package org.fenixedu.idcards.dto;

import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderCardStateTransition;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SantanderStateDto {
    public SantanderCardState state;
    public String when;

    public SantanderStateDto(SantanderCardStateTransition transition) {
        this.state = transition.getState();

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        DateTime transitionDate = transition.getTransitionDate();

        // Date of arrival at pickup location
        if (SantanderCardState.ISSUED.equals(this.state)) {
            transitionDate = transitionDate.plusDays(SantanderCardDto.DAYS_TO_ARRIVE);
        }

        this.when = dateFormatter.print(transitionDate);
    }
}
