package com.voc.service.logs.model;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserMenuVisitRecordModel implements Serializable {


    @TableId
    @Schema(description = "主键",hidden = true)
    private String id;

    @Schema(description = "用户id",hidden = true)
    private String userId;
    @Schema(description = "工号",hidden = true)
    private String userCode;
    @Schema(description = "姓名",hidden = true)
    private String userName;
    @Schema(description = "访问url")
    private String visitUrl;

    @Schema(description = "菜单id")
    private String menuId;

    @Schema(description = "会话id",hidden = true)
    private String sessionId;
    
    @Schema(description = "APP类型(app,pc)",hidden = true)
    private String appType;
    
    @Schema(description = "模块编码", hidden = true)
    private String modelCode;
    
    @Schema(description = "菜单名称")
    private String menuName;
    
    @Schema(description = "IP地址",hidden = true)
    private String ipAddr;
    @Schema(description = "前端访问路由")
    private String frontRouting;
    
    @Schema(description = "开始访问时间",hidden = true)
    private LocalDateTime visitTime;
    @Schema(description = "开始访日期",hidden = true)
    private String visitDate;
    
    @Schema(description = "结束访问时间",hidden = true)
    private LocalDateTime endTime;
    @Schema(description = "创建时间",hidden = true)
    private LocalDateTime createTime;
}
