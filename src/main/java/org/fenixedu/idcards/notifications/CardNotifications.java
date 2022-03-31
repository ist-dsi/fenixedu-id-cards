package org.fenixedu.idcards.notifications;

import org.fenixedu.bennu.core.domain.User;
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
        id = "message.template.santander.card.state.transition.pickup.with.working.hours",
        description = "Card is ready for pickup message",
        subject = "Ready for pickup",
        text = "Your card is ready to be delivered to you at {{ pickupLocation }}, {{ campus }}, between {{ morningHours }} and {{ afternoonHours }}. (workdays)",
        parameters = {
                @TemplateParameter(id = "pickupLocation", description = "Location to pickup card"),
                @TemplateParameter(id = "morningHours", description = "Morning hours"),
                @TemplateParameter(id = "afternoonHours", description = "Afternoon Hours")
        }
)
@DeclareMessageTemplate(
        id = "message.template.santander.card.state.transition.pickup",
        description = "Card is ready for pickup message",
        subject = "Ready for pickup",
        text = "Your card is ready to be delivered to you at {{ pickupLocation }}, {{ campus }}. (workdays)",
        parameters = {
                @TemplateParameter(id = "pickupLocation", description = "Location to pickup card")
        }
)
@DeclareMessageTemplate(
        id = "message.template.santander.card.expiring",
        description = "Card is expiring in some days",
        subject = "Card expiring soon",
        text = "Your card is expiring soon. Please review your card info, so it comes accurately in your next card and it can proceed to the automatic request."
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

@DeclareMessageTemplate(
        id = "message.template.santander.card.first",
        description = "Review information for first card",
        subject = "IST card information",
        text = "Please review your card info, so it comes accurately in your next card and it can proceed to the automatic request. The automatic request will happen in {{ waitingDays }} days. Alternatively you can use this (link) to make a manual request after validating your information",
        parameters = {
                @TemplateParameter(id = "waitingDays", description = "Waiting days until automatic request")
        }
)

public class CardNotifications {

    public static void notifyStateTransition(final SantanderEntry entry) {
        if (SantanderCardState.NEW.equals(entry.getState())) {
            Message.fromSystem()
                    .to(Group.users(entry.getUser()))
                    .template("message.template.santander.card.state.transition.requested")
                    .and().wrapped().send();
        }
    }

    public static void notifyCardPickup(final User user) {
        final SantanderEntry entry = user.getCurrentSantanderEntry();
        final PickupLocation pickupLocation = entry.getSantanderCardInfo().getPickupLocation();
        final String template = hasWorkingHours(pickupLocation) ?
                "message.template.santander.card.state.transition.pickup.with.working.hours" :
                "message.template.santander.card.state.transition.pickup";

        Message.TemplateMessageBuilder builder = Message.fromSystem()
                .to(Group.users(entry.getUser()))
                .template(template)
                .parameter("pickupLocation", pickupLocation.getPickupLocation())
                .parameter("campus", pickupLocation.getCampus());

        if (PickupLocation.ALAMEDA_SANTANDER.equals(pickupLocation)) {
            builder = builder.parameter("morningHours", "10:00h")
                    .parameter("afternoonHours", "13:00h");
        } else if (PickupLocation.TAGUS_AGRHA.equals(pickupLocation)) {
            builder = builder.parameter("morningHours", "10:00h - 12:00h")
                    .parameter("afternoonHours", "14:00h - 16:00h");
        }

        builder.and().wrapped().send();
    }

    public static void notifyCardExpiring(final User user) {
        Message.fromSystem()
                .to(Group.users(user))
                .template("message.template.santander.card.expiring")
                .and().wrapped().send();
    }

    public static void notifyMissingInformation(final User user, final String missingInfo) {
        Message.fromSystem()
                .to(Group.users(user))
                .template("message.template.santander.card.request.missing.info")
                .parameter("missingInfo", missingInfo)
                .and().wrapped().send();
    }

    public static void notifyFirstCardInfoCheck(final User user, final int waitingDays) {
        Message.fromSystem()
            .to(Group.users(user))
            .template("message.template.santander.card.first")
            .parameter("waitingDays", waitingDays)
            .and().wrapped().send();
    }

    private static boolean hasWorkingHours(final PickupLocation pickupLocation) {
        return PickupLocation.ALAMEDA_SANTANDER.equals(pickupLocation)
                || PickupLocation.TAGUS_AGRHA.equals(pickupLocation);
    }

}
