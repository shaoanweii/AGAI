package com.voc.service.data.integration.mpp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.data.integration.api.IModelOutputRecordService;
import com.voc.service.data.integration.entity.ModelOutputRecordEntity;
import com.voc.service.data.integration.mapper.ModelOutputRecordMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @Title: ModelProcessedResultDataServiceImpl
 * @Package: com.voc.service.data.integration.mpp.scheduled.xxljob
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/12 13:19
 * @Version:1.0
 */
@Service
public class ModelOutputRecordServiceImpl
        extends ServiceImpl<ModelOutputRecordMapper, ModelOutputRecordEntity>
    implements IModelOutputRecordService {
    @Override
    public long record(Set<String> channelList, Set<String> attrs) {

        return this.baseMapper.record(channelList,attrs);
    }
}
