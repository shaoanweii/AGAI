package com.voc.service.analysis.model;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RuleModel
 * @createTime 2024年03月07日 12:34
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleModel implements Serializable {
    @Nonnull
    String id;
    @Nonnull
    String title;

    @Nonnull
    //对应事件node id值
    String eventCode;


    //对应事件node id值
    /**
     * 处理阶段
     * pre: 前置  post： 后置
     */
    @Builder.Default
    @Nonnull
    String stage = "pre";
    /**
     * 内容类型
     * 0：文本:1：对话、2：工单
     */
    @Builder.Default
    String contentType = "0";
    /**
     * 对应规则中表名
     */
    String dataSourceCode;
    @Nonnull
    @Builder.Default
    Set<String> conditionAttrName = new HashSet<>();

    @Builder.Default
    @Nonnull
    Set<String> resultAttrName = new HashSet<>();

}