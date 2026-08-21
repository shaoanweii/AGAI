package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 16:07
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsAccountLexiconVo implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "账号ID")
    private String accountId;
    @Schema(description = "渠道")
    private String channel;
    @Schema(description = "末级渠道")
    private List<String> finalChannel;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "状态名称")
    private String statusName;
    @Schema(description = "资源组id")
    private String resourceId;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
