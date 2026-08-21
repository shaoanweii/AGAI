package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_menu_permission")
public class InsMenuPermissionEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String parentId;

    private String name;

    private String htmlUri;

    private String apiUri;

    private Integer sortNo;

    private String icon;

    private String lastLevel;

    private String appId;

    private LocalDateTime createTime;

}
