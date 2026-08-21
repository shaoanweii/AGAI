package com.voc.service.analysis.core.v2.entity;

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
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_flow_pre_rules_result_data_full")
public class AysPreprocessDataEntity implements Serializable {
    /**
     * 主键
     */
    String id;
    String dataId;
    /**
     * 接收处理标识
     */
    String workId;
    /**
     * 客户标识
     */
    String clientId;

    String contentType;
    /**
     * 渠道标识
     */
    String channelId;
    /**
     * 原始数据
     */
    Object data;
    /**
     * 内容md5值
     */
    String dataMd5;

    LocalDateTime publishTime;
    /**
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();

    /**
     * 记录在规则执行过程中命中了哪些规则 【规则id集合】
     */
//    @Builder.Default
    String hitRules;

    /**
     * 是否遗弃数据 是：1，否：0"
     */
    @Builder.Default
    String abandon = "0";
    /**
     * 是否完成计算 是：1，否：0"
     */
    @Builder.Default
    String done = "0";


    Integer modelType;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    Object custExtAttrs;        //扩展字段3
    Object vhlExtAttrs;        //扩展字段3
    Object dealerExtAttrs;        //扩展字段3
    Object prdExtAttrs;        //扩展字段3



    String oneId;

    /**
     * 重试次数
     */
   /* @Builder.Default
    String retry = "0";*/

}
