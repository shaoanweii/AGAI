package com.voc.service.insights.engine.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsCustomerInfoDao;
import com.voc.service.insights.engine.entity.InsCustomerInfoEntity;
import com.voc.service.insights.engine.mapper.InsCustomerInfoMapper;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:22
 * @描述:
 **/
@Repository
public class InsCustomerInfoDaoImpl extends ServiceImpl<InsCustomerInfoMapper, InsCustomerInfoEntity> implements InsCustomerInfoDao {
    private static final Logger log = LoggerFactory.getLogger(InsCustomerInfoDaoImpl.class);
    @Autowired
    InsCustomerInfoMapper customerInfoMapper;

    @Autowired
    ISecurityServiceClient securityServiceClient;


    /**
     * @param customerInfo
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 15:58
     * @描述 保存客户信息
     **/
    @Override
    public void saveCustomerInfo(InsCustomerInfoEntity customerInfo) {
        int insert = customerInfoMapper.insert(customerInfo);
        if (insert > 0) {
            log.info("保存客户信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_CUSTOMER_ERROR);
        }
    }

    /**
     * @param customerInfo
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 15:58
     * @描述 更新客户信息
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerInfo(InsCustomerInfoEntity customerInfo) {
        try {
            customerInfoMapper.updateCustomerInfo(customerInfo);
            log.info("更新客户信息成功");
        }catch (Exception e){
            throw new BussinessException(InsCommonErrorEnum.UPDATE_CUSTOMER_ERROR);
        }
    }

    /**
     * @param customerId
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 15:58
     * @描述 删除客户信息
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomerInfo(String customerId) {
        customerInfoMapper.deleteCustomerInfo(customerId, 1, LocalDateTime.now());
    }

    /**
     * @param customerId
     * @return com.voc.service.insights.engine.entity.InsCustomerInfoEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 15:58
     * @描述 查询客户信息
     **/
    @Override
    public InsCustomerInfoEntity findCustomerInfo(String customerId) {
        return customerInfoMapper.findCustomerInfo(customerId);
    }

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsCustomerInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 15:58
     * @描述 查询所有客户信息
     **/
    @Override
    public List<InsCustomerInfoEntity> findAllCustomerList() {
        return customerInfoMapper.findAllCustomerList();
    }

    /**
     * @param customerInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsCustomerInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 16:00
     * @描述 根据条件查询客户信息
     **/
    @Override
    public List<InsCustomerInfoEntity> findCustomerListByCondition(InsCustomerInfoModel customerInfoModel) {
        return customerInfoMapper.findCustomerListByCondition(customerInfoModel);
    }

    /**
     * @param customerInfoModel
     * @return java.lang.Boolean
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/23 14:33
     * @描述 校验客户编码是否存在
     **/
    @Override
    public Boolean checkCustomerCode(InsCustomerInfoModel customerInfoModel) {
        Integer codeNumber = customerInfoMapper.checkCustomerCode(customerInfoModel);
        return codeNumber > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Integer getAccountNumber(String appId, String clientId) {
        AtomicInteger accountNumber = new AtomicInteger(0);
        UserModel userModel = UserModel.builder().appId(appId).clientId(clientId).build();
        Result<List<UserModel>> result = securityServiceClient.findAll(userModel);
        if (!"200".equals(result.getCode())) {
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "调用用户服务异常," + result.getMessage());
        }
        if (ObjectUtils.isEmpty(result.getResult())) {
            return 0;
        } else {
            List<UserModel> userModelList = result.getResult();
            userModelList.stream().forEach(e -> {
                accountNumber.getAndAdd(e.getAccounts().size());
            });
            return accountNumber.get();
        }
    }
}
