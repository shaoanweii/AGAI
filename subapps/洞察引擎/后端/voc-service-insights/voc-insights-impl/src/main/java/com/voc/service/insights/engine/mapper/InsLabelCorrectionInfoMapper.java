package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.entity.InsLabelCorrectionInfoEntity;
import com.voc.service.insights.engine.entity.InsLabelCorrectionRecordEntity;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsLabelCorrectionInfoMapper extends BaseMapper<InsLabelCorrectionInfoEntity> {

    @SwitchClientDS(datasource = "starrock_dndc")
    int batchInsert(@Param("id") String id, @Param("newIdList") List<String> newIdList);

}
