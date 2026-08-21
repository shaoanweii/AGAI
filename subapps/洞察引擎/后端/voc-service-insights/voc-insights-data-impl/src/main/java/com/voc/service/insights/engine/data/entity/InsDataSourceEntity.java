package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源集(InsDataSource)表实体类
 *
 * @author leiww
 * @since 2024-02-27 15:31:45
 */
@Data
@TableName("ins_data_source")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsDataSourceEntity  implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 数据源名称
     */
    private String dataSourceName;
    /**
     * 数据源类型
     */
    private String dataSourceType;
    /**
     * 数据源接入方式
     */
    private String dataSourceAccessWay;
    /**
     * 所属客户id
     */
    @TableField(exist = false)
    private String clientId;
    /**
     * 标签类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> labelType;
    /**
     * 模型类型
     */
    private String modelType;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建用户
     */
    private String createUser;
}

