package com.voc.service.insights.engine.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsProjectDetailsDao;
import com.voc.service.insights.engine.entity.InsProjectDetailsEntity;
import com.voc.service.insights.engine.mapper.InsProjectDetailsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 下午2:30
 * @描述:
 **/
@Repository
public class InsProjectDetailsDaoImpl extends ServiceImpl<InsProjectDetailsMapper, InsProjectDetailsEntity> implements InsProjectDetailsDao {
    private static final Logger log = LoggerFactory.getLogger(InsProjectDetailsDaoImpl.class);
    @Autowired
    InsProjectDetailsMapper insProjectDetailsMapper;

    @Override
    @SwitchClientDS
    public void saveBatchProjectDetails(String clientId, List<InsProjectDetailsEntity> insProjectDetailsEntity) {
        boolean saveBatch = this.saveBatch(insProjectDetailsEntity);
        if(saveBatch){
            log.info("批量保存项目详情成功");
        }else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_BATCH_PROJECT_DETAILS_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateBatchProjectDetails(String clientId, List<InsProjectDetailsEntity> insProjectDetailsEntity) {
        boolean updateBatch = this.saveOrUpdateBatch(insProjectDetailsEntity);
        if(updateBatch){
            log.info("批量更新项目详情成功");
        }else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_BATCH_PROJECT_DETAILS_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public List<InsProjectDetailsEntity> findProjectInfo(String clientId, String projectId) {
        return insProjectDetailsMapper.findProjectInfo(projectId);
    }

    @Override
    @SwitchClientDS
    public void deleteProjectInfo(String clientId, String projectId) {
        QueryWrapper<InsProjectDetailsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsProjectDetailsEntity::getProjectId, projectId);
        this.remove(queryWrapper);
    }
}
