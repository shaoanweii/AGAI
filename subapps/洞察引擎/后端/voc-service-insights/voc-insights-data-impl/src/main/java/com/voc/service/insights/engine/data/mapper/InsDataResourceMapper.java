package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voc.service.insights.engine.data.entity.InsDataResourceEntity;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源库(InsDataResource)表持久层
 *
 * @author leiww
 * @since 2024-04-02 16:37:37
 */
public interface InsDataResourceMapper extends BaseMapper<InsDataResourceEntity> {
    IPage<InsDataResourceEntity> findDataResourceList(Page<InsDataResourceEntity> page, @Param("model") InsDataResourceModel model);

    List<InsDataResourceEntity> findAllDataResourceList(@Param("model") InsDataResourceModel model);
}

