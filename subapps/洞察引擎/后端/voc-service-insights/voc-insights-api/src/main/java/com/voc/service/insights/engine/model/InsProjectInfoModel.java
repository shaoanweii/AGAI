package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time")
})
public class InsProjectInfoModel extends Page {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;
    /**
     * 项目描述
     */
    @Schema(description = "项目描述")
    private String projectDesc;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 客户id
     */
    @Schema(description = "客户id")
    private String clientId;

    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private List<BrandModel> brand;


}
