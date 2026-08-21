package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 下午1:53
 * @描述:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsDataSourceDescVo implements Serializable {
    private String dataName;
    private String importResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Dict(code = InsightsConstants.PROCESSING_STATUS)
    private String status;
    private String batchId;
    private Boolean processible;
    @Builder.Default
    private Boolean invalid = false;
    @Builder.Default
    private Boolean fail = false;

}
