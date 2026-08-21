package com.voc.service.insights.engine.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsChannelInfoDao;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.enums.ChannelType;
import com.voc.service.insights.engine.mapper.InsChannelInfoMapper;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 16:00
 * @描述:
 **/
@Repository
public class InsChannelInfoDaoImpl extends ServiceImpl<InsChannelInfoMapper, InsChannelInfoEntity> implements InsChannelInfoDao {
    private static final Logger log = LoggerFactory.getLogger(InsChannelInfoDaoImpl.class);
    @Autowired
    InsChannelInfoMapper channelMapper;
    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsChannelInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:02
     * @描述 获取全部渠道
     **/
//    @Override
//    public List<InsChannelInfoEntity> findAllChannel() {
//        return channelMapper.findAllChannel();
//    }
    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoEntity.clientId")
    public void saveChannel(InsChannelInfoEntity insChannelInfoEntity) {
        int insert = channelMapper.insert(insChannelInfoEntity);
        if (insert > 0) {
            log.info("保存渠道信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_CHANNEL_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoEntity.clientId")
    public void updateChannel(InsChannelInfoEntity insChannelInfoEntity) {
        int update = channelMapper.updateById(insChannelInfoEntity);
        if (update > 0) {
            log.info("更新渠道信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_CHANNEL_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> findChannel(InsChannelInfoModel insChannelInfoModel) {
        return channelMapper.findChannel(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> findChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel) {
        return channelMapper.findChannelByParentId(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> findDownChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel) {
        insChannelInfoModel.setType(ChannelType.CHANNEL.getCode());
        return channelMapper.findDownChannelInfoByParentId(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> findDownChannelInfoByCode(InsChannelInfoModel insChannelInfoModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(insChannelInfoModel.getChannelCodes()), "渠道编码不能为空");
        insChannelInfoModel.setType(ChannelType.CHANNEL.getCode());
        return channelMapper.findDownChannelInfoByCode(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public void deleteChannel(InsChannelInfoModel insChannelInfoModel) {
        insChannelInfoModel.setType(ChannelType.CATEGORY.getCode());
        List<InsChannelInfoEntity> downChannelInfoByParentId = channelMapper.findDownChannelInfoByParentId(insChannelInfoModel);
        Set<String> ids = downChannelInfoByParentId.stream().map(e -> e.getId()).collect(Collectors.toSet());
        int deleted = channelMapper.deleteBatchIds(ids);
        if (deleted > 0) {
            log.info("删除渠道分类及其子分类成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.DELETE_CHANNEL_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> upwardFindChannelHierarchical(InsChannelInfoModel insChannelInfoModel) {
        return channelMapper.UpwardFindChannelHierarchical(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "channelCode.clientId")
    public List<String> findChannelIdsByChannelCode(InsChannelInfoModel channelCode) {
        return channelMapper.findChannelIdsByChannelCode(channelCode.getChannelIds());
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public List<InsChannelInfoEntity> findAllChannelInfo(InsChannelInfoModel insChannelInfoModel) {
        insChannelInfoModel.setType(ChannelType.CHANNEL.getCode());
        return channelMapper.findAllChannelInfo(insChannelInfoModel);
    }

    @Override
    @SwitchClientDS
    public List<InsChannelInfoEntity> findChannelInfoByName(String channelName, String clientId) {
        return channelMapper.findChannelInfoByName(channelName);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public InsChannelInfoEntity findChannelNameByChannelCode(InsChannelInfoModel insChannelInfoModel) {
        return channelMapper.findChannelByChannelCode(insChannelInfoModel.getCode());
    }

    @Override
    @SwitchClientDS(objectAttribute = "insChannelInfoModel.clientId")
    public InsChannelInfoEntity findChannelCountByParentIdAndName(InsChannelInfoModel insChannelInfoModel) {
        QueryWrapper<InsChannelInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsChannelInfoEntity::getParentId, insChannelInfoModel.getParentId());
        queryWrapper.lambda().eq(InsChannelInfoEntity::getName, insChannelInfoModel.getName());
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    @SwitchClientDS
    public List<InsChannelInfoEntity> upwardFindAllChannelHierarchical(String clientId) {
        return this.baseMapper.UpwardFindAllChannelHierarchical();
    }
}
