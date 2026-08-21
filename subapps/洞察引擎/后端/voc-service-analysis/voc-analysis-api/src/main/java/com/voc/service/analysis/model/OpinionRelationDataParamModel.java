package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpinionRelationDataParamModel implements Serializable {

    private String opinion;

    private String topic;

    private String relationLabel;

    private String clientId;

    private String clusterId;

    private String opinionId;

    private String channelId;

    private Integer labelType;

    private LocalDateTime createTime;
}
