package com.voc.service.security.service;

import cn.hutool.core.util.ObjectUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.ILogoutService;
import com.voc.service.security.api.clients.ILogoutServiceClient;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName LogoutServiceImpl
 * @createTime 2024年03月01日 17:30
 * @Copyright futong
 */
@Service("defaultLogoutService")
public class LogoutServiceImpl implements ILogoutService {
    private static final Logger log = LoggerFactory.getLogger(LogoutServiceImpl.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ILogoutServiceClient logoutServiceClient;
    @Autowired
    ISecurityServiceClient iSecurityService;
    @Autowired
    Executor executor;
    @Override
    public void logout() {
        log.info("logout appId:{}, userId:{}", ServiceContextHolder.getAppId(), ServiceContextHolder.getUserId());

        try {
            final String token = ServiceContextHolder.getToken();
            final Result<UserModel> getUserRs = iSecurityService.userinfo(token);
            if (!"200".equals(getUserRs.getCode()) && ObjectUtil.isNull(getUserRs.getResult())) {
                log.error("auth.userinfo service:{}", getUserRs.getMessage());
            }

            final Optional<UserModel> userIdRs = Optional.ofNullable(getUserRs.getResult());
            if (!userIdRs.isPresent()) {
                log.error("token非法! error: {} ", getUserRs.getMessage());
            }else {
                List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();

               /* futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                    //清楚本地缓存
//                    CacheUtil.cleanAll();
                    return null;
                })));
*/
                /*futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                    //清楚redis缓存
                    final String key1 = StrUtil.format("{}:users:{}".concat(":*")
                            , ServiceContextHolder.getAppId()
                            , ServiceContextHolder.getUserId()
                    );
                    log.info("del redis keys: {}", key1);
                    final Set keys = redisTemplate.keys(key1);
                    if (CollUtil.isNotEmpty(keys)) {
                        //TODO
//                        redisTemplate.delete(keys);
                    }
                    return null;
                })));*/


                /*futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                    //清楚userInfo缓存
                    final String key1 = StrUtil.format("{}:userinfo:tokens:{}"
                            , ServiceContextHolder.getAppId()
                            , MD5.create().digestHex(ServiceContextHolder.getToken())
                    );
                    log.info("del redis keys: {}", key1);
                    final Set keys = redisTemplate.keys(key1);
                    if (CollUtil.isNotEmpty(keys)) {
//                        redisTemplate.delete(keys);
                    }
                    return null;
                })));*/


                /*futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {

                    return null;
                })));*/

                // 遍历futureList中的元素并执行操作
                /*futureList.stream().forEach(f -> {
                    try {
                        // 尝试获取future对象的结果，在1秒内未获取到结果则抛出异常
                        f.get(3, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        // 捕获异常并打印堆栈信息
                        e.printStackTrace();
                    }
                });*/

                executor.execute(() -> {
                    //调用认证服务注销
                    log.info("调用认证服务注销");
                    logoutServiceClient.logout();
                    log.info("认证服务注销完成");
                    this.systemLogout(token);
                    log.info("注销完成");
                });
            }
            log.info("注销成功");
        } catch (Exception e) {
            log.error("注销异常");
            log.error(e.getMessage(), e);
        }
    }

    public void systemLogout(String token){

    }
}
