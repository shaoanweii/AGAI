package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ChannelInfoModel implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "id")
    private String id;

    @Schema(description = "code")
    private String code;

    /**
     * 父级id
     */
    @Schema(description = "级id")
    private String parentId;

    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    private String name;

    /**
     * 渠道英文名称
     */
    @Schema(description = "渠道英文")
    private String nameEn;

    @Schema(description = "子级渠道")
    private List<ChannelInfoModel> child;
}
