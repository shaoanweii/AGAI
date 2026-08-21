package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ays_api_reslt_data_analysis")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysModelResltDataAnalysisEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String newId;
    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;
    private String originalId;

    private String sampleDataType;

    private String originalTextScene;

    private String brandCodeName;

    private String carSeriesName;

    private String labelType;

    private String labelTypeLevelFirst;

    private String labelTypeLevelSecond;

    private String labelTypeLevelThree;

    private String labelTypeLevelFour;

    private String labelTypeLevelFive;

    private String scenario;

    private String sentiment;

    private String intentionType;

    private String topic;

    private String opinion;

    private String subject;

    private String faultLevel;

    private String description;

    private String sentimentScore;

    private String keywords;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();

    @TableField(exist = false)
    String startTime;
    @TableField(exist = false)
    String endTime;

    private Integer modelType;

    private Object extFields;

    private Object bizExtAttrs;

    private Object bizExtAttrs2;

    private Object bizExtAttrs3;

    private String oneId;


    String done;

    @TableField(exist = false)
    private LocalDateTime max;

    @TableField(exist = false)
    private LocalDateTime min;

}
