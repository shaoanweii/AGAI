package com.voc.service.insights.engine.model.mapper;
 

import com.voc.service.insights.engine.model.entity.InsModelDescEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (InsModelDesc)表数据库访问层
 *
 * @author leiww
 * @since 2024-02-21 15:32:06
 */
public interface InsModelDescMapper  extends BaseMapper<InsModelDescEntity> {
 
    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param model List<InsModelDescEntity> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("model") List<InsModelDescEntity> model);
 
    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param model List<InsModelDescEntity> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("model") List<InsModelDescEntity> model);
 


 
}
