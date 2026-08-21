package com.voc.service.insights.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ModOpinionRelationDataVo implements Serializable {

    private String opinion;

    private String topic;

    private String relationLabel;

    private LocalDateTime createTime;

    private String opinionId;

    private Integer labelType;

    private String newId;

    private String clientId;

    private String channelId;


}
