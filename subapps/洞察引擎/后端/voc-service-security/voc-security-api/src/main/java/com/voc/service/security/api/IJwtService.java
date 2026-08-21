package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;

import java.util.Map;

public interface IJwtService {
    static final String USERNAME = "username";
    static final String USER_ID = "user_id";
    static final String APP_ID = "app_id";
    static final String IDENTITY_TYPE = "identity_type";
    static final String ATTRIBUTES = "attrs";

    Map<String,String> extractClaim(String token);

    boolean isTokenValid(String token, UserModel userModel);


    Boolean checkToken(UserModel userModel);
}
