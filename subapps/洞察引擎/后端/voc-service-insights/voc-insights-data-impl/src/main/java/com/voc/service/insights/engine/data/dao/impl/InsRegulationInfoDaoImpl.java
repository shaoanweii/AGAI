package com.voc.service.insights.engine.data.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsRegulationInfoDao;
import com.voc.service.insights.engine.data.entity.InsRegulationInfoEntity;
import com.voc.service.insights.engine.data.mapper.InsRegulationInfoMapper;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.entity.InsTableInfoEntity;
import com.voc.service.insights.engine.enums.ChannelType;
import com.voc.service.insights.engine.enums.RuleStatusType;
import com.voc.service.insights.engine.mapper.InsChannelInfoMapper;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:28
 * @描述:
 **/
@Repository
public class InsRegulationInfoDaoImpl extends ServiceImpl<InsRegulationInfoMapper, InsRegulationInfoEntity> implements InsRegulationInfoDao {
    private static final Logger log = LoggerFactory.getLogger(InsRegulationInfoDaoImpl.class);
    @Autowired
    InsRegulationInfoMapper regulationInfoMapper;
    @Autowired
    InsChannelInfoMapper channelInfoMapper;

    @Override
    @SwitchClientDS(objectAttribute = "insRegulationInfoEntity.clientId")
    public void saveRegulationInfo(InsRegulationInfoEntity insRegulationInfoEntity) {
        int insert = regulationInfoMapper.insert(insRegulationInfoEntity);
        if (insert > 0) {
            log.info("保存规则信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGULATION_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insRegulationInfoEntity.clientId")
    public void updateRegulationInfo(InsRegulationInfoEntity insRegulationInfoEntity) {
        int update = regulationInfoMapper.updateById(insRegulationInfoEntity);
        if (update > 0) {
            log.info("更新规则信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_REGULATION_ERROR);
        }
    }

    @Override
    public void deleteRegulationInfo(String id, String userName) {
        regulationInfoMapper.deleteRegulationInfo(id, 1, 0, LocalDateTime.now(), userName);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public List<InsRegulationInfoEntity> findRegulationInfoList(InsRegulationInfoModel regulationInfoModel) {
        return regulationInfoMapper.findRegulationInfoList(regulationInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public InsRegulationInfoEntity findRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        return regulationInfoMapper.findRegulationInfoById(regulationInfoModel.getId());
    }

    @Override
    public Boolean checkRegulationName(InsRegulationInfoModel regulationInfoModel) {
        Integer codeNumber = regulationInfoMapper.checkRegulationName(regulationInfoModel);
        return codeNumber > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Boolean checkRegulationStatusById(String id) {
        String status = regulationInfoMapper.checkRegulationStatusById(id);
        return RuleStatusType.Enabled.getCode().equalsIgnoreCase(status) ? Boolean.TRUE : Boolean.FALSE;
    }


    @Override
    public Set<String> findStaticTableNames(String regulationType) {
        return regulationInfoMapper.findStaticTableNames(regulationType);
    }

    @Override
    public Set<String> findTableNames(InsTableInfoModel tableInfoModel) {
        return regulationInfoMapper.findTableNames(tableInfoModel);
    }

    @Override
    public List<InsTableInfoEntity> findTableInfoList(Set<String> tableNames, List<String> tableColumns) {
        return regulationInfoMapper.findTableInfoList(tableNames, tableColumns);
    }

    @Override
    public List<JSONObject> findTableData(String tableName, List<String> columns) {
        return regulationInfoMapper.findTableDataInfo(tableName, columns);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoMode.clientId")
    public List<InsRegulationInfoEntity> findRuleInfoList(InsRegulationInfoModel regulationInfoMode) {
        return regulationInfoMapper.findRegulationInfoList(regulationInfoMode);
    }

    @Override
    public List<InsRegulationInfoEntity> findStandardRuleInfoList(InsRegulationInfoModel regulationInfoMode) {
        return regulationInfoMapper.findRegulationInfoList(regulationInfoMode);
    }

    @Override
    public List<InsRegulationInfoEntity> findResourceGroupRegulationList(InsRegulationInfoModel detailsModel) {
        return regulationInfoMapper.findResourceGroupRegulationList(detailsModel);
    }

    @Override
    public List<InsRegulationInfoEntity> findResourceGroupRegulationStatusCount(InsRegulationInfoModel detailsModel) {
        return regulationInfoMapper.findResourceGroupRegulationStatusCount(detailsModel);
    }

    @Override
    @SwitchClientDS
    public List<String> findChannelHierarchical(String clientId, List<String> channelIds, Boolean isFinal) {
        List<InsChannelInfoEntity> channelHierarchical = channelInfoMapper.UpwardFindChannelHierarchical(InsChannelInfoModel.builder().channelIds(channelIds).build());
        List<String> collect = null;
        if (isFinal) {
            collect = channelHierarchical.stream().filter(e -> ChannelType.CHANNEL.getCode().equalsIgnoreCase(e.getType())).map(e -> e.getId()).collect(Collectors.toList());
        } else {
            collect = channelHierarchical.stream().map(e -> e.getId()).collect(Collectors.toList());
        }
        return collect;
    }

    @Override
    @SwitchClientDS
    public List<String> findChannelCodeHierarchical(String clientId, List<String> channelIds, Boolean isFinal) {
        List<InsChannelInfoEntity> channelHierarchical = channelInfoMapper.UpwardFindChannelHierarchical(InsChannelInfoModel.builder().channelIds(channelIds).build());
        List<String> collect = null;
        if (isFinal) {
            collect = channelHierarchical.stream().filter(e -> ChannelType.CHANNEL.getCode().equalsIgnoreCase(e.getType())).map(e -> e.getCode()).collect(Collectors.toList());
        } else {
            collect = channelHierarchical.stream().map(e -> e.getCode()).collect(Collectors.toList());
        }
        return collect;
    }

    @Override
    @SwitchClientDS
    public String findRegulationName(String clientId, String name) {
        return regulationInfoMapper.findRegulationName(name);
    }
}
