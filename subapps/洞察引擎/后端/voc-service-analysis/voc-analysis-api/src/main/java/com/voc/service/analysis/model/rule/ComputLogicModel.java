package com.voc.service.analysis.model.rule;

import com.voc.service.insights.engine.enums.RuleStage;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ComputLogicModel
 * @createTime 2024年03月15日 10:24
 * @Copyright cuick
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComputLogicModel implements Serializable {
    @Nonnull
    String ruleId;
    @Nonnull
    String ruleName;
    @Nonnull
    String eventCode;
    @Nonnull
    int weight;
    private LocalDateTime createTime;
    @Builder.Default
    Set<String> channelIds = new HashSet<>();

    String contentType;
    /**
     * 处理阶段
     * pre: 前置  post： 后置
     */
    @Builder.Default
    String stage = RuleStage.PostRule.getCode();
    //状态
    String status;
    @Builder.Default
    private ConditionModel condition = ConditionModel.builder().build();
    @Builder.Default
    private List<ResultDataModel> resultData = new ArrayList<>();
}
