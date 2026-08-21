package com.voc.service.security.service;

import com.voc.service.security.api.ICustomInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Title: CustomInfoService
 * @Package: com.voc.service.security.service
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/1 10:22
 * @Version:1.0
 */
@Service("defaultCustomInfoService")
public class CustomInfoService implements ICustomInfoService {
    private static final Logger log = LoggerFactory.getLogger(CustomInfoService.class);

    @Override
    public Object getUserInfo() {
        log.info("使用默认服务用户信息接口");
        return null;
    }

    @Override
    public Object getUserPermissions() {
        log.info("使用默认服务权限接口");
        return null;
    }
}
