package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class AysCqCaPostprocessDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // 基础信息字段
    @ExcelIgnore
    private String id;
    @ExcelProperty(value = "原始数据ID", order = 0)
    @ColumnWidth(20)
    private String dataId; // data_id
    @ExcelProperty(value = "标题", order = 1)
    @ColumnWidth(20)// content_type
    private String title;
    @ExcelProperty(value = "原始声音", order = 2)
    @ColumnWidth(20)// content_type
    private String originalText; //
    @ExcelProperty(value = "品牌", order = 3)
    @ColumnWidth(20)
    private String brandName; // brand_name
    @ExcelProperty(value = "车系", order = 4)
    @ColumnWidth(20)
    private String carSeriesName; // car_series_name
    @ExcelProperty(value = "标准观点", order = 5)
    @ColumnWidth(20)
    private String topicText; // topic_text（主题文本）
    @ExcelProperty(value = "情感", order = 6)
    @ColumnWidth(20)
    private String sentiment; // sentiment（情感倾向）
    @ExcelProperty(value = "意图", order = 7)
    @ColumnWidth(20)
    private String intention; // intention（用户意图）
    @ExcelProperty(value = "内容类型", order = 8)
    @ColumnWidth(20)
    private String contentType;
    @ExcelProperty(value = "用车场景一级", order = 9)
    @ColumnWidth(20)
    private String usageScenarioFirst; // user_journey2（用户旅程2）
    @ExcelProperty(value = "用车场景二级", order = 10)
    @ColumnWidth(20)
    private String usageScenarioSecond; // user_journey2（用户旅程2）
    @ExcelProperty(value = "一级渠道分类", order = 11)
    @ColumnWidth(20)
    private String isOuter; // is_outer（是否外部）
    @ExcelProperty(value = "二级渠道分类", order = 12)
    @ColumnWidth(20)
    private String secondChannelName; //
    @ExcelProperty(value = "渠道名称", order = 13)
    @ColumnWidth(20)
    private String channelName; // channel_name
    @ExcelProperty(value = "车企名称", order = 14)
    @ColumnWidth(20)
    private String seriesFactory; // channel_series_factory
    @ExcelProperty(value = "车型名称", order = 15)
    @ColumnWidth(20)
    private String modelName;
    @ExcelProperty(value = "发布时间", order = 16)
    @ColumnWidth(20)
    private String publishTime; // intention（用户意图）
    @ExcelProperty(value = "用户旅程一级", order = 17)
    @ColumnWidth(20)
    private String userJourney1; // user_journey1（用户旅程1）
    @ExcelProperty(value = "用户旅程二级", order = 18)
    @ColumnWidth(20)
    private String userJourney2; // user_journey2（用户旅程2）
    @ExcelProperty(value = "用户旅程三级", order = 19)
    @ColumnWidth(20)
    private String userJourney3; // user_journey2（用户旅程2）

    @ExcelProperty(value = "CPT标签1级", order = 20)
    @ColumnWidth(20)
    private String cptTagFirst;
    @ExcelProperty(value = "CPT标签2级", order = 21)
    @ColumnWidth(20)
    private String cptTagSecond;
    @ExcelProperty(value = "CPT标签3级", order = 22)
    @ColumnWidth(20)
    private String cptTagThree;
    @ExcelProperty(value = "CPT标签4级", order = 23)
    @ColumnWidth(20)
    private String cptTagFour;
    @ExcelProperty(value = "全旅程客户标签1级", order = 24)
    @ColumnWidth(20)
    private String ujyTagFirst;
    @ExcelProperty(value = "全旅程客户标签2级", order = 25)
    @ColumnWidth(20)
    private String ujyTagSecond;
    @ExcelProperty(value = "全旅程客户标签3级", order = 26)
    @ColumnWidth(20)
    private String ujyTagThree;
    @ExcelProperty(value = "全旅程客户标签4级", order = 27)
    @ColumnWidth(20)
    private String ujyTagFour;
    @ExcelProperty(value = "CMA标签1级", order = 28)
    @ColumnWidth(20)
    private String cmaTagFirst;
    @ExcelProperty(value = "CMA标签2级", order = 29)
    @ColumnWidth(20)
    private String cmaTagSecond;
    @ExcelProperty(value = "CMA标签3级", order = 30)
    @ColumnWidth(20)
    private String cmaTagThree;
    @ExcelProperty(value = "CMA标签4级", order = 31)
    @ColumnWidth(20)
    private String cmaTagFour;
    @ExcelProperty(value = "全领域业务标签1级", order = 32)
    @ColumnWidth(20)
    private String domTagFirst;
    @ExcelProperty(value = "全领域业务标签2级", order = 33)
    @ColumnWidth(20)
    private String domTagSecond;
    @ExcelProperty(value = "全领域业务标签3级", order = 34)
    @ColumnWidth(20)
    private String domTagThree;
    @ExcelProperty(value = "全领域业务标签4级", order = 35)
    @ColumnWidth(20)
    private String domTagFour;
    @ExcelProperty(value = "NPS标签1级", order = 36)
    @ColumnWidth(20)
    private String npsTagFirst;
    @ExcelProperty(value = "NPS标签2级", order = 37)
    @ColumnWidth(20)
    private String npsTagSecond;
    @ExcelProperty(value = "NPS标签3级", order = 38)
    @ColumnWidth(20)
    private String npsTagThree;
    @ExcelProperty(value = "NPS标签4级", order = 39)
    @ColumnWidth(20)
    private String npsTagFour;
    @ExcelProperty(value = "VRT标签1级", order = 40)
    @ColumnWidth(20)
    private String vtrTagFirst;
    @ExcelProperty(value = "VRT标签2级", order = 41)
    @ColumnWidth(20)
    private String vtrTagSecond;
    @ExcelProperty(value = "VRT标签3级", order = 42)
    @ColumnWidth(20)
    private String vtrTagThree;
    @ExcelProperty(value = "VRT标签4级", order = 43)
    @ColumnWidth(20)
    private String vtrTagFour;
    @ExcelProperty(value = "标签-事件清晰度", order = 44)
    @ColumnWidth(20)
    private String tagEventClarity; // tag_event_clarity（事件清晰度标签）
    @ExcelProperty(value = "是否水军", order = 45)
    @ColumnWidth(20)
    private String isWsaterArmy; // is_wsater_army（可能为is_water_army笔误，是否水军）
    @ExcelProperty(value = "用户昵称", order = 46)
    @ColumnWidth(20)
    private String userNick; // user_nick（用户昵称）
    @ExcelProperty(value = "是否主贴", order = 47)
    @ColumnWidth(20)
    private String isMainPost; // is_main_post（是否主帖）
    @ExcelProperty(value = "原文链接", order = 48)
    @ColumnWidth(20)
    private String originalLink; // original_link（原始链接）
    @ExcelProperty(value = "工单ID", order = 49)
    @ColumnWidth(20)
    private String workOrderId; // work_order_id（工单ID）
    @ExcelProperty(value = "问卷ID", order = 50)
    @ColumnWidth(20)
    private String questId; // quest_id（问题ID）
    @ExcelProperty(value = "问卷类型", order = 51)
    @ColumnWidth(20)
    private String questType; // quest_type（问题类型）
    @ExcelProperty(value = "问卷答案分数", order = 52)
    @ColumnWidth(20)
    private String questAnswerScore; // quest_answer_score（问题回答评分）
    @ExcelProperty(value = "ONE_ID", order = 53)
    @ColumnWidth(20)
    private String oneId; // one_id（唯一标识）
    @ExcelProperty(value = "用户名", order = 54)
    @ColumnWidth(20)
    private String custName; // cust_global_id（客户全球ID）
    @ExcelProperty(value = "是否车主", order = 55)
    @ColumnWidth(20)
    private String isCarOwner;
    @ExcelProperty(value = "性别", order = 56)
    @ColumnWidth(20)
    private String custGender; // cust_gender（客户性别：男/女/未知）

    // 车辆信息相关字段
    @ExcelProperty(value = "VIN", order = 57)
    @ColumnWidth(20)
    private String vhlVin;

    // content_type
    @ExcelIgnore
    private String originalTextScene;

    @ExcelIgnore
    private String channelCatagory; // channel_catagory

    @ExcelIgnore
    private String hotWord; // hot_word（热词）

    @ExcelIgnore
    private String keywords; // keywords（关键词）

    @ExcelIgnore
    private String topic; // topic（主题）
    @ExcelIgnore
    private String opinion; // opinion（观点）

    // 其他标签属性
    @ExcelIgnore
    private String tagAccuracy; // tag_accuracy（标签准确率）
    @ExcelIgnore
    private String tagCustomerIssueClassification; // tag_customer_issue_classification（客户问题分类标签）
    @ExcelIgnore
    private String tagIssueSeverity; // tag_issue_severity（问题严重程度标签）
    @ExcelIgnore
    private String tagCodeStatus; // tag_code_status（标签编码状态）
    @ExcelIgnore
    private String tagBusinessDomain; // tag_business_domain（标签业务领域）
    @ExcelIgnore
    private String tagHighValueFlag; // tag_high_value_flag（高价值标签标识）
    @ExcelIgnore
    private String tagComplaintFlagNeedingReply; // tag_complaint_flag_needing_reply（需回复投诉标识）
    @ExcelIgnore
    private String tagHighQualityVocFlag; // tag_high_quality_voc_flag（高质量VOC标识）
    @ExcelIgnore
    private String tagNewEnergyOrFuel; // tag_new_energy_or_fuel（新能源/燃油车标签）
    @ExcelIgnore
    private String tagNeedForvclosedLoop; // tag_need_forvclosed_loop（是否需要闭环标识）

    @ExcelIgnore
    private String isManagerFocused; // is_manager_focused（是否经理关注）
    @ExcelIgnore
    private String isBigV; // is_big_v（是否大V）

    @ExcelIgnore
    private String authorId; // author_id（作者ID）
    @ExcelIgnore
    private String viewCount; // view_count（浏览量）
    @ExcelIgnore
    private String commentCount; // comment_count（评论数）
    @ExcelIgnore
    private String likeCount; // like_count（点赞数）
    @ExcelIgnore
    private String shareCount; // share_count（分享数）
    @ExcelIgnore
    private String favoriteCount; // favorite_count（收藏数）
    @ExcelIgnore
    private String questBusinessType; // quest_business_type（问题业务类型）
    @ExcelIgnore
    private String questBusinessScenario; // quest_business_scenario（问题业务场景）
    @ExcelIgnore
    private String d2cResponsibleDept; // d2c_responsible_dept（D2C责任部门）
    @ExcelIgnore
    private String d2cAccountableDept; // d2c_accountable_dept（D2C负责部门）
    @ExcelIgnore
    private String d2cCcDept; // d2c_cc_dept（D2C抄送部门）
    @ExcelIgnore
    private String custClassify; // d2c_cc_dept（D2C抄送部门）

    @ExcelIgnore
    private String custGlobalId; // cust_global_id（客户全球ID）
    @ExcelIgnore
    private String custMainPhone; // cust_global_id（客户全球ID）
    @ExcelIgnore
    private String custAge; // cust_age（客户年龄）
    @ExcelIgnore
    private String custAgeGroup; // cust_age（客户年龄）

    // 客户信息相关字段
    @ExcelIgnore
    private String custHighEducaion;
    @ExcelIgnore
    private String marrigeStatue;
    @ExcelIgnore
    private String familyIncome;
    @ExcelIgnore
    private String isExchangeFlg;
    @ExcelIgnore
    private String purchaseCarTimes;
    @ExcelIgnore
    private String isMemberFlg;
    @ExcelIgnore
    private String custProvinceCode;
    @ExcelIgnore
    private String custProvince;
    @ExcelIgnore
    private String custCityCode;
    @ExcelIgnore
    private String custCity;
    @ExcelIgnore
    private String custType;
    @ExcelIgnore
    private String custLivedProv;
    @ExcelIgnore
    private String custLivedCity;
    @ExcelIgnore
    private String custProfession;

    @ExcelIgnore
    private String vhlColorName;
    @ExcelIgnore
    private String vhlProductDate;
    @ExcelIgnore
    private String vhlOfflineDate;
    @ExcelIgnore
    private String vhlIsAbroad;
    @ExcelIgnore
    private String vhlDisCh;
    @ExcelIgnore
    private String vhlDisMt;
    @ExcelIgnore
    private String vhlEngClsf;
    @ExcelIgnore
    private String vhlEngSeris;
    @ExcelIgnore
    private String vhlVehType;
    @ExcelIgnore
    private String vhlCountry;
    @ExcelIgnore
    private String vhlBdClsf;
    @ExcelIgnore
    private String vhlSegMt;
    @ExcelIgnore
    private String vhlPowClsf;
    @ExcelIgnore
    private String vhlFuClsf;
    @ExcelIgnore
    private String vhlModlSt;
    @ExcelIgnore
    private String vhlStdPlntCode;
    // 经销商相关字段
    @ExcelIgnore
    private String dlrOcId;
    @ExcelIgnore
    private String dlrOcName;
    @ExcelIgnore
    private String dlrOcProvince;
    @ExcelIgnore
    private String dlrOcCity;

    // dc级经销商信息
    @ExcelIgnore
    private String dlrDcId;
    @ExcelIgnore
    private String dlrDcName;
    @ExcelIgnore
    private String dlrDcProvince;
    @ExcelIgnore
    private String dlrDcCity;

    // mc级经销商信息
    @ExcelIgnore
    private String dlrMcId;
    @ExcelIgnore
    private String dlrMcName;
    @ExcelIgnore
    private String dlrMcProvince;
    @ExcelIgnore
    private String dlrMcCity;
    @ExcelIgnore
    private String dataStatus;


}
