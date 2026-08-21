package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/28 16:10
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsValidateModel extends Page  implements Serializable {
    @Schema(description = "规则ID")
    private String rulesId;

    @Schema(description = "工作ID")
    private String workId;

    @Schema(description = "客户ID")
    private String clientId;

    @Schema(description = "渠道ID")
    private List<String> channelId;

    @Schema(description = "是否命中 0没命中 1命中")
    private String hitState;

    @Schema(description = "数据对比 0一致 1不同")
    private String dataCompare;

    @Schema(description = "数据处理类型 单规则类型：0 测试类型：1'")
    private String dataType;

}
