package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: topic标签返回对象
 * @author: LiuQiang
 * @time: 2025/11/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagLibTopicVo implements Serializable {
    /**
     * id
     */
    private String id;
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
}
