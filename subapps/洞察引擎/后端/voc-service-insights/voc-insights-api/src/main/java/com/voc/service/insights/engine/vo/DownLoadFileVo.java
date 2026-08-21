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
 * @创建时间: 2026/1/15 14:19
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownLoadFileVo implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件路径")
    private String filePath;
    @Schema(description = "下载时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime downloadTime;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "当前状态 空：正在下载，0：下载失败,1:下载成功")
    private String status;
}
