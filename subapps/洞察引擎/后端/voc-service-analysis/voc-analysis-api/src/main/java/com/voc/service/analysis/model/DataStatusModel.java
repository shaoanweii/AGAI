package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStatusModel implements Serializable {

    @Schema(description = "成功数量")
    @Builder.Default
    private String finishCount = "0";

    @Schema(description = "失败数量")
    @Builder.Default
    private String failCount = "0";

    @Schema(description = "失败数量")
    private String createTime;

    private String dataStatus;

    @Builder.Default
    private Integer num = 0;


}
