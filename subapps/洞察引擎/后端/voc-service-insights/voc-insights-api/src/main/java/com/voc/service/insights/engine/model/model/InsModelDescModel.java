package com.voc.service.insights.engine.model.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (InsModelDesc)请求实体对象
 *
 * @author leiww
 * @since 2024-02-21 15:32:06
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "模型详情", description = "模型详情")
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time"),
        @SortField(source = "testAcc", targer = "test_acc")
})
public class InsModelDescModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * ins_model_info的主键id
     */
    @Schema(description = "ins_model_info的主键id")
    private String modelId;
    /**
     * 应用标签
     */
    @Schema(description = "应用标签")
    private String modelLabel;
    /**
     * 模型路径
     */
    @Schema(description = "模型路径")
    private String modelPath;
    /**
     * 模型描述
     */
    @Schema(description = "模型描述")
    private String modelDesc;
    /**
     * 模型状态
     */
    @Dict(code = InsightsConstants.MODEL_STATUS)
    @Schema(description = "模型状态")
    private String status;
    /**
     * F1值
     */
    @Schema(description = "F1值")
    private String testAcc;
    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String version;
    /**
     * 版本说明
     */
    @Schema(description = "版本说明")
    private String versionDesc;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime createTime;
    /**
     * 修改用户
     */
    @Schema(description = "修改用户")
    private String updateBy;
    /**
     * 创建用户
     */
    @Schema(description = "创建用户")
    private String createBy;
}

