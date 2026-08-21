package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 
 * @version 1.0.0
 * @ClassName ModelSentimentResultEntity
 * @description 模型情感分析结果实体类
 * @createTime 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_flow_model_sentiment_result")
public class ModelSentimentResultEntity implements Serializable {
    
    /**
     * 主键id
     */
    private String id;
    
    /**
     * 发布时间（用于按天分区）
     */
    private LocalDateTime publishTime;
    
    /**
     * 渠道标识
     */
    private String channelId;
    
    /**
     * 业务主键id
     */
    private String dataId;
    
    /**
     * 唯一Id
     */
    private String oneId;
    
    /**
     * 接收处理标识
     */
    private String workId;
    
    /**
     * 客户标识
     */
    private String clientId;
    
    /**
     * 内容类型：文本：text、 工单：order
     */
    private String contentType;
    
    /**
     * 是否是示例数据
     */
    private String sampleDataType;
    
    /**
     * 原文id
     */
    private String originalId;
    
    /**
     * 原文关联id
     */
    private String inputDataId;
    
    /**
     * 原文片段
     */
    private String originalTextScene;
    
    /**
     * 品牌名称
     */
    private String brandCode;
    
    /**
     * 车系名称
     */
    private String carSeriesCode;
    
    /**
     * 标签类型：1服务 2产品 3品质
     */
    private String labelType;
    
    /**
     * 用车场景
     */
    private String scenario;
    
    /**
     * 情感倾向
     */
    private String sentiment;
    
    /**
     * 用户意图
     */
    private String intentionType;
    
    /**
     * 聚合后的观点=>标签叶子结点
     */
    private String topic;
    
    /**
     * 原始观点
     */
    private String opinion;
    
    /**
     * 评价主体【如：雨刮器】
     */
    private String subject;
    
    /**
     * 故障问题严重性等级
     */
    private String faultLevel;
    
    /**
     * 描述/评价内容
     */
    private String description;
    
    /**
     * 情感严重程度
     */
    private String sentimentScore;
    
    /**
     * 提取的热词
     */
    private String keywords;
    
    /**
     * 模型类型：1 智谱AI离线 2智谱AI实时 3聚类大模型
     */
    private Integer modelType;
    
    /**
     * 是否遗弃数据 是：1，否：0
     */
    private Integer abandon;
    
    /**
     * 是否完成计算（整型）：是=1，否=0
     */
    private Integer done;
    
    /**
     * 原始数据
     */
    private String rawData;
    
    /**
     * 通用扩展字段
     */
    private String extFields;
    
    /**
     * 业务扩展字段1
     */
    private String bizExtAttrs;
    
    /**
     * 业务扩展字段2
     */
    private String bizExtAttrs2;
    
    /**
     * 业务扩展字段3
     */
    private String bizExtAttrs3;
    
    /**
     * 客户信息扩展字段
     */
    private String custExtAttrs;
    
    /**
     * 车辆信息扩展字段
     */
    private String vhlExtAttrs;
    
    /**
     * 经销商信息扩展字段
     */
    private String dealerExtAttrs;
    
    /**
     * 产品经销商信息扩展字段
     */
    private String prdExtAttrs;
    
    /**
     * 标签产品经销商信息扩展字段
     */
    private String tagsExtAttrs;
    
    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 记录更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 插入时间
     */
    private LocalDateTime insertDt;
    
    /**
     * 标题
     */
    private String title;
}
