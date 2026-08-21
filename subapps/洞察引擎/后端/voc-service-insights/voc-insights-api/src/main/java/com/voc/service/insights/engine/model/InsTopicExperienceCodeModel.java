package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/18 14:41
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTopicExperienceCodeModel {
    @Schema(description = "类型")
    private String type;
    @Schema(description = "父级id")
    private String parentId;
}
