package com.voc.service.insights.engine.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.PhoneUtil;
import com.alibaba.fastjson.JSONArray;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsAccountInfoDao;
import com.voc.service.insights.engine.dao.InsCustomerInfoDao;
import com.voc.service.insights.engine.entity.InsCustomerInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import com.voc.service.insights.engine.model.InsMenuPermissionsModel;
import com.voc.service.insights.engine.vo.CustomerInfoVo;
import com.voc.service.insights.engine.vo.ProjectInfoVo;
import com.voc.service.insights.engine.vo.RoleAuthTree;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:20
 * @描述:
 **/
@Service
public class InsCustomerInfoServiceImpl implements IInsCustomerInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsCustomerInfoServiceImpl.class);
    @Autowired
    InsCustomerInfoDao insCustomerInfoDao;
    @Autowired
    InsConvertMapperService insCustomerInfoConvert;
    @Autowired
    IInsProjectInfoService iInsProjectInfoService;
    @Autowired
    IInsAccountInfoService accountInfoService;
    @Autowired
    IInsCustomerPermissionService iInsCustomerPermissionService;
    @Autowired
    IInsMenuPermissionService iInsMenuPermissionService;
    @Autowired
    InsAccountInfoDao accountInfoDao;
    @Value("${username.default.client:admin}")
    private String userName;

    @Value("${password.default.client:Q2m#yC18@Yo?}")
    private String password;


    /**
     * @param customerInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 10:20
     * @描述 新增客户信息
     **/
    @Override
    @CacheInvalidate(area="VDP" ,name = ":users:", key = "'C{userId}:tokens:C{token}:client'")
    @Transactional(rollbackFor = Exception.class)
    public void saveCustomerInfo(InsCustomerInfoModel customerInfoModel) {
        //必填项校验
        this.checkParameter(customerInfoModel);
        //校验客户code是否存在
        Boolean checked = this.checkCustomerCode(customerInfoModel);
        if (checked) {
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "新增客户信息失败:客户编码已存在");
        }
        final String username = ServiceContextHolder.getUsername();
        InsCustomerInfoEntity insCustomerInfoEntity = insCustomerInfoConvert.customerInfoModelConvertEntity(customerInfoModel);

        String clientId = insCustomerInfoEntity.getId();
        if (ObjectUtils.isEmpty(insCustomerInfoEntity.getId())) {
            insCustomerInfoEntity.setId(IdWorker.getId());
            clientId = insCustomerInfoEntity.getId();
        }
        insCustomerInfoEntity.setCreateTime(LocalDateTime.now());
        insCustomerInfoEntity.setCreateUser(username);
        insCustomerInfoDao.saveCustomerInfo(insCustomerInfoEntity);
        try {
            //保存客户权限信息
            iInsCustomerPermissionService.saveOrUpdate(clientId, customerInfoModel.getPermissionIdList());
            //用户数据组装
            customerInfoModel.setId(clientId);
            UserModel userModel = this.userDataAssembly(customerInfoModel);
            //注册账号
            accountInfoDao.registerAccountInfo(userModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param customerInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 11:02
     * @描述 更新客户信息
     **/
    @Override
    @CacheInvalidate(area="VDP" ,name = ":users:", key = "'C{userId}:tokens:C{token}:client'")
    public void updateCustomerInfo(InsCustomerInfoModel customerInfoModel) {
        //必填项校验
        this.checkParameter(customerInfoModel);
        //单独参数校验
        Assert.hasLength(customerInfoModel.getId(), "id不允许为空");

        boolean checked = this.checkCustomerCode(customerInfoModel);
        if (checked) {
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "更新客户信息失败:客户编码已存在");
        }
        final String username = ServiceContextHolder.getUsername();
        InsCustomerInfoEntity insCustomerInfoEntity = insCustomerInfoConvert.customerInfoModelConvertEntity(customerInfoModel);
        insCustomerInfoEntity.setUpdateTime(LocalDateTime.now());
        insCustomerInfoEntity.setUpdateUser(username);
        insCustomerInfoDao.updateCustomerInfo(insCustomerInfoEntity);
        Boolean saveBatch = iInsCustomerPermissionService.saveOrUpdate(customerInfoModel.getId(), customerInfoModel.getPermissionIdList());
        if (saveBatch) {
            try {
                iInsMenuPermissionService.updateUserMenuPermission(InsMenuPermissionsModel.builder().clientId(customerInfoModel.getId()).build());
            } catch (Exception e) {
                log.error("更新客户权限异常:", e);
            }
        }
        //更新客户下所有账号
        accountInfoDao.changeUserByClientId(UserModel.builder().clientId(customerInfoModel.getId()).enabled(customerInfoModel.getStatus().equals("1")).build());

    }

    /**
     * @param customerInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 11:24
     * @描述 删除客户信息
     **/
    @Override
    @CacheInvalidate(area="VDP" ,name = ":users:", key = "'C{userId}:tokens:C{token}:client'")
    public void deleteCustomerInfo(InsCustomerInfoModel customerInfoModel) {
        //单独参数校验
        Assert.hasLength(customerInfoModel.getId(), "id不允许为空");

        insCustomerInfoDao.deleteCustomerInfo(customerInfoModel.getId());
    }

    /**
     * @param customerInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 14:23
     * @描述 根据id查询客户信息
     **/
    @Override
    public CustomerInfoVo findCustomerInfo(InsCustomerInfoModel customerInfoModel) {
        //单独参数校验
        Assert.hasLength(customerInfoModel.getId(), "id不允许为空");

        InsCustomerInfoEntity customerInfo = insCustomerInfoDao.findCustomerInfo(customerInfoModel.getId());
        if (ObjectUtils.isEmpty(customerInfo)) {
            log.warn("id为{}的客户信息不存在", customerInfoModel.getId());
            return null;
        } else {
            CustomerInfoVo customerInfoVo = insCustomerInfoConvert.customerInfoEntityConvertVo(customerInfo);
            List<RoleAuthTree> roleAuthTreeList = iInsCustomerPermissionService.queryCustomerPermissionList(customerInfoModel.getId());
            customerInfoVo.setRoleAuthTreeList(roleAuthTreeList);
            return customerInfoVo;
        }
    }

    /**
     * @param customerInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.CustomerInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 17:40
     * @描述 根据条件查询客户列表
     **/
    @Override
    public PageInfo findCustomerList(InsCustomerInfoModel customerInfoModel) {

        final String appId = ServiceContextHolder.getSystemId();
        PageHelper.startPage(customerInfoModel.getPageNum(), customerInfoModel.getPageSize());
        List<InsCustomerInfoEntity> customerList = insCustomerInfoDao.findCustomerListByCondition(customerInfoModel);
        if (ObjectUtils.isEmpty(customerList)) {
            log.info("暂无客户信息");
            return new PageInfo<>();
        }
        PageInfo pageInfo = new PageInfo<>(customerList);
        List<CustomerInfoVo> customerInfoVos = insCustomerInfoConvert.customerInfoEntityListConvertVoList(customerList);
        //获取全部账号信息
        List<UserModel> allAccountInfoList = accountInfoService.findAllAccountInfoList(UserModel.builder().appId(appId).build());
        //获取客户开通账号数
        customerInfoVos.stream().forEach(e -> {
            AtomicInteger accountNumber = new AtomicInteger(0);
            if (ObjectUtils.isNotEmpty(allAccountInfoList)) {
                List<UserModel> collect = allAccountInfoList.stream()
                        .filter(k -> ObjectUtils.isNotEmpty(k.getClientId()))
                        .filter(k -> k.getClientId().equalsIgnoreCase(e.getId()))
                        .collect(Collectors.toList());
                collect.stream().forEach(v -> {
                    accountNumber.getAndAdd(v.getAccounts().size());
                });
                e.setAccountNumber(accountNumber.get());
            }
            try {
                List<ProjectInfoVo> allProjectInfo = iInsProjectInfoService.findAllProjectInfo(e.getId());
                if (ObjectUtils.isNotEmpty(allProjectInfo)) {
                    List<String> projectNames = allProjectInfo.stream().map(ProjectInfoVo::getProjectName).collect(Collectors.toList());
                    String s = StringUtils.join(projectNames, ",");
                    e.setAssociationProjects(s);
                }
            }catch (Exception ex){
                log.warn("未获取到客户创建的项目");
            }
        });

        pageInfo.setList(customerInfoVos);
        return pageInfo;
    }

    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.CustomerInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 17:41
     * @描述 获取所有客户信息
     **/
    @Override
    @Cached(area="VDP" ,name = ":users:", key = "':C{appId}:C{userId}:tokens:C{token}:client'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<CustomerInfoVo> findAllCustomerInfo() {
        log.trace("读取数据库");
        List<InsCustomerInfoEntity> customerList = insCustomerInfoDao.findAllCustomerList();
        if (ObjectUtils.isEmpty(customerList)) {
            log.info("暂无客户信息");
            return Collections.EMPTY_LIST;
        }
        List<CustomerInfoVo> list = insCustomerInfoConvert.customerInfoEntityListConvertVoList(customerList);
        if(log.isDebugEnabled()) {
            log.debug("转换后 CustomerInfoVo:{}", JSONArray.toJSONString(list));
        }
        return list;
    }


    /**
     * @param customerInfoModel
     * @return java.lang.Boolean
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/23 14:29
     * @描述 检验客户编码是否存在
     **/
    @Override
    public Boolean checkCustomerCode(InsCustomerInfoModel customerInfoModel) {
        //单独参数校验
        Assert.hasLength(customerInfoModel.getCode(), "客户编码不能为空");
        if (ObjectUtils.isEmpty(customerInfoModel.getId())) {
            return insCustomerInfoDao.checkCustomerCode(customerInfoModel);
        } else {
            InsCustomerInfoEntity customerInfo = insCustomerInfoDao.findCustomerInfo(customerInfoModel.getId());
            if (customerInfo.getCode().equals(customerInfoModel.getCode())) {
                return false;
            } else {
                return insCustomerInfoDao.checkCustomerCode(customerInfoModel);
            }
        }
    }

    @Override
    public String queryCodeById(String clientId) {
        Assert.hasLength(clientId, "客户编码不能为空");
        InsCustomerInfoEntity customerInfo = insCustomerInfoDao.findCustomerInfo(clientId);
        if (ObjectUtils.isNotEmpty(customerInfo)) {
            return customerInfo.getCode();
        }
        return null;
    }

    @Override
    public List<RoleAuthTree> queryCustomerPermissionList(InsCustomerInfoModel customerInfoModel) {
        Assert.hasLength(customerInfoModel.getId(), "id不允许为空");
        return iInsCustomerPermissionService.queryCustomerPermissionList(customerInfoModel.getId());
    }

    @Override
    public List<CustomerInfoVo> findCustomerListModel() {
        List<InsCustomerInfoEntity> customerList = insCustomerInfoDao.findAllCustomerList();
        if (ObjectUtils.isEmpty(customerList)) {
            log.info("暂无客户信息");
            return Collections.EMPTY_LIST;
        }
        List<CustomerInfoVo> list = insCustomerInfoConvert.customerInfoEntityListConvertVoList(customerList);
        if(log.isDebugEnabled()) {
            log.debug("转换后 CustomerInfoVo:{}", JSONArray.toJSONString(list));
        }
        return list;
    }


    /**
     * @param customerInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/28 09:15
     * @描述 必填项校验
     **/
    private void checkParameter(InsCustomerInfoModel customerInfoModel) {
        Assert.hasLength(customerInfoModel.getFullName(), "客户全称不允许为空");
        Assert.isTrue(customerInfoModel.getFullName().length() >= 1
                && customerInfoModel.getFullName().length() <= 50, "全称长度不符，字数限制为50字以内");
        Assert.hasLength(customerInfoModel.getAbbreviation(), "客户简称不允许为空");
        Assert.notEmpty(customerInfoModel.getPermissionIdList(), "客户菜单权限不允许为空");
        Assert.isTrue(customerInfoModel.getAbbreviation().length() >= 1
                && customerInfoModel.getAbbreviation().length() <= 10, "简称长度不符，字数限制10字以内");
        Assert.hasLength(customerInfoModel.getCode(), "编码不允许为空");
        Assert.isTrue(customerInfoModel.getCode().length() >= 1
                && customerInfoModel.getCode().length() <= 10, "编码长度不符，字数限制10字以内");
        Assert.hasLength(customerInfoModel.getProvince(), "省份不允许为空");
        Assert.hasLength(customerInfoModel.getCity(), "市不允许为空");
        Assert.hasLength(customerInfoModel.getStatus(), "停用/启用状态 不允许为空");
        Assert.isTrue(Integer.valueOf(customerInfoModel.getStatus()).intValue() >= 0
                && Integer.valueOf(customerInfoModel.getStatus()).intValue() <= 1, "状态码无效");
        if (ObjectUtils.isNotEmpty(customerInfoModel.getPhone())) {
            Assert.isTrue(PhoneUtil.isPhone(customerInfoModel.getPhone()), "手机号格式错误");
        }
        if (ObjectUtils.isNotEmpty(customerInfoModel.getEmail())) {
            Assert.isTrue(Validator.isEmail(customerInfoModel.getEmail()), "邮箱格式错误");
            Assert.isTrue(customerInfoModel.getEmail().length() >= 1 && customerInfoModel.getEmail().length() <= 50, "邮箱长度不符，字数限制50字以内");
        }
        if (ObjectUtils.isNotEmpty(customerInfoModel.getContacts())) {
            Assert.isTrue(customerInfoModel.getContacts().length() >= 1 && customerInfoModel.getContacts().length() <= 10, "联系人长度不符，字数限制10字以内");
        }

        if (ObjectUtils.isNotEmpty(customerInfoModel.getAddress())) {
            Assert.isTrue(customerInfoModel.getAddress().length() >= 1 && customerInfoModel.getAddress().length() <= 50, "联系地址长度不符，字数限制50字以内");
        }

        if (ObjectUtils.isNotEmpty(customerInfoModel.getRemark())) {
            Assert.isTrue(customerInfoModel.getRemark().length() >= 1 && customerInfoModel.getRemark().length() <= 200, "备注长度不符，字数限制200字以内");
        }
    }


    private UserModel userDataAssembly(InsCustomerInfoModel customerInfoModel) {
        final String appId = ServiceContextHolder.getSystemId();
        String name = customerInfoModel.getCode() + "_" + userName;
        UserModel userModel = UserModel.builder()
                .username(name)
                .password(password)
                .firstname(name)
                .lastname(name)
                .clientId(customerInfoModel.getId())
                .type("base")
                .phone(customerInfoModel.getPhone())
                .enabled(Boolean.TRUE)
                .build();
        userModel.setAdmin("0");
        userModel.setAppId(appId);
        return userModel;
    }

}
