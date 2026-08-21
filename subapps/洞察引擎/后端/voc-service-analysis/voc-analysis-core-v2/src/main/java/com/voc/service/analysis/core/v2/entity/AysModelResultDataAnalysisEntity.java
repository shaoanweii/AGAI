package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("voc_anal_flow_model_tags_result_data_full")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysModelResultDataAnalysisEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;
    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;
    private String originalId;

    private String sampleDataType;

    private String originalTextScene;

    private String brandCode;

    private String carSeriesCode;

    private String labelType;

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

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
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

    private Object rawData;
    private Object custExtAttrs;
    private Object vhlExtAttrs;
    private Object dealerExtAttrs;
    private Object prdExtAttrs;

    private String oneId;

    @Builder.Default
    String done = "0";

    @TableField(exist = false)
    private LocalDateTime max;

    @TableField(exist = false)
    private LocalDateTime min;

}
