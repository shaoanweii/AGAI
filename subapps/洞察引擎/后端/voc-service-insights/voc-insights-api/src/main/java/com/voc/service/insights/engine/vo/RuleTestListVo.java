package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTestListVo implements Serializable {

    @Schema(description = "id")
    private String id;

    @Schema(description = "规则测试信息")
    private String ruleTestInfo;

    private String  batchId;

    @Schema(description = "规则类型")
    private String ruleType;
    @Schema(description = "规则类型")
    private String ruleTypeText;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "测试状态")
    private String ruleCount;

    @Schema(description = "样本数量")
    private String sampleCount;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "完成时间")
    private String finishTime;

    @Schema(description = "测试状态")
    private String testStatus;

    @Schema(description = "测试状态")
    private String testStatusStr;

    private List<String> ruleTestList;

    private String fileName;

    private String fileBaseName;

    private String url;
}
