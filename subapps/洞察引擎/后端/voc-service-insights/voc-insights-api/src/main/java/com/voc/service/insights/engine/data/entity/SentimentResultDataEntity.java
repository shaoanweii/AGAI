package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "情感分析结果数据实体")
@TableName("voc_anal_flow_model_sentiment_result")
public class SentimentResultDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // 基础信息字段
    @Schema(description = "主键id")
    private String id;

    @Schema(description = "发布时间（用于按天分区）")
    private LocalDateTime publishTime;

    @Schema(description = "渠道标识")
    private String channelId;

    @Schema(description = "业务主键id")
    private String dataId;

    @Schema(description = "唯一Id")
    private String oneId;

    @Schema(description = "接收处理标识")
    private String workId;

    @Schema(description = "客户标识")
    private String clientId;

    @Schema(description = "内容类型：文本：text、 工单：order")
    private String contentType;

    @Schema(description = "是否是示例数据")
    private String sampleDataType;

    @Schema(description = "原文id")
    private String originalId;

    @Schema(description = "原文关联id")
    private String inputDataId;

    @Schema(description = "原文片段")
    private String originalTextScene;

    @Schema(description = "品牌名称")
    private String brandCode;

    @Schema(description = "车系名称")
    private String carSeriesCode;

    @Schema(description = "标签类型：1服务 2产品 3品质")
    private String labelType;

    @Schema(description = "用车场景")
    private String scenario;

    @Schema(description = "情感倾向")
    private String sentiment;

    @Schema(description = "用户意图")
    private String intentionType;

    @Schema(description = "聚合后的观点=>标签叶子结点")
    private String topic;

    @Schema(description = "原始观点")
    private String opinion;

    @Schema(description = "评价主体【如：雨刮器】")
    private String subject;

    @Schema(description = "故障问题严重性等级")
    private String faultLevel;

    @Schema(description = "描述/评价内容")
    private String description;

    @Schema(description = "情感严重程度")
    private String sentimentScore;

    @Schema(description = "提取的热词")
    private String keywords;

    @Schema(description = "模型类型：1 智谱AI离线 2智谱AI实时 3聚类大模型")
    private Integer modelType;

    @Schema(description = "是否遗弃数据 是：1，否：0")
    private Integer abandon;

    @Schema(description = "是否完成计算（整型）：是=1，否=0")
    private Integer done;

    @Schema(description = "原始数据")
    private String rawData;

    @Schema(description = "通用扩展字段")
    private String extFields;

    @Schema(description = "业务扩展字段1")
    private String bizExtAttrs;

    @Schema(description = "业务扩展字段2")
    private String bizExtAttrs2;

    @Schema(description = "业务扩展字段3")
    private String bizExtAttrs3;

    @Schema(description = "客户信息扩展字段")
    private String custExtAttrs;

    @Schema(description = "车辆信息扩展字段")
    private String vhlExtAttrs;

    @Schema(description = "经销商信息扩展字段")
    private String dealerExtAttrs;

    @Schema(description = "产品经销商信息扩展字段")
    private String prdExtAttrs;

    @Schema(description = "标签产品经销商信息扩展字段")
    private String tagsExtAttrs;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "标题")
    private String title;
}
