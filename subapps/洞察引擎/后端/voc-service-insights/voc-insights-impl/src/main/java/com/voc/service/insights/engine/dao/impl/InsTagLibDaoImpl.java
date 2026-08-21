package com.voc.service.insights.engine.dao.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsTagLibDao;
import com.voc.service.insights.engine.entity.InsTagLibEntity;
import com.voc.service.insights.engine.mapper.InsTagLibMapper;
import com.voc.service.insights.engine.model.InsTagLibModel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 下午2:30
 * @描述:
 **/
@Repository
public class InsTagLibDaoImpl extends ServiceImpl<InsTagLibMapper, InsTagLibEntity> implements InsTagLibDao {
    private static final Logger log = LoggerFactory.getLogger(InsTagLibDaoImpl.class);
    @Autowired
    private InsTagLibMapper tagLibMapper;

    @Override
    public void saveTagLib(InsTagLibEntity tagLibEntity) {
        int insert = tagLibMapper.insert(tagLibEntity);
        if (insert > 0) {
            log.info("保存标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_TAGLIB_ERROR);
        }
    }

    @Override
    public Boolean checkTagLibName(String tagLibName, String tagLibId, String tagParentId) {
        Assert.hasLength(tagLibName, "标签名称不能为空");
        Assert.hasLength(tagParentId, "标签所属分类不能为空");
        InsTagLibEntity insTagLibEntity = tagLibMapper.checkTagLibName(tagLibName, tagParentId);
        if (ObjectUtils.isNotEmpty(insTagLibEntity)) {
            if (ObjectUtils.isNotEmpty(tagLibId) && tagLibId.equals(insTagLibEntity.getId())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateTagLib(InsTagLibEntity tagLibEntity) {
        System.out.println(JSON.toJSONString(tagLibEntity.getEnergyType()));
        tagLibEntity.setCarTypes(JSON.toJSONString(tagLibEntity.getCarType()));
//        tagLibEntity.setUserJourneys(JSON.toJSONString(tagLibEntity.getUserJourney()));
        tagLibEntity.setEnergyTypes(JSON.toJSONString(tagLibEntity.getEnergyType()));
        tagLibEntity.setAppClients(JSON.toJSONString(tagLibEntity.getAppClient()));
        int update = tagLibMapper.updateTagLibById(tagLibEntity);
        if (update > 0) {
            log.info("更新标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_TAGLIB_ERROR);
        }
    }

    @Override
    public List<InsTagLibEntity> findTagLibList(InsTagLibModel tagLibModel) {
        return tagLibMapper.findTagLibList(tagLibModel);
    }

    @Override
    public String findTagLibNameHierarchical(String tagLibId) {
        return tagLibMapper.findTagLibNameHierarchical(tagLibId);
    }

    @Override
    public List<InsTagLibEntity> findTagLibHierarchical(List<String> tagLibIds) {
        return tagLibMapper.findTagLibHierarchical(tagLibIds);
    }

    @Override
    public List<InsTagLibEntity> findTagLibListByParentId(String tagLibId) {
        List<InsTagLibEntity> tagLibHierarchicalToDown = tagLibMapper.findTagLibListByParentId(tagLibId);
        return tagLibHierarchicalToDown;
    }

    @Override
    public void updateBatchTagLib(List<InsTagLibEntity> tagLibEntityList) {
        boolean updatedBatchById = this.updateBatchById(tagLibEntityList);
        if (!updatedBatchById) {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_BATCH_TAGLIB_ERROR);
        }
        log.info("批量更新标签信息成功");
    }

    @Override
    public List<InsTagLibEntity> findTagLibByQueryWrapper(String type, String pid) {
        LambdaQueryWrapper<InsTagLibEntity> query = new LambdaQueryWrapper<InsTagLibEntity>().eq(InsTagLibEntity::getTagParentId, pid).eq(InsTagLibEntity::getTagType, type).isNotNull(InsTagLibEntity::getTagCode).orderByDesc(InsTagLibEntity::getTagCode);
        return this.list(query);
    }

    @Override
    public List<InsTagLibEntity> findTagLibChildNodeByParentId(String tagLibId) {
        LambdaQueryWrapper<InsTagLibEntity> queryWrapper = new LambdaQueryWrapper<InsTagLibEntity>().eq(InsTagLibEntity::getId, tagLibId).isNotNull(InsTagLibEntity::getTagCode);
        return this.list(queryWrapper);
    }

    @Override
    public InsTagLibEntity findTagLibById(String tagLibId) {
        Assert.hasLength(tagLibId, "标签id不能为空");
        return tagLibMapper.findTagLibById(tagLibId);
    }

    @Override
    public List<InsTagLibEntity> UpwardFindTagLibHierarchical(List<String> tagParentIds) {
        Assert.notEmpty(tagParentIds, "标签父级id不能为空");
        return tagLibMapper.UpwardFindTagLibHierarchical(tagParentIds);
    }

    @Override
    public InsTagLibEntity findTagLibByName(String name, String tagParentId) {
        return tagLibMapper.checkTagLibName(name, tagParentId);
    }

    @Override
    public List<InsTagLibEntity> findTagLibByIds(List<String> ids) {
        Assert.isTrue(ObjectUtils.isNotEmpty(ids), "标签id集合不能为空");
        return tagLibMapper.findTagLibByIds(ids);
    }

    @Override
    public InsTagLibEntity findTagLibByCode(String code) {
        return tagLibMapper.findTagLibByCode(code);
    }
}
