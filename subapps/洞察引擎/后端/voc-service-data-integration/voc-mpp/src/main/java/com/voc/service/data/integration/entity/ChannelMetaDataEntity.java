package com.voc.service.data.integration.entity;

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
@TableName(value = "voc_anal_di_stg_mate_data_m_inc_v")
public class ChannelMetaDataEntity implements Serializable {
    String id;
    String oneId;            //客户标识
    String channelCode;        //渠道（中台分类）
    String dataId;           //原数据标识
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();       //中台数据入库时间
    LocalDateTime dataCreateTime;  //业务数据产生时间
    LocalDateTime dataUpdateTime;  //业务数据更新时间
    String brand ;
    String series;
    String model;
    String isOuter;
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
    String isWsaterArmy;
    String weight;
    String content;
    String contentType; //内容
    Object attrs;         //扩展字段1
    Object attrs2;        //扩展字段2
    Object attrs3;        //扩展字段3
    Object custExtAttrs;        //扩展字段3
    Object vhlExtAttrs;        //扩展字段3
    Object dealerExtAttrs;        //扩展字段3
    Object prdExtAttrs;        //扩展字段3

}
