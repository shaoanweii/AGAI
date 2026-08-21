package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2026/4/9 13:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsAttributeLabelVo implements Serializable {
    @Schema(description = "id")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
