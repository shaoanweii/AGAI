package com.voc.service.insights.engine.model.data;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 数据集详情(InsDataDesc)请求实体对象
 *
 * @author leiww
 * @since 2024-02-27 16:48:54
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time"),
        @SortField(source = "publishTime", targer = "publish_time")
})
@EqualsAndHashCode(callSuper = false)
@Tag(name = "数据集详情", description = "数据集详情")
public class InsDataSourceDescModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 数据源集id
     */
    @Schema(description = "data_id")
    private String dataId;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;
    /**
     * 渠道ID
     */
    @Schema(description = "渠道ID")
    private String channelId;
    /**
     * 区域
     */
    @Schema(description = "区域")
    private String province;
    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;
    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String customerName;
    /**
     * 模型名称
     */
    @Schema(description = "模型名称")
    private String modelName;
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    @Schema(description = "数据源集过滤")
    private Set<String> dataFilters;
}

