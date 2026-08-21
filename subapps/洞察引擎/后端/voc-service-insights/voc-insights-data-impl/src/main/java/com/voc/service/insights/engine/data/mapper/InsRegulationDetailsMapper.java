package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsRegulationDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:47
 * @描述:
 **/
@Mapper
@Repository
public interface InsRegulationDetailsMapper extends BaseMapper<InsRegulationDetailEntity> {

    List<InsRegulationDetailEntity> findRegulationDetails(@Param("regulationId") String id);

    void deleteRegulationInfo(@Param("regulationId")String regulationId, @Param("delFlag") Integer delFlag, @Param("status")Integer status, @Param("updateTime") LocalDateTime updateTime, @Param("updateUser") String updateUser);

    List<InsRegulationDetailEntity> findAllRegulationDetails();

    void removeRegulationDetails(@Param("regulationId") String regulationId);
}
