package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.api.annotation.Client;
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
public class InsTagClientModel  implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    /**
     * 应用客户id
     */
    @Client
    private String clientId;

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签英文名称
     */
    private String nameEn;

    /**
     * 标签编码
     */
    private String code;

    /**
     * 标签类型(业务标签:BIZ，质量标签:QY)
     */
    private String type;

    /**
     * 新增类型：1末级标签，2分类
     */
    private String labelType;

    /**
     * 关联能源
     */
    private String energy;

    /**
     * 关联阶段
     */
    private String stage;

    /**
     * 关联状态
     */
    private String associationStatus;

    /**
     * 应用客户数
     */
    private Integer applyNumber;

    /**
     * 标注语料
     */
    private Integer taggedCorpus;

    /**
     * 状态(停用:0，启用:1)
     */
    private Integer enable;

    /**
     * 来源
     */
    private String source;

    /**
     * 严重性
     */
    private String seriousness;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联标签id
     */
    private String tagId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
