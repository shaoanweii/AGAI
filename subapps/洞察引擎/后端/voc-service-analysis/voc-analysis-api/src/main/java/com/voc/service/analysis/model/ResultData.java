package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResultData implements Serializable {

    private List<AiData> result;

    private String clientId;

}
