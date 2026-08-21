package com.voc.service.insights.engine.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:34
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoVo implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "id")
    private String id;

    @Schema(description = "code")
    private String code;

    /**
     * 父级id
     */
    @Schema(description = "父级id")
    private String parentId;

    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    private String name;

    /**
     * 渠道英文名称
     */
    @Schema(description = "渠道英文")
    private String nameEn;

    @Schema(description = "渠道状态")
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String status;

    @Schema(description = "是否选中")
    @Builder.Default
    Boolean checked = Boolean.FALSE;

    private LocalDateTime createTime;

    @Schema(description = "子级渠道")
    private List<ChannelInfoVo> child;


    /**
     * 一级渠道编码
     */
    private String channelLevelOneCode;
    /**
     * 一级渠道名称
     */
    private String channelLevelOneName;
    /**
     * 二级渠道编码
     */
    private String channelLevelTwoCode;
    /**
     * 二级渠道名称
     */
    private String channelLevelTwoName;
    /**
     * 三级渠道编码
     */
    private String channelLevelThreeCode;
    /**
     * 三级渠道名称
     */
    private String channelLevelThreeName;
}
