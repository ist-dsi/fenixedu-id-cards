package org.fenixedu.idcards.pluggable;

import com.google.common.collect.ImmutableSet;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.BennuSpringContextHelper;
import org.fenixedu.bennu.vue.pluggable.Pluggable;
import org.fenixedu.bennu.vue.pluggable.PluggableParams;
import org.fenixedu.bennu.vue.pluggable.Provider;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.idcards.service.SantanderIdCardsService;

import java.util.Collections;
import java.util.Set;

/**
 * @author Tiago Pinho
 */
public class IdCardsPluggablesProvider implements Provider {

    private static final String BUNDLE = "resources/FenixEduIdCardsResources";
    private static final String FRONTEND_URL = CoreConfiguration.getConfiguration().applicationUrl() + "/tecnico-card";

    private final SantanderIdCardsService cardsService = BennuSpringContextHelper.getBean(SantanderIdCardsService.class);

    @Override
    public String getIdentifier() {
        return "connect-activities";
    }

    @Override
    public String getName() {
        return "FenixEdu Id Cards";
    }

    @Override
    public Set<? extends Pluggable> get(final PluggableParams pluggableParams) {
        final User user = (User) pluggableParams.get("user");
        if (user != null) {
            final SantanderEntry entry = cardsService.getOrUpdateState(user);
            if (entry == null || entry.canRenovateCard()) {
                final String titleKey = entry != null ? "title.card.renewal.activity" : "title.card.request.activity";
                final String descriptionKey = entry != null ? "description.card.renewal.activity" : "description.card.request.activity";
                return ImmutableSet.of(new IdCardsPluggableLink(this, BundleUtil.getLocalizedString(BUNDLE, titleKey),
                        BundleUtil.getLocalizedString(BUNDLE, descriptionKey), FRONTEND_URL));
            }
        }
        return Collections.emptySet();
    }

}
