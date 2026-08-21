package com.voc.service.insights.engine.model.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.Client;
import com.voc.service.insights.engine.api.annotation.ResourceType;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import com.voc.service.insights.engine.vo.InsDataResourceDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * (InsDataResource)请求返回实体类
 *
 * @author leiww
 * @since 2024-04-02 15:27:05
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "InsDataResource", description = "")
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class InsDataResourceModel extends Page  implements Serializable{

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 资源名称
     */
    @Schema(description = "资源名称")
    private String name;
    @Schema(description = "数量")
    private Integer cnt;
    /**
     * 资源类型 custom:定制 general:标准
     */
    @Schema(description = "资源类型")
    private String type;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "图标")
    private String icon;
    /**
     * 所属客户
     */
    @Schema(description = "所属客户")
    private String customer;
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

    @Schema(description = "资源名称模糊过滤")
    private String nameFilter;

    @Schema(description = "不为当前数据id")
    private String notIdFilter;

    @Schema(description = "类型集合")
    private List<String> typeList;

    private List<Serializable> idList;
    @Schema(description = "关键词信息")
    private List<InsDataResourceDetailVo> keywordList;

    private String status;
    @Schema(description = "是否允许删除")
    private Boolean allowDeletion;
}

