package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.api.annotation.TagType;
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
public class InsHighFrequencyOpinionsModel extends Page implements Serializable {


    @Schema(description = "主键Id")
    private String id;

    @Schema(description = "标签类型名称")
    @TagType
    private String tagType;

    @Schema(description = "标签分类")
    private String tagCategoryName;

    @Schema(description = "归一观点")
    private String normalizedOpinions;

    @Schema(description = "对应观点")
    private String correspondingOpinions;

    @Schema(description = "当前频次")
    private String currentFrequency;

    @Schema(description = "环比")
    private String ratio;

    @Schema(description = "分配状态")
    private String allocationStatus;

}
