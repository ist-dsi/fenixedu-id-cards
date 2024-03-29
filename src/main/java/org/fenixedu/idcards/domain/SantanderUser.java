package org.fenixedu.idcards.domain;

import java.awt.image.BufferedImage;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.exception.SantanderCardNoPermissionException;
import org.fenixedu.idcards.service.IUserInfoService;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.RegisterAction;
import pt.ist.fenixframework.Atomic;

public class SantanderUser {

    private final User user;
    private final IUserInfoService userInfoService;

    public SantanderUser(final User user, final IUserInfoService userInfoService) {
        this.user = user;
        this.userInfoService = userInfoService;
    }

    public CreateRegisterRequest toCreateRegisterRequest(final RegisterAction action) throws SantanderCardNoPermissionException {
        final CreateRegisterRequest createRegisterRequest = new CreateRegisterRequest();

        createRegisterRequest.setRole(getRole());
        createRegisterRequest.setPhoto(getPhoto());
        createRegisterRequest.setCardName(getName());
        createRegisterRequest.setFullName(getFullName());
        createRegisterRequest.setDepartmentAcronym(getDepartmentAcronym());
        createRegisterRequest.setCampus(getCampus());
        createRegisterRequest.setUsername(user.getUsername());
        createRegisterRequest.setAction(action);
        createRegisterRequest.setPickupAddress(getUserPickupLocation().toPickupAddress());

        return createRegisterRequest;
    }

    public String getRole() throws SantanderCardNoPermissionException {
        final List<String> roles = userInfoService.getUserRoles(user);

        if (roles.contains("EMPLOYEE")) {
            return "EMPLOYEE";
        } else if (roles.contains("STUDENT")) {
            return "STUDENT";
        } else if (roles.contains("GRANT_OWNER")) {
            return "GRANT_OWNER";
        } else if (roles.contains("RESEARCHER")) {
            return "RESEARCHER";
        } else if (roles.contains("TEACHER")) {
            return "TEACHER";
        } else {
            throw new SantanderCardNoPermissionException("santander.id.cards.missing.permission");
        }
    }

    public PickupLocation getUserPickupLocation() throws SantanderCardNoPermissionException {
        final String userRole = getRole();
        final String userCampus = getCampus();

        switch (userCampus) {
            case "Alameda":
                return userRole.equals("STUDENT") ? PickupLocation.ALAMEDA_SANTANDER : PickupLocation.ALAMEDA_DRH;
            case "Taguspark":
                return PickupLocation.TAGUS_AGRHA;
            case "Tecnológico e Nuclear":
                return PickupLocation.CTN_RH;
        }

        return null;
    }

    public BufferedImage getPhoto() {
        return userInfoService.getUserPhoto(user);
    }

    public String getDepartmentAcronym() {
        return userInfoService.getUserDepartmentAcronym(user);
    }

    public String getCampus() throws SantanderCardNoPermissionException {
        final String userCampus = userInfoService.getCampus(user);
        if (userCampus == null) {
            throw new SantanderCardNoPermissionException("santander.id.cards.missing.permission");
        }
        return userCampus;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        if (user.getSantanderUserInfo() == null) {
            setUserInfo();
        }
        return user.getSantanderUserInfo().getCardName();
    }

    @Atomic
    private void setUserInfo() {
        final String fullName = getFullName();
        final String[] fullNameParts = fullName.trim().split(" ");
        final String cardName = fullNameParts[0] + " " + fullNameParts[fullNameParts.length - 1];

        final SantanderUserInfo santanderUserInfo = new SantanderUserInfo();
        santanderUserInfo.setCardName(cardName);
        user.setSantanderUserInfo(santanderUserInfo);
    }

    public String getFullName() {
        return user.getProfile().getFullName();
    }

}
