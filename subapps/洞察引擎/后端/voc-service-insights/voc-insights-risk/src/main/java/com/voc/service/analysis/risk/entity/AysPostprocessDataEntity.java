package com.voc.service.analysis.risk.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "ays_post_process_data")
public class AysPostprocessDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String newId;

    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;

    private String inputDataId;

    private String originalId;

    private String sampleDataType;

    private String originalTextScene;

    private String brandCodeName;

    private String carSeriesName;

    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;

    private String sentiment;

    private String intentionType;

    private String scenario;

    private String topic;

    private String opinion;

    private String subject;

    private String faultLevel;

    private String description;

    private String sentimentScore;

    private String keywords;

    private LocalDateTime publishTime;

    private Integer modelType;

    private Object extFields;


    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();


    String hitRules;
    @Builder.Default
    String done = "0";

    /**
     * 是否遗弃数据 是：1，否：0"
     */
    @Builder.Default
    String abandon = "0";
    @TableField(exist = false)
    private String url;

}
