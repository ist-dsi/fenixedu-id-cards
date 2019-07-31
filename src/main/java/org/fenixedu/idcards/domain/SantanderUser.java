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
    private User user;
    private IUserInfoService userInfoService;

    public SantanderUser(User user, IUserInfoService userInfoService) {
        this.user = user;
        this.userInfoService = userInfoService;
    }

    public CreateRegisterRequest toCreateRegisterRequest(RegisterAction action) throws SantanderCardNoPermissionException {
        CreateRegisterRequest createRegisterRequest = new CreateRegisterRequest();

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
        List<String> roles = userInfoService.getUserRoles(user);

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
        String userRole = getRole();
        String userCampus = getCampus();

        if (userRole.equals("STUDENT")) {
            if (userCampus.equals("Alameda")) {
                return PickupLocation.ALAMEDA_SANTANDER;
            } else if (userCampus.equals("Taguspark")) {
                return PickupLocation.TAGUS_NAGT;
            }
        } else {
            if (userCampus.equals("Alameda")) {
                return PickupLocation.ALAMEDA_DRH;
            } else if (userCampus.equals("Taguspark")) {
                return PickupLocation.TAGUS_DRH;
            } else if (userCampus.equals("Tecnológico e Nuclear")) {
                return PickupLocation.CTN_RH;
            }
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
        String userCampus = userInfoService.getCampus(user);

        if (userCampus == null)
            throw new SantanderCardNoPermissionException("santander.id.cards.missing.permission");

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

        SantanderUserInfo santanderUserInfo = new SantanderUserInfo();
        santanderUserInfo.setCardName(cardName);
        user.setSantanderUserInfo(santanderUserInfo);
    }

    public String getFullName() {
        return user.getProfile().getFullName();
    }

}
