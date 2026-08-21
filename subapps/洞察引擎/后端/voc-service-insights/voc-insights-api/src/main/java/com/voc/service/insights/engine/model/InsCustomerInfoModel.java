package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class InsCustomerInfoModel extends Page {
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
    private String province;
    /**
     * 市
     */
    @Schema(description = "市")
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
    private String status;
    /**
     * 省份集合
     */
    @Schema(description = "省份集合")
    private List<String> provinces;
    /**
     * 市集合
     */
    @Schema(description = "市集合")
    private List<String> cities;
    @Builder.Default
    private Integer sort = 3;

    @Schema(description = "菜单IdList")
    List<String> permissionIdList;

}
