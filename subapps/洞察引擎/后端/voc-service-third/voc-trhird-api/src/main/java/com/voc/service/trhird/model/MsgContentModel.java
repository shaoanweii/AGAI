package com.voc.service.trhird.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MsgContentModel implements Serializable {

    @Schema(description = "消息标题")
    @Builder.Default
    private String title = "新增告警";

    @Schema(description = "消息话术")
    private List<String> content;

}
