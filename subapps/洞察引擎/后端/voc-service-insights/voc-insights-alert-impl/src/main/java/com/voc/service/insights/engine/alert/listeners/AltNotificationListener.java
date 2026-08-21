package com.voc.service.insights.engine.alert.listeners;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.insights.engine.api.alert.IInsAlertNotificationService;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component(AltNotificationListener.TOPIC)
public class AltNotificationListener extends MessageListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(AltNotificationListener.class);
    public static final String TOPIC = "altNotification";
    @Resource
    private RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    IInsAlertNotificationService alertNotificationService;


    @Override
    public void onMessage(Message message, byte[] bytes) {
        RLock rlock = null;
        try {
            log.trace(">>>>>>> 消息适配器收到 {} 的请求 <<<<<<<<<<<<<<", message);
            final String jsonObj = new String(message.getBody());
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", jsonObj);
            final String str = Base64.decodeStr(jsonObj.replaceAll("\"", ""), CharsetUtil.CHARSET_UTF_8);

            final AlertTaskModel param = JSONUtil.toBean(str, AlertTaskModel.class);
            Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
            Assert.isTrue(ObjectUtil.isNotNull(param.getId()), "getId cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(param.getChannelId()), "getTaskChannelId cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(param.getDataType()), "getTaskDataType cannot be empty");

            rlock = redissonClient.getLock(TOPIC.concat(param.getChannelId()).concat(param.getClientId()).concat(param.getDataType()));
            if (!rlock.isLocked()) {
                if (rlock.tryLock()) {
                    alertNotificationService.generate(param);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }
    }
}

