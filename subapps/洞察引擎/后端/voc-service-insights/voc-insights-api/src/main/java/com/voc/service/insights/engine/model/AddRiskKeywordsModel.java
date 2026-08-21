package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class AddRiskKeywordsModel implements Serializable {


    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "客户标识")
    @NotBlank(message = "客户标识不能为空")
    private String clientId;

    @Schema(description = "所属分类")
    @NotBlank(message = "所属分类不能为空")
    private String tagCategory;

    @Schema(description = "风险关键词")
    @NotBlank(message = "风险关键词不能为空")
    private String riskKeywords;

    @Schema(description = "扩展词")
    private String extendedWord;

    @Schema(description = "严重等级")
    @NotBlank(message = "严重等级不能为空")
    private String seriousLevel;

    @Schema(description = "启用状态: 0 待审核,1已启用,2已禁用")
    @NotBlank(message = "启用状态不能为空")
    private String enableStatus;

}
