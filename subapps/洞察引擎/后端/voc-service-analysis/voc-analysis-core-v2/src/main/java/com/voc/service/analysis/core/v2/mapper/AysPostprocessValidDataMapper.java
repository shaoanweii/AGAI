package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysPostprocessValidDataEntity;
import com.voc.service.analysis.model.AysProcessValidDataModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysPostprocessValidDataMapper extends BaseMapper<AysPostprocessValidDataEntity> {
   /* long findByDistinctByAttr(@Param("attr") String attr
            , @Param("value") String value, @Param("startTime") String startTime);*/


    List<AysProcessValidDataModel> queryProcessValidData(@Param("workId") String workId,
                                                         @Param("clientId") String clientId,
                                                         @Param("channelIdList") List<String> channelId);

    long removeHistoryData(@Param("days") int days,@Param("workIds")  Set<String> workIds);
}
