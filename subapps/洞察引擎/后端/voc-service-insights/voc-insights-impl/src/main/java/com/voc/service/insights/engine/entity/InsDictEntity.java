package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @since 2018-12-28
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@TableName("ins_dict")
public class InsDictEntity extends Model<InsDictEntity>  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
//    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * [预留字段，暂时无用]
     * 字典类型,0 string,1 number类型,2 boolean
     * 前端js对stirng类型和number类型 boolean 类型敏感，需要区分。在select 标签匹配的时候会用到
     * 默认为string类型
     */
    private Integer type;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 删除状态
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 创建人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
