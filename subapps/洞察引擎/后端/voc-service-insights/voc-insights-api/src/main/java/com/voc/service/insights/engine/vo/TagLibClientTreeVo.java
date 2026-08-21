package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/22 上午9:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagLibClientTreeVo implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 标签所属分类
     */
    private String tagParentId;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 标签英文名称
     */
    private String tagNameEn;
    /**
     * 标签编码
     */
    private String tagCode;
    /**
     * 标签类型
     */
    private String tagType;
    /**
     * 标签属性
     */
    private String tagAttribute;
    /**
     * 能源类型
     */
    private List<String> energyType;
    /**
     * 车辆类型
     */
    private List<String> carType;
    /**
     * 标签状态 禁用:0 启用:1
     */
    private String tagStatus;
    /**
     * 标签定义
     */
    private String tagDescription;
    /**
     * 严重性
     */
    private String seriousness;
    /**
     * 用户旅途
     */
    private List<String> userJourney;

    private String emotion;
    //意图
    private String intention;
    /**
     * 应用客户
     */
//    @Client
    private String appClient;

    private String tagLibNameHierarchical;
}
