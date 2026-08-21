package com.voc.service.insights.engine.model.alert;

import com.voc.service.insights.engine.api.annotation.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author leiww
 * @since 2024/04/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "数据监控", description = "数据监控相关接口")
@EqualsAndHashCode(callSuper = false)
public class InsAltDataModel  implements Serializable {

    @Channel
    @Schema(description = "渠道ID")
    private String channelId;

    @Schema(description = "统计数量")
    private BigDecimal statistics;

    @Schema(description = "阈值: 1异常，0正常")
    private BigDecimal threshold;

}
