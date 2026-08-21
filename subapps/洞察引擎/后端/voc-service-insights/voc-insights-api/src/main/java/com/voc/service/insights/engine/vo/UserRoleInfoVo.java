package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class UserRoleInfoVo implements Serializable {

    @Schema(description = "二级菜单权限树")
    List<RoleAuthListVo> roleAuthListVoList;

    @Schema(description = "所有权限集合")
    List<InsRolePermissionVo> insRolePermissionVos;

}
