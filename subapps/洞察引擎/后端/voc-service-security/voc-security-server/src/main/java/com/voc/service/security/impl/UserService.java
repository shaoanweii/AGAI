package com.voc.service.security.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.*;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.security.api.*;
import com.voc.service.security.config.JwtService;
import com.voc.service.security.config.WhiteListServerProperties;
import com.voc.service.security.crypto.PasswordUtil;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.CredentialsEntity;
import com.voc.service.security.impl.entity.UserEntity;
import com.voc.service.security.impl.mapper.UserMapper;
import com.voc.service.security.impl.token.TokenRepository;
import com.voc.service.security.model.*;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
/*@SofaService(
        interfaceType = ISecurityService.class,
        bindings = {@SofaServiceBinding(bindingType = "bolt")})*/
//@CacheConfig(cacheNames = "accountsCache", keyGenerator = "keyGenerator")
public class UserService extends ServiceImpl<UserMapper, UserEntity> implements ISecurityService, UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    SecurityConverMapperService securityConverMapperService;
    @Autowired
    ICredentialsService credentialsService;
    @Autowired
    WhiteListServerProperties whiteListServerProperties;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    IAppService appService;
    @Autowired
    JwtService jwtService;
    @Autowired
    TokenRepository tokenService;
    @Autowired
    IPassService passService;
    @Autowired
    IUserChangeRecordService userChangeRecordService;

    @Override
    public List<AccountModel> accouns() {
        final String userId = ServiceContextHolder.getUserId();
        final String appId = ServiceContextHolder.getAppId();
        logger.trace("userId {}", userId);
        logger.trace("appId {}", appId);

        List<CredentialsModel> list = credentialsService.findByUserId(userId, appId);
        if (CollUtil.isNotEmpty(list)) {
            return list.stream().map(model -> {
                return AccountModel.builder()
                        .id(model.getId())
                        .userId(model.getUserId())
                        .identifier(model.getIdentifier())
                        .identityType(model.getIdentityType())
                        .appId(model.getAppId())
                        .username(model.getIdentifier())
                        .password(model.getCredential())
                        .build();
            }).collect(Collectors.toList());
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            final String appId = ServiceContextHolder.getAppId();
            final String identityType = ServiceContextHolder.getIdentityType();
            Assert.notNull(appId, "app_id cannot be empty");
            Assert.notNull(identityType, "identity_type cannot be empty");

            final CredentialsModel paramModel = CredentialsModel.builder()
                    .appId(appId)
                    .identifier(username)
                    .identityType(identityType)
                    .build();

            logger.trace("paramModel {}", paramModel);

            //appId 有效性检验、
            appService.find(AppModel.builder().appId(appId).build());

            //用户信息 + 授权信息
            return this.findByUsername(paramModel);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException("查询用户失败！");
        }
    }

    public UserDetails loadUserByMobile(String username) throws UsernameNotFoundException {
        try {
            UserDetails user = this.loadUserByUsername(username);
            UserEntity userEntity = (UserEntity) user;
            //获取短信吗
            final String code = "5566";
            userEntity.setPassword(code);
            //用户信息 + 授权信息
            return user;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException("查询用户失败！");
        }
    }


    public UserDetails loadUserByFree(String userId) throws UsernameNotFoundException {
        try {
            logger.info("ServiceContextHolder.getAppId():{}", ServiceContextHolder.getAppId());
            final String appId = StrUtil.isBlank(ServiceContextHolder.getAppId())?"report-cqca":ServiceContextHolder.getAppId();
            Assert.notNull(appId, "app_id cannot be empty");
            Assert.notNull(userId, "user_id cannot be empty");
            logger.info("从上下文中获取的appId:{}",appId);

            logger.info("从rucanuserId:{}",userId);


            final CredentialsModel paramModel = CredentialsModel.builder()
                    .appId(appId)
                    .userId(userId)
                    .build();

            logger.trace("paramModel {}", paramModel);
            logger.info("appId {}", appId);
            //appId 有效性检验、
            appService.find(AppModel.builder().appId(appId).build());

            //用户信息 + 授权信息
            return this.findByUser(paramModel);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException("查询用户失败！");
        }
    }


    public UserDetails loadUser(String userId,String appId) throws UsernameNotFoundException {
        try {
            logger.info("入参中获取appId:{}", appId);
            final String appid = StrUtil.isBlank(appId)?"report-cqca":appId;
            Assert.notNull(appId, "app_id cannot be empty");
            Assert.notNull(userId, "user_id cannot be empty");
            logger.info("从上下文中获取的appId:{}",appid);

            logger.info("从入参中获取Id:{}",userId);


            final CredentialsModel paramModel = CredentialsModel.builder()
                    .appId(appid)
                    .userId(userId)
                    .build();

            logger.trace("paramModel {}", paramModel);
            logger.info("appId {}", appId);
            //appId 有效性检验、
            appService.find(AppModel.builder().appId(appid).build());

            //用户信息 + 授权信息
            return this.findByUser(paramModel);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new UsernameNotFoundException("查询用户失败！");
        }
    }

    @Transactional(readOnly = true)
    public UserEntity findByUser(final CredentialsModel param) throws UsernameNotFoundException {
        Assert.notNull(param.getAppId(), "app_id cannot be empty");
        Assert.notNull(param.getUserId(), "user_id cannot be empty");

        final UserEntity rs = baseMapper.selectByUserId(param);
        if (ObjectUtil.isNull(rs)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_PASSWORD_NULL);
        }
        if (StrUtil.isBlank(rs.getId())) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
        if(!rs.isEnabled()){
            throw new BussinessException(CommonErrorEnum.ACCOUNT_DISABLE);
        }

        return rs;
    }

    /*@Cacheable(
            value = "accountsCache"
            , key = "T(String).valueOf(#param.getAppId()).concat(':accounts:').concat( #param.getIdentifier()).concat('_').concat( #param.getIdentityType())"
            , unless = "#result == null"
    )*/
    @Transactional(readOnly = true)
    public UserEntity findByUsername(final CredentialsModel param) throws UsernameNotFoundException {
        Assert.notNull(param.getAppId(), "app_id cannot be empty");
        Assert.notNull(param.getIdentityType(), "identity cannot be empty");
        Assert.notNull(param.getIdentifier(), "identity_type cannot be empty");

        UserEntity rs = baseMapper.selectByIdentifier(param);
        if (ObjectUtil.isNull(rs)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_PASSWORD_NULL);
        }
        rs.setPassword(PBEStringEncryptor.getInstance().decrypt(rs.getPassword()));

        if (StrUtil.isBlank(rs.getUsername())) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
