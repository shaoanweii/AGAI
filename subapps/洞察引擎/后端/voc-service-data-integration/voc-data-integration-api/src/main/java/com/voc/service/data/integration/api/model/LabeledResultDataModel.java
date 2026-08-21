package com.voc.service.data.integration.api.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: LabeledResultDataModel
 * @Package: com.voc.service.data.integration.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/30 11:15
 * @Version:1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class LabeledResultDataModel implements Serializable {
    String   id;
    String   oneId;            //客户标识
    String   channelBiz;       //渠道（业务分类）
    String   channelDc;        //渠道（中台分类）
    String   dataId;           //原数据标识
    LocalDateTime   createTime;       //中台数据入库时间
    LocalDateTime   dataCreateTime;  //业务数据产生时间
    String   content;           //内容
    String   customerName;     //客户名称
    String   customerSex;      //客户性别
    String   licensePlate;     //车牌号
    String   carSeriesCode;      //车系编码
    String   dealershipCodeDelivery;      //专营店编码（交车）
    String   dealershipCodePurchase;      //专营店编码（销售）
    String   dealershipCodeReturn;        //专营店编码（最后回厂）
    Object   extAttrs;         //扩展字段1
    Object   voc_attrs;        //voc解析后的属性值
}
