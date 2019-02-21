package org.fenixedu.idcards.domain;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

import java.util.Locale;

public enum RegisterAction implements IPresentableEnum {
    NOVO,
    REMI,
    RENU,
    ATUA,
    CANC;

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
