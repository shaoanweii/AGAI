package com.voc.service.insights.engine.impl;

import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsMenuPermsInfoService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsMenuPermsInfoDao;
import com.voc.service.insights.engine.entity.InsMenuPermsInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.model.InsMenuPermsInfoModel;
import com.voc.service.insights.engine.vo.InsMenuPermsInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 17:10
 * @描述:
 **/
@Service
public class InsMenuPermsInfoServiceImpl implements IInsMenuPermsInfoService {

    private static final Logger log = LoggerFactory.getLogger(InsMenuPermsInfoServiceImpl.class);
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    InsMenuPermsInfoDao menuPermsInfoDao;


    @Override
    public void saveMenuPerms(List<InsMenuPermsInfoModel> menuPermsInfoModel) {
        try {
            this.checkParams(menuPermsInfoModel);
        } catch (Exception e) {
            log.error("参数校验异常:{}", e.getMessage());
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, e.getMessage());
        }
        //对象转换
        log.debug("转换前 menuPermsInfoModel:{}", menuPermsInfoModel);
        List<InsMenuPermsInfoEntity> insMenuPermsInfoEntities = insConvertMapperService.menuPermsInfoModelListConvertEntityList(menuPermsInfoModel);
        log.debug("转换后 insMenuPermsInfoEntity:{}", insMenuPermsInfoEntities);
        insMenuPermsInfoEntities.stream().forEach(e -> e.setId(IdWorker.getId()));
        menuPermsInfoDao.saveBatchMenuPermsInfo(insMenuPermsInfoEntities);
    }

    @Override
    public void updateMenuPerms(List<InsMenuPermsInfoModel> menuPermsInfoModel) {
        try {
            this.checkParams(menuPermsInfoModel);
        } catch (Exception e) {
            log.error("参数校验异常:{}", e.getMessage());
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, e.getMessage());
        }
        final String username = ServiceContextHolder.getUsername();
        final InsMenuPermsInfoModel insMenuPermsInfoModel = menuPermsInfoModel.stream().findFirst().get();
        //获取用户已存在的菜单权限
        List<InsMenuPermsInfoEntity> menuPermsInfoByUserId = menuPermsInfoDao.findMenuPermsInfoByUserId(insMenuPermsInfoModel.getUserId());
        log.debug("转换前 menuPermsInfoModel:{}", menuPermsInfoModel);
        List<InsMenuPermsInfoEntity> insMenuPermsInfoEntities = insConvertMapperService.menuPermsInfoModelListConvertEntityList(menuPermsInfoModel);
        log.debug("转换后 insMenuPermsInfoEntity:{}", insMenuPermsInfoEntities);
        //新增菜单权限
        List<InsMenuPermsInfoEntity> collect = insMenuPermsInfoEntities.stream().filter(e -> ObjectUtils.isEmpty(e.getId())).collect(Collectors.toList());
        collect.stream().forEach(e -> {
            e.setId(IdWorker.getId());
            e.setCreateTime(LocalDateTime.now());
        });

        menuPermsInfoByUserId.stream().forEach(e -> {
            InsMenuPermsInfoEntity menuPermsInfo = insMenuPermsInfoEntities.stream().filter(k -> e.getMenuId().equalsIgnoreCase(k.getMenuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(menuPermsInfo)) {
                //删除规则
                e.setDelFlag(1);
                e.setEnabled(0);
            } else {
                BeanUtils.copyProperties(menuPermsInfo, e);
            }
            e.setUpdateTime(LocalDateTime.now());
            e.setOperator(username);
        });
        menuPermsInfoByUserId.addAll(collect);

        menuPermsInfoDao.updateBatchMenuPermsInfo(menuPermsInfoByUserId);
    }

    @Override
    public List<InsMenuPermsInfoVo> findMenuPermsList(String userId) {
        Assert.hasLength(userId, "userId不允许为空");

        List<InsMenuPermsInfoEntity> menuPermsInfo = menuPermsInfoDao.findMenuPermsInfoByUserId(userId);
        if (ObjectUtils.isEmpty(menuPermsInfo)) {
            log.debug("当前用户:{} 暂无菜单权限");
        }
        log.debug("转换前 menuPermsInfo:{}", menuPermsInfo);
        List<InsMenuPermsInfoVo> insMenuPermsInfoVos = insConvertMapperService.menuPermsInfoEntitylListConvertVoList(menuPermsInfo);
        log.debug("转换后 insMenuPermsInfoVos:{}", insMenuPermsInfoVos);
        return insMenuPermsInfoVos;
    }

    @Override
    public void deleteMenuPerms(String userId) {
        Assert.hasLength(userId, "userId不允许为空");
        menuPermsInfoDao.deleteMenuPerms(userId);
    }


    private void checkParams(List<InsMenuPermsInfoModel> menuPermsInfoModel) {
        menuPermsInfoModel.stream().forEach(e -> {
            if (ObjectUtils.isEmpty(e.getUserId())) {
                throw new RuntimeException("用户id不允许为空");
            } else if (ObjectUtils.isEmpty(e.getMenuId())) {
                throw new RuntimeException("菜单id不允许为空");
            } else if (ObjectUtils.isEmpty(e.getOperator())) {
                throw new RuntimeException("操作人不允许为空");
            } else if (ObjectUtils.isEmpty(e.getAppId())) {
                throw new RuntimeException("系统标识不允许为空");
            }
        });
    }
}
