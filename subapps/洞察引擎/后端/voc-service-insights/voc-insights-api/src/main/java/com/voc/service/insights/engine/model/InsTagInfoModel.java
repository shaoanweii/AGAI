package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:04
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTagInfoModel  implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

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
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String enable;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
