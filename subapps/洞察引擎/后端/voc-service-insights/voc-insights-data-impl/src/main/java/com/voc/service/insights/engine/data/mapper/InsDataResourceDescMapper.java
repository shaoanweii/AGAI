package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsDataResourceDescEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资源详情(InsDataResourceDesc)表持久层
 *
 * @author leiww
 * @since 2024-04-02 17:00:18
 */
public interface InsDataResourceDescMapper extends BaseMapper<InsDataResourceDescEntity> {

    List<InsDataResourceDescEntity> countByResourceIds(@Param("resourceIds")Set<String> resourceIds);
}

