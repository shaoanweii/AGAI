package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsClosedBatchOperationModel;
import com.voc.service.insights.engine.model.InsClosedRuleModel;
import com.voc.service.insights.engine.model.InsClosedRuleQueryModel;
import com.voc.service.insights.engine.vo.InsRegulationConditionConfigVo;
import jakarta.validation.Valid;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 闭环规则服务接口
 */
public interface IInsClosedRuleService {

    /**
     * 分页查询规则列表（只查询基础信息表）
     *
     * @param queryModel 查询模型
     * @return 分页结果
     */
    PageInfo<InsClosedRuleModel> queryRulePage(InsClosedRuleQueryModel queryModel);

    /**
     * 根据ID查询规则详情（需要多线程去查询条件配置、预警配置并组装到结果中）
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    InsClosedRuleModel queryRuleDetail(String ruleId);

    /**
     * 新增规则
     * 包括规则基础信息、条件配置、预警配置，并记录版本号和创建人信息
     *
     * @param ruleModel 规则模型
     * @return 是否成功
     */
    boolean insertRule(InsClosedRuleModel ruleModel);

    /**
     * 编辑规则
     * 包括删除之前的条件配置和预警配置重新入库，并将上版本的条件配置放到历史表中
     * 记录版本号和更新人信息
     *
     * @param ruleModel 规则模型
     * @return 是否成功
     */
    boolean updateRule(InsClosedRuleModel ruleModel);

    /**
     * 复制规则
     * 包括规则基础信息、条件配置、预警配置，并记录版本号和创建人信息
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean copyRule(String ruleId);

    /**
     * 根据分类ID列表查询规则数量
     *
     * @param categoryIds 分类ID列表
     * @return 分类ID与规则数量的映射
     */
    Map<String, Integer> countByCategoryIds(Set<String> categoryIds);

    /**
     * 获取条件配置
     * @return
     */
    List<InsRegulationConditionConfigVo> findConditionConfig();

    /**
     * 批量操作规则
     *
     * @param batchOperationModel 批量操作模型
     * @return 操作成功的规则数量
     */
    Integer batchOperation(@Valid InsClosedBatchOperationModel batchOperationModel);


    List<String> findRuleIdsByCategoryIds(@Param("categoryIds") Set<String> categoryIds);
}