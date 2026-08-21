package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.model.InsChannelInfoModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 16:00
 * @描述:
 **/
public interface InsChannelInfoDao {

//    List<InsChannelInfoEntity> findAllChannel();

    /**
     * @param insChannelInfoEntity
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午1:57
     * @描述 新增渠道信息
     **/
    void saveChannel(InsChannelInfoEntity insChannelInfoEntity);

    /**
     * @param insChannelInfoEntity
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:15
     * @描述 更新渠道信息
     **/
    void updateChannel(InsChannelInfoEntity insChannelInfoEntity);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:28
     * @描述 获取全部渠道分类
     **/
    List<InsChannelInfoEntity> findChannel(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午4:15
     * @描述 根据父级id获取渠道列表
     **/
    List<InsChannelInfoEntity> findChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/12 上午9:43
     * @描述 根据渠道分类父级id向下查找渠道
     **/
    List<InsChannelInfoEntity> findDownChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel);
    List<InsChannelInfoEntity> findDownChannelInfoByCode(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/12 上午9:48
     * @描述 根据id删除当前渠道分类及其子渠道分类
     **/
    void deleteChannel(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午3:53
     * @描述 向上查找渠道
     **/
    List<InsChannelInfoEntity> upwardFindChannelHierarchical(InsChannelInfoModel insChannelInfoModel);
    List<String> findChannelIdsByChannelCode(InsChannelInfoModel channelCode);

    /**
     * @param insChannelInfoModel
     * @return com.voc.service.insights.engine.entity.InsChannelInfoEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/14 上午9:19
     * @描述 根据渠道名称获取渠道
     **/
    List<InsChannelInfoEntity> findAllChannelInfo(InsChannelInfoModel insChannelInfoModel);

    /**
     * @param channelName
     * @param clientId
     * @return com.voc.service.insights.engine.entity.InsChannelInfoEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/9 上午9:22
     * @描述 根据渠道名称获取渠道信息
     **/
    List<InsChannelInfoEntity> findChannelInfoByName(String channelName,String clientId);

    InsChannelInfoEntity findChannelNameByChannelCode(InsChannelInfoModel insChannelInfoModel);

    /**
     * 根据渠道名称与上级渠道id获取当前渠道下是否有重复渠道
     *
     * @param insChannelInfoModel
     * @return
     */
    InsChannelInfoEntity findChannelCountByParentIdAndName(InsChannelInfoModel insChannelInfoModel);

    List<InsChannelInfoEntity> upwardFindAllChannelHierarchical(String clientId);
}
