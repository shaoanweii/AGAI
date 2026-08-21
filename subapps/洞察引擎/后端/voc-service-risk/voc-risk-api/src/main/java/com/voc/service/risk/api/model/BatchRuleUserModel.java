package com.voc.service.risk.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
public class BatchRuleUserModel implements Serializable {
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "账号ID")
    private String accountId;
    @Schema(description = "渠道")
    private List<String> channel;
}
