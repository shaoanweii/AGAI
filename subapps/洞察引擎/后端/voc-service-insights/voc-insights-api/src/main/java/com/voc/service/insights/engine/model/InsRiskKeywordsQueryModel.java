package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
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
public class InsRiskKeywordsQueryModel extends Page  implements Serializable {

    @Schema(description = "所属分类")
    private String tagCategory;

    @Schema(description = "严重等级")
    private String seriousLevel;

    @Schema(description = "风险关键词")
    private String riskKeywords;

    @Schema(description = "增加类型")
    private String increaseType;

    @Schema(description = "客户ID")
    private String clientId;

}
