package com.voc.service.analysis.largeModel.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class NormalizedOpinion implements Serializable {

    private String id;
    private String text;
}
