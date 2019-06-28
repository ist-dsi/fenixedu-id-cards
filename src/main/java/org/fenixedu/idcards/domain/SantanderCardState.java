package org.fenixedu.idcards.domain;

import org.fenixedu.bennu.core.i18n.BundleUtil;

import java.util.Locale;

public enum SantanderCardState {

    PENDING,        //Initial state; If not changed it means that something went when communicating with santander

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

