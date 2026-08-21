package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
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
public class InsOpinionsInfoModel  implements Serializable {


    @Schema(description = "主键Id")
    private String id;

    @Schema(description = "归一观点")
    private String normalizedOpinions;

    @Schema(description = "对应观点")
    private String correspondingOpinions;

    @Schema(description = "当前频次")
    private String currentFrequency;

    @Schema(description = "历史频次")
    private Long historyTotalFrequency;

    @Schema(description = "来源渠道")
    private String channelSource;

    @Schema(description = "系统建议业务")
    private List<String> systemSuggestedBusiness;

    @Schema(description = "系统建议质量")
    private List<String> systemSuggestedQuality;

    @Schema(description = "分配纪录")
    private List<InsAllocationRecordModel> allocationRecord;

    @Schema(description = "标签信息")
    private InsBaseTagInfoModel insBaseTagInfoModel;
}
