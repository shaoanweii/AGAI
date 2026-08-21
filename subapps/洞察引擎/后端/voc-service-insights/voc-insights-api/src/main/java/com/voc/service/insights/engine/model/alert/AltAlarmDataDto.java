package com.voc.service.insights.engine.model.alert;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author leiww
 * @since 2024/04/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "业务指标对象", description = "业务指标对象")
@EqualsAndHashCode(callSuper = false)
public class AltAlarmDataDto  implements Serializable {

    private String channelId;

    private LocalDateTime createTime;

}