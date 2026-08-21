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
@TableName("voc_anal_flow_model_tags_unlabeled_data_full")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysModelResultDataAnalysisMissEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;
    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;

    private String brandCode;

    private String carSeriesCode;

    private String opinion;

    private String opinionSentiment;

    private String subject;

    private String description;

    private String carBodyLabel;

    private String viewLabel;

    private Integer modelType;

    private Object extFields;

    private Object bizExtAttrs;

    private Object bizExtAttrs2;

    private Object bizExtAttrs3;

    private String oneId;

    private LocalDateTime publishTime;

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
    @Builder.Default
    String done = "1";

    @TableField(exist = false)
    private Object rawData;

}
