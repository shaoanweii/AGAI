package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class ValidDataModel extends Page implements Serializable {

    @Schema(description = "规则ID")
    @NotBlank(message = "规则ID不能为空")
    private String rulesId;

    @Schema(description = "工作ID")
    @NotBlank(message = "工作ID不能为空")
    private String workId;

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "渠道ID")
    private List<String> channelId;

    @Schema(description = "是否命中 0没命中 1命中")
    private String hitState;

    @Schema(description = "数据对比 0一致 1不同")
    private String dataCompare;

}
