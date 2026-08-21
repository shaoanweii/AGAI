package com.voc.service.insights.engine.data.mapper;


import com.voc.service.insights.engine.data.entity.InsDataExpectDescEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * 语料库数据详情(InsDataExpectDesc)表数据库访问层
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
public interface InsDataExpectDescMapper extends BaseMapper<InsDataExpectDescEntity> {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param model List<InsDataExpectDescEntity> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("model") List<InsDataExpectDescEntity> model);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param model List<InsDataExpectDescEntity> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("model") List<InsDataExpectDescEntity> model);


}
