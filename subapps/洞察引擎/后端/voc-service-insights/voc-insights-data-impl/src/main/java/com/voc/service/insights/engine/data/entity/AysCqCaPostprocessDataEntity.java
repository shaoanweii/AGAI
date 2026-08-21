package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_sentiment_annotations_results_ins_v")
public class AysCqCaPostprocessDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. 基础数据标识
    private String id;
    private String dataId;
    private String workOrderId;
    private String questId;


    // 2. 渠道与内容信息
    private String channelCatagory; // 注意：原字段"catagory"可能为"category"拼写，保持原样
    private String channelCode;
    private String channelName;
    private String contentType;
    private String title;
    private String originalTextScene;
    private String originalText;
    private String originalLink;
    private String topic;
    private String topicText;
    private String opinion;


    // 3. 品牌与车型信息
    private String brandCode;
    private String brandName;
    private String automark;
    private String carSeriesCode;
    private String carSeriesName;
    private String modelName;
    private String marketId;
    private String competitiveType;
    private String seriesFactory;

    // 4. 情感与意图标识
    private String sentiment;
    private String intention;
    private String tagAccuracy;
    private String tagCustomerIssueClassification;
    private String tagIssueSeverity;
    private String tagCodeStatus;
    private String tagBusinessDomain;
    private String tagEventClarity;
    private String tagHighValueFlag;
    private String tagComplaintFlagNeedingReply;
    private String tagComplaintFlagNeedingPrtvMsg; // 推测"prtv"为特定业务缩写
    private String tagHighQualityVocFlag; // "voc"可能为"Voice of Customer"缩写
    private String tagNewEnergyOrFuel;
    private String tagNeedForvclosedLoop; // 推测"forvclosed"为业务特定拼写


    // 5. 时间信息
    private LocalDateTime dataCreateTime; // 建议使用时间类型
    private LocalDateTime publishTime;
    private LocalDateTime createTime;


    // 6. 用户旅程与场景
    private String oneId;
    private String userJourney1;
    private String userJourney2;
    private String userJourney3;
    private String usageScenarioFirst;
    private String usageScenarioSecond;
    private String d2cResponsibleDept; // "d2c"可能为"Direct to Consumer"缩写
    private String d2cAccountableDept;
    private String d2cCcDept;


    // 7. 客户信息
    private String custGlobalId;
    private String custClassify;
    private String custMainPhone;
    private String isCarOwner; // 可改为Boolean
    private String custAge; // 可改为Integer
    private String custAgeGroup;
    private String custName;
    private String custGender;
    private String custHighEducaion; // 注意：原字段"educaion"可能为"education"拼写
    private String marrigeStatue; // 注意：原字段"marrige"可能为"marriage"，"statue"可能为"status"
    private String familyIncome;
    private String isExchangeFlg; // 可改为Boolean
    private String purchaseCarTimes; // 可改为Integer
    private String isMemberFlg; // 可改为Boolean
    private String custProvinceCode;
    private String custProvince;
    private String custCityCode;
    private String custCity;
    private String custType;
    private String custLivedProv;
    private String custLivedCity;
    private String custProfession;


    // 8. 车辆信息
    private String vhlVin;
    private String vhlColorName;
    private String vhlProductDate; // 可改为LocalDate
    private String vhlOfflineDate; // 可改为LocalDate
    private String vhlIsAbroad; // 可改为Boolean
    private String vhlDisCh;
    private String vhlDisMt;
    private String vhlEngClsf; // "clsf"可能为"classification"缩写
    private String vhlEngSeris; // 注意：原字段"seris"可能为"series"
    private String vhlVehType;
    private String vhlCountry;
    private String vhlBdClsf;
    private String vhlSegMt;
    private String vhlPowClsf;
    private String vhlFuClsf; // "fu"可能为"fuel"缩写
    private String vhlModlSt; // "modl"可能为"model"缩写
    private String vhlStdPlntCode; // "plnt"可能为"plant"缩写


    // 9. 经销商信息（oc/dc/mc三级）
    // oc级
    private String dlrOcId;
    private String dlrOcCode;
    private String dlrOcName;
    private String dlrOcProvinceCode;
    private String dlrOcProvince;
    private String dlrOcCityCode;
    private String dlrOcCity;
    // dc级
    private String dlrDcId;
    private String dlrDcCode;
    private String dlrDcName;
    private String dlrDcProvinceCode;
    private String dlrDcProvince;
    private String dlrDcCityCode;
    private String dlrDcCity;
    // mc级
    private String dlrMcId;
    private String dlrMcCode;
    private String dlrMcName;
    private String dlrMcProvinceCode;
    private String dlrMcProvince;
    private String dlrMcCityCode;
    private String dlrMcCity;


    // 10. 作者与互动数据
    private String isWsaterArmy; // 推测"wsater"为"water"拼写（水军标识）
    private String isManagerFocused; // 可改为Boolean
    private String isBigV; // 可改为Boolean
    private String authorId;
    private String authorNick;
    private String isMainPost; // 可改为Boolean
    private String viewCount; // 可改为Integer
    private String commentCount; // 可改为Integer
    private String likeCount; // 可改为Integer
    private String shareCount; // 可改为Integer
    private String favoriteCount; // 可改为Integer


    // 11. 问题与工单信息
    private String questType;
    private String questAnswerScore; // 可改为Integer
    private String questBusinessType;
    private String questBusinessScenario;
    private String isOuter; // 可改为Boolean
    private String hotWord;
    private String keywords;


    // 12. 多类型标签（cpt/ujy/cma/dom/nps/vtr）
    // cpt标签
    private String cptTagFirstCode;
    private String cptTagSecondCode;
    private String cptTagThreeCode;
    private String cptTagFourCode;
    private String cptTagFirst;
    private String cptTagSecond;
    private String cptTagThree;
    private String cptTagFour;
    // ujy标签
    private String ujyTagFirstCode;
    private String ujyTagSecondCode;
    private String ujyTagThreeCode;
    private String ujyTagFourCode;
    private String ujyTagFirst;
    private String ujyTagSecond;
    private String ujyTagThree;
    private String ujyTagFour;
    // cma标签
    private String cmaTagFirstCode;
    private String cmaTagSecondCode;
    private String cmaTagThreeCode;
    private String cmaTagFourCode;
    private String cmaTagFirst;
    private String cmaTagSecond;
    private String cmaTagThree;
    private String cmaTagFour;
    // dom标签
    private String domTagFirstCode;
    private String domTagSecondCode;
    private String domTagThreeCode;
    private String domTagFourCode;
    private String domTagFirst;
    private String domTagSecond;
    private String domTagThree;
    private String domTagFour;
    // nps标签
    private String npsTagFirstCode;
    private String npsTagSecondCode;
    private String npsTagThreeCode;
    private String npsTagFourCode;
    private String npsTagFirst;
    private String npsTagSecond;
    private String npsTagThree;
    private String npsTagFour;
    // vtr标签
    private String vtrTagFirstCode;
    private String vtrTagSecondCode;
    private String vtrTagThreeCode;
    private String vtrTagFourCode;
    private String vtrTagFirst;
    private String vtrTagSecond;
    private String vtrTagThree;
    private String vtrTagFour;
    private String abandon;



}
