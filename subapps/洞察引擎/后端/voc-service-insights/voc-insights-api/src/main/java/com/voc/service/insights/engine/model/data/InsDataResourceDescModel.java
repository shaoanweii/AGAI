package com.voc.service.insights.engine.model.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.IConditionFilters;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 资源详情(InsDataResourceDesc)请求返回实体类
 *
 * @author leiww
 * @since 2024-04-02 17:00:19
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "InsDataResourceDesc", description = "资源详情")
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class InsDataResourceDescModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 资源id
     */
    @Schema(description = "资源id")
//    @DataResources
    private String resourceId;
    @Schema(description = "所属客户")
    private String customer;
    /**
     * 资源详情
     */
    @Schema(description = "资源详情")
    private String name;
    /**
     * 状态：全部、已启用、未启用、已禁用
     */
    @Schema(description = "状态：全部、已启用、未启用、已禁用")
    @Dict(code = IConditionFilters.REPOSITORY_STATUS)
    private String status;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    @Schema(description = "资源详情模糊过滤")
    private String nameDescFilter;

    @Schema(description = "不为当前数据id")
    private String notIdFilter;

    @Schema(description = "状态过滤")
    private Set<String> statusFilters;

    @Schema(description = "id集合")
    private List<String> ids;
}

