package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 16:07
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_account_lexicon", autoResultMap = true)
public class InsAccountLexiconEntity implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 资源id
     */
    private String resourceId;
    /**
     * 账号名称
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String accountName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String accountId;
    /**
     * 渠道
     */
    private String channel;
    /**
     * 末级渠道
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> finalChannel;
    /**
     * 状态
     */
    private String status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 更新人
     */
    private String updateUser;
    @TableField(exist = false)
    private Integer cnt;
    @TableField(exist = false)
    private String statusName;
}
