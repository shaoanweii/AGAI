package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsCategoryRuleVo implements Serializable {

    private String categoryName;
    private String categoryId;

    private String ruleName;

    private String ruleId;


}
