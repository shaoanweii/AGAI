package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsOriginDataListVo implements Serializable {


    @ExcelProperty(value = "原文Id", order = 1)
    @ColumnWidth(10)
    private String id;

    @Schema(description = "渠道名称")
    @ExcelProperty(value = "渠道名称", order = 2)
    @ColumnWidth(15)
    private String channelName;

    @Schema(description = "本品车系")
    @ExcelProperty(value = "本品车系", order = 3)
    @ColumnWidth(15)
    private String ownCarSeriesList;


    @Schema(description = "竞品车系")
    @ExcelProperty(value = "竞品车系", order = 4)
    @ColumnWidth(15)
    private String competitorsCarSeriesList;


    @Schema(description = "同时提及车系")
    @ExcelProperty(value = "同时提及车系", order = 5)
    @ColumnWidth(15)
    private String mentionCarSeries;


    @Schema(description = "标题")
    @ExcelProperty(value = "标题", order = 6)
    private String title;

    @Schema(description = "内容")
    @ExcelProperty(value = "内容", order = 7)
    @ColumnWidth(20)
    private String content;

    @Schema(description = "发布时间")
    @ExcelProperty(value = "发布时间", order = 8)
    @ColumnWidth(15)
    private String publishTime;

    @Schema(description = "昵称")
    @ExcelProperty(value = "昵称", order = 9)
    private String userName;

    @Schema(description = "阅读数")
    @ExcelProperty(value = "阅读数", order = 10)
    private String readingCount;

    @Schema(description = "关注数")
    @ExcelProperty(value = "关注数", order = 11)
    private String focusCount;

    @Schema(description = "评论数")
    @ExcelProperty(value = "评论数", order = 12)
    private String commentsCount;

    @Schema(description = "点赞数")
    @ExcelProperty(value = "点赞数", order = 13)
    private String favorCount;

    @Schema(description = "收藏数")
    @ExcelProperty(value = "收藏数", order = 14)
    private String collectionsCount;

    @Schema(description = "转发数")
    @ExcelProperty(value = "转发数", order = 15)
    private String redirectionCount;

    @Schema(description = "数据状态")
    @ExcelProperty(value = "数据状态", order = 16)
    @ColumnWidth(15)
    private String dataStatus;

    @Schema(description = "原文链接")
    @ExcelProperty(value = "原文链接", order = 17)
    @ColumnWidth(15)
    private String url;

}
