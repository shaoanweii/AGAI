package com.voc.service.insights.engine.dao;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.entity.InsProjectInfoEntity;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;
import com.voc.service.insights.engine.vo.InsOriginDataListVo;
import com.voc.service.insights.engine.vo.InsResultDataListVo;
import com.voc.service.insights.engine.vo.InsRiskWarningResultData;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/14 上午10:42
 * @描述:
 **/
public interface InsProjectInfoDao {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:46
     * @描述   新增项目信息
     * @param clientId
     * @param projectEntity
     * @return void
     **/
    void saveProjectInfo(String clientId,InsProjectInfoEntity projectEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:46
     * @描述   更新项目信息
     * @param clientId
     * @param projectEntity
     * @return void
     **/
    void updateProjectInfo(String clientId,InsProjectInfoEntity projectEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:46
     * @描述   获取项目列表
     * @param insProjectInfoModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsProjectInfoEntity>
     **/
    List<InsProjectInfoEntity> findProjectList(InsProjectInfoModel insProjectInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:47
     * @描述  根据id获取项目信息
     * @param insProjectInfoModel
     * @return com.voc.service.insights.engine.entity.InsProjectInfoEntity
     **/
    InsProjectInfoEntity findProjectInfo(InsProjectInfoModel insProjectInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/20 上午11:41
     * @描述  获取项目原始数据
     * @param dataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findRawData(InsDataSourceModel dataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/20 上午11:41
     * @描述   获取项目结果数据
     * @param dataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findResultData(InsDataSourceModel dataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:50
     * @描述   获取需导出的原始数据
     * @param insDataSourceModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsOriginDataListVo>
     **/
    List<InsOriginDataListVo> exportProjectRawDataResult(InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:55
     * @描述  获取需导出的结果数据
     * @param insDataSourceModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsResultDataListVo>
     **/
    List<InsResultDataListVo> exportProjectResultData(InsDataSourceModel insDataSourceModel);

    InsDataSourceSearchCriteriaVo findSearchCriteria(InsDataSourceModel dataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午9:58
     * @描述   获取风险预警数据
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findRiskWarningData(InsDataSourceModel insDataSourceModel);
    /**
     * @param insDataSourceModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午10:28
     * @描述 获取需导出的风险预警数据
     **/
    List<InsRiskWarningResultData> exportRiskWarningData(InsDataSourceModel insDataSourceModel);
}
