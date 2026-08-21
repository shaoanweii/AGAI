package com.voc.service.insights.engine.api.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Title: LargeDigitaFilesModel
 * @Package: com.voc.service.insights.engine.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/15 18:46
 * @Version:1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LargeDigitaFilesModel extends Page implements Serializable {

    String id;
    @Schema(description = "任务id")
    String taskId;
    @Schema(description = "用户id")
    String userId;
    @Schema(description = "用户名称")
    String userName;
    @Schema(description = "任务名称")
    String taskName;
    @Schema(description = "文件类型")
    String type;
    @Schema(description = "下载状态")
    String status;
    @Schema(description = "文件key")
    String fileKey;
    @Schema(description = "文件下载地址")
    String fileUrl;
    @Schema(description = "文件名称")
    String fileName;
    @Schema(description = "请求参数")
    String parameters;
    @Schema(description = "创建时间")
    LocalDateTime createTime;
    @Schema(description = "是否全部可见")
    private Boolean isAllVisible;

    @Builder.Default
    private String appClient="764547797eb2e192763f5334028d49c9";

    private List<String> userIds;
    private String appId;
}
