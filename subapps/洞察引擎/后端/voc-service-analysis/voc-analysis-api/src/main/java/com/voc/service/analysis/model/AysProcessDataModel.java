package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class AysProcessDataModel implements Serializable {
    /**
     * 主键
     */
    String dataId;

    String id;

    String originalId;

    Integer modelType;
    /**
     * 接收处理标识
     */
    String workId;
    /**
     * 客户标识
     */
    String clientId;
    /**
     * 渠道标识
     */
    String channelId;
    String contentType;
    String adType;
    /**
     * 原始数据
     */
    Object data;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    private Object custExtAttrs;
    private Object vhlExtAttrs;
    private Object dealerExtAttrs;
    private Object prdExtAttrs;

    String oneId;

    String validData;
    /**
     * 内容md5值
     */
    String dataMd5;

    LocalDateTime publishTime;
    /**
     * 接收时间
     */
    LocalDateTime createTime;

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

    Integer modelCount;

    /**
     * 记录在规则执行过程中命中了哪些规则
     */
    @Builder.Default
    List<RuleModel> hitRuleList = new ArrayList<>();

   /* @Builder.Default
    List<RuleModel> hitValidRuleList = new ArrayList<>();*/
}
