package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class WarningTaskResultModel implements Serializable {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "数据ID")
    private String dataId;

    @Schema(description = "渠道分类（注：字段拼写按表结构保留catagory）")
    private String channelCatagory;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "品牌编码")
    private String brandCode;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "车系编码")
    private String carSeriesCode;

    @Schema(description = "车系名称")
    private String carSeriesName;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "情感（正面/负面/中性）")
    private String sentiment;

    @Schema(description = "意图")
    private String intention;

    @Schema(description = "数据创建时间")
    private String dataCreateTime;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "记录创建时间")
    private String createTime;

    @Schema(description = "是否外部数据（0=否，1=是）")
    private String isOuter;

    @Schema(description = "热词")
    private String hotWord;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "原文场景")
    private String originalTextScene;

    @Schema(description = "市场ID")
    private String marketId;

    @Schema(description = "竞争类型")
    private String competitiveType;

    @Schema(description = "车系工厂")
    private String seriesFactory;

    @Schema(description = "汽车品牌标识")
    private String automark;

    @Schema(description = "统一ID")
    private String oneIdRisk;

    @Schema(description = "使用场景一级")
    private String usageScenarioFirst;

    @Schema(description = "使用场景二级")
    private String usageScenarioSecond;

    @Schema(description = "D2C责任部门")
    private String d2cResponsibleDept;

    @Schema(description = "D2C问责部门")
    private String d2cAccountableDept;

    @Schema(description = "D2C抄送部门")
    private String d2cCcDept;

    @Schema(description = "客户全局ID")
    private String custGlobalId;

    @Schema(description = "客户分类")
    private String custClassify;

    @Schema(description = "客户主手机号")
    private String custMainPhone;

    @Schema(description = "是否车主（0=否，1=是）")
    private String isCarOwner;

    @Schema(description = "客户年龄")
    private String custAge;

    @Schema(description = "客户年龄组")
    private String custAgeGroup;

    @Schema(description = "客户姓名")
    private String custName;

    @Schema(description = "客户性别（男/女/未知）")
    private String custGender;

    @Schema(description = "客户是否高学历（0=否，1=是）")
    private String custHighEducaion;

    @Schema(description = "婚姻状况")
    private String marrigeStatue;

    @Schema(description = "家庭收入")
    private String familyIncome;

    @Schema(description = "是否置换（0=否，1=是）")
    private String isExchangeFlg;

    @Schema(description = "购车次数")
    private String purchaseCarTimes;

    @Schema(description = "是否会员（0=否，1=是）")
    private String isMemberFlg;

    @Schema(description = "客户省份编码")
    private String custProvinceCode;

    @Schema(description = "客户省份")
    private String custProvince;

    @Schema(description = "客户城市编码")
    private String custCityCode;

    @Schema(description = "客户城市")
    private String custCity;

    @Schema(description = "客户类型")
    private String custType;

    @Schema(description = "客户常住省份")
    private String custLivedProv;

    @Schema(description = "客户常住城市")
    private String custLivedCity;

    @Schema(description = "客户职业")
    private String custProfession;

    @Schema(description = "是否水军（0=否，1=是）")
    private String isWsaterArmy;

    @Schema(description = "是否管理员关注（0=否，1=是）")
    private String isManagerFocused;

    @Schema(description = "是否大V（0=否，1=是）")
    private String isBigV;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "作者昵称")
    private String authorNick;

    @Schema(description = "是否主帖（0=否，1=是）")
    private String isMainPost;

    @Schema(description = "原文链接")
    private String originalLink;

    private String retweetedUrl;

    private String retweetedUserId;

    private String retweetedUserName;

    private String retweetedContent;

    private String retweetedTitle;

    private String retweetedTime;

    private String commentUserName;

    private String commentUserId;

    private String commentUrl;

    private String vhlVin;

    private String dlrOcName;

    private String dlrOcCode;

    private String content;

    @Schema(description = "浏览量")
    private String viewCount;

    @Schema(description = "评论数")
    private String commentCount;

    @Schema(description = "点赞数")
    private String likeCount;

    @Schema(description = "分享数")
    private String shareCount;

    @Schema(description = "收藏数")
    private String favoriteCount;

    @Schema(description = "工单ID")
    private String workOrderId;

    @Schema(description = "问题ID")
    private String questId;

    @Schema(description = "问题类型")
    private String questType;

    @Schema(description = "问题回答评分")
    private String questAnswerScore;

    @Schema(description = "问题业务类型")
    private String questBusinessType;

    @Schema(description = "问题业务场景")
    private String questBusinessScenario;

    @Schema(description = "主题")
    private String topic;

    @Schema(description = "主题文本")
    private String topicText;

    @Schema(description = "用户观点")
    private String opinion;

    @Schema(description = "全领域一级标签编码")
    private String domTagFirstCode;

    @Schema(description = "全领域二级标签编码")
    private String domTagSecondCode;

    @Schema(description = "全领域三级标签编码")
    private String domTagThreeCode;

    @Schema(description = "全领域四级标签编码")
    private String domTagFourCode;

    @Schema(description = "全领域一级标签名称")
    private String domTagFirst;

    @Schema(description = "全领域二级标签名称")
    private String domTagSecond;

    @Schema(description = "全领域三级标签名称")
    private String domTagThree;

    @Schema(description = "全领域四级标签名称")
    private String domTagFour;

    @Schema(description = "事件清晰度（如清晰/模糊/一般）")
    private String sensitiveType;

    private String tagEventClarity;

    private String tagComplaintFlagNeedingReply;

    private String tagNeedForvclosedLoop;

    @Schema(description = "是否废弃（0=否，1=是）")
    private String abandon;

    private String topicId;
    private String batchId;
    private String publishUserId;
    private String publishUserNickname;
    private String mainPostUserId;
    private String mainPostUserName;
    private String ruleId;
    private String ruleName;

}
