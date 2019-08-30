package org.fenixedu.idcards.domain;

import java.util.Locale;

import org.fenixedu.bennu.core.i18n.BundleUtil;

public enum SantanderCardState {

    PENDING,        //Initial state; If not changed it means that something went wrong when communicating with santander

    WAITING_INFO,   //Used for first card (this state is used to wait for the user to update his information)

    IGNORED,        //Request was rejected  

    REJECTED,       //If a successful request is rejected later (can happen if photo is not appropriate)

    NEW,            //After first card request was successful

    READY_FOR_PRODUCTION,

    PRODUCTION,     //When card is in production

    ISSUED,         //After card was sent

    DELIVERED,

    EXPIRED;

    public String getLocalizedName(Locale locale) {
        return BundleUtil.getString("resources.FenixEduIdCardsResources", locale, this.getClass().getName() + "." + name());
    }
}

