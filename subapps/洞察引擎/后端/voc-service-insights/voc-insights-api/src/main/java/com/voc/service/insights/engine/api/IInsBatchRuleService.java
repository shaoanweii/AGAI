package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsBatchRuleBatchOperationModel;
import com.voc.service.insights.engine.model.InsBatchRuleHisModel;
import com.voc.service.insights.engine.model.InsBatchRuleModel;
import com.voc.service.insights.engine.model.InsBatchRuleQueryModel;
import com.voc.service.insights.engine.vo.InsBatchRegulationConditionConfigVo;
import com.voc.service.insights.engine.vo.InsIndicatorConfigVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 批量规则服务接口
 * 提供规则的增删改查、复制、批量操作等功能
 */
public interface IInsBatchRuleService {

    /**
     * 分页查询规则列表
     * 支持按规则名称、分类、品牌、状态等条件筛选
     *
     * @param queryModel 查询模型
     * @return 分页结果
     */
    PageInfo<InsBatchRuleModel> queryRulePage(InsBatchRuleQueryModel queryModel);

    /**
     * 查询规则详情
     * 获取规则的完整信息，包括分类名称等
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    InsBatchRuleModel queryRuleDetail(String ruleId);

    /**
     * 新增规则
     * 包括规则基本信息、维度配置、指标配置等
     *
     * @param ruleModel 规则模型
     * @return 是否成功
     */
    boolean insertRule(InsBatchRuleModel ruleModel);

    /**
     * 编辑规则
     * 包括更新规则信息、保存历史记录等
     *
     * @param ruleModel 规则模型
     * @return 是否成功
     */
    boolean updateRule(InsBatchRuleModel ruleModel);

    /**
     * 复制规则
     * 复制规则的所有配置，生成新的规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean copyRule(String ruleId);

    /**
     * 删除规则
     * 删除规则及其相关的历史记录
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean deleteRule(String ruleId);

    /**
     * 批量操作规则
     * 支持批量启用/禁用规则
     *
     * @param batchOperationModel 批量操作模型
     * @return 操作成功的规则数量
     */
    Integer batchOperation(InsBatchRuleBatchOperationModel batchOperationModel);

    /**
     * 根据分类ID列表查询规则数量
     * 用于分类管理时的规则数量统计
     *
     * @param categoryIds 分类ID列表
     * @return 分类ID与规则数量的映射
     */
    Map<String, Integer> countByCategoryIds(Set<String> categoryIds);

    /**
     * 查询规则历史记录
     * 获取规则的所有历史版本
     *
     * @param ruleId 规则ID
     * @return 历史记录列表
     */
    List<InsBatchRuleHisModel> queryRuleHistory(String ruleId);

    /**
     * 查询历史记录详情
     * 获取特定历史版本的详细信息
     *
     * @param hisId 历史记录ID
     * @return 历史记录详情
     */
    InsBatchRuleHisModel queryRuleHistoryDetail(String hisId);

    /**
     * 获取条件配置
     * 获取规则条件配置信息
     *
     * @return 条件配置列表
     */
    List<InsBatchRegulationConditionConfigVo> findConditionConfig();

    /**
     * 获取指标条件配置
     * 获取指标条件配置信息，用于动态指标维护
     *
     * @return 指标条件配置列表
     */
    List<InsIndicatorConfigVo> findIndicatorConditionConfig();
}
