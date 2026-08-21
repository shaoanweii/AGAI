package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class AddWordsInfoModel implements Serializable {


    @Schema(description = "新词名称")
    private String wordName;

    @Schema(description = "来源渠道")
    private String channelSource;

    @Schema(description = "系统建议业务")
    private List<String> systemSuggestedBusiness;

    @Schema(description = "系统建议质量")
    private List<String> systemSuggestedQuality;

}
