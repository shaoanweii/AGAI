package com.voc.service.security.impl.token;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.ITokenService;
import com.voc.service.security.config.JwtService;
import com.voc.service.security.model.TokenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
/*@SofaService(
        interfaceType = ITokenService.class,
        bindings = {@SofaServiceBinding(bindingType = "bolt")})*/
public class TokenRepository implements ITokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenRepository.class);
    //    @Autowired
//    JwtService jwtService;
//    private static final String USER_SH = "tokens:";
//    private static final String TOKENS_SH = "{}:users:{}:*"; //auth:token:voc:base_admin12
    private static final String TOKEN_SH = "auth:{}:users:{}:{}"; //auth:token:voc:base_admin12
    private static final String USER_SH = "auth:{}:users:{}"; //auth:token:voc:base_admin12
//    private static final String DEL_TOKEN_SH = "{}:users:{}"; //auth:token:voc:base_admin12
    //    @Autowired
//    RedisTemplate redisTemplate;
    @Autowired
    JwtService jwtService;
    @CreateCache(area = "VDP",name = ":", expire = 60 * 24 * 30, cacheType = CacheType.REMOTE, timeUnit = TimeUnit.MINUTES)
    private Cache<String, TokenModel> userCache;
//public interface TokenRepository extends JpaRepository<Token, Integer> {

    /* @Query(value = """
         select t from Token t inner join User u\s
         on t.user.id = u.id\s
         where u.id = :id and (t.expired = false or t.revoked = false)\s
         """)
     List<Token> findAllValidTokenByUser(Integer id);*/
    /*@Autowired
    private ObjectMapper objectMapper;*/

    @Override
    public Optional<TokenModel> findByToken(UserModel user) {
        Assert.notNull(user.getUserId(), "user_id cannot be empty");

        final String key = this.getKey(user);
//        Optional<Object> obj = Optional.ofNullable(redisTemplate.opsForValue().get(key));
        Optional<Object> obj = Optional.ofNullable(userCache.get(key));
        logger.debug("获取用户缓存 {}", key);
        logger.debug("obj {}", obj);

        if (obj.isPresent()) {
            return Optional.of((TokenModel) obj.get());
        }
        return Optional.empty();
    }

    public void save(UserModel user, LocalDateTime expirationTime) {
        Assert.notNull(user.getUsername(), "username cannot be empty");
        Assert.notNull(user.getToken(), "token cannot be empty");
        final String key = this.getKey(user);
//        final String userKey = this.getUserKey(user);

//        if (redisTemplate.hasKey(key)) {
//            redisTemplate.delete(key);
//        }
        final String tokenMd5 = MD5.create().digestHex(user.getToken());
        /*final String tokens = StrUtil.format(TOKENS_SH, user.getAppId(),user.getUserId());
        final Set keys1 = redisTemplate.keys(tokens);
        if(ObjectUtils.isNotEmpty(keys1)){
//            redisTemplate.delete(keys1);
            userCache.REMOVE()
        }*/
//        userCache.REMOVE(key);

        user.setTokenKey(tokenMd5);
//        user.setBizAuthRoles(null);
//        user.setSysAuthRoles(null);
        user.setPassword(null);
        user.setLoginTime(LocalDateTime.now());
        try {
            userCache.remove(key);
            userCache.put(key, TokenModel.builder()
                    .createTime(user.getLoginTime())
                    .expirationTime(expirationTime)
                    .token(user.getToken())
                    .tid(ServiceContextHolder.traceId())
                    .expired(false)
                    .revoked(false)
                    .user(user)
                    .build());
            logger.debug("存入用户缓存 {}", key);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    private String getKey(UserModel user) {
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "userid cannot be empty");
//        return StrUtil.format(TOKEN_SH, user.getAppId(),user.getUserId(), MD5.create().digestHex(user.getToken()));
        return StrUtil.format(TOKEN_SH, user.getAppId(),user.getUserId(), MD5.create().digestHex(user.getUserId()));
    }

    private String getUserKey(UserModel user) {
//        Assert.isTrue(StrUtil.isNotBlank(user.getType()), "type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
//        Assert.isTrue(StrUtil.isNotBlank(user.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "userid cannot be empty");
//        return StrUtil.format(TOKEN_SH, user.getAppId(), user.getUserId());
        return StrUtil.format(USER_SH, user.getAppId(),user.getUserId());
        //{}:users:{}:token_{}
//        return StrUtil.format(TOKEN_SH, user.getAppId(), user.getType(),user.getUsername());
    }

   /* private String getDelKey(UserModel user) {
//        Assert.isTrue(StrUtil.isNotBlank(user.getType()), "type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getAppId()), "appId cannot be empty");
//        Assert.isTrue(StrUtil.isNotBlank(user.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(user.getUserId()), "userid cannot be empty");
//        return StrUtil.format(DEL_TOKEN_SH, user.getAppId(), user.getUserId());
        return StrUtil.format(TOKEN_SH, user.getAppId(),user.getUserId(), MD5.create().digestHex(user.getToken()));
    }*/

    public void delete(UserModel user) {
        Optional<TokenModel> tokenObj = this.findByToken(user);
        if (tokenObj.isPresent()) {
            final String key = this.getKey(user);
            /*if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
            }*/
            userCache.remove(key);
//            Set<String> keys = redisTemplate.keys(key.concat("*"));
//            if (!keys.isEmpty()) {
//                redisTemplate.delete(keys);
//            }
            logger.debug("删除用户缓存 {}", key);
        }
    }
}
