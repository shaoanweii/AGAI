package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.model.BrandModel;
import com.voc.service.insights.engine.model.ProvinceModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/28 17:41
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfoVo {
    /**
     * id
     */
    private String id;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目描述
     */
    private String projectDesc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
    /**
     * 状态
     */
    @Dict(code = "enable_type")
    private String status;

    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private List<BrandVo> brand;

}
