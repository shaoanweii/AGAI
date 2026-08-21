package com.voc.service.analysis.model;

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
public class AiData implements Serializable {

    private String id;

    private String brand;

    private String brandCode;

    private String carSeries;

    private String carSeriesCode;

    private String sentence;

    private List<String> mentionCarSeries;

    private List<String> mentionCarSeriesCode;

    private List<FaultsModel> faults;

    private List<ServicesModel> services;

    private List<DimensionsModel> dimensions;

    private List<String> keywords;

    private List<OpinionsModel> unmarkedOpinions;

}
