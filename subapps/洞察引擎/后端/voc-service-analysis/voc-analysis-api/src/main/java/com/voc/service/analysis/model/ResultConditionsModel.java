package com.voc.service.analysis.model;

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
public class ResultConditionsModel implements Serializable {

    private String startTime;

    private String endTime;

    private List<BrandCarModel> brandCarModelList;

    private  List<String> mentionCarSeriesList;


}
