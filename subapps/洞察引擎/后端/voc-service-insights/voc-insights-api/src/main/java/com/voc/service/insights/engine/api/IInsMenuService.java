package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.model.InsMenuModel;

import java.util.List;

public interface IInsMenuService {

    void addInsMenu(InsMenuModel insMenuModel);

    void updateInsMenu(InsMenuModel insMenuModel);

    void delInsMenu(String id);

    List<InsMenuModel> queryByParam(InsMenuModel insMenuModel);

    /**
     * 根据用户标识返回用户菜单集合
     * @param userId
     * @return
     */
    List<InsMenuModel> findMenuInfoByUesrId(final String userId);

    Boolean checkMenuInfoExistByMenuId(String menuId);



}