package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsLabelCorrectionRecordEntity;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.InsLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.vo.InsCqCaLabelCorrectionRecordPageVo;
import com.voc.service.insights.engine.vo.InsLabelCorrectionRecordPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsLabelCorrectionRecordMapper extends BaseMapper<InsLabelCorrectionRecordEntity> {

    List<InsLabelCorrectionRecordEntity> queryLabelCorrectionList(InsCqCaLabelCorrectionRecordQueryModel model);
}
