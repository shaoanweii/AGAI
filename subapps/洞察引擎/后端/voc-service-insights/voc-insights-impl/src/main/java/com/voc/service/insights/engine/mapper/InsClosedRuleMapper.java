package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.voc.service.insights.engine.entity.InsClosedRuleEntity;
import com.voc.service.insights.engine.model.InsClosedRuleQueryModel;
import com.voc.service.insights.engine.vo.InsClosedRuleCountVo;
import com.voc.service.insights.engine.vo.InsClosedRuleMd5Vo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 闭环规则主表数据访问接口
 */
@Mapper
@Repository
public interface InsClosedRuleMapper extends BaseMapper<InsClosedRuleEntity> {

    Page<InsClosedRuleEntity> queryRulePage(@Param("model") InsClosedRuleQueryModel model);

    Set<String> selectIdsByModel(@Param("model") InsClosedRuleQueryModel model);

    InsClosedRuleEntity queryRuleById(@Param("ruleId") String ruleId);

    List<InsClosedRuleCountVo> countByCategoryIds(@Param("categoryIds") Set<String> categoryIds);

    int batchUpdateIsEnabled(@Param("isEnabled") String isEnabled, @Param("idSet") Set<String> idSet,
                             @Param("operator") String operator, @Param("updateTime") LocalDateTime updateTime);

    String selectRepetitionCount(@Param("ruleId") String ruleId, @Param("resultStr") String resultStr);

    List<InsClosedRuleMd5Vo> selectRepetitionCountBatch(@Param("idSet") Set<String> idSet);

    List<String> findIdsByCategoryIds(@Param("categoryIds") Set<String> categoryIds,@Param("status")String status);
}