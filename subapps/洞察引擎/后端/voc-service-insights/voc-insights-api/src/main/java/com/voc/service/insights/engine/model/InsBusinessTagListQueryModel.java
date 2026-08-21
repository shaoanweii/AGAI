package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "业务指标对象", description = "业务指标对象")
@EqualsAndHashCode(callSuper = false)
public class InsBusinessTagListQueryModel extends Page  implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "搜索关键词")
    String searchKeyword;
    @Schema(description = "id编辑时传入")
    private String id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "编码")
    private String tagCode;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "父级节点")
    private String pid;
    @Schema(description = "能源类型: 0为全部，1为燃油车，2为新能源")
    private String associatedEnergy;
    @Schema(description = "是否应用,1为是")
    private Integer enable;
    @Schema(description = "咨询指标:标签类型:0:通用  1:咨询")
    private String tagType;
    @Schema(description = "相关描述")
    private String relatedDescription;
//    @Schema(description = "关联部门Ids")
//    private Set<TagDepartRelationModel> departIds;


}
