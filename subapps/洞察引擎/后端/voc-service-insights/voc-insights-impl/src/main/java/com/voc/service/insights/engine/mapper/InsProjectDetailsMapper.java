package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsProjectDetailsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 下午2:29
 * @描述:
 **/
@Mapper
@Repository
public interface InsProjectDetailsMapper extends BaseMapper<InsProjectDetailsEntity> {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/25 下午3:26
     * @描述   根据项目id查询项目详情信息
     * @param projectId
     * @return com.voc.service.insights.engine.entity.InsProjectDetailsEntity
     **/
    List<InsProjectDetailsEntity> findProjectInfo(@Param("projectId") String projectId);
}