//        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(rs));
        return rs;
    }

    @Override
    @Transactional
    public boolean register(final UserModel userModel) throws SecurityException {
        try {
            Assert.notNull(userModel.getAppId(), "app_id cannot be empty");
            Assert.notNull(userModel.getType(), "type cannot be empty");

            return this.addUser(userModel);
        } catch (AccountException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BussinessException(e);
        }
    }


    @Override
    public UserModel userinfo(final String token) {
        try {
            logger.info("call.userinfo");
            final Map<String, String> map = jwtService.extractClaim(token);

            final String username = map.get(IJwtService.USERNAME);
            final String userId = map.get(IJwtService.USER_ID);
            final String appid = map.get(IJwtService.APP_ID);
            final String identityType = map.get(IJwtService.IDENTITY_TYPE);
            logger.trace("username {}, appid {}, identityType {}", username, appid, identityType);

            if(whiteListServerProperties.getTokens().contains(token)){
                return UserModel.builder().userId(userId).username(username).appId(appid).type(identityType)
                        .tokenKey(MD5.create().digestHex(token)).build();
            }
            final Optional<TokenModel> tokenModel = tokenService.findByToken(UserModel.builder()
                    .username(username)
                    .userId(userId)
                    .appId(appid)
                    .token(token)
                    .type(identityType)
                    .build());

            if (tokenModel.isPresent()) {
                logger.debug("OK. {}", tokenModel.get());
                logger.info("get userinfo ok.");
                UserModel user = tokenModel.get().getUser();
//                tokenModel.get().getUser().setPassword(PBEStringEncryptor.getInstance().decrypt(tokenModel.get().getUser().getPassword()));
                user.setPassword(null);
                user.setToken(tokenModel.get().getToken());

                return user;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ExpiredJwtException(e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public boolean unlock(final UserModel user) {
        //判断app集合中是否合法，并有当前用户信息
//        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "user_id cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUsername()), "user_name cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();

        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
//        wrapperQuery.lambda().eq(UserEntity::getId, user.getUserId());
        wrapperQuery.lambda().eq(UserEntity::getUsername, user.getUsername());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }

        //操作人
        final String userId = ServiceContextHolder.getUserId();

        //同时锁定账号
        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(UserEntity::getId, findEntity.getUserId());
        wrapper.lambda().eq(UserEntity::isNonLocked, false);
        wrapper.set("non_locked", true);
        wrapper.set("update_time", updateTime);
        wrapper.set("operator", ObjectUtils.isNotEmpty(userId)?userId:"-1");
        this.update(wrapper);

        credentialsService.unlock(CredentialsModel.builder()
//                .appId(findEntity.getAppId())
                .userId(findEntity.getId()).updateTime(updateTime).operator(ObjectUtils.isNotEmpty(userId)?userId:"-1").build());
        //缓存
//        final String key = findEntity.getAppId().concat(":accounts");
//        redisTemplate.delete(key);
        return true;
    }


    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    @Transactional
    public boolean lock(final UserModel user) {
        //判断app集合中是否合法，并有当前用户信息
//        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "user_id cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUsername()), "user_name cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();

        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
//        wrapperQuery.lambda().eq(UserEntity::getId, user.getUserId());
        wrapperQuery.lambda().eq(UserEntity::getUsername, user.getUsername());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }

        //操作人
        final String userId = ServiceContextHolder.getUserId();

        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(UserEntity::getId, findEntity.getUserId());
        wrapper.lambda().eq(UserEntity::isNonLocked, true);
        wrapper.set("non_locked", false);
        wrapper.set("update_time", updateTime);
        wrapper.set("operator", ObjectUtils.isNotEmpty(userId)?userId:"-1");
        this.update(wrapper);

        credentialsService.lock(CredentialsModel.builder()
//                .appId(findEntity.getAppId())
                .userId(findEntity.getId())
                .updateTime(updateTime).operator(ObjectUtils.isNotEmpty(userId)?userId:"-1").build());
        //缓存
//        this.deleteAccountCache(findEntity.getAppId());
        return true;
    }

    /**
     * 删除账号缓存
     */
    private void deleteAccountCache(@NonNull final String appId) {
        final String key = appId.concat(":accounts:*");
        final Set keys = redisTemplate.keys(key);
        if (CollUtil.isNotEmpty(keys)) {
//            redisTemplate.delete(keys);
        }
    }

    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    @Transactional
    public boolean enable(final UserModel user) {
        //判断app集合中是否合法，并有当前用户信息
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "user_id cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();
        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
        wrapperQuery.lambda().eq(UserEntity::getId, user.getUserId());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }

        //操作人
        final String userId = ServiceContextHolder.getUserId();

        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(UserEntity::getId, user.getUserId());
        wrapper.lambda().eq(UserEntity::isEnabled, false);
        wrapper.set("enabled", true);
        wrapper.set("update_time", updateTime);
        wrapper.set("operator", userId);
        this.update(wrapper);

        credentialsService.enable(CredentialsModel.builder()
//                .appId(findEntity.getAppId())
                .userId(user.getUserId()).updateTime(updateTime).operator(userId).build());
        //缓存
//        this.deleteAccountCache(findEntity.getAppId());

        return true;
    }

    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    public boolean disable(final UserModel user) {
        //判断app集合中是否合法，并有当前用户信息
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "user_id cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();
        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
        wrapperQuery.lambda().eq(UserEntity::getId, user.getUserId());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }

        //操作人
        final String userId = ServiceContextHolder.getUserId();

        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(UserEntity::getId, user.getUserId());
        wrapper.lambda().eq(UserEntity::isEnabled, true);
        wrapper.set("enabled", false);
        wrapper.set("update_time", updateTime);
        wrapper.set("operator", userId);
        this.update(wrapper);

        credentialsService.disable(CredentialsModel.builder()
//                .appId(findEntity.getAppId())
                .userId(user.getUserId()).updateTime(updateTime).operator(userId).build());
        //缓存
//        this.deleteAccountCache(findEntity.getAppId());

        return true;
    }

    /**
     * 根据userId 判断当前用户，还是制定用户
     */
    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    public boolean resetPassword(final ChangePasswordRequest changePwd) throws AccountNotFoundException {
        Assert.notNull(changePwd.getType(), "type cannot be empty");
        Assert.notNull(changePwd.getUserId(), "user_id cannot be empty");
        Assert.notNull(changePwd.getAppId(), "app_id cannot be empty");
//        Assert.notNull(changePwd.getCurrentPassword(), "current password cannot be empty");
        Assert.notNull(changePwd.getNewPassword(), "new password cannot be empty");

        // check if the current password is correct
//        if (changePwd.getCurrentPassword().equals(changePwd.getNewPassword())) {
//            throw new BussinessException("Wrong password");
//        }
        // check if the two new passwords are the same
        if (!changePwd.getNewPassword().equals(changePwd.getConfirmationPassword())) {
            throw new BussinessException("Password are not the same");
        }

        final CredentialsModel find = credentialsService.find(CredentialsModel.builder()
                .identityType(changePwd.getType())
                .userId(changePwd.getUserId())
                .appId(changePwd.getAppId())
                .build()
        ).orElseThrow(() -> new AccountNotFoundException(changePwd.getUserId()));
        // 密码校验
//        if (!PBEStringEncryptor.getInstance().decrypt(find.getCredential()).equals(changePwd.getCurrentPassword())) {
//            throw new BussinessException(CommonErrorEnum.LOGIN_PASSWORD_EXECPTION);
//        }

        //操作人
        final String userId = ServiceContextHolder.getUserId();

        credentialsService.changePassword(CredentialsModel.builder()
                .id(find.getId())
                .userId(find.getUserId())
                .updateTime(LocalDateTime.now())
                .operator(userId)
                .credential(PBEStringEncryptor.getInstance().encrypt(changePwd.getNewPassword()))
                .build());

        //缓存
        this.deleteAccountCache(find.getAppId());

        return true;
    }

    @Override
    public boolean valiedatePassword(final ValidatePasswordRequest valiedatePwd) {
        Assert.notNull(valiedatePwd.getType(), "type cannot be empty");
        Assert.notNull(valiedatePwd.getUserId(), "user_id cannot be empty");
        Assert.notNull(valiedatePwd.getAppId(), "app_id cannot be empty");
        Assert.notNull(valiedatePwd.getCurrentPassword(), "current password cannot be empty");

        //密码策略验证
        final List<String> messages = passService.passwordValidator(valiedatePwd.getCurrentPassword());
        if (CollUtil.isNotEmpty(messages)) {
            throw new BussinessException(String.join(",", messages));
        }

        return true;
    }


    @Override
    @Transactional
    public boolean addUser(final UserModel request) {
        Assert.notNull(request.getAppId(), "app_id cannot be empty");
        Assert.notNull(request.getType(), "type cannot be empty");

        //认证类型验证
        /*if (!ICredentialsService.IDENTITY_TYPES.contains(request.getType().toLowerCase())) {
            logger.error("login type is {}", request.getType());
            throw new BussinessException(CommonErrorEnum.LOGIN_TYPE_DISABLE);
        }*/
        //appId 有效性检验、
        final String appIds = request.getAppId();
        StrUtil.split(appIds, ",").stream()
                .filter(StrUtil::isNotBlank).map(appId -> StrUtil.trim(appId)).filter(StrUtil::isNotBlank)
                .forEach(appId -> {
                    appService.find(AppModel.builder().appId(appId).build());
                });

        //        final String appId = request.getAppId();
        final LocalDateTime createDateTime = LocalDateTime.now();
        final String newId = IdWorker.getId();
        final String operator = Optional.ofNullable(ServiceContextHolder.getUserId()).orElse("-1");

        try {
            final String userId;
            final UserEntity is = baseMapper.selectOne(this.matchRule(request));

//        UserEntity is =  null;
            if (ObjectUtil.isNull(is)) {
                //匹配用户信心如果已存在则不创新新数据 ，唯一数据。 例如：员工号、手机号
                final UserEntity entity = securityConverMapperService.converToEntity(request);
//                if(ObjectUtils.isNotEmpty(request.getId())){
//                    entity.setId(request.getId());
//                }else {
//                    entity.setId(newId);
//                }
                entity.setId(newId);
//                entity.setAppId(appId);
                entity.setNonExpired(true);
                entity.setNonLocked(true);
                entity.setEnabled(true);
                entity.setCreateTime(createDateTime);
                entity.setUpdateTime(createDateTime);
                entity.setStartExpireDate(ObjectUtil.isNull(request.getStartExpireDate()) ? createDateTime : request.getStartExpireDate());
                entity.setOperator(operator);
                logger.info("user: {}", entity);
                baseMapper.insert(entity);
                userId = entity.getId();
            } else {
                userId = is.getId();
                logger.info("用户信息已存在 {}", is);
            }

            final String identifier = this.getIdentifier(request);
            final String password = StrUtil.isNotBlank(request.getPassword()) ? request.getPassword() : null;

            LocalDateTime localDateTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

            StrUtil.split(appIds, ",").stream()
                    .filter(StrUtil::isNotBlank).map(appId -> StrUtil.trim(appId)).filter(StrUtil::isNotBlank)
                    .forEach(appId -> {
                        //创建授权信息
                        final CredentialsModel credentialsModel = CredentialsModel.builder()
                                .id(IdWorker.getId())
                                .userId(userId)
                                .appId(appId)
                                .identityType(request.getType())
                                .identifier(identifier)
                                .credential(password)
                                .enabled(true)
                                .nonExpired(true)
                                .nonLocked(true)
                                .createTime(createDateTime)
                                .updateTime(createDateTime)
                                .startExpireDate(ObjectUtil.isNull(request.getStartExpireDate()) ? createDateTime : request.getStartExpireDate())
                                .expireDate(ObjectUtil.isNull(request.getExpireDate()) ? localDateTime : request.getExpireDate())
                                .operator(operator)
                                .admin(ObjectUtils.isNotEmpty(request.getAdmin())&&"1".equalsIgnoreCase(request.getAdmin())?true:false)
                                .build();
                        logger.info("credential: {}", credentialsModel);
                        credentialsService.add(credentialsModel);
                    });
            return true;
        } catch (  AccountException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (
                Exception e) {
            logger.error(e.getMessage(), e);
            throw new BussinessException(CommonErrorEnum.ADD_USER_ERROR);
        }

    }

    public String getIdentifier(UserModel user) {
        return switch (user.getType()) {
            case ICredentialsService.IDENTITY_TYPE_BASE -> user.getUsername();
            case ICredentialsService.IDENTITY_TYPE_PHONE -> user.getPhone();
            case ICredentialsService.IDENTITY_TYPE_PHONE_SMS -> user.getPhone();
            case ICredentialsService.IDENTITY_TYPE_EMAIL -> user.getEmail();
            default -> null;
        };

    }


    private QueryWrapper<UserEntity> matchRule(UserModel request) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        if (ICredentialsService.IDENTITY_TYPE_BASE.equalsIgnoreCase(request.getType())) {
            Assert.isTrue(StrUtil.isNotBlank(request.getUsername()), "username cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(request.getPassword()), "password cannot be empty");
            wrapper.eq("username", request.getUsername());
        } else if (ICredentialsService.IDENTITY_TYPE_PHONE.equalsIgnoreCase(request.getType())) {
            Assert.isTrue(StrUtil.isNotBlank(request.getPhone()), "phone cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(request.getPassword()), "password cannot be empty");
            wrapper.eq("phone", request.getPhone());
        } else if (ICredentialsService.IDENTITY_TYPE_PHONE_SMS.equalsIgnoreCase(request.getType())) {
            Assert.isTrue(StrUtil.isNotBlank(request.getPhone()), "phone cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(request.getSmscode()), "phone sms_code cannot be empty");
            wrapper.eq("phone", request.getPhone());
        } else if (ICredentialsService.IDENTITY_TYPE_EMAIL.equalsIgnoreCase(request.getType())) {
            Assert.isTrue(StrUtil.isNotBlank(request.getEmail()), "email cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(request.getPassword()), "password cannot be empty");
            wrapper.eq("email", request.getEmail());
        } else {
            throw new BussinessException("唯一字段匹配规则异常!");
        }
//        Assert.isTrue(StrUtil.isNotBlank(request.getUsername()), "matchRule 方法中 username cannot be empty");

        return wrapper;
    }

    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    public boolean modifyUser(final UserModel modifyRequest) {
        Assert.notNull(modifyRequest.getUserId(), "user id cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();
        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
        wrapperQuery.lambda().eq(UserEntity::getId, modifyRequest.getUserId());
//        wrapperQuery.lambda().eq(UserEntity::getUsername, modifyRequest.getUserId());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
        //操作人
        final String userId = ServiceContextHolder.getUserId();

        //判断app集合中是否合法，并有当前用户信息
        UserEntity entity = securityConverMapperService.converTo(modifyRequest);
        entity.setUpdateTime(updateTime);
        entity.setOperator(userId);
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username", entity.getUsername());
//        wrapper.eq("enabled", "1");

        //缓存
//        this.deleteAccountCache(findEntity.getAppId());
        int update = baseMapper.update(entity, wrapper);
        if(update > 0){
            CredentialsModel credentialModel = CredentialsModel.builder()
                    .userId(entity.getUserId()).updateTime(updateTime).appId(entity.getAppId()).operator(userId).expireDate(entity.getExpireDate()).build();
            if (entity.isEnabled()) {
                credentialsService.enable(credentialModel);
            } else {
                credentialsService.disable(credentialModel);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean changeUser(UserModel modifyRequest) {
        Assert.notNull(modifyRequest.getClientId(), "clientId cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();
        //操作人
        final String userId = ServiceContextHolder.getUserId();

        List<UserEntity> userEntities = List.of();

        Boolean saveUserChangeRecord = false;
        boolean updated = false;
        List<UserModel> userModels = List.of();
        if(modifyRequest.isEnabled()){
            List<UserModel> userModelList = userChangeRecordService.findLastUserChangeRecordByClientId(modifyRequest.getClientId());
            userEntities = userModelList.stream().map(e->{
                return securityConverMapperService.converTo(e);
            }).collect(Collectors.toList());
        }else {
            //查询客户下的所有用户
            QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
            wrapperQuery.lambda().eq(UserEntity::getClientId, modifyRequest.getClientId());
            userEntities = this.baseMapper.selectList(wrapperQuery);
            if(ObjectUtils.isEmpty(userEntities)){
                logger.warn("当前客户{}下暂无用户信息，不进行任何操作", modifyRequest.getClientId());
                return true;
            }
            //批量新增用户变更记录
            userModels = userEntities.stream().map(e -> {
                return securityConverMapperService.converTo(e);
            }).collect(Collectors.toList());
            saveUserChangeRecord = userChangeRecordService.saveBatchChangeRecord(userModels);
        }

        if(ObjectUtils.isEmpty(userEntities)){
            logger.warn("当前客户尚未停用，不更新用户状态");
            return true;
        }
        //更新用户状态
        userEntities.stream().forEach(e->{
            e.setUpdateTime(updateTime);
            if(modifyRequest.isEnabled()){
                e.setEnabled(e.isEnabled());
            }else {
             e.setEnabled(modifyRequest.isEnabled());
            }
            e.setOperator(userId);
        });
        updated = this.updateBatchById(userEntities);


        if(updated) {
            userEntities.stream().forEach(e->{
                //变更所有账号
                CredentialsModel credentialModel = CredentialsModel.builder().userId(e.getId()).enabled(modifyRequest.isEnabled()).build();
                credentialsService.changeAllCredentials(credentialModel);
            });
        }else {
            logger.error("更新用户状态异常");
            throw new BussinessException(CommonErrorEnum.CONSTRAINTVIOLATION_EXECPTION,"更新用户状态异常");
        }

        return true;
    }


    @Override
//    @CacheEvict(cacheNames = "accountsCache", allEntries = true)
    public boolean removeUser(final UserModel user) {
        Assert.notNull(user.getUserId(), "user id cannot be empty");
//        Assert.notNull(user.getId(), "user id cannot be empty");

        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
        wrapperQuery.lambda().eq(UserEntity::getId, user.getUserId());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
        //操作人
        final String userId = ServiceContextHolder.getUserId();
        user.setOperator(userId);
        //缓存
//        this.deleteAccountCache(findEntity.getAppId());

        return this.disable(user);
    }


    @Override
    public boolean removeTestUsers(UserModel user) {
        UserEntity entity = baseMapper.selectByIdentifier(CredentialsModel.builder()
                .appId(user.getAppId())
                .identityType(user.getType())
                .identifier(user.getUsername())
                .build());
        if (ObjectUtil.isNull(entity) || StrUtil.isBlank(entity.getUserId())) {
            return true;
        }
        //删除用户数据
        baseMapper.removeTestUsers(Set.of(entity.getUserId()));
        //删除账号数据
        credentialsService.removeTestUsers(Set.of(entity.getId()));

        return true;
    }

    @Override
    public List<UserModel> findAll(UserModel userModel) {
        return this.selectUserModel(userModel,null);
    }

    @Override
    public List<UserModel> findByUserId(UserModel userModel) {
        return this.selectUserModel(userModel,null);
//        List<UserEntity> entityList = baseMapper.selectAll(userModel);
//        final Set<String> userIds = entityList.stream().map(UserEntity::getUserId).collect(Collectors.toSet());
//        if (CollUtil.isEmpty(userIds)) {
//            return Collections.EMPTY_LIST;
//        }
//        List<AccountModel> credentials = credentialsService.findByUserIds(userIds, userModel.getAppId());
//        if (CollUtil.isEmpty(credentials)) {
//            return entityList.stream().map(entity ->
//                    securityConverMapperService.converTo(entity)
//            ).collect(Collectors.toList());
//        }
//
//        Map<String, List<AccountModel>> map = new HashMap<>();
//        credentials.stream()
//                .forEach(model -> {
//                    if (map.containsKey(model.getUserId())) {
//                        map.get(model.getUserId()).add(model);
//                    } else {
//                        map.put(model.getUserId(), new ArrayList<>(Arrays.asList(model)));
//                    }
//                });
//
//
//        return entityList.stream()
//                .filter(model -> map.containsKey(model.getUserId()))   //过滤掉不包含账号的数据
//                .map(entity -> {
//                            UserModel model = securityConverMapperService.converTo(entity);
//                            model.setAccounts(map.get(model.getUserId()));
//                            return model;
//                        }
//                )
//                .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> findUserByUserId(UserModel userModel) {
        List<UserEntity> userEntities = baseMapper.selectUserByUserId(userModel);
        if(ObjectUtils.isEmpty(userEntities)){
            return Collections.EMPTY_LIST;
        }
        return userEntities.stream()
                .filter(e -> ObjectUtils.isNotEmpty(e.getCredentials())) //过滤掉不包含账号的数据
                .map(e -> {
                    UserModel model = securityConverMapperService.converTo(e);
                    final List<CredentialsEntity> credentials = e.getCredentials();
                    final List<AccountModel> accountModels = securityConverMapperService.converToAccountList(credentials);
                    model.setAccounts(accountModels);
                    return model;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo findByConditional(UserModel userModel) {
        PageInfo pageInfo = new PageInfo();
        List<UserModel> userModelList = this.selectUserModel(userModel, pageInfo);
        pageInfo.setList(userModelList);
        return pageInfo;
    }

    @Override
    public Boolean modifyClientUser(UserModel modifyRequest) {
        logger.info("更新用户，入参:{}", JSONObject.toJSONString(modifyRequest));
        Assert.notNull(modifyRequest.getUsername(), "user name cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(modifyRequest.getClientId()), "clientId cannot be empty");
        final LocalDateTime updateTime = LocalDateTime.now();
        QueryWrapper<UserEntity> wrapperQuery = new QueryWrapper<>();
//        wrapperQuery.lambda().eq(UserEntity::getId, modifyRequest.getUserId());
        wrapperQuery.lambda().eq(UserEntity::getUsername, modifyRequest.getUsername());
        UserEntity findEntity = this.baseMapper.selectOne(wrapperQuery);
        if (ObjectUtil.isNull(findEntity)) {
            throw new BussinessException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
        //操作人
        final String userId = ServiceContextHolder.getUserId();

        //判断app集合中是否合法，并有当前用户信息
        UserEntity entity = securityConverMapperService.converTo(modifyRequest);
        entity.setUpdateTime(updateTime);
        entity.setOperator(ObjectUtils.isNotEmpty(userId)?userId:"-1");
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username", entity.getUsername());
//        wrapper.eq("enabled", "1");

        //缓存
//        this.deleteAccountCache(findEntity.getAppId());
        int update = baseMapper.update(entity, wrapper);
        logger.info("更新用户信息结束,结果:{}", update);
        if(update > 0){
            LocalDateTime localDateTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
            if(ObjectUtils.isNotEmpty(modifyRequest.getAppId())&&modifyRequest.getAppId().contains(",")){
                logger.info("开始更新多系统账号信息:{}", modifyRequest.getAppId());
                //若存在多个appId，则循环处理，若appid对应的账号不存在，则创建，否则仅更新
                String[] appIds = modifyRequest.getAppId().split(",");
                for (String appId : appIds) {
                    final List<CredentialsModel> byUserId = credentialsService.findByUserId(findEntity.getId(), appId);
                    if(ObjectUtils.isEmpty(byUserId)){
                        //创建账号
                        final CredentialsModel credentialsModel = CredentialsModel.builder()
                                .id(IdWorker.getId())
                                .userId(findEntity.getId())
                                .credential(ObjectUtils.isNotEmpty(modifyRequest.getPassword())?modifyRequest.getPassword():"")
                                .appId(appId)
                                .identityType(modifyRequest.getType())
                                .identifier(findEntity.getUsername())
                                .enabled(true)
                                .nonExpired(true)
                                .nonLocked(true)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .startExpireDate(localDateTime)
                                .expireDate(localDateTime)
                                .operator(findEntity.getOperator())
                                .admin(false)
                                .build();
                        logger.info("新建账号，系统:[{}],credential: [{}]",appId ,credentialsModel);
                        credentialsService.add(credentialsModel);
                    }else{
                        //更新账号
                        CredentialsModel credentialModel = CredentialsModel.builder()
                                .userId(findEntity.getId()).updateTime(updateTime).appId(appId).operator(ObjectUtils.isNotEmpty(userId)?userId:"-1").expireDate(findEntity.getExpireDate()).build();
                        logger.info("更新账号，系统:[{}],credential: [{}]",appId ,credentialModel);
                        if (entity.isEnabled()) {
                            boolean enable = credentialsService.enable(credentialModel);
                            logger.info("userId:[{}]启用结果:{}",credentialModel.getUserId(),enable);
                        } else {
                            boolean disable = credentialsService.disable(credentialModel);
                            logger.info("userId:[{}]停用结果:{}",credentialModel.getUserId(),disable);
                        }
                    }
                }
            }else{
                logger.info("开始更新单系统账号信息:{}", modifyRequest.getAppId());
                //更新账号
                CredentialsModel credentialModel = CredentialsModel.builder()
                        .userId(findEntity.getId()).updateTime(updateTime).appId(entity.getAppId()).operator(ObjectUtils.isNotEmpty(userId)?userId:"-1").expireDate(findEntity.getExpireDate()).build();
                logger.info("更新账号，系统:[{}],credential: [{}]",modifyRequest.getAppId() ,credentialModel);
                if (entity.isEnabled()) {
                    boolean enable = credentialsService.enable(credentialModel);
                    logger.info("userId:[{}]启用结果:{}",credentialModel.getUserId(),enable);
                } else {
                    boolean disable = credentialsService.disable(credentialModel);
                    logger.info("userId:[{}]停用结果:{}",credentialModel.getUserId(),disable);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 16:37
     * @描述  查询用户及账号信息
     * @param userModel  查询条件
     * @param pageInfo  是否分页
     * @return java.util.List<com.voc.service.common.model.UserModel>
     **/
    private List<UserModel> selectUserModel(UserModel userModel,PageInfo pageInfo){
        if(ObjectUtils.isNotEmpty(pageInfo)){
            PageHelper.startPage(userModel.getPageNum(), userModel.getPageSize(),false);
        }
        List<UserEntity> userEntities = baseMapper.selectByConditional(userModel);
        if(ObjectUtils.isEmpty(userEntities)){
            return Collections.EMPTY_LIST;
        }
        if(ObjectUtils.isNotEmpty(pageInfo)){
            PageInfo<UserEntity> pageInfo1 = new PageInfo<>(userEntities);
            Integer i = baseMapper.selectCountByConditional(userModel);
            pageInfo1.setTotal(i);
            BeanUtils.copyProperties(pageInfo1,pageInfo);
        }
        return userEntities.stream()
                .filter(e -> ObjectUtils.isNotEmpty(e.getCredentials())) //过滤掉不包含账号的数据
                .map(e -> {
                    UserModel model = securityConverMapperService.converTo(e);
                    final List<CredentialsEntity> credentials = e.getCredentials();
                    final List<AccountModel> accountModels = securityConverMapperService.converToAccountList(credentials);
                    model.setAccounts(accountModels);
                    return model;
                })
                .collect(Collectors.toList());
    }


    @Override
    public String getTokenByUserId(UserModel user) {
        Assert.hasLength(user.getAppId(), "appId cannot be empty");
        logger.info("入参 user:{}", JSONObject.toJSONString( user));

//        if(ObjectUtils.isEmpty(user.getAppId())){
//            ServiceContextHolder.setAppId("report-cqca");
//        }else{
//            ServiceContextHolder.setAppId(user.getAppId());
//        }
        UserEntity userEntity =(UserEntity)  this.loadUser(user.getUserId(),user.getAppId());
        final UserModel model = securityConverMapperService.converTo(userEntity);
        Optional<TokenModel> byToken = tokenService.findByToken(model);
        if(byToken.isPresent()){
            return  byToken.get().getToken();
        }

        final long expirationHours = this.getExpirationHours(userEntity.getExpireDate());
//        //根据账号有效期生成token过期时间
        final String token = jwtService.generateToken(model, expirationHours);
        logger.trace("token.{}", token);
//
        model.setToken(token);
        //保存用户新token
        tokenService.save(model, LocalDateTime.ofInstant(
                new Date(System.currentTimeMillis() + expirationHours * 1000 * 60 * 60).toInstant(), ZoneId.systemDefault()));
        return token;
    }

    @Override
    public UserModel checkAndGetToken(UserModel user) {
        final String uid = PasswordUtil.decrypt(user.getUserId());
        ServiceContextHolder.setAppId(user.getAppId());
        UserEntity userEntity =(UserEntity)  this.loadUserByFree(uid);
        final UserModel model = securityConverMapperService.converTo(userEntity);
        Optional<TokenModel> byToken = tokenService.findByToken(model);
        if(byToken.isPresent()){
            UserModel models = byToken.get().getUser();
            models.setTokenKey(byToken.get().getToken());
            return  models;
        }

        return null;
    }

    private long getExpirationHours(LocalDateTime expireDate) {
        final long expirationDate;

        final LocalDateTime now = LocalDateTime.now();
        if (now.plusHours(jwtService.getJwtExpiration()).compareTo(expireDate) > 1) {
            expirationDate = ChronoUnit.HOURS.between(now, expireDate);
        } else {
            expirationDate = jwtService.getJwtExpiration();
        }
        return expirationDate;
    }

    /*@Override
    public List<AccountModel> accouns() {
        final String userId = ServiceContextHolder.getUserId();
        logger.trace("userId {}", userId);
        List<CredentialsModel> list = credentialsService.findByUserId(userId);
        if (CollUtil.isNotEmpty(list)) {
            return list.stream().map(model -> {
                return AccountModel.builder()
                        .appId(model.getAppId())
                        .username(model.getIdentifier())
                        .password(model.getCredential())
                        .build();
            }).collect(Collectors.toList());
        }

        return Collections.EMPTY_LIST;
    }*/


}
