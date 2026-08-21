package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.model.AuthenticationResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface ILoginService {

    Result<AuthenticationResponse> login(@RequestBody UserModel login);
}
