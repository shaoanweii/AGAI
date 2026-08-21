package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTagInfoBatchModel  implements Serializable {

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 标签类型(业务标签:BIZ，质量标签:QY)
     */
    private String type;

    /**
     * 新增类型：1末级标签，2分类
     */
    private String labelType;

    /**
     * 状态(停用:0，启用:1)
     */
    private String enable;

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
     * 来源
     */
    private String source;

    /**
     * 描述
     */
    private String description;

    /**
     * 严重性
     */
    private String seriousness;
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

    List<InsTagInfoModel> tagInfos;

}
