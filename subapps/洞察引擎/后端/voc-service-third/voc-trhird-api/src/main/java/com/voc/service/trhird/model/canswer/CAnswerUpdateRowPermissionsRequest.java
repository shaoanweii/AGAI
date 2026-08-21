package com.voc.service.trhird.model.canswer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/11/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CAnswerUpdateRowPermissionsRequest {

    private String memberUid;

    private int rowPermissionMode = 1;

    private List<CAnswerRowPermissionRequest> rowPermission;
}
