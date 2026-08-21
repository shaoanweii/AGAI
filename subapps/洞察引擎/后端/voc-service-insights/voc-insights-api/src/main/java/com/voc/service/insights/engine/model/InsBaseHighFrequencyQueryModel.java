package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class InsBaseHighFrequencyQueryModel extends Page  implements Serializable {


    private String id;

    @Schema(description = "分配状态 0否,1是")
    private String allocationStatus;

    @Schema(description = "开始时间")
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @Schema(description = "结束时间")
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "客户ID")
    private String clientId;

}
