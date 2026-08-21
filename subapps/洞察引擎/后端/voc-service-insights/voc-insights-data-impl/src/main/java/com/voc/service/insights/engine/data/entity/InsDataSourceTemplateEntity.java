package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/13 下午3:28
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_date_source_template")
public class InsDataSourceTemplateEntity  implements Serializable {
    private String newId;
    @Schema(description = "数据记录号")
    private String id;

    @Schema(description = "服务单号")
    private String serverorder;

    @Schema(description = "原始单号")
    private String source_id;

    @Schema(description = "OneID")
    private String one_id;

    @Schema(description = "单据来源")
    private String data_source;

    @Schema(description = "单据类型")
    private String voc_types;

    @Schema(description = "渠道")
    private String channel;

    @Schema(description = "渠道细分")
    private String channel_subclass;

    @Schema(description = "渠道细分编码")
    private String channel_subclass_code;

    @Schema(description = "报表是否显示")
    private String is_show;

    @Schema(description = "专营店代码")
    private String dlr_code_;

    @Schema(description = "专营店名称")
    private String dlr_short_name;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "摘要")
    private String abstracts;
    @Schema(description = "内容")
    private String voc_content;
    @Schema(description = "追加序号")
    private String add_order;
    @Schema(description = "坐席回复")
    private String serveranswer;
    @Schema(description = "是否首问解决")
    private String resolvemethodname;
    @Schema(description = "投诉级别")
    private String serverurgency;
    @Schema(description = "处理内容")
    private String deal_content1;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "结案时间")
    private String casedate;
    @Schema(description = "结案状态")
    private String statusname;
    @Schema(description = "1类")
    private String category_1;
    @Schema(description = "2类")
    private String category_2;
    @Schema(description = "3类")
    private String category_3;
    @Schema(description = "4类")
    private String category_4;
    @Schema(description = "5类")
    private String category_5;
    @Schema(description = "车辆年龄")
    private String voc_age;
    @Schema(description = "订单类型")
    private String order_type;
    @Schema(description = "客户类型")
    private String cust_type;
    @Schema(description = "车系")
    private String car_series;
    @Schema(description = "车系码")
    private String car_series_code;
    @Schema(description = "基准车系")
    private String base_series;
    @Schema(description = "基准车系码")
    private String base_series_code;
    @Schema(description = "车型")
    private String car_config_cn;
    @Schema(description = "原始车系")
    private String original_car_series;
    @Schema(description = "性别")
    private String gender;
    @Schema(description = "车主年龄")
    private String age;
    @Schema(description = "客户满意度")
    private String satisfaction_score;
    @Schema(description = "车系类型")
    private String carseriestype;
    @Schema(description = "CCS平台")
    private String carseries_platform;
    @Schema(description = "原文链接")
    private String links;
    @Schema(description = "帖子ID")
    private String commentable_id;
    @Schema(description = "评论ID")
    private String comment_id;
    @Schema(description = "新增时间")
    private String voc_process_at;
    @Schema(description = "业务类别")
    private String max_business_type;
    @Schema(description = "建单时间")
    private String created_at;
    @Schema(description = "车型码")
    private String car_config_code;
    @Schema(description = "用户名")
    private String user_name;
    @Schema(description = "低满意度原因")
    private String extendjsonmain;
    @Schema(description = "排障方式")
    private String fault_removing;
    @Schema(description = "派单时长")
    private String pdtotaltime;
    @Schema(description = "专营店响应时长")
    private String dlrxytotaltime;
    @Schema(description = "专营店接单时间")
    private String dlrjdtime;
    @Schema(description = "上牌省份")
    private String car_license_province;
    @Schema(description = "上牌城市")
    private String car_license_city;
    /**
     * 总数
     */
    @Builder.Default
    private Long totalNum = 0L;
    /**
     * 成功数
     */
    @Builder.Default
    private Long successNum = 0L;
    /**
     * 失败数
     */
    @Builder.Default
    private Long failNum = 0L;
    /**
     * 数据有效性(0:无效 1：有效)
     */
    @Builder.Default
    private String dataValidity = "1";
    /**
     * 批次id
     */
    private String batchId;

    private LocalDateTime createTime;
    private String createUser;
    private String channelId;
}
