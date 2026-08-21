package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cli_risk_keywords")
public class InsRiskKeywordsEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String clientId;

    private String riskKeywords;

    private String extendedWord;

    private String seriousLevel;

    private String tagCategory;

    private Long currentFrequency;

    private Integer increaseType;

    private Integer enableStatus;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private String operateUser;

    private LocalDateTime operateTime;
}
