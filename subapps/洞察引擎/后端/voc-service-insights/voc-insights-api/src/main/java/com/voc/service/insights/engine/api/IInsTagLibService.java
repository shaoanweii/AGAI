package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsTagLibModel;
import com.voc.service.insights.engine.vo.DictInfoVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibVo;

import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 上午10:11
 * @描述:
 **/
public interface IInsTagLibService {

    /**
     * @param tagLibModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/21 上午10:14
     * @描述 新增标签库
     **/
    String saveTagLib(InsTagLibModel tagLibModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/21 下午3:20
     * @描述   更新标签库
     * @param tagLibModel
     * @return void
     **/
     void updateTagLib(InsTagLibModel tagLibModel);

    /**
     * 分页获取标签库列表
     * @param tagLibModel
     * @return
     */
     PageInfo findTagLibList(InsTagLibModel tagLibModel);

     /**
      * @创建者/修改者 fanrong
      * @创建/更新日期 2024/5/23 下午4:06
      * @描述   获取标签详情
      * @param tagLibModel
      * @return com.voc.service.insights.engine.vo.TagLibVo
      **/
    TagLibVo findTagLib(InsTagLibModel tagLibModel);

    /**
     * @param tagLibModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/27 下午1:28
     * @描述 根据标签分类获取关联项
     **/
    Map<String, List<DictInfoVo>> findTagLibRelatedItems(InsTagLibModel tagLibModel);

    /**
     * @param clientId
     * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/27 下午2:17
     * @描述 获取标签分类树
     **/
    List<TagLibCategoryVo> findTagLibCategoryTree(String clientId, String tagLibType);

    List<TagLibVo> findTagLibByIds(List<String> ids);

    TagLibVo findTagLibByCode(String code);

}
