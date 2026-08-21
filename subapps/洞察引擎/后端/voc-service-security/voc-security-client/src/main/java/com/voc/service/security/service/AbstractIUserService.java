package com.voc.service.security.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.components.redis.util.RedisUtil;
import com.voc.service.security.api.IUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AbstractIUserService
 * @createTime 2024年01月30日 15:27
 * @Copyright futong
 */
public abstract class AbstractIUserService implements IUserService {
    //    {}:users:{}:token_{}
    private static final String BIZ_DATA_SH = "{}:users:{}:tokens:{}:perms:biz"; //auth:token:voc:base_admin12
    //    private static final String SYS_DATA_SH = "{}:users:{}:tokens:{}:perms:sys"; //auth:token:voc:base_admin12
    private static final String SYS_DATA_SH = "{}:users:{}:tokens:{}:perms:sys"; //auth:token:voc:base_admin12
    //    private static final String ACC_DATA_SH = "{}:users:{}:tokens:{}:perms:acc"; //auth:token:voc:base_admin12
    private static final String ACC_DATA_SH = "{}:users:{}:tokens:{}:perms:acc"; //auth:token:voc:base_admin12
    private static final Logger log = LoggerFactory.getLogger(AbstractIUserService.class);

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @CreateCache(area = "VDP", name = ":", expire = 60 * 24, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, Object> sysBizCache;

    @CreateCache(area = "VDP", name = ":",  expire = 60 * 24, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, Object> sysPermsCache;

    @CreateCache(area = "VDP", name = ":",  expire = 60 * 24, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, Object> accPermsCache;

    @Override
    public Optional<UserModel> readPermissions(UserModel user) {

        //redis 缓存 getRoleAuthKey
        log.trace("调用默认实现 userId:{}, {}", user.getUserId(), DefaultUserService.class.getSimpleName());
        final String sysKey = this.getSysAuthKey(user);
        log.trace("readPermissions userId:{} 获取用户系统权限-开始 sysKey={}", user.getUserId(), sysKey);
        //读取系统权限 -- 菜单+按钮
        this.setSystemPermissions(user);
        log.trace("setSystemPermissions-done userId:{},size:{}", user.getUserId(), user.getSystemPermissions().getValues().size());
        //读取业务权限 -- 数据权限
        this.setBusinessPermissions(user);
        log.trace("setBusinessPermissions-done userId:{},size:{}", user.getUserId(), user.getBusinessPermissions().getValues().size());
        //读取访问权限 -- API访问权即模块访问权限
        this.setAccessPermissions(user);
        log.trace("setAccessPermissions-done userId:{},size:{}", user.getUserId(), user.getAccessPermissions().getValues().size());
        log.trace("readPermissions userId{} 获取用户系统权限-结束 sysKey={}", user.getUserId(), sysKey);
        return Optional.of(user);
    }

    /**
     * 读取用户的业务权限
     * @param user 用户模型
     * @return 业务权限模型的Optional对象
     */
    public abstract Optional<PermissionModel> readBusinessPermissions(UserModel user);

    /**
     * 读取用户的系统权限
     * @param user 用户模型
     * @return 系统权限模型的Optional对象
     */
    public abstract Optional<PermissionModel> readSystemPermissions(UserModel user);

    /**
     * 读取用户的访问权限
     * @param user 用户模型
     * @return 访问权限模型的Optional对象
     */
    public abstract Optional<PermissionModel> readAccessPermissions(UserModel user);
    //    @Cached(name = "users:", key = "#user.userId+':tokens':#user.tokenKey+'perms:biz'", expire = 24 * 60, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE )
    public String setBusinessPermissions(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");

        try {
            PermissionModel authorities = PermissionModel.builder().build();
            final String key = this.getBizAuthKey(user);
            final String cacheKey = key + "perms:biz";
            if (ObjectUtils.isNotEmpty(sysBizCache.get(cacheKey))) {
                log.debug("从缓存中读取用户业务数据");
                authorities = (PermissionModel) sysBizCache.get(cacheKey);
                user.setBusinessPermissions(authorities);
                return key;
            }

            log.debug("key {}", key);
            //读取数据库
            Optional<PermissionModel> perms = readBusinessPermissions(user);
            if (perms.isPresent() && !perms.get().isEmpty()) {
                authorities = perms.get();
            } else {
                log.debug("读取数据库权限数据为空");
            }
            user.setBusinessPermissions(authorities);
            sysBizCache.put(cacheKey, authorities);
            return key;

        } catch (Exception e) {
            log.error("设置用户权限异常");
            log.error(e.getMessage(), e);
        }
        return null;
    }

    //    @Cached(name = "userPerms:", key = "#user.userId+':tokens':#user.tokenKey+'perms:sys'", expire = 24 * 60, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE )
    public String setSystemPermissions(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");

        try {
            PermissionModel authorities = PermissionModel.builder().build();
            final String key = this.getSysAuthKey(user);
            final String cacheKey = key + "perms:sys";
            if (ObjectUtils.isNotEmpty(sysPermsCache.get(cacheKey))) {
                log.debug("从缓存中读取用户系统权限数据");
                authorities = (PermissionModel) sysPermsCache.get(cacheKey);
                user.setSystemPermissions(authorities);
                return key;
            }
            Optional<PermissionModel> perms = readSystemPermissions(user);
            if (perms.isPresent() && !perms.get().isEmpty()) {
                authorities = perms.get();
            }
            user.setSystemPermissions(authorities);
            sysPermsCache.put(cacheKey, authorities);
            return key;

        } catch (Exception e) {
            log.error("设置用权限异常");
            log.error(e.getMessage(), e);
        }
        return null;
    }

    //    @Cached(name = "users:", key = "#user.userId+':tokens':#user.tokenKey+'perms:acc'", expire = 24 * 60, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE )
    public String setAccessPermissions(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");

        try {
            PermissionModel authorities = PermissionModel.builder().build();
            final String key = this.getAccAuthKey(user);
            final String cacheKey = key + "perms:acc";
            if (ObjectUtils.isNotEmpty(accPermsCache.get(cacheKey))) {
                log.debug("从缓存中读取用户API访问权限数据");
                authorities = (PermissionModel) accPermsCache.get(cacheKey);
                user.setAccessPermissions(authorities);
                return key;
            }
            //读取数据库
            Optional<PermissionModel> perms = readAccessPermissions(user);
            if (perms.isPresent() && !perms.get().isEmpty()) {
                authorities = perms.get();
            }
            if (ObjectUtil.isNull(authorities)) {
                log.debug("权限数据为空");
                authorities = PermissionModel.builder().build();
            }
            user.setAccessPermissions(authorities);
            accPermsCache.put(cacheKey, authorities);
            return key;

        } catch (Exception e) {
            log.error("设置用权限异常");
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private String getBizAuthKey(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "getUserId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");
        return StrUtil.format(BIZ_DATA_SH, user.getAppName(), user.getUserId(), user.getTokenKey());
    }

    private String getSysAuthKey(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "getUserId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");
//        return StrUtil.format(SYS_DATA_SH, user.getAppId(),user.getUserId(), user.getTokenKey());
        return StrUtil.format(SYS_DATA_SH,user.getAppName(), user.getUserId(), user.getTokenKey());
    }

    private String getAccAuthKey(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "getUserId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getTokenKey()), "token cannot be empty");
//        return StrUtil.format(ACC_DATA_SH, user.getAppId(),user.getUserId(),user.getTokenKey());
        return StrUtil.format(ACC_DATA_SH, user.getAppName(),user.getUserId(), user.getTokenKey());
    }

    @Override
    public boolean sessionTimeout(String token) {
        //白名单跳过
        return false;
    }

    @Override
    public boolean generateSession(String token) {
        //白名单跳过
        return true;
    }

    @Override
    public boolean removeSession(String token) {
        return true;
    }
}
