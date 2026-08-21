package com.voc.service.insights.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 树形下拉框
 */
@Data
public class TreeSelectVo implements Serializable {

    private static final long serialVersionUID = 9016390975325574747L;

    private String key;

    private String title;

    private boolean isLeaf;

    private String icon;

    private String parentId;

    private String value;

    private String code;

    private List<TreeSelectVo> children;

    public boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean leaf) {
        isLeaf = leaf;
    }
}
