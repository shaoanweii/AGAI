package com.voc.service.trhird.api;


import com.google.gson.Gson;
import com.voc.service.trhird.model.ZhiPuAiContentModel;
import com.voc.service.trhird.model.ZhiPuStatusAiModel;
import com.zhipu.oapi.service.v4.batchs.BatchResponse;
import com.zhipu.oapi.service.v4.batchs.QueryBatchResponse;
import com.zhipu.oapi.service.v4.file.FileApiResponse;
import com.zhipu.oapi.service.v4.file.QueryBatchRequest;
import com.zhipu.oapi.service.v4.file.UploadFileRequest;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;


/**
 * 定义了智谱AI接口，用于与智谱AI系统的交互。
 * 智谱AI接口提供了文件上传、批量任务查询以及根据批次ID获取批量任务信息的功能。
 */
public interface ZhiPuAiApi {

    /**
     * 调用文件上传接口。
     *
     * @param request 上传文件的请求对象，包含上传文件的相关信息。
     * @return 文件上传的响应对象，包含上传结果及上传后的文件信息。
     */
    FileApiResponse invokeUploadFileApi(UploadFileRequest request);

    /**
     * 查询批量任务的状态。
     *
     * @param queryBatchRequest 查询批量任务的请求对象，包含查询条件。
     * @return 批量任务状态的响应对象，包含查询结果。
     */
    QueryBatchResponse batchesListStatus(QueryBatchRequest queryBatchRequest);

    /**
     * 根据批次ID获取批量任务信息。
     *
     * @param batchId 批次的唯一标识ID。
     * @return 批量任务的响应对象，包含指定批次的任务信息。
     */
    /**
     * 根据批处理ID批量检索数据
     *
     * @param batchId 批处理ID，用于标识一批处理任务
     * @return ZhiPuStatusAiModel 返回智谱AI模型的状态信息，用于表示该批处理任务的执行状态
     */
    ZhiPuStatusAiModel batchesRetrieveByBatchId(String batchId);

    /**
     * 根据文件ID下载文件数据
     *
     * @param outFileId 文件ID，用于标识需要下载的文件
     * @return List<ZhiPuAiContentModel> 返回一个列表，包含下载文件解析后的数据，列表中的每个元素代表文件中的一条记录
     */
    Pair<List<ZhiPuAiContentModel>, List<String>> downloadFileData(String outFileId);

    /**
     * 创建批处理任务
     *
     * @param id 批处理任务的唯一标识符，用于标识特定的批处理任务
     * @return BatchResponse 返回批处理创建操作的响应对象，其中包含操作状态和结果信息
     */
    BatchResponse batchesCreate(String id);

    boolean batchFileDelete(String inputFileId);

    String downloadFilePathByFileId(String errorFileId);

    void aiModelResultJsonParse(String result, Gson gson, ZhiPuAiContentModel zhiPuAiContentModel, List<ZhiPuAiContentModel> zhiPuAiContentModelList) throws Exception;
}
