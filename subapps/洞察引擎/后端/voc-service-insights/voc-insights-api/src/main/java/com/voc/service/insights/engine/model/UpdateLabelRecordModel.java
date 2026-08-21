package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateLabelRecordModel implements Serializable {

    @Schema(description = "主键")
    @NotBlank(message = "主键不能为空")
    private String newId;

    @Schema(description = "审核状态")
    @NotBlank(message = "状态不能为空")
    private String auditStatus;

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

}
