package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
@TableName(value = "ays_meta_data_analysis")
public class AysMetaDataAnalysisEntity implements Serializable {
    /**
     * 主键
     */
    String newId;

    String id;
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
    String done;

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
//    @Builder.Default
//    LocalDateTime createTime = LocalDateTime.now();

//    LocalDateTime publishTime;

    Integer modelType;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    String oneId;

    @TableField(exist = false)
    String carSeriesName;


    @TableField(exist = false)
    String mentionCarSeries;


}
