package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用车场景操作人返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用车场景操作人返回对象")
public class InsCarSceneOperatorVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id")
    private String id;

    @Schema(description = "用户名")
    private String userName;
}
