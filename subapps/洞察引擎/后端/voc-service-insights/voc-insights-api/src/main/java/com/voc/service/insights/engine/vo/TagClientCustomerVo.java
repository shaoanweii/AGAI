package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.Client;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagClientCustomerVo implements Serializable {

    /**
     * 应用客户id
     */
    @Schema(description = "标签id")
    private String tagId;

    /**
     * 应用客户id
     */
    @Schema(description = "应用客户id")
    private String clientId;

    /**
     * 全称
     */
    @Schema(description = "全称")
    private String fullName;
    /**
     * 简称
     */
    @Schema(description = "简称")
    private String abbreviation;
    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;
}
