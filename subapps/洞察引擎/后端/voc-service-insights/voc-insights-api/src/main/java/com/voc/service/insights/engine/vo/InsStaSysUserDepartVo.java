package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2025/9/11 10:06
 * @描述:
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InsStaSysUserDepartVo implements Serializable {
    private String id;

    private String userId;

    private String depId;

    private String orgType;

    private String deptName;
}
