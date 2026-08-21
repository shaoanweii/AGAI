package com.voc.service.insights.engine.model.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsLabelCorrectionInfoModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    private String dataId;


    private String correctionRecordId;

    /**
     * 渠道ID
     */
    private String channelId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 意图类型
     */
    private String intentionType;

    /**
     * 情感
     */
    private String sentiment;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * ID（具体含义可根据业务补充）
     */
    private String id;

    /**
     * 唯一ID（one_id，具体含义可根据业务补充）
     */
    private String oneId;

    /**
     * 工号
     */
    private String workId;

    /**
     * 场景
     */
    private String scenario;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 样本数据类型
     */
    private String sampleDataType;

    /**
     * 原始ID
     */
    private String originalId;

    /**
     * 输入数据ID
     */
    private String inputDataId;

    /**
     * 原始文本场景
     */
    private String originalTextScene;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 车系编码
     */
    private String carSeriesCode;

    /**
     * 标签类型
     */
    private String labelType;

    /**
     * 观点
     */
    private String opinion;

    /**
     * 主题/主体（与topic区分，可根据业务细化）
     */
    private String subject;

    /**
     * 故障等级
     */
    private String faultLevel;

    /**
     * 描述
     */
    private String description;

    /**
     * 情感得分
     */
    private String sentimentScore; // 若为数值型，可改为Integer/Double

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 原始数据
     */
    private Object rawData;

    /**
     * 扩展字段
     */
    private Object extFields;

    /**
     * 业务扩展属性1
     */
    private Object bizExtAttrs;

    /**
     * 业务扩展属性2
     */
    private Object bizExtAttrs2;

    /**
     * 业务扩展属性3
     */
    private Object bizExtAttrs3;

    /**
     * 客户扩展属性
     */
    private Object custExtAttrs;

    /**
     * 车辆扩展属性
     */
    private Object vhlExtAttrs;

    /**
     * 经销商扩展属性
     */
    private Object dealerExtAttrs;

    /**
     * 产品扩展属性
     */
    private Object prdExtAttrs;

    /**
     * 标签扩展属性
     */
    private Object tagsExtAttrs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否废弃（0/1或布尔值，可根据业务调整类型）
     */
    private String abandon; // 若为布尔型，可改为Boolean

    /**
     * 是否完成（0/1或布尔值，可根据业务调整类型）
     */
    private String done; // 若为布尔型，可改为Boolean
}


