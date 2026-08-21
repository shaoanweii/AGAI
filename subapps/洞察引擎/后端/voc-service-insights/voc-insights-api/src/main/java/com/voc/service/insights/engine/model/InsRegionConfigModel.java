package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:09
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsRegionConfigModel extends Page {
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

    /**
     * 区域英文名称
     */
    @Schema(description = "区域英文名称")
    private String nameEn;
    /**
     * 区域状态
     */
    @Schema(description = "区域状态")
    private String status;
    /**
     * 所属客户
     */
    @Schema(description = "所属客户")
    private String clientId;
    /**
     * 区域(省份+城市)
     */
    @Schema(description = "区域(省份+城市)")
    private List<ProvinceModel> region;

    private List<String> regionIds;
    private Set<String> parentIds;

    private String brandName;
}
