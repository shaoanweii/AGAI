package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VOC_RISK_HANDLING_RECORD
 */
@Tag(name = "风险处理记录表")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskHandlingRecordModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    /**
     * 审核id
     */
    @Schema(description = "警示风险id")
    private String warningRiskId;
    /**
     * 处理接收人员id
     */
    @Schema(description = "处理接收人员id")
    private String processUserId;
    @TableField(exist = false)
    private String processUserName;
    /**
     * 邮件内容
     */
    @Schema(description = "邮件内容")
    private String mailContent;

//    @ApiModelProperty(value="邮件图")
//    private String mailChart;
    /**
     * 邮件地址
     */
    @Schema(description = "邮件地址")
    private String mailAddress;
    /**
     * 是否发送邮件
     */
    @Schema(description = "是否发送邮件")
    private boolean sendMail;
    @Schema(description = "是否钉钉推送")
    private boolean sendDingtalk;
    @Schema(description = "是否发送短信")
    private boolean sendMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean delFlag;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 处理人员的组织id
     */
    @Schema(description = "处理人员的组织id")
    private String processDepartId;
    @TableField(exist = false)
    private String processDepartName;
    /**
     * 计划完成时间
     */
    @Schema(description = "计划完成时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planCompletionTime;
    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;
    /**
     * 解决方案与说明
     */
    @Schema(description = "解决方案与说明")
    private String solutionDescription;
    /**
     * 解决方案附件
     */
    @Schema(description = "解决方案附件")
    private String solutionAttachment;
    @Schema(description = "处理状态0:未处理，1：已处理")
    private Integer processStatus;
}
