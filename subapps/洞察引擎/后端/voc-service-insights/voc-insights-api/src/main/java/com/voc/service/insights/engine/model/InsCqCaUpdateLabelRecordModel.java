package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsCqCaUpdateLabelRecordModel implements Serializable {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "主键")
    @NotBlank(message = "主键不能为空")
    private List<String> idList;

    @Schema(description = "审核状态")
    @NotBlank(message = "状态不能为空")
    private String auditStatus;

}
