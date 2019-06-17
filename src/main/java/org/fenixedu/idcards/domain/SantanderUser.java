package org.fenixedu.idcards.domain;

import java.awt.image.BufferedImage;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.service.IUserInfoService;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.PickupAddress;
import org.fenixedu.santandersdk.dto.RegisterAction;
import org.fenixedu.santandersdk.exception.SantanderNoRoleAvailableException;

public class SantanderUser {
    private User user;
    private IUserInfoService userInfoService;

    public SantanderUser(User user, IUserInfoService userInfoService) {
        this.user = user;
        this.userInfoService = userInfoService;
    }

    public CreateRegisterRequest toCreateRegisterRequest(RegisterAction action) throws SantanderNoRoleAvailableException {
        CreateRegisterRequest createRegisterRequest = new CreateRegisterRequest();

        createRegisterRequest.setRole(getRole());
        createRegisterRequest.setPhoto(getPhoto());
        createRegisterRequest.setName(user.getDisplayName());
        createRegisterRequest.setDepartmentAcronym(getDepartmentAcronym());
        createRegisterRequest.setCampus(getCampus());
        createRegisterRequest.setUsername(user.getUsername());
        createRegisterRequest.setAction(action);
        createRegisterRequest.setPickupAddress(getUserPickupLocation().toPickupAddress());

        return createRegisterRequest;
    }

    public String getRole() throws SantanderNoRoleAvailableException {
        List<String> roles = userInfoService.getUserRoles(user);

        if (roles.contains("STUDENT")) {
            return "STUDENT";
        } else if (roles.contains("TEACHER")) {
            return "TEACHER";
        } else if (roles.contains("RESEARCHER")) {
            return "RESEARCHER";
        } else if (roles.contains("EMPLOYEE")) {
            return "EMPLOYEE";
        } else if (roles.contains("GRANT_OWNER")) {
            return "GRANT_OWNER";
        } else {
            throw new SantanderNoRoleAvailableException("Person has no valid role");
        }
    }

    public PickupLocation getUserPickupLocation() throws SantanderNoRoleAvailableException {
        String userRole = getRole();
        String userCampus = getCampus().toLowerCase();

        if (userRole.equals("STUDENT")) {
            if (userCampus.equals("alameda")) {
                return PickupLocation.ALAMEDA_SANTANDER;
            } else if (userCampus.equals("tagus")) {
                return PickupLocation.TAGUS_NAGT;
            }
        } else {
            if (userCampus.equals("alameda")) {
                return PickupLocation.ALAMEDA_DRH;
            } else if (userCampus.equals("tagus")) {
                return PickupLocation.TAGUS_DRH;
            } else if (userCampus.equals("itn")) {
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

    public String getCampus() {
        return userInfoService.getCampus(user);
    }

    public User getUser() {
        return user;
    }
}
