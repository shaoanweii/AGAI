package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDepartAccountRelationVo implements Serializable {
    private String deptCode;
    private String userId;
    private String userName;
    private String employeeId;
}
