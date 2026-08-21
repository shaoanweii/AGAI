package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class InsRolePermissionVo  implements Serializable {


    private String id;

    private String parentId;

    private String name;

    private String htmlUri;

    private String apiUrl;

    @Schema(description = "1菜单 2按钮")
    private Integer permissionType;

    private String permissionKey;

    private Integer buttonCode;

    private Integer sortNo;

    private String icon;

    private String lastLevel;

    private String appId;

    private Integer filterStatus;

    private LocalDateTime createTime;


}
