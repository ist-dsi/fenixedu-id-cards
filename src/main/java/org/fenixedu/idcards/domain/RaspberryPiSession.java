package org.fenixedu.idcards.domain;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.joda.time.DateTime;

public class RaspberryPiSession extends RaspberryPiSession_Base {

    private RaspberryPiSession(String ipAddress) {
        super();
        setIpAddress(ipAddress);
        setBennu(Bennu.getInstance());
    }

    public static RaspberryPiSession init(String ipAddress, User admin) {
        RaspberryPiSession piSession =  Bennu.getInstance().getRaspberryPiSessionSet().stream()
                .filter(r -> r.getIpAddress().equals(ipAddress))
                .findAny()
                .orElseGet(() -> new RaspberryPiSession(ipAddress));

        piSession.setAdmin(admin);
        piSession.setCreatedAt(new DateTime());
        piSession.setUserMifare(null);
        piSession.setUserCardInfo(null);

        return piSession;
    }

    public static RaspberryPiSession getSessionByIpAddress(String ipAddress) {
        return Bennu.getInstance().getRaspberryPiSessionSet().stream()
                .filter(r -> r.getIpAddress().equals(ipAddress))
                .findAny()
                .orElse(null);
    }
}
