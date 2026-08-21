package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.ChannelExcelModel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:33
 * @描述:
 **/
public interface IInsChannelInfoService {

//    List<ChannelInfoVo> findChannelInfoTree();
//    List<ChannelInfoVo> findAllChannelInfo();

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午1:46
     * @描述   新增渠道
     * @param insChannelInfoModel
     * @return void
     **/
    void saveChannel(InsChannelInfoModel insChannelInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午1:47
     * @描述   更新渠道
     * @param insChannelInfoModel
     * @return void
     **/
    void updateChannel(InsChannelInfoModel insChannelInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:26
     * @描述   获取渠道分类树
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.ChannelInfoVo>
     **/
    List<ChannelInfoVo> findChannelCategoryTree(InsChannelInfoModel insChannelInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:44
     * @描述  根据父级id分页获取渠道列表
     * @param insChannelInfoModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/12 上午9:26
     * @描述   删除渠道分类及其下级所有分类
     * @param insChannelInfoModel
     * @return void
     **/
    void deleteChannel(InsChannelInfoModel insChannelInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/12 上午10:11
     * @描述  获取渠道树
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.ChannelInfoVo>
     **/
    List<ChannelInfoVo> findChannelTree(InsChannelInfoModel insChannelInfoModel);

    List<String> findDownChannelByCode(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.lang.Boolean
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午6:06
     * @描述 获取全部渠道信息
     **/
    List<ChannelInfoVo> findAllChannelInfo(InsChannelInfoModel insChannelInfoModel);

    List<ChannelInfoVo> findAll(InsChannelInfoModel insChannelInfoModel);

    List<ChannelInfoVo> upwardFindChannelHierarchical(InsChannelInfoModel insChannelInfoModel);
    List<ChannelInfoVo> upwardFindChannelHierarchicalTree(InsChannelInfoModel insChannelInfoModel);

    String findChannelNameByChannelCode(InsChannelInfoModel insChannelInfoModel);

    void analyzeExcelData(List<ChannelExcelModel> list);

    void uploadExcel(MultipartFile file);

    List<ChannelInfoVo> upwardFindAllChannelHierarchical(String clientId);

}
