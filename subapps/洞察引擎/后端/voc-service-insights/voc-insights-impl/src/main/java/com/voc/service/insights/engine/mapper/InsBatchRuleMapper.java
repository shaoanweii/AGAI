package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.voc.service.insights.engine.entity.InsBatchRuleEntity;
import com.voc.service.insights.engine.model.InsBatchRuleQueryModel;
import com.voc.service.insights.engine.vo.InsBatchRuleCountVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 批量规则Mapper
 * 对应数据库表：ins_batch_rule
 */
public interface InsBatchRuleMapper extends BaseMapper<InsBatchRuleEntity> {

    /**
     * 分页查询规则列表
     *
     * @param model 查询模型
     * @return 分页结果
     */
    Page<InsBatchRuleEntity> queryRulePage(InsBatchRuleQueryModel model);

    /**
     * 根据规则ID查询规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    InsBatchRuleEntity queryRuleById(String ruleId);

    /**
     * 批量更新规则状态
     *
     * @param isEnabled 状态：enabled/disabled
     * @param idSet 规则ID集合
     * @param updater 更新人
     * @param updateTime 更新时间
     * @return 更新成功的数量
     */
    int batchUpdateIsEnabled(@Param("isEnabled") String isEnabled, @Param("idSet") Set<String> idSet, 
                             @Param("updater") String updater, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据分类ID列表统计规则数量
     *
     * @param categoryIds 分类ID列表
     * @return 分类规则数量统计
     */
    List<InsBatchRuleCountVo> countByCategoryIds(@Param("categoryIds") Set<String> categoryIds);

    /**
     * 根据分类ID查询规则数量
     *
     * @param categoryId 分类ID
     * @return 规则数量
     */
    int countByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 查询已启用的规则数量
     *
     * @return 已启用的规则数量
     */
    int countEnabledRules();
}
