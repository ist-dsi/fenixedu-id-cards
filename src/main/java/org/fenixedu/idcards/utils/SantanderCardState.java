package org.fenixedu.idcards.utils;

import java.util.Locale;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;

import org.fenixedu.idcards.domain.RegisterAction;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SantanderCardState implements IPresentableEnum {

    PENDING,    //Initial state; If not changed it means that something went wrong with fenix

    RESPONSE_ERROR, //If the response did not arrive successfully

    REJECTED,       //The request was accepted but ended up being rejected (the photo might not be appropriate)

    NEW,            //After first card request was successful

    PRODUCTION,     //When card is in production

    ISSUED,         //After card was sent

    CANCELED;       //After canceling card

    public String getName() {
        return name();
    }

    @Override
    public String getLocalizedName() {
        return getLocalizedNameI18N().getContent();
    }

    public String getLocalizedName(final Locale locale) {
        return getLocalizedNameI18N().getContent(locale);
    }

    public LocalizedString getLocalizedNameI18N() {
        //TODO Escrever os nomes para os estados no FenixeduIstIntegrationResources
        return BundleUtil.getLocalizedString("resources.FenixeduIstIntegrationResources", getClass().getName() + "." + name());
    }

    public boolean canRegisterNew() {
        return this == SantanderCardState.REJECTED || this == SantanderCardState.CANCELED
                || this == SantanderCardState.RESPONSE_ERROR;
    }
}

