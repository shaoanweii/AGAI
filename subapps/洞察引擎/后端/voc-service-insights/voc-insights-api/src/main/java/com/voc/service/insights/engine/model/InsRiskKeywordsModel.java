package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class InsRiskKeywordsModel extends Page  implements Serializable {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "风险关键词")
    private String riskKeywords;

    @Schema(description = "扩展词")
    private String extendedWord;

    @Schema(description = "所属分类名称")
    private String tagCategoryName;

    @Schema(description = "所属分类")
    private String tagCategory;

    @Schema(description = "严重等级")
    @Dict(code = InsightsConstants.SERIOUSNESS)
    private String seriousLevel;

    @Schema(description = "添加类型")
    private String increaseType;

    @Schema(description = "添加类型名称")
    private String increaseTypeName;

    @Schema(description = "频次")
    private String currentFrequency;

    @Schema(description = "是否启用")
    private String enableStatus;

    private String enableStatusName;

}
