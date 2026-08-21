package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 知识库表(InsKnowledgeBase)表数据库访问层
 *
 * @author makejava
 * @since 2024-09-06 14:51:57
 */
public interface InsKnowledgeBaseDao {

    /**
     * 通过ID查询单条数据
     *
     * @param 主键
     * @return 实例对象
     */
    InsKnowledgeBase queryById();

    /**
     * 查询指定行数据
     *
     * @param insKnowledgeBase 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<InsKnowledgeBase> queryAllByLimit(InsKnowledgeBase insKnowledgeBase, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param insKnowledgeBase 查询条件
     * @return 总行数
     */
    long count(InsKnowledgeBase insKnowledgeBase);

    /**
     * 新增数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 影响行数
     */
    int insert(InsKnowledgeBase insKnowledgeBase);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<InsKnowledgeBase> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<InsKnowledgeBase> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<InsKnowledgeBase> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<InsKnowledgeBase> entities);

    /**
     * 修改数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 影响行数
     */
    int update(InsKnowledgeBase insKnowledgeBase);

    /**
     * 通过主键删除数据
     *
     * @param 主键
     * @return 影响行数
     */
    int deleteById();

}

