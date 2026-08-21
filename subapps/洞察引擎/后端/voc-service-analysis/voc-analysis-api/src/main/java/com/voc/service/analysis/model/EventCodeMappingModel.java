package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName EventCodeMappingModel
 * @createTime 2024年03月12日 11:24
 * @Copyright cuick
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCodeMappingModel implements Serializable {
    String code;
    Set<String> attrNameAnd;
    Set<String> attrNameOr;

}
