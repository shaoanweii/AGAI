package com.voc.service.insights.engine.dao.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.api.IInsStaSysUserDepartService;
import com.voc.service.insights.engine.api.IInsUserRoleService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsAccountInfoDao;
import com.voc.service.insights.engine.model.InsStaSysUserDepartModel;
import com.voc.service.insights.engine.model.InsUserRoleModel;
import com.voc.service.insights.engine.vo.InsAccountInfoVo;
import com.voc.service.insights.engine.vo.InsStaSysUserDepartVo;
import com.voc.service.insights.engine.vo.InsUserRoleVo;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import com.voc.service.security.model.ChangePasswordRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 14:54
 * @描述:
 **/
@Repository
public class InsAccountInfoDaoImpl implements InsAccountInfoDao {

    private static final Logger log = LoggerFactory.getLogger(InsAccountInfoDaoImpl.class);
    @Autowired
    ISecurityServiceClient securityServiceClient;

    @Autowired
    IInsUserRoleService iInsUserRoleService;
    @Autowired
    IInsStaSysUserDepartService staSysUserDepartService;

    @Override
    public void registerAccountInfo(UserModel userModel) {
        Result<Boolean> register = securityServiceClient.register(userModel);
        if (!"200".equals(register.getCode()) || ObjectUtils.isEmpty(register.getResult())) {
            if("100040".equals(register.getCode())){
                log.warn("{}", register.getMessage());
                return;
            }else {
                log.error("调用用户服务异常:{}", register.getMessage());
                throw new BussinessException(Integer.valueOf(register.getCode()), register.getMessage());
            }
        }
        Boolean result = register.getResult();
        if (result) {
            log.info("新增账号成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.REGISTER_ACCOUNT_ERROR);
        }
    }


    @Override
    public List<UserModel> findAccountInfoList(UserModel userModel) {
        Result<List<UserModel>> result = securityServiceClient.findAll(userModel);
        if (!"200".equals(result.getCode())) {
            log.error("调用用户服务异常:{}", result.getMessage());
            throw new BussinessException(Integer.valueOf(result.getCode()), result.getMessage());
        }
        if (ObjectUtils.isEmpty(result.getResult())) {
            return Collections.EMPTY_LIST;
        } else {
            return result.getResult();
        }
    }

    @Override
    public void modifyAccountInfo(UserModel userModel) {
        Result<Boolean> modified = securityServiceClient.modifyUser(userModel);
        if (!"200".equals(modified.getCode())) {
            log.error("调用用户服务异常:{}", modified.getMessage());
            throw new BussinessException(Integer.valueOf(modified.getCode()), modified.getMessage());
        }
        Boolean result = modified.getResult();
        if (result) {
            log.info("更新账号成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.MODIFY_ACCOUNT_ERROR);
        }
    }

    @Override
    public UserModel findAccountInfo(UserModel userModel) {
        Result<List<UserModel>> user = securityServiceClient.findAll(userModel);
        if (!"200".equals(user.getCode())) {
            log.error("调用用户服务异常:{}", user.getMessage());
            throw new BussinessException(Integer.valueOf(user.getCode()), user.getMessage());
        }
        List<UserModel> result = user.getResult();
        if (ObjectUtils.isEmpty(result)) {
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "用户" + userModel.getUserId() + "不存在");
        }
        UserModel userInfo = result.stream().findFirst().get();
        return userInfo;
    }

    @Override
    public PageInfo findAccountInfoByConditional(UserModel userModel, String clientId,String roleId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("findAccountInfoList.dao 开始- 调用认证服务");
        if (ObjectUtils.isNotEmpty(roleId)){
            List<String> userIdList = iInsUserRoleService.getUserIdList(roleId, clientId);
            if (ObjectUtils.isEmpty(userIdList)){
                return new PageInfo();
            }else {
                userModel.setUserIds(userIdList);
            }
        }
        Result<PageInfo> result = securityServiceClient.findByConditional(userModel);
        if (!"200".equals(result.getCode())) {
            log.error("调用用户服务异常:{}", result.getMessage());
            throw new BussinessException(Integer.valueOf(result.getCode()), result.getMessage());
        }
        stopWatch.stop();
        stopWatch.start("findAccountInfoList.dao 认证服务返回数据组装");

        PageInfo pageInfo = result.getResult();
        List list = pageInfo.getList();
        List<String> userIdList = new ArrayList<>();
        List<String> userNameList = new ArrayList<>();
        list.stream().forEach(e -> userNameList.add(JSONUtil.toBean(JSONUtil.parseObj(e), UserModel.class).getUsername()));
        list.stream().forEach(e -> userIdList.add(JSONUtil.toBean(JSONUtil.parseObj(e), UserModel.class).getUserId()));
        InsUserRoleModel insUserRoleModel = new InsUserRoleModel();
        insUserRoleModel.setUserIdList(userIdList);
        insUserRoleModel.setClientId(clientId);
        List<InsUserRoleVo> roleInfoList = new ArrayList<>();
        try {
            roleInfoList = iInsUserRoleService.getRoleInfo(insUserRoleModel);
        } catch (Exception e) {
            log.error("获取角色信息异常:", e);
        }
        final List<InsStaSysUserDepartVo> staSysUserDepartList = staSysUserDepartService.findStaSysUserDepartList(InsStaSysUserDepartModel.builder().userIds(userNameList).build(), clientId);

        Map<String, List<InsStaSysUserDepartVo>> userDeptVoMap = staSysUserDepartList.stream().collect(Collectors.groupingBy(InsStaSysUserDepartVo::getUserId));


        Map<String, InsUserRoleVo> insUserRoleVoMap = roleInfoList.stream().collect(Collectors.toMap(InsUserRoleVo::getUserId, Function.identity()));
        List<InsAccountInfoVo> accountInfoVos = new ArrayList<>();
        list.stream().forEach(e -> {
            final UserModel user = JSONUtil.toBean(JSONUtil.parseObj(e), UserModel.class);
            List<AccountModel> accounts = user.getAccounts();
            List<InsAccountInfoVo> list1 = new ArrayList<>();
            for (AccountModel k : accounts) {
                InsAccountInfoVo build = InsAccountInfoVo.builder()
                        .userId(k.getUserId())
                        .accountName(user.getUsername())
                        .userName(user.getFirstname())
                        .employeeId(user.getEmployeeId())
                        .roleId(insUserRoleVoMap.containsKey(k.getUserId()) ? insUserRoleVoMap.get(k.getUserId()).getRoleId() : "")
                        .roleName(insUserRoleVoMap.containsKey(k.getUserId()) ? insUserRoleVoMap.get(k.getUserId()).getRoleName() : "")
                        .status(user.isEnabled() ? "1" : "0")
                        .loginCounts(k.getLoginCounts())
                        .lastLoginTime(k.getLastLoginTime())
                        .completeRate(user.getLoginCompleteRate())
                        .build();

                if(ObjectUtils.isNotEmpty(userDeptVoMap)&&userDeptVoMap.containsKey(user.getUsername())){
                    final List<InsStaSysUserDepartVo> staSysUserDepartVos = userDeptVoMap.get(user.getUsername());
                    String deptName = staSysUserDepartVos.stream()
                            .map(InsStaSysUserDepartVo::getDeptName)
                            .filter(ObjectUtils::isNotEmpty)
                            .collect(Collectors.joining("、"));
                    build.setDeptName(deptName);
                    build.setDeptId(ObjectUtils.isEmpty(staSysUserDepartVos)?null:staSysUserDepartVos.get(0).getDepId());
                }
                list1.add(build);
            }
            accountInfoVos.addAll(list1);
        });

        pageInfo.setList(accountInfoVos);
        stopWatch.stop();
        stopWatch.prettyPrint();
        return pageInfo;
    }

    @Override
    public void deleteAccountInfo(UserModel userModel) {
        Result<Boolean> result = securityServiceClient.removeUser(userModel);
        if (!"200".equals(result.getCode())) {
            log.error("调用用户服务异常:{}", result.getMessage());
            throw new BussinessException(Integer.valueOf(result.getCode()), result.getMessage());
        }
        Boolean remove = result.getResult();
        if (remove) {
            log.info("删除账号成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.REMOVE_ACCOUNT_ERROR);
        }

    }

    @Override
    public void resetPassword(ChangePasswordRequest changePwd) {
        Result<Boolean> result = securityServiceClient.resetPassword(changePwd);
        if (!"200".equals(result.getCode())) {
            log.error("调用用户服务异常:{}", result.getMessage());
            throw new BussinessException(Integer.valueOf(result.getCode()), result.getMessage());
        }
        Boolean resetPassword = result.getResult();
        if (resetPassword) {
            log.info("重置密码成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.RESET_PASSWORD_ERROR);
        }
    }

    @Override
    public void changeUserByClientId(UserModel userModel) {
        Result<Boolean> changeUser = securityServiceClient.changeUser(userModel);
        if (!"200".equals(changeUser.getCode())) {
            log.error("调用用户服务异常:{}", changeUser.getMessage());
            throw new BussinessException(Integer.valueOf(changeUser.getCode()), changeUser.getMessage());
        }
        Boolean result = changeUser.getResult();
        if (result) {
            log.info("更新账号成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.MODIFY_ACCOUNT_ERROR);
        }
    }
}
