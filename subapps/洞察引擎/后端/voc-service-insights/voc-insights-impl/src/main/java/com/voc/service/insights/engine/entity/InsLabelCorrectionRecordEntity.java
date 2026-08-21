package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "report_label_correction_record")
public class InsLabelCorrectionRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;

    /**
     * 错误类型（1:无效声音,2:有效声音）
     */
    private String errorType;

    /**
     * 纠错数量
     */
    private String correctionCount;

    /**
     * 纠错时间（原注释笔误修正）
     */
    private String correctionTime;

    /**
     * 情感信息
     */
    private String correctionInfo;

    /**
     * 情感信息
     */
    private String correctionData;

    /**
     * 审核状态（1:无效声音,2:有效声音）
     */
    private String auditStatus;

    /**
     * 审核人
     */
    private String auditUser;

    /**
     * 审核人ID（原注释补充修正）
     */
    private String auditUserId;

    /**
     * 审核时间（原注释"发布时间"修正）
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    private LocalDateTime operateTime;

    /**
     * 操作人
     */
    private String operateUser;

    /**
     * 操作人ID（原注释补充修正）
     */
    private String operateUserId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String auditStatusText;

}
