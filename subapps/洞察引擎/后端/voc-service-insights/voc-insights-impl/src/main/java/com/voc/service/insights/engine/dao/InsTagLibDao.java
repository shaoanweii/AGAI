package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsTagLibEntity;
import com.voc.service.insights.engine.model.InsTagLibModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 下午2:30
 * @描述:
 **/
public interface InsTagLibDao {

    void saveTagLib(InsTagLibEntity tagLibEntity);

    Boolean checkTagLibName(String tagLibName, String tagLibId, String tagParentId);

    void updateTagLib(InsTagLibEntity tagLibEntity);

    List<InsTagLibEntity> findTagLibList(InsTagLibModel tagLibModel);

    String findTagLibNameHierarchical(String tagLibId);
    List<InsTagLibEntity> findTagLibHierarchical(List<String> tagLibIds);

    List<InsTagLibEntity> findTagLibListByParentId(String tagLibId);

    void updateBatchTagLib(List<InsTagLibEntity> tagLibEntityList);


    List<InsTagLibEntity> findTagLibByQueryWrapper(String type, String pCode);

    List<InsTagLibEntity> findTagLibChildNodeByParentId(String tagLibId);

    InsTagLibEntity findTagLibById(String tagLibId);

    List<InsTagLibEntity> UpwardFindTagLibHierarchical(List<String> tagParentIds);

    InsTagLibEntity findTagLibByName(String name, String tagParentId);

    List<InsTagLibEntity> findTagLibByIds(List<String> ids);

    InsTagLibEntity findTagLibByCode(String code);

}
