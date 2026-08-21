package com.voc.service.insights.engine.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 下午3:43
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsProjectDetailsVo {
    /**
     * id
     */
    private String id;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 品牌编码
     */
    @Schema(description = "品牌编码")
    private String brandCode;
    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 应用标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    /**
     * 数据源
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dataSource;

    /**
     * 渠道
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> channel;

    /**
     * 区域
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> region;

    /**
     * 品牌车系
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BrandVo> brand;
}
