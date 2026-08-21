package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class OpinionInfoModel implements Serializable {


    @Schema(description = "归一观点")
    private String normalizedOpinions;

    @Schema(description = "对应观点")
    private Set<String> correspondingOpinions;

    @Schema(description = "频次")
    private Long currentFrequency;

    @Schema(description = "来源渠道")
    private Set<String> channelSource;

    @Schema(description = "系统建议业务")
    private Set<String> systemSuggestedBusiness;

    @Schema(description = "系统建议质量")
    private Set<String> systemSuggestedQuality;

}
