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
@TableName("cli_high_frequency_words")
public class InsHighFrequencyWordsEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String clientId;

    private String tagId;

    private String tagType;

    private String tagCategory;

    private String wordName;

    private Long currentFrequency;

    private String systemSuggestedBusiness;

    private String systemSuggestedQuality;

    private String channelSource;

    private Integer allocationStatus;

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
