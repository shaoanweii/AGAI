package com.voc.service.insights.engine.vo;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 10:58
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "key")
public class ConditionVo  implements Serializable {
    String key;
    List<ConditionDetailsVo> details;
}
