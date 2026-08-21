package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName(value = "report_rule_test_data")
public class InsReportRuleTestDataEntity implements Serializable {

    @Schema(description = "id")
    private String id;

    private String ruleId;

    private String  batchId;

    @Schema(description = "规则测试信息")
    private String ruleTestInfo;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "测试状态")
    private String ruleCount;

    @Schema(description = "样本数量")
    private String sampleCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "测试状态")
    private String testStatus;

    @TableField(exist = false)
    private String testStatusText;

    private String fileName;

    private String fileBaseName;


}
