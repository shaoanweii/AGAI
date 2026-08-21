package com.voc.service.analysis.model.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ConditionModel
 * @createTime 2024年03月15日 10:24
 * @Copyright cuick
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionModel implements Serializable {
    private String logicalSymbol;
    @Builder.Default
    private List<ConditionAttrModel> attrs= new ArrayList<>();
}
