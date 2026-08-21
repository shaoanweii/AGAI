package com.voc.service.insights.engine.model.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.Client;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 模型配置数据(InsModelInfo)请求实体对象
 *
 * @author leiww
 * @since 2024-02-22 11:36:03
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
@Tag(name = "模型配置数据", description = "模型配置数据")
public class InsModelInfoModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 模型名称
     */
    @Schema(description = "模型名称")
    @Size(max = 20, message = "模型名称长度不符")
    private String modelName;
    /**
     * 模型类型
     */
    @Dict(code = InsightsConstants.MODEL_TYPE)
    @Schema(description = "模型类型")
    private String modelType;
    /**
     * 数据格式
     */
    @Dict(code = InsightsConstants.DATA_TYPE)
    @Schema(description = "数据格式")
    private String format;
    /**
     * ⽤户客户ID
     */
    @Client
    @Schema(description = "⽤户客户ID")
    private String clientId;
    /**
     * 项目Id
     */
    @Schema(description = "项目Id")
    private String projectId;
    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;
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

    @Schema(description = "模型类型多选过滤")
    private Set<String> modelTypeFilters;

    @Schema(description = "数据格式多选过滤")
    private Set<String> formatFilters;

    @Schema(description = "模型名称模糊过滤")
    @Size(max = 20, message = "名称长度不符")
    private String nameFilter;
}

