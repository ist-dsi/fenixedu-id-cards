package org.fenixedu.idcards.service;

import java.awt.image.BufferedImage;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.idcards.domain.PickupLocation;

public interface IUserInfoService {

    List<String> getUserRoles(User user);
    BufferedImage getUserPhoto(User user);
    String getUserDepartmentAcronym(User user);
    String getCampus(User user);
}
