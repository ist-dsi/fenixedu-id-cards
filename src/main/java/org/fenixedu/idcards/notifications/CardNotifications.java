package org.fenixedu.idcards.notifications;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.idcards.domain.PickupLocation;
import org.fenixedu.idcards.domain.SantanderCardState;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;

@DeclareMessageTemplate(
        id = "message.template.santander.card.state.transition.requested",
        description = "Card requested message",
        subject = "Card requested",
        text = "Your card request was successful, you'll be notified for it's pickup soon."
)
@DeclareMessageTemplate(
        id = "message.template.santander.card.state.transition.pickup",
        description = "Card is ready for pickup message",
        subject = "Ready for pickup",
        text = "Your card is ready to be delivered to you at {{ pickupLocation }}, {{ campus }}, between {{ morningHours }} and {{ afternoonHours }}. (workdays)",
        parameters = {
                @TemplateParameter(id = "pickupLocation", description = "Location to pickup card"),
                @TemplateParameter(id = "morningHours", description = "Morning hours"),
                @TemplateParameter(id ="afternoonHours", description = "Afternoon Hours")
        }
)
@DeclareMessageTemplate(
        id = "message.template.santander.card.expiring",
        description = "Card is expiring in some days",
        subject = "Card expiring soon",
        text = "Your card is expiring soon. Please review your card info, so it comes accurately in your next card and it can proceed to the automatic request.",
        parameters = {
                @TemplateParameter(id = "pickupLocation", description = "Location to pickup card"),
                @TemplateParameter(id = "morningHours", description = "Morning hours"),
                @TemplateParameter(id ="afternoonHours", description = "Afternoon Hours")
        }
)
@DeclareMessageTemplate(
        id = "message.template.santander.card.request.missing.info",
        description = "Missing info when requesting a card",
        subject = "Missing info",
        text = "It seems that you have the following required fields are missing in Fenix: {{ missingInfo }}. Please fill it so your card proceeds to production",
        parameters = {
                @TemplateParameter(id = "missingInfo", description = "Missing info")
        }
)
public class CardNotifications {

    public static void notifyStateTransition(SantanderEntry entry) {
        if (SantanderCardState.NEW.equals(entry.getSantanderCardInfo().getCurrentState())) {
            Message.fromSystem()
                    .to(Group.users(entry.getUser()))
                    .template("message.template.santander.card.state.transition.requested")
                    .and().wrapped().send();
        } else if (SantanderCardState.ISSUED.equals(entry.getSantanderCardInfo().getCurrentState())) {
            PickupLocation pickupLocation = entry.getSantanderCardInfo().getPickupLocation();
            Message.fromSystem()
                    .to(Group.users(entry.getUser()))
                    .template("message.template.santander.card.state.transition.pickup")
                    .parameter("pickupLocation", pickupLocation.getPickupLocation())
                    .parameter("campus", pickupLocation.getCampus())
                    .parameter("morningHours", pickupLocation.getMorningHours().toString())
                    .parameter("afternoonHours", pickupLocation.getAfternoonHours().toString())
                    .and().wrapped().send();
        }
    }
}
