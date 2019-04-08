package org.fenixedu.idcards.utils;

import java.util.Locale;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SantanderCardState implements IPresentableEnum {

    PENDING,        //Initial state; If not changed it means that something went when communicating with santander

    IGNORED,        //Request was rejected  

    REJECTED,       //If a succeseful request is rejected later (can happen if photo is not appropriate)

    NEW,            //After first card request was successful

    PRODUCTION,     //When card is in production

    ISSUED;         //After card was sent

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
}

