package com.voc.service.insights.engine.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsTagLibClientEntity;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.InsTopicModel;
import com.voc.service.insights.engine.vo.TagClientVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibTopicVo;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午9:46
 * @描述:
 **/
public interface InsTagLibClientDao {
    Boolean checkTagLibName(String tagLibClientName, String tagLibClientId, String tagType,String identifier);

    void saveTagLibClient(InsTagLibClientEntity tagLibClientEntity);

    void updateTagLibClient(InsTagLibClientEntity tagLibClientEntity);

    void deleteTagLibClient(String tagLibClientId);

    List<InsTagLibClientEntity> findTagLibByQueryWrapper(String type, String pid);

    List<InsTagLibClientEntity> findTagLibChildNodeByParentId(String pid);

    InsTagLibClientEntity findTagLibClientById(String id);

    List<InsTagLibClientEntity> findTagLibClientList(InsTagLibClientModel tagLibClientModel);

    List<InsTagLibClientEntity> findCategoryList(InsTagLibClientModel tagLibClientModel);

    IPage<InsTagLibClientEntity> findExperienceCodeList(IPage<InsTagLibClientEntity> page, InsTagLibClientModel tagLibClientModel);

    Long countExperienceCodeList(InsTagLibClientModel tagLibClientModel);

    List<InsTagLibClientEntity> findFinalTagLibClientBaseList(InsTagLibClientModel tagLibClientModel);

    String findTagLibClientNameHierarchical(String id);
    List<InsTagLibClientEntity> findTagLibClientHierarchical(List<String> ids);
    List<InsTagLibClientEntity> findTagLibClientHierarchicalByCodes(List<String> codes, String clientId);

    void saveBatchTagLibClient(List<InsTagLibClientEntity> tagLibClientEntityList, String clientId);

    List<String> findCalledTagLibClient(InsTagLibClientModel tagLibClientModel);

    InsTagLibClientEntity findTagLibClientByName(String name, String tagParentId, String clientId);

    List<InsTagLibClientEntity> findDownTagLibHierarchical(List<String> tagParentIds, List<String> tagStatusList, String tagName, List<String> ids,List<String> tagCodes,String tagType);
    List<InsTagLibClientEntity> findDownAllTagLibHierarchical(List<String> tagParentIds);
    List<InsTagLibClientEntity> findUpTagLibHierarchical(List<String> tagParentIds, List<String> tagStatusList, String tagName, List<String> ids);

    void deleteBatchTagLibClient(List<String> ids);

    void batchMoveTagLibClient(List<String> ids, String tagParentId, Integer level);

    void batchUpdateStatusTagLibClient(List<String> ids, String tagStatus);

    List<TagLibCategoryVo> findAllFinalTagLib(InsTagLibClientModel tagLibClientModel);

    List<String> findTaglibCodeByName(List<String> codes,String tagAttribute);

    List<TagLibCategoryVo> findTagLib(List<Integer> level, List<String> tagType, String tagAttribute, List<String> tagStatusList);

    List<TagClientVo> findAllUpTagLibHierarchicalByTagId(InsTagLibClientModel tagLibClientModel);

    List<TagClientVo> findAllUpTagLibHierarchicalByTopicCode(Set<String> codes);

    IPage<InsTagLibClientEntity> findlAllTopic(IPage<InsTagLibClientEntity> page, InsTopicModel tagLibClientModel);

    List<TagLibTopicVo> findTopicList(InsTopicModel tagLibClientModel);


    void batchChangeTopicStatus(InsTopicModel tagLibClientModel);

    void batchUpdateTopic(InsTopicModel insTopicModel, String scenarioAttr, List<String> attributeLabelIds);


    List<InsTagLibClientEntity> findTopic(InsTopicModel tagLibClientModel);

    Boolean findTopicCount(String topicName);

    String findMaxCode();

    void updateBatch(List<InsTagLibClientEntity> entities,String clientId);

}
