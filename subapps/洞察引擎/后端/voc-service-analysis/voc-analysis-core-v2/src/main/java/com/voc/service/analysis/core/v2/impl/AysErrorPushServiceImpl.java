package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.producers.kafka.ErrorPushProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.common.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysErrorPushServiceImpl implements IAysErrorPushService {
    private static final Logger logger = LoggerFactory.getLogger(AysErrorPushServiceImpl.class);
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    ErrorPushProducer errorPushProducer;

    //    @SwitchClientDS
    @Override
    public void push(ErrorPushModel model)
            throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(model.getClientId()), "clientId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(model.getTable()), "tableName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(model.getAction()), "action cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(model.getWorkId()), "workId cannot be empty");
        Assert.isTrue(ObjectUtil.isNotNull(model.getData()), "data cannot be empty");

        model.setId(IdWorker.getId());

        logger.error("[{}]被遗弃的数据[{}]-[]操作-数据项: ", model.getClientId(), model.getTable(), model.getAction()
                , JSONUtil.toJsonStr(model.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false)));

        MessageExt tableExt = MessageExt.builder().key("table").value(model.getTable()).build();
        MessageExt workIdxt = MessageExt.builder().key("workId").value(model.getWorkId()).build();
        errorPushProducer.pushData(MessageDTO.builder().source(model.getClientId()).ext(Set.of(tableExt, workIdxt))
                .type(model.getAction()).data(model).build());
    }

}
