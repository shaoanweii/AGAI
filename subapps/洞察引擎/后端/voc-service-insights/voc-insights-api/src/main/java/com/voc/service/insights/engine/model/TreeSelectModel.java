package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 树形下拉框
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeSelectModel extends Page implements Serializable {

    private static final long serialVersionUID = 9016390975325574747L;

    private String key;

    private String title;

    private boolean isLeaf;

    private String icon;

    private String parentId;

    private String value;

    private String code;

    private List<TreeSelectModel> children;

    public boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean leaf) {
        isLeaf = leaf;
    }
}
