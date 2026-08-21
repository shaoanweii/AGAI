package com.voc.service.security.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.voc.service.common.constant.GlobalConstants;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.model.UserModel;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.security.api.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
/*@SofaService(
        interfaceType = IJwtService.class,
        bindings = {@SofaServiceBinding(bindingType = "bolt")})*/
public class JwtService implements IJwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    @Value("${application.security.jwt.secret-key:405E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;
    @Value("${application.security.jwt.expiration:7200}")   //默认30天  （小时/单位）
    @Getter
    private long jwtExpiration = GlobalConstants.TOKEN_EXPIRATION;
    @Value("${application.security.jwt.refresh-token.expiration:7200}")  //默认30天（小时/单位）
    private long refreshExpiration = GlobalConstants.TOKEN_EXPIRATION;

    @Override
    public Map<String, String> extractClaim(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("不是标准 JWT");

            if(!JWTUtil.verify(token, getSignInKey().getEncoded() )) throw new IllegalArgumentException("不是标准 JWT");

            final Claims claims = extractAllClaims(token);

            final Map<String, String> map = claims.keySet().stream()
                    .filter(key -> ObjectUtil.isNotNull(claims.get(key)))
                    .collect(Collectors.toMap(key -> key, key -> String.valueOf(claims.get(key)), (key1, key2) -> key2));
            final String attrsStr = map.get(IJwtService.ATTRIBUTES);
            ConcurrentHashMap<String, String> rsMap = MapUtil.newConcurrentHashMap(map);

            if (StrUtil.isNotEmpty(attrsStr)) {
                final Map<String, String> attrsMap = JSONUtil.toBean(PBEStringEncryptor.getInstance().decrypt(attrsStr), Map.class);
                ConcurrentHashMap<String, String> finalMap = MapUtil.newConcurrentHashMap(map);
                finalMap.remove(IJwtService.ATTRIBUTES);
                finalMap.putAll(attrsMap);
                return finalMap;
            }

            final String username = map.get(IJwtService.USERNAME);
            if (StrUtil.isNotEmpty(username)) {
                rsMap.remove(IJwtService.USERNAME);
                rsMap.put(IJwtService.USERNAME, PBEStringEncryptor.getInstance().decrypt(username));
            }
            return map;
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            log.error("token 过期 {}", token);
            throw new SecurityException(CommonErrorEnum.LOGIN_EXPERD_EXECPTION);
        } catch (Exception e) {
            log.error("jwt 解析失败：{}", e.getMessage());

        }

        return Collections.EMPTY_MAP;
    }

    public String generateToken(UserModel user) {
        return this.generateToken(user, jwtExpiration);
    }

    /**
     * expirationDate 小时/单位
     *
     * @param user
     * @param expirationDate
     * @return
     */
    public String generateToken(UserModel user, LocalDateTime expirationDate) {
        //用户账号过期时间，例如 2024-01-04 00:00
        return this.generateToken(user, Duration.between(LocalDateTime.now(), expirationDate).toHours());
    }

    public String generateToken(UserModel user, long expiration) {
        Assert.isTrue(StrUtil.isNotEmpty(user.getUserId()), "user id cannot be empty");
        Assert.isTrue(StrUtil.isNotEmpty(user.getAppId()), "app id cannot be empty");
        Assert.isTrue(StrUtil.isNotEmpty(user.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotEmpty(user.getType()), "identity type cannot be empty");

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(IJwtService.USER_ID, user.getUserId());
        extraClaims.put(IJwtService.APP_ID, user.getAppId());
        extraClaims.put(IJwtService.USERNAME, PBEStringEncryptor.getInstance().encrypt(user.getUsername()));
        extraClaims.put(IJwtService.IDENTITY_TYPE, user.getType());
        /*extraClaims.put(IJwtService.ATTRIBUTES,
                PBEStringEncryptor.getInstance().encrypt(JSONUtil.toJsonStr(Map.of(
                "phone", StrUtil.isNotEmpty(user.getPhone()) ? user.getPhone() : ""
                ,"name",StrUtil.isNotEmpty(user.getFirstname()) ? user.getFirstname() : ""
                ,"email",StrUtil.isNotEmpty(user.getEmail()) ? user.getEmail() : ""
                ))));*/

        return generateToken(extraClaims, user, expiration);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserModel user, long jwtExpiration
    ) {
        return buildToken(extraClaims, user, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserModel user,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUserId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 100 * 60 * 60))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserModel userModel) {
        Map<String, String> map = this.extractClaim(token);
        final String username = map.get(IJwtService.USERNAME);
        return (username.equals(userModel.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public Boolean checkToken(UserModel userModel) {
        Assert.hasLength(userModel.getTokenKey(), "token cannot be empty");
        final String token = userModel.getTokenKey();
        this.extractClaim(token);
        return !isTokenExpired(token);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
//        .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
