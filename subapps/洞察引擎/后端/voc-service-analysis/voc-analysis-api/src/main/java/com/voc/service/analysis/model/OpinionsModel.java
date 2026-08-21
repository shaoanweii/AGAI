package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpinionsModel implements Serializable {

    private String id;

    private String desc;

    private String opinionSentiment;

    private String subject;

    private String opinion;

    private String carBodyLabel;

    private String viewLabel;

    private Object extFields;
}
