package com.voc.service.insights.engine.dao;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.model.ChangePasswordRequest;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 14:47
 * @描述:
 **/
public interface InsAccountInfoDao {
    /**
     * @param userModel
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 16:44
     * @描述 新增账号信息
     **/
    void registerAccountInfo(UserModel userModel);

    /**
     * @param userModel
     * @return java.util.List<com.voc.service.common.model.UserModel>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 09:03
     * @描述 查询账号信息
     **/
    List<UserModel> findAccountInfoList(UserModel userModel);

    /**
     * @param userModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 09:02
     * @描述 更新账号信息
     **/
    void modifyAccountInfo(UserModel userModel);

    /**
     * @param userModel
     * @return com.voc.service.common.model.UserModel
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 10:23
     * @描述 根据用户id查询账号信息
     **/
    UserModel findAccountInfo(UserModel userModel);

    /**
     * @param userModel
     * @return com.github.pagehelper.PageInfo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 14:34
     * @描述 按条件分页查询用户信息
     **/
    PageInfo findAccountInfoByConditional(UserModel userModel, String clientId,String roleId);

    /**
     * @param userModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 17:03
     * @描述 删除账号信息
     **/
    void deleteAccountInfo(UserModel userModel);

    /**
     * @param changePwd
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/15 下午1:26
     * @描述 修改密码
     **/
    void resetPassword(ChangePasswordRequest changePwd);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/31 下午12:45
     * @描述  根据客户id修改用户信息
     * @param userModel
     * @return void
     **/
    void changeUserByClientId(UserModel userModel);


}
