package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
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
public class InsTagInfoQueryModel extends Page  implements Serializable {

    private String type;

    private List<String> firstDimensionCodes;

    private String name;

    private String enable;

    private String seriousness;

    private String clientId;

    private String firstId;

    private String secondId;

    private String thirdId;

    private List<String> types;

}
