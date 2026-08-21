package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;

import java.io.IOException;

public interface IAuthenticationService {

    String createRandomImage(final String key) throws IOException;

    String checkCaptcha(UserModel request);
}
