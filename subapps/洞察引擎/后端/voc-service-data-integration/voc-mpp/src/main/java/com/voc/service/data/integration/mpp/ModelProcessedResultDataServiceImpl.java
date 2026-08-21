package com.voc.service.data.integration.mpp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.data.integration.api.IModelProcessedResultDataService;
import com.voc.service.data.integration.entity.ModelProcessedResultDataEntity;
import com.voc.service.data.integration.mapper.ModelProcessedResultDataMapper;
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
public class ModelProcessedResultDataServiceImpl
        extends ServiceImpl<ModelProcessedResultDataMapper, ModelProcessedResultDataEntity>
    implements IModelProcessedResultDataService {
    @Override
    public String refreshData() {
        return this.baseMapper.refreshData();
//        return "empty";
    }

    @Override
    public long saveOutputData(Set<String> channelList, Set<String> attrs) {

        return this.baseMapper.saveOutputData(channelList,attrs);
    }
}
