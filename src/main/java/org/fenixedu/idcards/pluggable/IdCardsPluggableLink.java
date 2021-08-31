package org.fenixedu.idcards.pluggable;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import org.fenixedu.bennu.vue.pluggable.Pluggable;
import org.fenixedu.bennu.vue.pluggable.Provider;
import org.fenixedu.commons.i18n.LocalizedString;

/**
 * @author Tiago Pinho
 */
public class IdCardsPluggableLink extends Pluggable {

    private final LocalizedString description;
    private final String url;

    public IdCardsPluggableLink(final Provider provider, final LocalizedString title, final LocalizedString description,
            final String url) {
        super(provider, title);
        this.description = description;
        this.url = url;
    }

    public IdCardsPluggableLink(final Provider provider, final String bundle, final String titleKey,
            final LocalizedString description, final String url) {
        super(provider, bundle, titleKey);
        this.description = description;
        this.url = url;
    }

    public IdCardsPluggableLink(final Provider provider, final String bundle, final String titleKey,
            final LocalizedString description, final String url, final String... titleArgs) {
        super(provider, bundle, titleKey, titleArgs);
        this.description = description;
        this.url = url;
    }

    @Override
    public JsonObject toJson() {
        final JsonObject data = super.toJson();
        if (description != null) {
            data.add("description", description.json());
        }
        if (!Strings.isNullOrEmpty(url)) {
            data.addProperty("url", url);
        }
        return data;
    }

}
