package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysCqCaMetaDataAnalysisVo implements Serializable {
    /**
     * 主键ID
     */
    @ExcelProperty(value = "ID", order = 0)
    @ColumnWidth(20)
    private String dataId;

    @ExcelIgnore
    private String id;


    /**
     * 标题
     */
    @ExcelProperty(value = "标题", order = 1)
    @ColumnWidth(20)
    private String title;

    /**
     * 内容
     */
    @ExcelProperty(value = "内容", order = 2)
    @ColumnWidth(20)
    private String content;

    /**
     * 是否外部数据
     */
    @ExcelProperty(value = "一级渠道分类", order = 3)
    @ColumnWidth(20)
    private String isOuter;

    @ExcelProperty(value = "二级渠道分类", order = 4)
    @ColumnWidth(20)
    private String secondChannelName;

    @ExcelProperty(value = "渠道名称", order = 5)
    @ColumnWidth(20)
    private String channelName;

    /**
     * 内容类型
     */
    @ExcelProperty(value = "内容类型", order = 6)
    @ColumnWidth(20)
    private String contentType;

    /**
     * 品牌
     */
    @ExcelProperty(value = "品牌", order = 7)
    @ColumnWidth(20)
    private String brand;

    /**
     * 车系
     */
    @ExcelProperty(value = "车系", order = 8)
    @ColumnWidth(20)
    private String series;

    /**
     * 数据创建时间
     */
    @ExcelProperty(value = "发布时间", order = 9)
    @ColumnWidth(20)
    private String dataCreateTime;

    /**
     * 车型
     */
    @ExcelProperty(value = "车型", order = 10)
    @ColumnWidth(20)
    private String model;

    /**
     * 是否水军
     */
    @ExcelProperty(value = "是否水军", order = 11)
    @ColumnWidth(20)
    private String isWsaterArmy;

    /**
     * 统一ID
     */
    @ExcelProperty(value = "ONE_ID", order = 12)
    @ColumnWidth(20)
    private String oneId;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名", order = 13)
    @ColumnWidth(20)
    private String userName;

    /**
     * 车辆VIN码
     */
    @ExcelProperty(value = "车辆车架号", order = 14)
    @ColumnWidth(20)
    private String vhlVin;


    /**
     * 作者昵称
     */
    @ExcelProperty(value = "用户昵称", order = 15)
    @ColumnWidth(20)
    private String authorNick;

    /**
     * 原始链接
     */
    @ExcelProperty(value = "原文链接", order = 16)
    @ColumnWidth(20)
    private String originalLink;

    /**
     * 工单ID
     */
    @ExcelProperty(value = "工单ID", order = 17)
    @ColumnWidth(20)
    private String workOrderId;

    /**
     * 问题ID
     */
    @ExcelProperty(value = "问卷ID", order = 18)
    @ColumnWidth(20)
    private String questId;

    /**
     * 问题类型
     */
    @ExcelProperty(value = "问卷类型", order = 19)
    @ColumnWidth(20)
    private String questType;

    /**
     * 问题回答评分
     */
    @ExcelProperty(value = "问卷答案分数", order = 20)
    @ColumnWidth(20)
    private String questAnswerScore;

    /**
     * 问题业务类型
     */
    @ExcelIgnore
    private String questBusinessType;

    /**
     * 是否主帖
     */
    @ExcelIgnore
    private String isMainPost;


    /**
     * 权重
     */
    @ExcelIgnore
    private Integer weight;

    /**
     * 车辆编号
     */
    @ExcelIgnore
    private String idCarNo;

    /**
     * 手机号
     */
    @ExcelIgnore
    private String mobile;

    /**
     * 邮箱
     */
    @ExcelIgnore
    private String email;

    /**
     * 全局ID
     */
    @ExcelIgnore
    private String globalId;

    /**
     * 用户ID
     */
    @ExcelIgnore
    private String userId;

    /**
     * 车辆ID
     */
    @ExcelIgnore
    private String vhlId;

    /**
     * 经销商ID
     */
    @ExcelIgnore
    private String dlrId;

    /**
     * 经销商编码
     */
    @ExcelIgnore
    private String dlrCode;

    /**
     * 经销商类型
     */
    @ExcelIgnore
    private String dlrType;

    /**
     * 市场ID
     */
    @ExcelIgnore
    private String marketId;

    /**
     * 是否被管理者关注
     */
    @ExcelIgnore
    private String isManagerFocused;

    /**
     * 是否大V
     */
    @ExcelIgnore
    private String isBigV;

    /**
     * 作者ID
     */
    @ExcelIgnore
    private String authorId;

    /**
     * 浏览量
     */
    @ExcelIgnore
    private String viewCount;

    /**
     * 评论数
     */
    @ExcelIgnore
    private String commentCount;

    /**
     * 点赞数
     */
    @ExcelIgnore
    private String likeCount;

    /**
     * 分享数
     */
    @ExcelIgnore
    private String shareCount;

    /**
     * 收藏数
     */
    @ExcelIgnore
    private String favoriteCount;



    /**
     * 问题业务场景
     */
    @ExcelIgnore
    private String questBusinessScenario;

    /**
     * 数据状态
     */
    @ExcelIgnore
    private String dataStatus;
    /**
     * 一级渠道编码
     */
    @ExcelIgnore
    private String channelLevelOneCode;
    /**
     * 一级渠道名称
     */
    @ExcelIgnore
    private String channelLevelOneName;
    /**
     * 二级渠道编码
     */
    @ExcelIgnore
    private String channelLevelTwoCode;
    /**
     * 二级渠道名称
     */
    @ExcelIgnore
    private String channelLevelTwoName;
    /**
     * 三级渠道编码
     */
    @ExcelIgnore
    private String channelLevelThreeCode;
    /**
     * 三级渠道名称
     */
    @ExcelIgnore
    private String channelLevelThreeName;

    @ExcelIgnore
    private String batchId;


}
