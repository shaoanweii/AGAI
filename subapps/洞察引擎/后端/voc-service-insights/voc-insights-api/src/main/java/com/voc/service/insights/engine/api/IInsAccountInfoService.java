package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsAccountInfoModel;
import com.voc.service.insights.engine.model.InsRoleQueryModel;
import com.voc.service.insights.engine.model.InsSysDepartModel;
import com.voc.service.insights.engine.vo.InsAccountInfoVo;
import com.voc.service.insights.engine.vo.InsSysDepartVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 13:51
 * @描述:
 **/
public interface IInsAccountInfoService {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 13:52
     * @描述  新增账号信息
     * @param accountInfoModel
     * @return void
     **/
    void saveAccountInfo(InsAccountInfoModel accountInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 09:00
     * @描述  更新账号信息
     * @param accountInfoModel
     * @return void
     **/
    void updateAccountInfo(InsAccountInfoModel accountInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 10:20
     * @描述   根据id获取账号信息
     * @param accountInfoModel
     * @return com.voc.service.insights.engine.vo.InsAccountInfoVo
     **/
    InsAccountInfoVo findAccountInfo(InsAccountInfoModel accountInfoModel);

    /**
     * @param accountInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsAccountInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 14:28
     * @描述 按条件分页查询账号信息
     **/
    PageInfo findAccountInfoList(InsAccountInfoModel accountInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 17:02
     * @描述   根据id删除账号信息
     * @param accountInfoModel
     * @return void
     **/
    void deleteAccountInfo(InsAccountInfoModel accountInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/6 16:29
     * @描述   查询所有账号信息
     * @param userModel
     * @return java.util.List<com.voc.service.common.model.UserModel>
     **/
    List<UserModel> findAllAccountInfoList(UserModel userModel);


    Result<?> queryRoleALlList(InsRoleQueryModel model);

    List<InsSysDepartModel> findDepartList(InsAccountInfoModel accountInfoModel);

    List<InsSysDepartVo> findDepartTree(InsAccountInfoModel accountInfoModel);

    List<InsAccountInfoVo> findAccountByDeptId(InsAccountInfoModel accountInfoModel);

    /**
     * 获取部门账号树
     * @return
     */
    List<InsSysDepartVo> findDepartAccountTree();

    /**
     * 根据部门id获取部门用户树
     * @param accountInfoModel
     * @return
     */
    List<InsSysDepartVo> findDepartAccountTreeByDeptId(InsAccountInfoModel accountInfoModel);
}
