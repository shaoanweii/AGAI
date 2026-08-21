package com.voc.service.insights.engine.data.dao.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsRegulationDetailsDao;
import com.voc.service.insights.engine.data.entity.InsRegulationDetailEntity;
import com.voc.service.insights.engine.data.mapper.InsRegulationDetailsMapper;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:46
 * @描述:
 **/
@Repository
public class InsRegulationDetailsDaoImpl extends ServiceImpl<InsRegulationDetailsMapper, InsRegulationDetailEntity> implements InsRegulationDetailsDao {
    private static final Logger log = LoggerFactory.getLogger(InsRegulationDetailsDaoImpl.class);
    @Autowired
    InsRegulationDetailsMapper regulationDetailsMapper;

    @Override
    @SwitchClientDS
    public void saveRegulationDetails(List<InsRegulationDetailEntity> regulationDetails, String clientId) {
        boolean insert = this.saveBatch(regulationDetails);
        if (insert) {
            log.info("保存规则详情信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGULATION_DETAILS_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateRegulationDetails(List<InsRegulationDetailEntity> regulationDetails, String clientId) {
        boolean updateBatch = this.saveOrUpdateBatch(regulationDetails);
        if (updateBatch) {
            log.info("保存规则详情信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_REGULATION_DETAILS_ERROR);
        }
    }

    @Override
    public void deleteRegulationDetails(String regulationId, String userName) {
        regulationDetailsMapper.deleteRegulationInfo(regulationId, 1, 0, LocalDateTime.now(), userName);
    }

    @Override
    @SwitchClientDS
    public List<InsRegulationDetailEntity> findRegulationDetailsById(String regulationId, String clientId) {
        if (StrUtil.isBlank(regulationId)) {
            log.error("参数异常:{}", "id不能为空");
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "id不能为空");
        }
        return regulationDetailsMapper.findRegulationDetails(regulationId);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoMode.clientId")
    public List<InsRegulationDetailEntity> findAllRegulationDetails(InsRegulationInfoModel regulationInfoMode) {
        return regulationDetailsMapper.findAllRegulationDetails();
    }

    @Override
    public List<InsRegulationDetailEntity> findAllStandardRegulationDetails() {
        return regulationDetailsMapper.findAllRegulationDetails();
    }

    @Override
    @SwitchClientDS
    public void removeRegulationDetails(String regulationId, String clientId) {
        regulationDetailsMapper.removeRegulationDetails(regulationId);
    }
}
