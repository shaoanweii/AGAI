package com.voc.service.trhird.model.ktm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KtmStartProcRequest {

    // 流程ID
    private String procId;

    // 流程实例名称
    private String procInstName;
}
