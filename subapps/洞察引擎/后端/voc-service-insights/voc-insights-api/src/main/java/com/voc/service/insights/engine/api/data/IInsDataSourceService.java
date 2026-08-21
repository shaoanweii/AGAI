package com.voc.service.insights.engine.api.data;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UploadModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.model.data.DataProcessingTaskQuery;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据源集(InsDataSource)表服务接口
 *
 * @author leiww
 * @since 2024-02-27 15:31:45
 */
public interface IInsDataSourceService {

    /**
     * @param file 本地文件
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午2:20
     * @描述 数据源本地文件上传
     **/
    UploadModel uploadDataSource(MultipartFile file) throws IOException;

    void downloadDataSource(HttpServletResponse response,String clientId,String fileName);

    /**
     * @param insDataSourceModel@return java.lang.String
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午3:10
     * @描述 数据源本地上传文件校验
     **/
    InsDataSourceValidateVo checkUploadDataSource(InsDataSourceModel insDataSourceModel) throws Exception;

    /**
     * @param list
     * @param batchId
     * @param clientId
     * @param fail
     * @param success
     * @param map
     * @param allChannelInfo
     * @param proviceAndCityInfo
     * @return java.util.Map<java.lang.String, java.util.List < com.voc.service.insights.engine.vo.InsDataSourceTemplateVo>>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/13 下午5:16
     * @描述 解析excel数据
     **/
    Map<String,Object> analyzeExcelData(List<InsDataSourceTemplateVo> list, String batchId, String clientId, AtomicInteger fail, AtomicInteger success, Map<String, Object> map, List<ChannelInfoVo> allChannelInfo, List<ProvinceAreaVo> proviceAndCityInfo);

    /**
     * @param insDataSourceModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/14 下午1:49
     * @描述 新增数据源数据
     **/
    void saveUploadDataSource(InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 上午11:07
     * @描述   新增数据源
     * @param insDataSourceModel
     * @return void
     **/
    void saveDataSource(InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 下午5:41
     * @描述   更新数据源
     * @param insDataSourceModel
     * @return void
     **/
    void updateDataSource(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 上午11:32
     * @描述   分页获取数据源
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findDataSource(InsDataSourceModel insDataSourceModel);

    PageInfo findDataProcessingTasks(DataProcessingTaskQuery query);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 上午11:56
     * @描述   分页获取数据源详情
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findDataSourceDetail(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 下午2:34
     * @描述  根据批次id删除数据源详情
     * @param insDataSourceModel
     * @return void
     **/
    void deleteDataSourceDetail(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 下午5:24
     * @描述   根据id删除数据源
     * @param insDataSourceModel
     * @return void
     **/
    void deleteDataSource(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/17 下午4:24
     * @描述   开始处理
     * @param insDataSourceModel
     * @return void
     **/
    void startProcessing(InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午9:44
     * @描述   接收推送结果
     * @param insDataSourceModel
     * @return void
     **/
    void pushResultData(List<InsDataSourceModel> insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:10
     * @描述  获取原始数据
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo getRawData(InsDataSourceModel insDataSourceModel);
    PageInfo getSIRawData(InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:16
     * @描述   获取原始数据结果
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo getRawDataResult(InsDataSourceModel insDataSourceModel);
    PageInfo getSIRawDataResult(InsDataSourceModel insDataSourceModel);

    /**
     * @param insDataSourceModel
     * @param response
     * @return
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:19
     * @描述 导出原始数据
     */
    Boolean exportRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response);
    Boolean exportSIRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response);
    /**
     * @param insDataSourceModel
     * @param response
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:19
     * @描述 导出原始数据结果
     **/
    Boolean exportRawDataResult(InsDataSourceModel insDataSourceModel, HttpServletResponse response);
    Boolean exportSIRawDataResult(InsDataSourceModel insDataSourceModel, HttpServletResponse response);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:40
     * @描述   根据数据源类型获取数据源列表
     * @param insDataSourceModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsDataSourceVo>
     **/
    List<InsDataSourceVo> getDataSourceList(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/19 下午1:35
     * @描述   获取查询条件
     * @param insDataSourceModel
     * @return com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo
     **/
    InsDataSourceSearchCriteriaVo getDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel);
    InsDataSourceSearchCriteriaVo getSIDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/25 上午9:25
     * @描述   根据状态导出原始数据
     * @param insDataSourceModel
     * @param response
     * @return void
     **/
    void exportRawDataByStatus(InsDataSourceModel insDataSourceModel, HttpServletResponse response);
    void exportSIRawDataByStatus(InsDataSourceModel insDataSourceModel, HttpServletResponse response);

    List<InsDataSourceTreeVo> findAllDataSource(InsDataSourceModel insDataSourceModel);

    Set<String> findAllWorkIdByDataSourceIds(String clientId, List<String> dataSourceIds);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/10/28 下午1:25
     * @描述  获取系统集成类型的数据源详情列表
     * @param insDataSourceModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findSIDataSourceList(InsDataSourceModel insDataSourceModel);

    void updateSIDataSource(InsDataSourceModel insDataSourceModel);
}
