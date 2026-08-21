package com.voc.service.insights.engine.model.data;

import lombok.*;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/19 下午5:21
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDataResourceExcelModel  implements Serializable {
    private String name;
}
