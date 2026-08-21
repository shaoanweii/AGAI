package com.voc.service.analysis.core.v2.adapter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.v2.api.IAnalysisCoreService;
import org.apache.commons.lang.StringEscapeUtils;
import org.redisson.api.RLock;
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
@Component("validTaskAdapter.v2")
public class ValidTaskAdapter extends MessageListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ValidTaskAdapter.class);
    @Autowired
    IAnalysisCoreService coreService;
    @Autowired
    AnalysisConfig config;
    @Autowired
    RedisTemplate redisTemplate;

  /*  static final  String urlPath = StrUtil.format("http://{}:{}/task/validate_flow"
            , "127.0.0.1", "8080");*/

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.trace(">>>>>>> 消息适配器收到 {} 的请求 <<<<<<<<<<<<<<", message);
        RLock rlock = config.getValidLock();
        try {
            /*Boolean flag = redisTemplate.opsForValue().setIfAbsent("validTaskAdapter:".concat(message.toString())
                    , "1", 1, TimeUnit.HOURS);
            // 加锁失败，已有消费端在此时对此消息进行处理，这里不再做处理
            if (!flag) {
                return;
            }*/
            if (!rlock.isLocked()) {
                rlock.lock();
                final String jsonObj = new String(message.getBody());
                log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}",  jsonObj);
                String json = jsonObj;
                if(jsonObj.startsWith("\"")){
                    log.warn("json 格式无效，转换成正确的json格式");
                    json = jsonObj.substring(1, jsonObj.length()-1);
                    json = StringEscapeUtils.unescapeJava(json);
                }
                final AysValidDataModel param = JSONUtil.toBean(json, AysValidDataModel.class);
                Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(param.getStartTime()), "getStartTime cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(param.getClientId()), "getClientId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(param.getEndTime()), "getEndTime cannot be empty");
                Assert.isTrue(CollUtil.isNotEmpty(param.getValidRuleIds()), "getValidRuleIds cannot be empty");

                coreService.validateFlow(param);

            }
        } catch (Exception e) {
            log.error("ValidTaskAdapter验证异常:",e);
        } finally {
            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        String jsonObj = "\"{\\\"workId\\\":\\\"db4bc128a0faef055d0a401ea9fa9ae6\\\",\\\"clientId\\\":\\\"e11ab369ea4d56a7a64ab0a3c491a2cc\\\",\\\"channel\\\":[\\\"-1\\\",\\\"a9f34253a58e855f0fa8dee5164c6764\\\"],\\\"contentType\\\":\\\"text\\\",\\\"matchingRule\\\":\\\"and\\\",\\\"startTime\\\":\\\"2023-08-01\\\",\\\"endTime\\\":\\\"2024-09-30\\\",\\\"attrs\\\":[{\\\"regulationId\\\":\\\"9fe218f7c52446d306fedbe945886acf\\\",\\\"fieldName\\\":\\\"brand_code_name\\\",\\\"variableValue\\\":\\\"textLength\\\",\\\"logicalOperator\\\":\\\"lessThenOrEqual\\\",\\\"conditionType\\\":\\\"value\\\",\\\"conditionDetail\\\":\\\"50\\\",\\\"detailType\\\":\\\"0\\\"}],\\\"validRuleIds\\\":[\\\"9fe218f7c52446d306fedbe945886acf\\\"],\\\"enabledRuleIds\\\":[],\\\"pageSize\\\":10,\\\"pageNum\\\":1}\"";
        final AysValidDataModel param = JSONUtil.toBean(jsonObj, AysValidDataModel.class);
        System.out.println(JSONUtil.toJsonStr(param));
    }
}

