package org.fenixedu.idcards.domain;

public enum SantanderCardState {

    PENDING,        //Initial state; If not changed it means that something went when communicating with santander

    IGNORED,        //Request was rejected  

    REJECTED,       //If a successful request is rejected later (can happen if photo is not appropriate)

    NEW,            //After first card request was successful

    READY_FOR_PRODUCTION,

    PRODUCTION,     //When card is in production

    ISSUED         //After card was sent
}

