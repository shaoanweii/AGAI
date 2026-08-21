package com.voc.service.insights.engine.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.api.ILabelCorrectionInfoService;
import com.voc.service.insights.engine.entity.InsLabelCorrectionInfoEntity;
import com.voc.service.insights.engine.mapper.InsLabelCorrectionInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class InsLabelCorrectionInfoImpl extends ServiceImpl<InsLabelCorrectionInfoMapper, InsLabelCorrectionInfoEntity> implements ILabelCorrectionInfoService {


    private static final Logger log = LoggerFactory.getLogger(InsLabelCorrectionInfoImpl.class);


    @Override
    public Boolean batchInsert(String id, List<String> newIdList) {
        int count = this.baseMapper.batchInsert(id, newIdList);
        return count > 0;
    }

    @Override
    public Boolean del(String correctionRecordId) {
        QueryWrapper<InsLabelCorrectionInfoEntity> dataEntityQueryWrapper = new QueryWrapper<>();
        dataEntityQueryWrapper.eq("correction_record_id", correctionRecordId);
        int delete = this.baseMapper.delete(dataEntityQueryWrapper);
        return delete > 0;
    }
}
