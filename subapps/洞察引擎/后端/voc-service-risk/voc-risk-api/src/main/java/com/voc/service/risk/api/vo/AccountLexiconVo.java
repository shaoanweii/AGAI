package com.voc.service.risk.api.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccountLexiconVo implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "账号ID")
    private String accountId;
    @Schema(description = "渠道")
    private String channel;
    @Schema(description = "末级渠道")
    @TableField(typeHandler = JacksonTypeHandler.class)
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
