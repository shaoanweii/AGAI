package com.voc.service.insights.engine.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class InsUserRoleListVo implements Serializable {

    private String userName;

    private String userId;

    private String employeeId;

    private String departName;

    private Integer linkType;

}
