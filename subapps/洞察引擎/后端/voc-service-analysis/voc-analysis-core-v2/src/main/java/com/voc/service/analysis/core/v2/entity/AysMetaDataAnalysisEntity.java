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
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_flow_mate_data_full")
public class AysMetaDataAnalysisEntity implements Serializable {
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
    /**
     * 渠道标识
     */
    String channelId;

    /**
     * 内容类型：文本：text、 工单：order
     */
    String contentType;
    /**
     * 原始数据
     */
    Object data;
    /**
     * 是否完成计算 是：1，否：0"
     */
    @Builder.Default
    String done = "0";

    /**
     * 数据状态
     */
    Integer dataStatus;

    /**
     * 标题
     */
    String title;

    /**
     * 内容
     */
    String content;

    /**
     * 昵称
     */
    String userName;
    /**
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();

    LocalDateTime publishTime;

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

    @TableField(exist = false)
    String carSeriesName;


    @TableField(exist = false)
    String mentionCarSeries;


}
