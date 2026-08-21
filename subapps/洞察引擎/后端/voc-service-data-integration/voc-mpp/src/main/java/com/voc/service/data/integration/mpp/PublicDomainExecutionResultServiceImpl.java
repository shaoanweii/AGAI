package com.voc.service.data.integration.mpp;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.data.integration.api.PublicDomainExecutionResultService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.PublicDomainInfoDataModel;
import com.voc.service.data.integration.producers.kafka.PublicDomainExecutionResultProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: DataIntegrationRecordServiceImpl
 * @Package: com.voc.service.data.integration.in.mpp
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/26 10:48
 * @Version:1.0
 */
@Service
public class PublicDomainExecutionResultServiceImpl
        implements PublicDomainExecutionResultService {

}
