package com.voc.service.logs.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @since 2018-12-26
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class OpsLogModel extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    String appId;
    String startDate;
    String endDate;
    String searchKeyword;
    List<String> userIds;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createTime;
    /**
     * 操作时间
     */
    private String operatorTime;
    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
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
     * 操作详情描述
     */
    private String logDesc;

    /**
     * 日志类型（1登录日志，2操作日志）
     */
    private Integer logType;
    @Schema(description = "访问来源（PC，APP）")
    private String accessSourceType;

    private String logTypeStr;
    /**
     * 操作类型（1查询，2添加，3修改，4删除,5导入，6导出）
     */
    private Integer operateType;

    private String tid;
    private String code;
    private String message;

    public String getLogTypeStr() {
        if(ObjectUtils.isEmpty(logType)){
            return null;
        }
        if (logType.equals(1)) {
            return "登录日志";
        } else {
            return "操作日志";
        }
    }

}
