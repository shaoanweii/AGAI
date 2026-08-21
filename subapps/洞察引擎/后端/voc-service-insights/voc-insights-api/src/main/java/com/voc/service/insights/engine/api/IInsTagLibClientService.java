package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.InsTopicModel;
import com.voc.service.insights.engine.model.TagLibExcelModel;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午9:28
 * @描述:
 **/
public interface IInsTagLibClientService {

    /**
     * @param tagLibClientModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 上午9:35
     * @描述 新增标签应用
     **/
    String saveTagLibClient(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 上午10:35
     * @描述   更新标签应用
     * @param tagLibClientModel
     * @return void
     **/
    void updateTagLibClient(InsTagLibClientModel tagLibClientModel);

    /**
     * @param tagLibClientModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 上午10:46
     * @描述 根据id删除标签应用
     **/
    void deleteTagLibClient(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 上午11:05
     * @描述   分页获取标签应用列表
     * @param tagLibClientModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findTagLibClientList(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 下午3:46
     * @描述   根据id获取标签应用
     * @param tagLibClientModel
     * @param tagLibClientModel
     * @return com.voc.service.insights.engine.vo.TagLibClientVo
     **/
    TagLibClientVo findTagLibClient(InsTagLibClientModel tagLibClientModel);

    List<TagLibClientVo> findTagLibClientVoList(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 下午4:34
     * @描述   复制指定标签至指定客户下
     * @param tagLibClientModel
     * @return void
     **/
    void copyTagLibClient(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/3/18
     * @描述   按标签类型获取分类列表树，过滤各类型末级分类并汇总末级数量，同时返回是否存在末级观点
     * @param tagLibClientModel
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findCategoryList(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/3/19
     * @描述   分页获取体验代码列表，根据分类id查询末级标签，并返回是否存在末级观点
     * @param tagLibClientModel
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.voc.service.insights.engine.vo.TagLibClientVo>
     **/
    IPage<TagLibClientVo> findExperienceCodeList(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/27 下午5:12
     * @描述  获取客户标签分类树
     * @param tagLibClientModel
     * @return java.util.Map<java.lang.String,java.util.List<com.voc.service.insights.engine.vo.DictInfoVo>>
     **/
    Map<String, List<DictInfoVo>> findTagLibRelatedItems(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/27 下午5:20
     * @描述        
     * @param clientId 
 * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findTagLibClientCategoryTree(String clientId, String tagLibType);

    List<TagLibCategoryVo> allLibClientCategoryTree(String clientId, String tagLibType);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/28 下午3:32
     * @描述   获取标签树
     * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findTagLibTree(String tagLibType);


    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/19 下午2:07
     * @描述  获取客户标签分类树
     * @param clientId
     * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findTagLibClientTree(String clientId, String tagLibType);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/11/20 下午4:04
     * @描述     获取客户标签一级+二级分类树
     * @param clientId
     * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findTagLibClientTreeLevel(String clientId, String tagLibType);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/19 下午2:07
     * @描述   获取客户一级+二级标签集合
     * @param clientId
     * @param tagLibType
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibCategoryVo>
     **/
    List<TagLibCategoryVo> findTagLibTwoLevel(String clientId, List<Integer> tagLibType);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/26 上午8:55
     * @描述   获取已调用的客户标签集
     * @param tagLibClientModel
     * @return java.util.List<java.lang.String>
     **/
    List<String> findCalledTagLibClient(InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> findDownTagLibHierarchical(InsTagLibClientModel tagLibClientModel);
    List<TagLibCategoryVo> findUpTagLibHierarchical(InsTagLibClientModel tagLibClientModel);

    List<String> findAllTagLibClientIds(InsTagLibClientModel tagLibClientModel);

    void analyzeExcelData(List<TagLibExcelModel> list);

    void uploadExcel(MultipartFile file);

    void batchDeleteTagLibClient(InsTagLibClientModel tagLibClientModel);

    void batchMoveTagLibClient(InsTagLibClientModel tagLibClientModel);

    void batchUpdateStatusTagLibClient(InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> findAllFinalTagLib(InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> findClientCategoryTree(InsTagLibClientModel tagLibClientModel);

    void batchDownloadTagLibClient(InsTagLibClientModel tagLibClientModel, HttpServletResponse response);

    void removeReportTagLibeCache();
    void removeTagLibeCache(String clientId);

    List<TagLibClientTreeVo> findAllFinalTagLibClientVoList(InsTagLibClientModel tagLibClientModel);

    InsTagLibVo findAllDisableTagLibClient(InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> findTagLibClientTree(InsTagLibClientModel tagLibClientModel);

    List<TagLibCategoryVo> getTagLibClientTree(InsTagLibClientModel tagLibClientModel);


    void test(String clientId,String tagType);


    List<TagClientVo> findAllUpTagLibHierarchicalByTagId(InsTagLibClientModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/3/20
     * @描述   查询观点操作人列表
     * @param insTopicModel
     * @param isAllVisible
     * @return java.util.List<com.voc.service.insights.engine.vo.InsTopicOperatorVo>
     **/
    List<InsTopicOperatorVo> findTopicOperatorList(InsTopicModel insTopicModel, Boolean isAllVisible);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2025/12/17 下午5:01
     * @描述   获取观点列表
     * @param tagLibClientModel
     * @return com.github.pagehelper.PageInfo
     **/
    IPage<TopicVo> findAllTopicList(InsTopicModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/4/17
     * @描述   获取观点名称编码列表
     * @param tagLibClientModel
     * @return java.util.List<com.voc.service.insights.engine.vo.TagLibTopicVo>
     **/
    List<TagLibTopicVo> findTopicList(InsTopicModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2025/12/17 下午5:01
     * @描述   批量修改观点状态
     * @param tagLibClientModel
     * @return void
     **/
    void batchChangeTopicStatus(InsTopicModel tagLibClientModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2025/12/18 下午5:01
     * @描述   保存观点
     * @param insTopicModel
     * @return void
     **/
    void saveTopic(InsTopicModel  insTopicModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/1/4 10:28
     * @描述 批量更新观点
     * @return void
     **/
    void batchUpdateTopic(InsTopicModel  insTopicModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/4/13
     * @描述   批量合并观点
     * @param insTopicModel
     * @return void
     **/
    void batchMergeTopic(InsTopicModel insTopicModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2026/1/5 15:07
     * @描述   根据编码获取观点
     * @param insTopicModel
     * @return com.voc.service.insights.engine.vo.InsTopicVo
     **/
    InsTopicVo findTopicByCode(InsTopicModel insTopicModel);

}
