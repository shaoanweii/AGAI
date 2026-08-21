package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:31
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_region")
public class InsRegionEntity {
    /**
     * 主键
     */
    private String id;

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 区域名称/分类名称
     */
    private String name;

    /**
     * 区域英文名称
     */
    private String nameEn;
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
     * 修改人
     */
    private String updateUser;
}
