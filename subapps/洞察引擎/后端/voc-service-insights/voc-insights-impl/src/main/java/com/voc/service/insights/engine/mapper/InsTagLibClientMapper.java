package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsTagLibClientEntity;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.InsTopicModel;
import com.voc.service.insights.engine.vo.InsTopicOperatorVo;
import com.voc.service.insights.engine.vo.TagClientVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibTopicVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午9:51
 * @描述:
 **/
@Mapper
@Repository
public interface InsTagLibClientMapper extends BaseMapper<InsTagLibClientEntity> {

    InsTagLibClientEntity checkTagLibName(@Param("tagLibClientName") String tagLibClientName, @Param("tagType") String tagType, @Param("identifier") String identifier);

    InsTagLibClientEntity findTagLibClientById(@Param("tagLibClientId") String id);

    List<InsTagLibClientEntity> findTagLibClientList(@Param("tagLibClientModel") InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> findCategoryList(@Param("tagLibClientModel") InsTagLibClientModel tagLibClientModel);

    IPage<InsTagLibClientEntity>  findExperienceCodeList(IPage<InsTagLibClientEntity> page,@Param("tagLibClientModel") InsTagLibClientModel tagLibClientModel);

    Long countExperienceCodeList(@Param("tagLibClientModel") InsTagLibClientModel tagLibClientModel);

    String findTagLibClientNameHierarchical(@Param("tagLibClientId") String id);

    List<String> findCalledTagLibClient(@Param("tagLibClientModel") InsTagLibClientModel tagLibClientModel);

    List<InsTagLibClientEntity> findTagLibClientHierarchical(@Param("ids") List<String> ids);
    List<InsTagLibClientEntity> findTagLibClientHierarchicalByCodes(@Param("codes") List<String> codes);

    List<InsTagLibClientEntity> findDownTagLibHierarchical(@Param("ids") List<String> tagParentIds, @Param("tagStatusList")List<String> tagStatusList, @Param("tagName") String tagName, @Param("tagCode") List<String> tagCodes, @Param("tagType") String tagType);
    List<InsTagLibClientEntity> findDownAllTagLibHierarchical(@Param("ids") List<String> tagParentIds);

    List<String> findTaglibCodeByName(@Param("codes") List<String> codes, @Param("tagAttribute") String tagAttribute);

    List<TagClientVo> findAllUpTagLibHierarchicalByTagId(@Param("model") InsTagLibClientModel tagLibClientModel);

    List<String> findTopicOperatorUserIds();

    List<InsTopicOperatorVo> findVisibleTopicOperatorList(@Param("userIds") List<String> userIds, @Param("appId") String appId);

    IPage<InsTagLibClientEntity> findlAllTopic(IPage<InsTagLibClientEntity> page, @Param("model") InsTopicModel tagLibClientModel);

    List<TagLibTopicVo> findTopicList(@Param("model") InsTopicModel tagLibClientModel);

    List<TagClientVo> findAllUpTagLibHierarchicalByTopicCode(@Param("codes") Set<String> codes);

    String findMaxCode();

}
