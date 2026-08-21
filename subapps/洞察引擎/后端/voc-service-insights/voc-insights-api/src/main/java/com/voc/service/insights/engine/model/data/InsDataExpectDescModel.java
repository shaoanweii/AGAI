package com.voc.service.insights.engine.model.data;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 语料库数据详情(InsDataExpectDesc)请求实体对象
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
@EqualsAndHashCode(callSuper = false)
@Tag(name = "语料库数据详情", description = "语料库数据详情")
public class InsDataExpectDescModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 语料库数据集id
     */
    @Schema(description = "语料库数据集id")
    private String expectId;
    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;
    /**
     * 业务标签
     */
    @Schema(description = "业务标签")
    private String business;
    /**
     * 质量标签
     */
    @Schema(description = "质量标签")
    private String quality;
    /**
     * 场景标签
     */
    @Schema(description = "场景标签")
    private String scene;
    /**
     * 情感
     */
    @Schema(description = "情感")
    private String emotion;
    /**
     * 意图
     */
    @Schema(description = "意图")
    private String intention;
    /**
     * 观点
     */
    @Schema(description = "观点")
    private String viewpoint;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime createTime;
    /**
     * 修改用户
     */
    @Schema(description = "修改用户")
    private String updateBy;
    /**
     * 创建用户
     */
    @Schema(description = "创建用户")
    private String createBy;

    @Schema(description = "数据集多选过滤")
    private Set<String> expectFilters;
}

