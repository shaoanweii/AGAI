package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: OrderTypeModel
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 15:47
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ods_channel_meta_data_view")
public class ChannelMetaDataEntity implements Serializable {
    String id;
    String oneId;            //客户标识
    String channelBiz;       //渠道（业务分类）
    String channelDc;        //渠道（中台分类）
    String dataId;           //原数据标识
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();       //中台数据入库时间
    LocalDateTime bizCreateTime;  //业务数据产生时间
    LocalDateTime bizUpdateTime;  //业务数据更新时间
    LocalDateTime deliveryAtTime;  //交车时间
    String content;           //内容
    String customerName;     //客户名称
    String customerSex;      //客户性别
    String customerPhone;      //客户手机号
    String licensePlate;     //车牌号
    String carSeriesCode;      //车系编码
    String dealershipCodeDelivery;      //专营店编码（交车）
    String dealershipCodePurchase;      //专营店编码（销售）
    String dealershipCodeReturn;        //专营店编码（最后回厂）
    Object extAttrs;         //扩展字段1
    Object extAttrs2;        //扩展字段2
    Object extAttrs3;        //扩展字段3

}
