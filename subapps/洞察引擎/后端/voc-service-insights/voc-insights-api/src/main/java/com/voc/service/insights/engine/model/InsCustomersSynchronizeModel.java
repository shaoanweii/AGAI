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
public class InsCustomersSynchronizeModel implements Serializable {


    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "加密key")
    @NotBlank(message = "加密key不能为空")
    private String encryptKey;

}
