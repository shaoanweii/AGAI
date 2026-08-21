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
 * @创建者: fanrong
 * @创建时间: 2024/6/19 下午3:24
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDataSourceResultDataVo implements Serializable {
    @ExcelProperty(value = "数据记录号", order = 0)
    @ColumnWidth(20)
    private String id;

    @Schema(description = "服务单号")
    @ExcelProperty(value = "服务单号", order = 1)
    @ColumnWidth(20)
    private String serverorder;

    @Schema(description = "原始单号")
    @ExcelProperty(value = "原始单号", order = 2)
    @ColumnWidth(20)
    private String source_id;

    @Schema(description = "OneID")
    @ExcelProperty(value = "OneID", order = 3)
    @ColumnWidth(20)
    private String one_id;

    @Schema(description = "单据来源")
    @ExcelProperty(value = "单据来源", order = 4)
    @ColumnWidth(20)
    private String data_source;

    @Schema(description = "单据类型")
    @ExcelProperty(value = "单据类型", order = 5)
    @ColumnWidth(20)
    private String voc_types;

    @Schema(description = "渠道")
    @ExcelProperty(value = "渠道", order = 6)
    @ColumnWidth(20)
    private String channel;

    @Schema(description = "渠道细分")
    @ExcelProperty(value = "渠道细分", order = 7)
    @ColumnWidth(20)
    private String channel_subclass;

    @Schema(description = "报表是否显示")
    @ExcelProperty(value = "报表是否显示", order = 8)
    @ColumnWidth(20)
    private String is_show;

    @Schema(description = "专营店代码")
    @ExcelProperty(value = "专营店代码", order = 9)
    @ColumnWidth(20)
    private String dlr_code_;

    @Schema(description = "专营店名称")
    @ExcelProperty(value = "专营店名称", order = 10)
    @ColumnWidth(20)
    private String dlr_short_name;

    @Schema(description = "品牌")
    @ExcelProperty(value = "品牌", order = 11)
    @ColumnWidth(20)
    private String brand;

    @Schema(description = "摘要")
    @ExcelProperty(value = "摘要", order = 12)
    @ColumnWidth(20)
    private String abstracts;
    @Schema(description = "内容")
    @ExcelProperty(value = "内容", order = 13)
    @ColumnWidth(20)
    private String voc_content;
    @Schema(description = "追加序号")
    @ExcelProperty(value = "追加序号", order = 14)
    @ColumnWidth(20)
    private String add_order;
    @Schema(description = "坐席回复")
    @ExcelProperty(value = "坐席回复", order = 15)
    @ColumnWidth(20)
    private String serveranswer;
    @Schema(description = "是否首问解决")
    @ExcelProperty(value = "是否首问解决", order = 16)
    @ColumnWidth(20)
    private String resolvemethodname;
    @Schema(description = "投诉级别")
    @ExcelProperty(value = "投诉级别", order = 17)
    @ColumnWidth(20)
    private String serverurgency;
    @Schema(description = "处理内容")
    @ExcelProperty(value = "处理内容", order = 18)
    @ColumnWidth(20)
    private String deal_content1;
    @Schema(description = "备注")
    @ExcelProperty(value = "备注", order = 19)
    @ColumnWidth(20)
    private String remark;
    @Schema(description = "结案时间")
    @ExcelProperty(value = "结案时间", order = 20)
    @ColumnWidth(20)
    private String casedate;
    @Schema(description = "结案状态")
    @ExcelProperty(value = "结案状态", order = 21)
    @ColumnWidth(20)
    private String statusname;
    @Schema(description = "1类")
    @ExcelProperty(value = "1类", order = 22)
    @ColumnWidth(20)
    private String category_1;
    @Schema(description = "2类")
    @ExcelProperty(value = "2类", order = 23)
    @ColumnWidth(20)
    private String category_2;
    @Schema(description = "3类")
    @ExcelProperty(value = "3类", order = 24)
    @ColumnWidth(20)
    private String category_3;
    @Schema(description = "4类")
    @ExcelProperty(value = "4类", order = 25)
    @ColumnWidth(20)
    private String category_4;
    @Schema(description = "5类")
    @ExcelProperty(value = "5类", order = 26)
    @ColumnWidth(20)
    private String category_5;
    @Schema(description = "车辆年龄")
    @ExcelProperty(value = "车辆年龄", order = 27)
    @ColumnWidth(20)
    private String voc_age;
    @Schema(description = "订单类型")
    @ExcelProperty(value = "订单类型", order = 28)
    @ColumnWidth(20)
    private String order_type;
    @Schema(description = "客户类型")
    @ExcelProperty(value = "客户类型", order = 29)
    @ColumnWidth(20)
    private String cust_type;
    @Schema(description = "车系")
    @ExcelProperty(value = "车系", order = 30)
    @ColumnWidth(20)
    private String car_series;
    @Schema(description = "车系码")
    @ExcelProperty(value = "车系码", order = 31)
    @ColumnWidth(20)
    private String car_series_code;
    @Schema(description = "基准车系")
    @ExcelProperty(value = "基准车系", order = 32)
    @ColumnWidth(20)
    private String base_series;
    @Schema(description = "基准车系码")
    @ExcelProperty(value = "基准车系码", order = 33)
    @ColumnWidth(20)
    private String base_series_code;
    @Schema(description = "车型")
    @ExcelProperty(value = "车型", order = 34)
    @ColumnWidth(20)
    private String car_config_cn;
    @Schema(description = "原始车系")
    @ExcelProperty(value = "原始车系", order = 35)
    @ColumnWidth(20)
    private String original_car_series;
    @Schema(description = "性别")
    @ExcelProperty(value = "性别", order = 36)
    @ColumnWidth(20)
    private String gender;
    @Schema(description = "车主年龄")
    @ExcelProperty(value = "车主年龄", order = 37)
    @ColumnWidth(20)
    private String age;
    @Schema(description = "客户满意度")
    @ExcelProperty(value = "客户满意度", order = 38)
    @ColumnWidth(20)
    private String satisfaction_score;
    @Schema(description = "车系类型")
    @ExcelProperty(value = "车系类型", order = 39)
    @ColumnWidth(20)
    private String carseriestype;
    @Schema(description = "CCS平台")
    @ExcelProperty(value = "CCS平台", order = 40)
    @ColumnWidth(20)
    private String carseries_platform;
    @Schema(description = "原文链接")
    @ExcelProperty(value = "原文链接", order = 41)
    @ColumnWidth(20)
    private String links;
    @Schema(description = "帖子ID")
    @ExcelProperty(value = "帖子ID", order = 42)
    @ColumnWidth(20)
    private String commentable_id;
    @Schema(description = "评论ID")
    @ExcelProperty(value = "评论ID", order = 43)
    @ColumnWidth(20)
    private String comment_id;
    @Schema(description = "新增时间")
    @ExcelProperty(value = "新增时间", order = 44)
    @ColumnWidth(20)
    private String voc_process_at;
    @Schema(description = "业务类别")
    @ExcelProperty(value = "业务类别", order = 45)
    @ColumnWidth(20)
    private String max_business_type;
    @Schema(description = "建单时间")
    @ExcelProperty(value = "建单时间", order = 46)
    @ColumnWidth(20)
    private String created_at;
    @Schema(description = "车型码")
    @ExcelProperty(value = "车型码", order = 47)
    @ColumnWidth(20)
    private String car_config_code;
    @Schema(description = "用户名")
    @ExcelProperty(value = "用户名", order = 48)
    @ColumnWidth(20)
    private String user_name;
    @Schema(description = "低满意度原因")
    @ExcelProperty(value = "低满意度原因", order = 49)
    @ColumnWidth(20)
    private String extendjsonmain;
    @Schema(description = "排障方式")
    @ExcelProperty(value = "排障方式", order = 50)
    @ColumnWidth(20)
    private String fault_removing;
    @Schema(description = "派单时长")
    @ExcelProperty(value = "派单时长", order = 51)
    @ColumnWidth(20)
    private String pdtotaltime;
    @Schema(description = "专营店响应时长")
    @ExcelProperty(value = "专营店响应时长", order = 52)
    @ColumnWidth(20)
    private String dlrxytotaltime;
    @Schema(description = "专营店接单时间")
    @ExcelProperty(value = "专营店接单时间", order = 53)
    @ColumnWidth(20)
    private String dlrjdtime;
    @Schema(description = "上牌省份")
    @ExcelProperty(value = "上牌省份", order = 54)
    @ColumnWidth(20)
    private String car_license_province;
    @Schema(description = "上牌城市")
    @ExcelProperty(value = "上牌城市", order = 55)
    @ColumnWidth(20)
    private String car_license_city;

}
