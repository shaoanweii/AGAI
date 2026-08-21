package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleListVo implements Serializable {

    private String roleId;
    private String roleName;
    private List<String> userName;
    private List<String> userIds;
    private String userIdsStr;
    private String status;
    private String roleStatusName;
    private String remark;
    private String userCount;

}
