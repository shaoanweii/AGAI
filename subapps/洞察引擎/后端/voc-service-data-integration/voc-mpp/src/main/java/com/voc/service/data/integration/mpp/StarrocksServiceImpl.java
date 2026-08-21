package com.voc.service.data.integration.mpp;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.entity.ChannelMetaDataEntity;
import com.voc.service.data.integration.mapper.ChannelMetaDataMapper;
import com.voc.service.data.integration.mpp.cenvert.DataIntegrationConvertMapperService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: StarrocksServiceImpl
 * @Package: com.voc.service.data.integration.in.mpp
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 14:56
 * @Version:1.0
 */
@Service
public class StarrocksServiceImpl extends ServiceImpl<ChannelMetaDataMapper, ChannelMetaDataEntity>
        implements IMppInputDataService {
    private static final Logger log = LoggerFactory.getLogger(StarrocksServiceImpl.class);
    @Autowired
    ChannelExecutionResultService dataIntegrationRecordService;
    @Autowired
    DataIntegrationConvertMapperService convertMapperService;

    @Override
    public List<ChannelMetaDataModel> loadData(final Set<String> ids) {
        QueryWrapper<ChannelMetaDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ChannelMetaDataEntity::getId, ids);
        List<ChannelMetaDataEntity> entityList = this.baseMapper.selectList(queryWrapper);
        return convertMapperService.cenvertToChannelMetaDataModelList(entityList);
    }

    @Override
    public Set<String> loadDataIds(String channelType, Date startDate, Date endDate) {

        log.info("<><><><><><>>开始加载数据<>>>>>>>>>>>>>");
        Set<String> channelList =  new HashSet<>();
        if (StringUtils.isNotBlank(channelType)){
            channelList.add(channelType);
        }
        List<ChannelMetaDataEntity> entityList = this.baseMapper.findAll(channelList, null, null);
        if (CollUtil.isEmpty(entityList)) {
            return Set.of();
        }

        final Set<String> ids = entityList.stream().map(ChannelMetaDataEntity::getId).collect(Collectors.toSet());

        log.info("<>><>><>ids<><><><>>><><>:{}", ids.size());
        //过滤掉已经执行的数据-缓存中的记录（避免mq中的数据未能及时落地导致重复执行
       // final Set<String> filteredDataIds = dataIntegrationRecordService.filterData(dataIds);
//        log.info("dataIds:{} filterDataIds:{}", dataIds, filteredDataIds);
//        if (CollUtil.isEmpty(filteredDataIds)) {
//            return Set.of();
//        }
//        final Set<String> ids = entityList.stream().filter(e -> filteredDataIds.contains(e.getDataId())).map(ChannelMetaDataEntity::getId).collect(Collectors.toSet());
//        log.info("过滤后数据:{}", ids.size());
        return ids;
    }

    /*@Override
    public List<DataIntegrationRecordModel> loadRetryData(String channelType, Date startDate, Date endDate) {

        final List<DataIntegrationRecordModel> list = dataIntegrationRecordService.loadRetryData(null, startDate, endDate);
//
        return list;
    }*/


    /* private List<ChannelMetaDataModel> mockData() {
         List<ChannelMetaDataModel> list = new ArrayList<>();
         list.add(ChannelMetaDataModel.builder().dataId(null).channelType("name_11").createTime(LocalDateTime.now()).build());
         list.add(ChannelMetaDataModel.builder().data(null).channelType("name_22").createTime(LocalDateTime.now()).build());
         list.add(ChannelMetaDataModel.builder().id("123").dataId("dataId_1").data("data_1").channelType("channel_123").createTime(LocalDateTime.now()).build());
         list.add(ChannelMetaDataModel.builder().id("456").dataId("dataId_2").data("data_1").channelType("channel_456").createTime(LocalDateTime.now()).build());
         list.add(ChannelMetaDataModel.builder().id("555").dataId("dataId_3").data("data_1").channelType("channel_555").createTime(LocalDateTime.now()).build());
         list.add(ChannelMetaDataModel.builder().id("678").build());
         return list;
     }*/
    @Override
    public long removeHistoryData(String clientId, int days) {
        //查询
        Long count = this.baseMapper.checkData(days);
        if (count <= 0) {
            return 0;
        }
        //备份
        this.baseMapper.moveToHistoryData(days);

        long checkLen = this.baseMapper.checkHistoryData();
        if (checkLen > 0) {
            this.baseMapper.deleteHistoryData(days);
        }

        return checkLen;
    }

    @Override
    public long loadDataCount() {
        return this.baseMapper.checkAllMetaData();
    }
}
