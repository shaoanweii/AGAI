package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:34
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsChannelInfoModel extends Page implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "id")
    private String id;

    /**
     * 父级id
     */
    @Schema(description = "父级id")
    @Builder.Default
    private String parentId = "0";

    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    private String name;

    /**
     * 渠道类型
     */
    @Schema(description = "渠道类型")
    private String type;

    /**
     * 渠道英文名称
     */
    @Schema(description = "渠道英文名称")
    private String nameEn;
    /**
     * 渠道状态
     */
    @Schema(description = "渠道状态")
    private String status;
    /**
     * 所属客户
     */
    @Schema(description = "所属客户")
    private String clientId;

    private List<String> channelIds;
    private List<String> channelCodes;
    private String code;
    @Schema(description = "渠道层级")
    private Integer level;
    @Schema(description = "渠道层级")
    private List<Integer> levels;
}
