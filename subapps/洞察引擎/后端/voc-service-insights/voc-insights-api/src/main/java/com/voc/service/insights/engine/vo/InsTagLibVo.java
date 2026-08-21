package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/23 下午4:12
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTagLibVo {
    /**
     * 一级禁用标签
     */
    private List<TagLibClientTreeVo> firstTagLib;
    /**
     * 二级禁用标签
     */
    private List<TagLibClientTreeVo> secondTagLib;
    /**
     * 三级禁用标签
     */
    private List<TagLibClientTreeVo> threeTagLib;
    /**
     * 四级禁用标签
     */
    private List<TagLibClientTreeVo> fourTagLib;
    /**
     * 五级禁用标签
     */
    private List<TagLibClientTreeVo> finalTagLib;
}
