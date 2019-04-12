package org.fenixedu.idcards.domain;

import java.awt.image.BufferedImage;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.service.IUserInfoService;
import org.fenixedu.santandersdk.dto.CreateRegisterRequest;
import org.fenixedu.santandersdk.dto.RegisterAction;

public class SantanderUser {
    private User user;
    private IUserInfoService userInfoService;

    public SantanderUser(User user, IUserInfoService userInfoService) {
        this.user = user;
        this.userInfoService = userInfoService;
    }

    public CreateRegisterRequest toCreateRegisterRequest(RegisterAction action) {
        CreateRegisterRequest createRegisterRequest = new CreateRegisterRequest();

        createRegisterRequest.setRoles(getRoles());
        createRegisterRequest.setPhoto(getPhoto());
        createRegisterRequest.setName(user.getDisplayName());
        createRegisterRequest.setDepartmentAcronym(getDepartmentAcronym());
        createRegisterRequest.setCampus(getCampus());
        createRegisterRequest.setUsername(user.getUsername());
        createRegisterRequest.setAction(action);

        return createRegisterRequest;
    }

    public List<String> getRoles() {
        return userInfoService.getUserRoles(user);
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
