package com.voc.service.insights.engine.api.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsRuleTestListModel extends Page implements Serializable {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "规则Id")
    private String ruleId;

    @Schema(description = "批次Id")
    private String batchId;

    @Schema(description = "规则测试信息")
    private String ruleTestInfo;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "创建人")
    private List<String> createUserName;

    @Schema(description = "测试状态")
    private String testStatus;

    private String fileName;
}
