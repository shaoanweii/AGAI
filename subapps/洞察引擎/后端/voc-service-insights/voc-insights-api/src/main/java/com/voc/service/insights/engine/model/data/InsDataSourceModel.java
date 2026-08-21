package com.voc.service.insights.engine.model.data;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 数据源集(InsDataSource)请求实体对象
 *
 * @author leiww
 * @since 2024-02-27 15:31:45
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "negativeNum", targer = "negative_num"),
        @SortField(source = "complainNum", targer = "complain_num"),
        @SortField(source = "riskWordsNum", targer = "risk_words_num"),
        @SortField(source = "userNum", targer = "user_num"),
        @SortField(source = "channelNum", targer = "channel_num"),
        @SortField(source = "emotionNum", targer = "emotion_num")
})
public class InsDataSourceModel extends Page  implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "数据源名称")
    private String dataSourceName;
    @Schema(description = "数据源类型")
    @Builder.Default
    private String dataSourceType = "text";
    @Schema(description = "数据源接入方式")
    @Builder.Default
    private String dataSourceAccessWay = "upload";
    @Schema(description = "文件名称")
    private String fileName;
    @Schema(description = "所属客户id")
    private String clientId;
    @Schema(description = "数据名称")
    private String dataName;
    @Schema(description = "数据源批次id")
    private String batchId;
    @Schema(description = "数据源id")
    private String dataSourceId;
    @Schema(description = "数据状态")
    private String status;
    @Schema(description = "数据源")
    private String dataSource;

    @Schema(description = "数据链路id集合")
    private Set<String> workIdList;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "数据链路id")
    private String workId;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "渠道ID")
    private List<String> channelIdList;

    @Schema(description = "数据状态")
    private List<String> dataStatus;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "情感")
    private List<String> sentiment;

    @Schema(description = "意图")
    private List<String> intention;

    @Schema(description = "品牌")
    private List<String> brandCode;

    @Schema(description = "车系")
    private List<String> carSeries;

    @Schema(description = "业务末级标签")
    private List<String> businessEndTag;

    @Schema(description = "质量末级标签")
    private List<String> qualityEndTag;
    @Schema(description = "标签类型")
    private List<String> labelType;
    @Schema(description = "处理模型")
    @Builder.Default
    private String modelType = "3";
    @Schema(description = "标签类型")
    private List<String> labelTypeList;
    /**
     * 请求id
     */
    private String requestId;
    /**
     * 总数据量
     */
    private String total;
    /**
     * 批次当前数量
     */
    private String currentBatchTotal;
    /**
     * 批次总页数
     */
    private String batchPageTotal;
    /**
     * 批次当前页数
     */
    private String currentBatchPage;
    /**
     * 当前批次数据集
     */
    private Object data;

    private String createUser;
    /**
     * 处理失败id集合
     */
    private List<String> errorIds;
    /**
     * 数据有效性 0：无效 1：有效
     */
    private String dataValidity;
    /**
     * 城市编码集
     */
    private List<String> cityCodeList;
    /**
     * 项目id
     */
    private String projectId;

    @Schema(description = "本品车系")
    private List<String> ownCarSeries;

    @Schema(description = "竞品车系")
    private List<String> competitorsCarSeries;
    @Schema(description = "同时提及车系")
    private List<String> mentionCarSeriesList;

    @Schema(description = "风险类型")
    private String riskType;

    @Schema(description = "洞察周期")
    private List<String> statisticType;

    @Schema(description = "风险等级")
    private List<String> riskLevel;
    @Schema(description = "品牌")
    private String brand;
    @Schema(description = "数据类型")
    private List<String> metaDataType;

    private List<String> contentType;


    private String dateType;



    /**
     * 执行成功数量
     */
    private Integer executeSuccessCount;
    /**
     * 执行失败数量
     */
    private Integer executeFailCount;
    /**
     * 日期 格式：YYYY-MM-DD
     */
    private String date;
    @Builder.Default
    private String showType = "2";

    private List<String> dateList;
}

