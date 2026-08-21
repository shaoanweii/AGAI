package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IModelServiceClient
 * @createTime 2024年03月08日 14:55
 * @Copyright futong
 * @Description 算法模型接口
 */

@FeignClient(name = "service.ays", url = "${service.analysis.v1}")
//@FeignClient(name = "service.ays", url = "http://192.168.6.195:8080")
public interface IAysCoreServiceClient {

    /**
     * 启动验证服务
     * @param param
     * @return
     */
    @PostMapping("/validateFlow")
    Result<?> validateFlow(@RequestBody InsValidateRuleInfoModel param);

    /**
     * 验证结果服务
     * @return
     */
    @PostMapping("/validResult")
    Result<?> validResult(@RequestBody InsValidateModel insValidateModel);

    /**
     * 验证数据时间范围等信息
     * @param param
     * @return
     */
    @PostMapping("/validDataCondition")
    Result<?> validDataCondition(@RequestBody InsValidateRuleInfoModel param);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:15
     * @描述   获取原始数据列表
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getRawDataResult")
    Result<?>  getRawData(@RequestBody InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:23
     * @描述   获取结果数据列表
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getResultDataList")
    Result<?>  getRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel);

    /**
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:23
     * @描述 导出原始数据
     **/
    @PostMapping(value = "/exportRawDataResult")
    Result<?>  exportRawData(@RequestBody InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:26
     * @描述  导出结果数据
     * @param insDataSourceModel
     * @return void
     **/
    @PostMapping("/exportResultData")
    Result<?> exportRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 下午3:13
     * @描述   数据处理
     * @param insDataSourceModelList
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/batchPushData")
    Result<?> batchPushData(@RequestBody InsDataSourceModel insDataSourceModelList);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/19 下午1:35
     * @描述   获取查询条件
     * @param insDataSourceModelList
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/conditions")
    Result<?> getSearchCriteria(@RequestBody InsDataSourceModel insDataSourceModelList);

    @PostMapping("/getCommonDataList")
    Result<?> getCommonDataList(InsCommonDataBaseModel commonDataBaseModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:15
     * @描述   获取项目原始数据列表
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getProjectRawDataResult")
    Result<?>  getProjectRawData(@RequestBody InsDataSourceModel insDataSourceModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午10:23
     * @描述   获取项目结果数据列表
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getProjectResultDataList")
    Result<?>  getProjectRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:51
     * @描述   获取需导出的原始数据
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/exportProjectRawDataResult")
    Result<?> exportProjectRawDataResult(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/23 上午10:52
     * @描述   获取需导出的结果数据
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/exportProjectResultData")
    Result<?> exportProjectResultData(InsDataSourceModel insDataSourceModel);
    @PostMapping("/projectConditions")
    Result<?> findSearchCriteria(InsDataSourceModel dataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/12/12 下午10:08
     * @描述  获取需导出的失败数据
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getFailDataList")
    Result<?> getFailDataList(InsDataSourceModel insDataSourceModel);

    @PostMapping("/getDataResultStatus")
    Result<?> getDataResultStatus(InsDataSourceModel insDataSourceModel);
}
