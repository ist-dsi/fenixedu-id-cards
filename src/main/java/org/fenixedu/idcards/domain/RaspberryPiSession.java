package org.fenixedu.idcards.domain;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.joda.time.DateTime;

public class RaspberryPiSession extends RaspberryPiSession_Base {

    private RaspberryPiSession(final String ipAddress) {
        super();
        setIpAddress(ipAddress);
        setBennu(Bennu.getInstance());
    }

    public static RaspberryPiSession init(final String ipAddress, final User admin) {
        final RaspberryPiSession piSession =  Bennu.getInstance().getRaspberryPiSessionSet().stream()
                .filter(r -> r.getIpAddress().equals(ipAddress))
                .findAny()
                .orElseGet(() -> new RaspberryPiSession(ipAddress));

        piSession.setAdmin(admin);
        piSession.setCreatedAt(new DateTime());
        piSession.setUserMifare(null);
        piSession.setUserCardInfo(null);
        return piSession;
    }

    public static RaspberryPiSession getSessionByIpAddress(final String ipAddress) {
        return Bennu.getInstance().getRaspberryPiSessionSet().stream()
                .filter(r -> r.getIpAddress().equals(ipAddress))
                .findAny()
                .orElse(null);
    }
}
