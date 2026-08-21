package com.voc.service.insights.engine.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsProvinceAreaEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域城市信息表(InsProvinceArea)表数据库访问层
 *
 * @author leiww
 * @since 2024-01-25 13:56:33
 */
public interface InsProvinceAreaMapper extends BaseMapper<InsProvinceAreaEntity> {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param model List<InsProvinceAreaEntity> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("model") List<InsProvinceAreaEntity> model);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param model List<InsProvinceAreaEntity> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("model") List<InsProvinceAreaEntity> model);


}
