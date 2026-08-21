package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * VOC_RISK_ALERT_REVIEWER
 */
@Tag(name = "generate.InsRiskProcessRecipient警示审核人员")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class InsRiskProcessRecipientModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    /**
     * 审核id
     */

    @Schema(name = "审核id")
    private String auditId;
    /**
     * 处理接收人员id多个用逗号分隔
     */

    @Schema(name = "处理接收人员id多个用逗号分隔")
    private String processUserId;
    @TableField(exist = false)
    private String processUserName;
    @TableField(exist = false)
    private String processUserNo;
    /**
     * 风险事件
     */

    @Schema(name = "风险事件")
    private boolean riskEvents;
    /**
     * 质量风险
     */

    @Schema(name = "质量风险")
    private boolean qualityRisk;
    /**
     * top用户
     */

    @Schema(name = "top用户")
    private boolean topUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer delFlag;
    /**
     * 备注
     */

    @Schema(name = "备注")
    private String remark;
    @Schema(description = "品牌范围")

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> brandCodes;
    /**
     * 审核人员的组织id
     */

    @Schema(name = "接收人员的组织id")
    private String processDepartId;
    @TableField(exist = false)
    private String processDepartName;

    public List<String> getBrandCodes() {
        return brandCodes == null ? new ArrayList<>() : brandCodes;
    }
}
