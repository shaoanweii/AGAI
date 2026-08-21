package com.voc.service.insights.engine.model;

import lombok.Data;

/**
 * @创建者: fanrong
 * @创建时间: 2025/7/18 16:04
 * @描述:
 **/
@Data
public class ChannelExcelModel {
    /**
     * 渠道类型
     */
    private  String type;
    /**
     * 一级渠道
     */
    private String firstChannel;
//    /**
//     * 二级渠道
//     */
    private String secondChannel;
    private String code;
    /**
     * 数据源类型
     */
    private String dataSourceType;
    /**
     * 渠道描述
     */
    private String describe;

    /**
     * 是否核心渠道
     */
    private String isCoreChannel;



}
