package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;
import com.voc.service.security.model.TokenModel;

import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ITotkenService
 * @Description ckcui
 * @createTime 2023年11月24日 18:50
 * @Copyright futong
 */
public interface ITokenService {

    Optional<TokenModel> findByToken(UserModel user);

}
