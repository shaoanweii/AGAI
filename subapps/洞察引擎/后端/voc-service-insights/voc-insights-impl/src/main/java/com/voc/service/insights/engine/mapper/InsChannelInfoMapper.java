package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:45
 * @描述:
 **/
@Mapper
@Repository
public interface InsChannelInfoMapper extends BaseMapper<InsChannelInfoEntity> {

//    /**
//     * @创建者/修改者 fanrong
//     * @创建/更新日期 2024/2/20 15:49
//     * @描述  获取全部渠道
//     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
//     **/
//    List<InsChannelInfoEntity> findAllChannel();

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:33
     * @描述 按条件获取渠道
     **/
    List<InsChannelInfoEntity> findChannel(@Param("insChannelInfoModel") InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午4:13
     * @描述 根据父级id获取渠道列表
     **/
    List<InsChannelInfoEntity> findChannelByParentId(@Param("insChannelInfoModel") InsChannelInfoModel insChannelInfoModel);

    /**
     * @param insChannelInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/12 上午9:44
     * @描述 根据渠道分类父级id向下查找渠道
     **/
    List<InsChannelInfoEntity> findDownChannelInfoByParentId(@Param("insChannelInfoModel") InsChannelInfoModel insChannelInfoModel);
    List<InsChannelInfoEntity> findDownChannelInfoByCode(@Param("insChannelInfoModel") InsChannelInfoModel insChannelInfoModel);

    /**
     * @param channelIds
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午3:51
     * @描述 向上查找渠道
     **/
    List<InsChannelInfoEntity> UpwardFindChannelHierarchical(InsChannelInfoModel insChannelInfoModel);

    /**
     * 递归向上查找全部渠道并聚合
     * @return
     */
    List<InsChannelInfoEntity> UpwardFindAllChannelHierarchical();

    /**
     * @param insChannelInfoModel
     * @return com.voc.service.insights.engine.entity.InsChannelInfoEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/14 上午9:26
     * @描述 获取全部渠道信息
     **/
    List<InsChannelInfoEntity> findAllChannelInfo(@Param("insChannelInfoModel") InsChannelInfoModel insChannelInfoModel);

    List<InsChannelInfoEntity> findChannelInfoByName(String channelName);

    List<String> findChannelIdsByChannelCode(@Param("channelCode") List<String> channelCode);

    InsChannelInfoEntity findChannelByChannelCode(@Param("channelCode")String code);
}
