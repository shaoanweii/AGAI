package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsSysDepartVo implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "编号")
    private String code;
    @Schema(description = "上级id")
    private String parentId;
    @Schema(description = "部门下的账号")
    private List<InsAccountVo> account;
    @Schema(description = "部门下的子部门")
    private List<InsSysDepartVo> child;

}
