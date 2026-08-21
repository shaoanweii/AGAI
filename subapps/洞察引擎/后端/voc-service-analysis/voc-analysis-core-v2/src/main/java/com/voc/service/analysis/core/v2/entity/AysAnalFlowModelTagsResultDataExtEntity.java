package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("voc_anal_flow_model_tags_result_data_ext")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysAnalFlowModelTagsResultDataExtEntity implements Serializable {

    private String id;

    private LocalDateTime publishTime;
    private String dataId;
    private String channelCatagory; // 注意：对应数据库 channel_catagory（含拼写）
    private String channelCode;
    private String channelName;
    private String brandCode;
    private String brandName;
    private String carSeriesCode;
    private String carSeriesName;
    private String modelName;
    private String contentType;
    private String title;
    private String content;
    private String sentiment;
    private String intention;
    private Date dataCreateTime;
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime;
    private String isOuter; // '1'/'0' 字符串标识
    private String hotWord;
    private String keywords;
    private String originalTextScene;
    private String marketId;
    private String competitiveType;
    private String isCore;
    private String seriesFactory;
    private String automark;
    private String oneId;
    private String userJourney1;
    private String userJourney2;
    private String userJourney3;
    private String usageScenarioFirst;
    private String usageScenarioSecond;
    private String d2cResponsibleDept;
    private String d2cAccountableDept;
    private String d2cCcDept;
    private String custGlobalId;
    private String custClassify;
    private String custMainPhone;
    private String isCarOwner;
    private String custAge;
    private String custAgeGroup;
    private String custName;
    private String custGender;
    private String custHighEducaion; // 拼写与 DDL 一致
    private String marrigeStatue;    // 拼写与 DDL 一致
    private String familyIncome;
    private String isExchangeFlg;
    private String purchaseCarTimes;
    private String isMemberFlg;
    private String custProvinceCode;
    private String custProvince;
    private String custCityCode;
    private String custCity;
    private String custType;
    private String custLivedProv;
    private String custLivedCity;
    private String custProfession;
    private String vhlVin;
    private String vhlColorName;
    private String vhlProductDate;
    private String vhlOfflineDate;
    private String vhlIsAbroad;
    private String vhlDisCh;
    private String vhlDisMt;
    private String vhlEngClsf;
    private String vhlEngSeris;
    private String vhlVehType;
    private String vhlCountry;
    private String vhlBdClsf;
    private String vhlSegMt;
    private String vhlPowClsf;
    private String vhlFuClsf;
    private String vhlModlSt;
    private String vhlStdPlntCode;
    private String dlrOcId;
    private String dlrOcCode;
    private String dlrOcName;
    private String dlrOcProvinceCode;
    private String dlrOcProvince;
    private String dlrOcCityCode;
    private String dlrOcCity;
    private String dlrDcId;
    private String dlrDcCode;
    private String dlrDcName;
    private String dlrDcProvinceCode;
    private String dlrDcProvince;
    private String dlrDcCityCode;
    private String dlrDcCity;
    private String dlrMcId;
    private String dlrMcCode;
    private String dlrMcName;
    private String dlrMcProvinceCode;
    private String dlrMcProvince;
    private String dlrMcCityCode;
    private String dlrMcCity;
    private String isWsaterArmy; // 拼写与 DDL 一致
    private String isManagerFocused;
    private String isBigV;
    private String authorId;
    private String authorNick;
    private String isMainPost;
    private String originalLink;
    private String viewCount;
    private String commentCount;
    private String likeCount;
    private String shareCount;
    private String favoriteCount;
    private String workOrderId;
    private String questId;
    private String questType;
    private String questAnswerScore;
    private String questBusinessType;
    private String questBusinessScenario;
    private String tagAccuracy;
    private String tagCustomerIssueClassification;
    private String tagIssueSeverity;
    private String tagCodeStatus;
    private String tagBusinessDomain;
    private String tagEventClarity;
    private String tagHighValueFlag;
    private String tagComplaintFlagNeedingReply;
    private String tagComplaintFlagNeedingPrtvMsg;
    private String tagHighQualityVocFlag;
    private String tagNewEnergyOrFuel;
    private String tagNeedForvclosedLoop;
    private String tagSort;
    private String topic; // NOT NULL
    private String topicText;
    private String opinion;

    // ========== 多级标签体系（精选示例，实际需补全全部层级） ==========
    private String cptTagFirstCode;
    private String cptTagSecondCode;
    private String cptTagThreeCode;
    private String cptTagFourCode;
    private String cptTagFirst;
    private String cptTagSecond;
    private String cptTagThree;
    private String cptTagFour;
    private String ujyTagFirstCode;
    private String ujyTagSecondCode;
    private String ujyTagThreeCode;
    private String ujyTagFourCode;
    private String ujyTagFirst;
    private String ujyTagSecond;
    private String ujyTagThree;
    private String ujyTagFour;
    private String cmaTagFirstCode;
    private String cmaTagSecondCode;
    private String cmaTagThreeCode;
    private String cmaTagFourCode;
    private String cmaTagFirst;
    private String cmaTagSecond;
    private String cmaTagThree;
    private String cmaTagFour;
    private String domTagFirstCode;
    private String domTagSecondCode;
    private String domTagThreeCode;
    private String domTagFourCode;
    private String domTagFirst;
    private String domTagSecond;
    private String domTagThree;
    private String domTagFour;
    private String npsTagFirstCode;
    private String npsTagSecondCode;
    private String npsTagThreeCode;
    private String npsTagFourCode;
    private String npsTagFirst;
    private String npsTagSecond;
    private String npsTagThree;
    private String npsTagFour;
    private String vtrTagFirstCode;
    private String vtrTagSecondCode;
    private String vtrTagThreeCode;
    private String vtrTagFourCode;
    private String vtrTagFirst;
    private String vtrTagSecond;
    private String vtrTagThree;
    private String vtrTagFour;
    @Builder.Default
    private String abandon = "0";
    ; // 0=否, 1=是
    private String sourceDataId;
    @Builder.Default
    private String highQuality = "0"; // 注：DDL 注释有误，实际为高质量标识

    private String retweetedUrl;
    private String retweetedUserId;
    private String retweetedUserName;
    private String retweetedContent;
    private String retweetedTitle;
    private String retweetedTime;
    private String commentUserName;
    private String commentUrl;
    private String commentUserId;
    private String oneIdRisk;

    private String adType;
    private String attributeTagCode;
    private String attributeTagName;
    private String emotionalLevel;
    @Builder.Default
    private Integer done = 0;


    @Builder.Default
    private LocalDateTime insertDt = LocalDateTime.now(); // 数据库 DEFAULT CURRENT_TIMESTAMP

}
