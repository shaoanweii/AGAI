package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.model.InsMenuPermsInfoModel;
import com.voc.service.insights.engine.vo.InsMenuPermsInfoVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 17:09
 * @描述:
 **/
public interface IInsMenuPermsInfoService {

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 16:08
     * @描述   新增菜单权限
     * @param menuPermsInfoModel
     * @return void
     **/
    void saveMenuPerms(List<InsMenuPermsInfoModel> menuPermsInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 11:02
     * @描述  更新菜单权限
     * @param menuPermsInfoModel
     * @return void
     **/
    void updateMenuPerms(List<InsMenuPermsInfoModel> menuPermsInfoModel);
    /**
     * @param userId
     * @return java.util.List<com.voc.service.insights.engine.vo.InsMenuPermsInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 11:02
     * @描述 根据用户id获取菜单权限
     **/
    List<InsMenuPermsInfoVo> findMenuPermsList(String userId);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 17:07
     * @描述   根据用户id删除菜单权限
     * @param userId
     * @return void
     **/
    void deleteMenuPerms(String userId);

}
