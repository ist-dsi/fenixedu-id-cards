package org.fenixedu.idcards.notifications;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;

@DeclareMessageTemplate(
        id = "message.template.santander.card.state.transition",
        description = "card transition notification",
        subject = "state update",
        text = "state update to {{ newState }}",
        parameters = {
                @TemplateParameter(id = "newState", description = "parameter.newState"),
        }
)
public class CardStateTransitionNotifications {

    public static void notifyUser(SantanderEntry entry) {
        Message.fromSystem()
                .to(Group.users(entry.getUser()))
                .template("message.template.santander.card.state.transition")
                .parameter("newState", entry.getSantanderCardInfo().getCurrentState().getLocalizedName(entry.getUser().getProfile().getPreferredLocale()))
        .and().wrapped().send();

    }
}
