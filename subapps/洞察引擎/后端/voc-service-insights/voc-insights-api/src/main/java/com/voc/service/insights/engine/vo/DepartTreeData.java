package com.voc.service.insights.engine.vo;

import java.util.List;
import java.util.Map;

public class DepartTreeData {
    private final List<InsSysDepartVo> topDepartList;
    private final Map<String, List<InsSysDepartVo>> childDepartMap;

    public DepartTreeData(List<InsSysDepartVo> topDepartList, Map<String, List<InsSysDepartVo>> childDepartMap) {
        this.topDepartList = topDepartList;
        this.childDepartMap = childDepartMap;
    }

    public List<InsSysDepartVo> getTopDepartList() {
        return topDepartList;
    }

    public Map<String, List<InsSysDepartVo>> getChildDepartMap() {
        return childDepartMap;
    }
}
