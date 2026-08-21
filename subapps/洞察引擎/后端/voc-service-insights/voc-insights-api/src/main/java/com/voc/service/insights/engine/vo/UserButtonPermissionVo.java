package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class UserButtonPermissionVo  implements Serializable {

    /**
     * 主键     primary key
     */
    private String id;

    private String parentId;

    private String name;

    private String apiUrl;

    private Integer buttonCode;

    private Integer sortNo;

    private String icon;

    private String lastLevel;

    private String appId;

    private LocalDateTime createTime;

}
