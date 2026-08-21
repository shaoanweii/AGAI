package com.voc.service.analysis.risk.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.risk.entity.VocAnalBatchDataRiskEntity;
import com.voc.service.analysis.risk.mapper.VocAnalBatchDataRiskMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.risk.api.IVocAnalBatchDataRiskService;
import com.voc.service.risk.api.model.VocAnalBatchDataRiskModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class IVocAnalBatchDataRiskImpl extends
        ServiceImpl<VocAnalBatchDataRiskMapper, VocAnalBatchDataRiskEntity> implements IVocAnalBatchDataRiskService {

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean batchInsert(List<VocAnalBatchDataRiskModel> vocAnalBatchDataRiskList) {
        log.info("批量插入数据: {}", vocAnalBatchDataRiskList.size());
        List<VocAnalBatchDataRiskEntity> vocAnalBatchDataRiskEntities = new ArrayList<>();
        for (VocAnalBatchDataRiskModel model : vocAnalBatchDataRiskList) {
            VocAnalBatchDataRiskEntity entity = new VocAnalBatchDataRiskEntity();
            BeanUtils.copyProperties(model, entity);
            vocAnalBatchDataRiskEntities.add(entity);
        }
        return this.saveBatch(vocAnalBatchDataRiskEntities);
    }
}
