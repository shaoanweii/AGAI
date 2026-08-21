package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.model.ProvinceModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:19
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionConfigVo {
    /**
     * 主键
     */
    @Schema(description = "id")
    private String id;

    /**
     * 父级id
     */
    @Schema(description = "父级id")
    private String parentId;

    /**
     * 区域名称/分类名称
     */
    @Schema(description = "区域名称/分类名称")
    private String name;

    private String code;

    /**
     * 区域英文名称
     */
    @Schema(description = "区域英文名称")
    private String nameEn;
    /**
     * 区域状态
     */
    @Schema(description = "区域状态")
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String status;

    @Schema(description = "是否选中")
    @Builder.Default
    Boolean checked = Boolean.FALSE;
    /**
     * 区域(省份+城市)
     */
    @Schema(description = "区域(省份+城市)")
    private List<ProvinceModel> region;

    private List<RegionConfigVo> child;
}
