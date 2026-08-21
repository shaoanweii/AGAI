package com.voc.service.insights.engine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.voc.service.insights.engine.api.annotation.Client;
import com.voc.service.insights.engine.vo.ConditionDetailsVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTagClientBatchModel  implements Serializable {

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
    private Integer enable;

    /**
     * 关联状态
     */
    private String associationStatus;

    /**
     * 是否默认延用末级标签(延用：0，不延用：1)
     */
    private Integer isUse;
    /**
     * 标签新增类型:1 默认延用，2 新增分类
     */
    private String tagAddType;

    @Builder.Default
    private List<InsTagClientModel> tagInfoList = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<ConditionDetailsVo> tagParentVos;

}
