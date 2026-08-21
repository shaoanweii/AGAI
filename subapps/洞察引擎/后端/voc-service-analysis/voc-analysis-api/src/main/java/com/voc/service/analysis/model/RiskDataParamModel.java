package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDataParamModel extends Page implements Serializable {

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "项目ID")
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    @Schema(description = "风险类型")
    @NotBlank(message = "风险类型不能为空")
    private String riskType;

    @Schema(description = "品牌")
    @NotEmpty(message = "品牌不能为空")
    private List<String> brandCode;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "洞察周期")
    private List<String> statisticType;

    @Schema(description = "风险等级")
    private List<String> riskLevel;

    @Schema(description = "关键词")
    private String keywords;
}
