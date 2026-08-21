package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 13:56
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mod_opinion_relation_data")
public class ModOpinionRelationDataEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String opinion;

    private String topic;

    private String relationLabel;

    private String clusterId;

    private String opinionId;

    private Integer labelType;

    private LocalDateTime createTime;
}
