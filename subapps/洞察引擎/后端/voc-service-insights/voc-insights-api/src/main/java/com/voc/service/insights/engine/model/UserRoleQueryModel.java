package com.voc.service.insights.engine.model;

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
public class UserRoleQueryModel implements Serializable {

    @Schema(description = "用户ID")
    @NotBlank(message = "用户ID不能为空")
    private String userId;


    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;
    @Builder.Default
    private Boolean tree = Boolean.FALSE;

    private Boolean admin;

}
