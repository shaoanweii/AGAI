package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
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
public class InsRoleQueryModel extends Page  implements Serializable {

    @Schema(description = "角色状态")
    private String enabled;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "客户ID")
    @Builder.Default
    private String clientId = "0";

    private String roleId;

    private String searchKeyword;

    private String brandCode;

    private String brandName;

    @Builder.Default
    private Integer sort =0;

    @Builder.Default
    private Boolean checkAdmin = Boolean.FALSE;

    @Builder.Default
    private Boolean selectAll = Boolean.FALSE;

    @Builder.Default
    private List<String> permissionIdList = new ArrayList<>();
    @Schema(description = "标签类型")
    private String tagLibType;

}
