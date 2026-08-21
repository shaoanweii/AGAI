package com.voc.service.insights.engine.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.insights.engine.api.IInsCustomersSynchronizeService;
import com.voc.service.insights.engine.entity.InsUserRoleEntity;
import com.voc.service.insights.engine.mapper.InsCustomersSynchronizeMapper;
import com.voc.service.insights.engine.model.InsCustomersSynchronizeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InsCustomersSynchronizeServiceImpl extends ServiceImpl<InsCustomersSynchronizeMapper, InsUserRoleEntity> implements IInsCustomersSynchronizeService {

    private static final Logger log = LoggerFactory.getLogger(InsCustomersSynchronizeServiceImpl.class);
    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Boolean syncCustomersInfo(InsCustomersSynchronizeModel model) {

        log.info("开始同步客户权限信息:{}", JSON.toJSONString(model));
        String encrypt = PBEStringEncryptor.getInstance().decrypt(model.getEncryptKey());
        if (!"洞察引擎加密QWE".equals(encrypt)) {
            log.info("密钥输入错误");
            return Boolean.FALSE;
        }
        String clientId = model.getClientId();
        int saveRole = this.baseMapper.saveRole();
        log.info("新增超级管理员角色:{}", saveRole);
        if (saveRole <= 0) {
            log.info("新增超级管理员角色失败");
            return Boolean.FALSE;
        }
        int saveRoleRelationPermission = this.baseMapper.saveRoleRelationPermission(clientId);
        log.info("新增角色关联权限信息:{}", saveRoleRelationPermission);
        if (saveRoleRelationPermission <= 0) {
            log.info("新增角色关联权限信息失败");
            return Boolean.FALSE;
        }
        int saveUserRole = this.baseMapper.saveUserRole(clientId);
        log.info("新增用户角色关联信息:{}", saveUserRole);
        if (saveUserRole <= 0) {
            log.info("新增用户角色关联信息失败");
            return Boolean.FALSE;
        }
        int saveBatchMenuPermission = this.baseMapper.saveBatchMenuPermission(clientId);
        log.info("新增菜单基础信息:{}", saveBatchMenuPermission);
        if (saveBatchMenuPermission <= 0) {
            log.info("新增菜单基础信息失败");
            return Boolean.FALSE;
        }
        int saveBatchButtonPermission = this.baseMapper.saveBatchButtonPermission(clientId);
        log.info("新增按钮基础信息:{}", saveBatchButtonPermission);
        if (saveBatchButtonPermission <= 0) {
            log.info("新增按钮基础信息失败");
            return Boolean.FALSE;
        }
        log.info("新增超级管理员权限相关信息结束");
        return Boolean.TRUE;
    }


}