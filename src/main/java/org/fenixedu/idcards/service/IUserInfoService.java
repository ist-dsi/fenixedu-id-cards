package org.fenixedu.idcards.service;

import org.fenixedu.bennu.core.domain.User;

import java.util.List;

public interface IUserInfoService {

    List<String> getUserRoles(User user);
    String getUserPhoto(User user);
    String getUserDepartmentAcronym(User user);
    String getCampus(User user);
}
