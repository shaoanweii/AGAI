package com.voc.service.security.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.security.api.ILoginHistoryService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.LoginHistroyEntity;
import com.voc.service.security.impl.mapper.LoginHistoryMapper;
import com.voc.service.security.model.LoginHistroyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName LoginHistoryService
 * @Description ckcui
 * @createTime 2023年12月26日 13:59
 * @Copyright futong
 */
@Service
public class LoginHistoryService extends ServiceImpl<LoginHistoryMapper, LoginHistroyEntity> implements ILoginHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryService.class);
    static TimedCache<String, LoginHistroyModel> loginMsgCache = CacheUtil.newTimedCache(1000 * 60 * 30);
    @Autowired
    SecurityConverMapperService securityConverMapperService;

    @Override
    public void add(LoginHistroyModel model) {
        LoginHistroyEntity entity = securityConverMapperService.converTo(model);
//        entity.setId(IdWorker.getId());
        baseMapper.insert(entity);
    }


    @Override
    public void addAsync(LoginHistroyModel model) {
        if (loginMsgCache.size() < 1000) {
            loginMsgCache.put(UUID.randomUUID().toString(), model);
        }
    }


    @Scheduled(fixedDelay = 1 * 1000)
    public void pushLoginLogs() {
        synchronized (loginMsgCache.keySet()) {
            if (loginMsgCache.isEmpty()) {
                return;
            }
            List<String> sub = CollUtil.sub(loginMsgCache.keySet(), 0, 100);
            if (CollUtil.isNotEmpty(sub)) {
                logger.info("执行登陆日期批量提交,本次批量处理 {}条日志", sub.size());
                List<LoginHistroyEntity> msgList = sub.stream().filter(loginMsgCache::containsKey)
                        .map(loginMsgCache::get)
                        .filter(ObjectUtil::isNotNull)
                        .map(model -> securityConverMapperService.converTo(model))
                        .collect(Collectors.toList());

                this.saveBatch(msgList);

                sub.stream().filter(loginMsgCache::containsKey).forEach(key -> {
                    loginMsgCache.remove(key);
                });

            }
        }
    }
}
