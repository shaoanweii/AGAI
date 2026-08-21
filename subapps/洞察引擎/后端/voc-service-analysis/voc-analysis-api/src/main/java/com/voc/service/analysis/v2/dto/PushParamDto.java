package com.voc.service.analysis.v2.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: PushParamDto
 * @Package: com.voc.service.analysis.dto
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/17 9:30
 * @Version:1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushParamDto implements Serializable {
    /**
     * 客户标识
     */
    @NotNull
    String clientId;
    /**
     * 渠道标识
     */
    @NotNull
    String channelId;
    /**
     * 内容类型：文本：text、 工单：order
     */
    @NotNull
    String contentType;

    @Builder.Default
    List<Object> data = new ArrayList<>();
}
