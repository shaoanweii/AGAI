package com.voc.service.security.service;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.model.auth.PermissionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName UserService
 * @Description ckcui
 * @createTime 2023年10月09日 14:54
 * @Copyright futong
 */
@Service("defaultUserService")
public class DefaultUserService extends AbstractIUserService {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    @Override
    public Optional<PermissionModel> readBusinessPermissions(UserModel user) {
        //
        log.trace("使用默认实现服务 ,{}","readBusinessPermissions");

        return Optional.empty();
    }

    @Override
    public Optional<PermissionModel> readSystemPermissions(UserModel user) {
        log.trace("使用默认实现服务 ,{}","readSystemPermissions");

        return Optional.empty();
    }

    @Override
    public Optional<PermissionModel> readAccessPermissions(UserModel user) {
        log.trace("使用默认实现服务 ,{}","readAccessPermissions");

        return Optional.empty();
    }
}
