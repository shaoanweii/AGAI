package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024 年 01 月 15 日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_flow_mate_data_full_ext")
public class AysMetaDataExtAnalysisEntity implements Serializable {
    /**
     * 主键
     */
    String id;

    String clientId;

    LocalDateTime dataCreateTime;

    String dataId;

    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();

    LocalDateTime dataUpdateTime;

    String contentType;

    String channelCode;

    String brand;

    String series;

    String model;

    String isOuter;

    String oneId;

    String idCarNo;

    String mobile;

    String email;

    String globalId;

    String userId;

    String userName;

    String vhlId;

    String vhlVin;

    String dlrId;

    String dlrCode;

    String dlrType;

    String marketId;

    String title;

    String content;

    String isWsaterArmy;

    String weight;

    Object attrs;

    Object attrs2;

    Object attrs3;

    Object custExtAttrs;
    Object vhlExtAttrs;
    Object dealerExtAttrs;
    Object prdExtAttrs;

    String workId;

    @Builder.Default
    String done = "0";

    @Builder.Default
    String abandon = "0";

    Integer dataStatus;

    Integer modelType;

    String ds;
    @Builder.Default
    LocalDateTime insertDt = LocalDateTime.now();

}
