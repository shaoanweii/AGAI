package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.data.entity.InsAccountLexiconEntity;
import com.voc.service.insights.engine.model.InsAccountLexiconModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 16:22
 * @描述:
 **/
@Mapper
@Repository
public interface InsAccountLexiconMapper extends BaseMapper<InsAccountLexiconEntity> {

    IPage<InsAccountLexiconEntity> findAccountLexiconList(IPage<InsAccountLexiconEntity> page, @Param("insAccountLexicon") InsAccountLexiconModel insAccountLexicon);

    List<InsAccountLexiconEntity> countByResourceIds(@Param("resourceIds")Set<String> resourceIds);

    List<String> findDownHierarchical(@Param("channelCode") String channelCode);
}
