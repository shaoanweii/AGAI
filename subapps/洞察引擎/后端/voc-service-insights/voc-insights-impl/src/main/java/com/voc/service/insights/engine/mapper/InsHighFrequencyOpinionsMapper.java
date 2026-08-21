package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsHighFrequencyOpinionsEntity;
import com.voc.service.insights.engine.model.InsAllocationRecordModel;
import com.voc.service.insights.engine.model.InsBaseHighFrequencyQueryModel;
import com.voc.service.insights.engine.model.InsOpinionsListModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsHighFrequencyOpinionsMapper extends BaseMapper<InsHighFrequencyOpinionsEntity> {


    /**
     * 统计高频观点
     *
     * @param model
     * @return
     */
    List<InsOpinionsListModel> queryPageHighFrequencyOpinionsList(@Param("model") InsBaseHighFrequencyQueryModel model);

    List<InsAllocationRecordModel> queryAllocationRecord(@Param("id") String id);

    Long queryHistoryTotalFrequency(@Param("name") String name,
                                    @Param("clientId") String clientId,
                                    @Param("channelSource") String channelSource);

    Boolean saveOrUpdateBatch(@Param("insHighFrequencyOpinionsEntities") List<InsHighFrequencyOpinionsEntity> insHighFrequencyOpinionsEntities);

}
