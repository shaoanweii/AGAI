package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsHighFrequencyWordsEntity;
import com.voc.service.insights.engine.model.InsAllocationRecordModel;
import com.voc.service.insights.engine.model.InsBaseHighFrequencyQueryModel;
import com.voc.service.insights.engine.model.InsWordsListModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsHighFrequencyWordsMapper extends BaseMapper<InsHighFrequencyWordsEntity> {

    /**
     * 统计高频词汇
     *
     * @param model
     * @return
     */
    List<InsWordsListModel> queryPageHighFrequencyWordsList(@Param("model") InsBaseHighFrequencyQueryModel model);

    List<InsAllocationRecordModel> queryAllocationRecord(@Param("id") String id);


    Long queryHistoryTotalFrequency(@Param("wordName") String wordName,
                                    @Param("clientId") String clientId,
                                    @Param("channelSource") String channelSource);

    Boolean saveOrUpdateBatch(@Param("insHighFrequencyWordsEntityList") List<InsHighFrequencyWordsEntity> insHighFrequencyWordsEntityList);
}
