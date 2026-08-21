package com.voc.service.insights.engine.model.data;

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
 * 语料库数据集(InsDataExpect)请求实体对象
 *
 * @author leiww
 * @since 2024-03-05 14:44:43
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
@Tag(name = "语料库数据集", description = "语料库数据集")
public class InsDataExpectModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 数据集语料库名称
     */
    @Size(max = 50, message = "名称长度不能超过50个字符")
    @Schema(description = "数据集语料库名称")
    private String name;
    /**
     * 数据格式
     */
    @Dict(code = InsightsConstants.DATA_TYPE)
    @Schema(description = "数据格式")
    private String format;
    /**
     * 数据总数
     */
    @Schema(description = "数据总数")
    private Integer count;
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

    @Size(max = 20, message = "名称模糊过滤长度不能超过20个字符")
    @Schema(description = "名称模糊过滤")
    private String nameFilter;

    @Schema(description = "数据格式过滤")
    private Set<String> formatFilters;
}

