package com.voc.service.insights.engine.dao;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/28 上午9:34
 * @描述:
 **/
public interface InsCommonDataBaseDao {

    PageInfo getCommonDataList(InsCommonDataBaseModel commonDataBaseModel);
}
