package com.voc.service.analysis.largeModel.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
public class NlpResult implements Serializable {

    private List<Dimension> dimensions;
}
