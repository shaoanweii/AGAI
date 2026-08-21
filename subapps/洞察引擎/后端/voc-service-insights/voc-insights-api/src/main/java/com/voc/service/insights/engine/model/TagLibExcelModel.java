package com.voc.service.insights.engine.model;

import lombok.Data;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/14 下午5:14
 * @描述:
 **/
@Data
public class TagLibExcelModel {
    private String id;
    private String scenarioAttr;
    //标签类型
//    private String tagType;
//    //一级代码
//    private String firstCode;
//    //二级代码
//    private String secondCode;
//    //三级代码
//    private String thirdCode;
    //四级代码
//    private String fourCode;
//    五级代码
//    private String fiveCode;
    //一级名称
//    private String firstName;
    //二级名称
//    private String secondName;
    //三级名称
//    private String thirdName;
    //四级名称
//    private String fourName;
    //五级名称
//    private String fiveName;
    //旅程
//    private String journey;



    //代码精准度
//    private String codePrecision;
    //客户问题
//    private String customerIssue;
    //问题程度
//    private String questionDegree;
    //代码状态
//    private String codeStatus;
    //事件清晰度
    private String eventClarity;
    //情感
    private String emotion;
    //意图
    private String intention;
    //客户问题分级(S、A、B、C等)
    private String tagCustomerIssueClassification;
    //代码状态(有效、无效等)
    private String tagCodeStatus;
    //业务领域
    private String businessDomain;
    //需推送的高价值建议标识
    private String tagHighValueFlag;
    //场景属性
//    private String sceneAttribute;
    //用车场景
//    private String carScenario;
    //需推送的
//    private String push;
    //需回复的
//    private String reply;
    //针对五级明细高质量VOC标识
//    private String highQualityVoc;
    //能源属性
//    private String energyAttribute;
    //是否需要闭环
//    private String needForvclosedLoop;
    //主责部门
//    private String responsibleDept;
    //责任部门
//    private String accountableDept;
    //抄送部门
//    private String ccDept;


}
