package com.voc.service.logs.impl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
@TableName(value = "ins_record_logs")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpsRecordLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */

    private String id;
    private String appId;

    /**
     * 创建人
     */

    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)

    String startDate;
    @TableField(exist = false)

    String endDate;

    @TableField(exist = false)
    private String reviewerUserName;
    @TableField(exist = false)
    private String reviewerUserNo;
    @TableField(exist = false)

    private String reviewerDepartId;
    @TableField(exist = false)
    private String reviewerDepartName;

    @TableField(exist = false)

    private String searchKeyword;

    @TableField(exist = false)

    private List<String> userIds;

    /**
     * 更新人
     */

    private String updateBy;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 耗时
     */

    private Long costTime;

    /**
     * IP
     */
    private String ip;

    /**
     * 请求参数
     */

    private String requestParam;

    /**
     * 请求类型
     */

    private String requestType;

    /**
     * 请求路径
     */

    private String requestUrl;
    /**
     * 请求方法
     */

    private String method;

    /**
     * 操作人用户名称
     */
    private String username;
    /**
     * 操作人用户账户
     */

    private String userid;

    @TableField(exist = false)

    private String departid;
    /**
     * 操作详细日志
     */
    private String logContent;

    /**
     * 日志类型（1登录日志，2操作日志）
     */

    private Integer logType;

    @TableField(exist = false)
    private String logTypeStr;

    /**
     * 操作类型（1查询，2添加，3修改，4删除,5导入，6导出）
     */

    private Integer operateType;

    private String tid;

    private String code;
    private String message;

    @Schema(description = "访问来源（PC，APP）")
    private String accessSourceType;

}
