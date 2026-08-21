package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/28 上午9:31
 * @描述:
 **/
public interface InsCommonDataBaseService {

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/8/28 上午9:33
     * @描述  获取数据列表
     * @param commonDataBaseModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo getCommonDataList(InsCommonDataBaseModel commonDataBaseModel);
}
