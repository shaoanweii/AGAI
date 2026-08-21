package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRawDataParamModel extends Page implements Serializable {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "workIdList")
    private List<String> workIdList;

    @Schema(description = "渠道ID")
    private List<String> channelIdList;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "品牌")
    private List<String> brandCode;

    @Schema(description = "本品车系")
    private List<String> ownCarSeries;

    @Schema(description = "竞品车系")
    private List<String> competitorsCarSeries;

    private List<String> mentionCarSeriesList;

    private String mentionCarSeriesString;

    @Schema(description = "城市code")
    private List<String> cityCodeList;

    @Schema(description = "车系合并集合")
    private Set<String> allCarSeriesList;

    @Schema(description = "数据类型")
    private List<String> metaDataType;

    private Integer pageType;

    private String dateType;
    @Builder.Default
    Set<String> labelTypeLevelFourDisableList = new HashSet<>();

    @Schema(description = "标签类型")
    private List<String> labelTypeList;

}
