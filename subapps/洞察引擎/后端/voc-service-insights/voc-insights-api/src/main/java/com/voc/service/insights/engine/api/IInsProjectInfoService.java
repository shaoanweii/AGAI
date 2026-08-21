package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;
import com.voc.service.insights.engine.vo.ProjectInfoVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/28 17:35
 * @描述:
 **/
public interface IInsProjectInfoService {
    /**
     * @param insProjectInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:17
     * @描述 新增项目信息
     **/
    void saveProjectInfo(InsProjectInfoModel insProjectInfoModel);

    /**
     * @param insProjectInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:17
     * @描述 更新项目信息
     **/
    void updateProjectInfo(InsProjectInfoModel insProjectInfoModel);

    /**
     * @param insProjectInfoModel
     * @return com.github.pagehelper.PageInfo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:18
     * @描述 获取项目列表
     **/
    PageInfo findProjectList(InsProjectInfoModel insProjectInfoModel);

    /**
     * @param insProjectInfoModel
     * @return com.voc.service.insights.engine.vo.ProjectInfoVo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:18
     * @描述 根据id获取项目信息
     **/
    ProjectInfoVo findProjectInfo(InsProjectInfoModel insProjectInfoModel);

    List<ProjectInfoVo> findAllProjectInfo(String clientId);

    /**
     * @param dataSourceModel
     * @return com.github.pagehelper.PageInfo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/19 下午5:25
     * @描述 获取项目原始数据
     **/
    PageInfo findRawData(InsDataSourceModel dataSourceModel);

    /**
     * @param dataSourceModel
     * @return com.github.pagehelper.PageInfo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/19 下午5:55
     * @描述 获取项目结果数据
     **/
    PageInfo findResultData(InsDataSourceModel dataSourceModel);

    /**
     * @param insDataSourceModel
     * @param response
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:48
     * @描述 导出原始数据
     **/
    Boolean exportRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response);

    /**
     * @param insDataSourceModel
     * @param response
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:59
     * @描述 导出结果数据
     **/
    Boolean exportResultData(InsDataSourceModel insDataSourceModel, HttpServletResponse response);

    InsDataSourceSearchCriteriaVo findSearchCriteria(InsDataSourceModel dataSourceModel);

    /**
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午10:05
     * @描述 获取风险预警数据
     **/
    PageInfo findRiskWarningData(InsDataSourceModel insDataSourceModel);


    List<TagLibCategoryVo> allLibClientCategoryTree(String clientId);

    /**
     * @param insDataSourceModel
     * @param response
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午10:06
     * @描述 导出风险预警数据
     **/
    void exportRiskWarningData(InsDataSourceModel insDataSourceModel, HttpServletResponse response);

    /**
     * @param projectInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/27 上午10:59
     * @描述 根据客户id获取全部项目的风险预警配置
     **/
    List<ProjectInfoVo> findRiskWarningInfo(InsProjectInfoModel projectInfoModel);

    /**
     * @param projectInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.BrandVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/27 上午11:01
     * @描述 根据项目id获取项目标签
     **/
    List<BrandVo> findBrandTabLabelByProjectId(InsProjectInfoModel projectInfoModel);
    /**
     * @param projectInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.BrandVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/27 上午11:01
     * @描述 根据项目id获取项目品牌信息
     **/
    List<BrandVo> findBrandInfo(InsProjectInfoModel projectInfoModel);

    /**
     * @param model
     * @return com.voc.service.insights.engine.api.model.LargeDigitaFilesModel
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/12/25 下午3:45
     * @描述 根据type获取最新生成的文件记录
     **/
    LargeDigitaFilesModel getFile(LargeDigitaFilesModel model);
}
