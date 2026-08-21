package com.voc.service.insights.engine.model.model;

import jakarta.validation.constraints.NotEmpty;
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
public class InsLabelCorrectionModel implements Serializable {

    @NotEmpty(message = "dataIdList不能为空")
    private List<String> dataIdList;

}
