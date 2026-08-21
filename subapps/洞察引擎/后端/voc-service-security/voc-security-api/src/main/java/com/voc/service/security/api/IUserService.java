package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;

import java.util.Optional;

public interface IUserService {
    Optional<UserModel> readPermissions(UserModel user);

    boolean sessionTimeout(String token);

    boolean generateSession(String token);

    boolean removeSession(String token);
}
