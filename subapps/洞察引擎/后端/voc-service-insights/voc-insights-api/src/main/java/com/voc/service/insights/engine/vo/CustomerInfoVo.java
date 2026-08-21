package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voc.service.insights.engine.api.annotation.City;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.Province;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 14:14
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = { "statusText"})
public class CustomerInfoVo  implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 全称
     */
    @Schema(description = "全称")
    private String fullName;
    /**
     * 简称
     */
    @Schema(description = "简称")
    private String abbreviation;
    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;
    /**
     * 省
     */
    @Schema(description = "省")
    @Province
    private String province;
    /**
     * 市
     */
    @Schema(description = "市")
    @City
    private String city;
    /**
     * 联系人
     */
    @Schema(description = "联系人")
    private String contacts;
    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;
    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String email;
    /**
     * 联系地址
     */
    @Schema(description = "联系地址")
    private String address;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 停用/启用状态 停用:0 启用:1 默认启用
     */
    @Schema(description = "停用/启用状态 停用:0 启用:1 默认启用")
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String status;

    /**
     * 开通账号数
     */
    @Schema(description = "开通账号数")
    @Builder.Default
    private Integer accountNumber = 0;

    /**
     * 关联项目
     */
    @Schema(description = "关联项目")
    private String associationProjects;
    private Integer sort;

    private List<RoleAuthTree> roleAuthTreeList;
}
