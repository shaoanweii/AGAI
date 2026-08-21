package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyDataModel implements Serializable {

    private String requestId;

    private List<String> ids;

    private List<FilterEntity> filters ;


    //1: 修改  2：删除
    @Builder.Default
    private Integer type = 1 ;

    private List<ModifyAttrs> attrs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyAttrs {
        String field;
        String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterEntity {
    	private String field;
    	private String value;
    }
}
