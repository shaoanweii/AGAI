package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateSourceModel implements Serializable {

    private String clientId;

    private String batchId;

    private List<String> errorIds;

    @Schema(description = "-1失败 2成功")
    private String status;

}
