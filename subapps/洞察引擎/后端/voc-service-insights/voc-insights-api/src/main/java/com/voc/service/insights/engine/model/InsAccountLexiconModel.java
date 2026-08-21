package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
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
public class InsAccountLexiconModel extends Page implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "资源id")
    private String resourceId;
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "账号ID")
    private String accountId;
    @Schema(description = "渠道")
    private String channel;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "关键词")
    private String keyword;
    @Schema(description = "id列表")
    private List<String> ids;
}
