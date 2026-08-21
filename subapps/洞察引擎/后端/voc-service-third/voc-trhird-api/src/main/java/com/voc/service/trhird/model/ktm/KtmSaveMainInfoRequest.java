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
public class KtmSaveMainInfoRequest {

    // 呈报标题
    private String title;

    // 工作项ID
    private String workItemId;

    // 手机号码
    private String phone;

    // 内容
    private String content;

    // 流程ID
    private String procInstId;
}
