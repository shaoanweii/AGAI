package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class ResultConditionsParamModel implements Serializable {

    private List<String> workIdList;

    private String clientId;

    @Schema(description = "时间")
    private String date;

    @Schema(description = "1本地上传 2系统集成")
    private Integer showType;

}
