package com.voc.service.trhird.zhipuai.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.voc.service.trhird.api.ZhiPuAiApi;
import com.voc.service.trhird.model.DownloadAIFileResultModel;
import com.voc.service.trhird.model.ResponseAiModel;
import com.voc.service.trhird.model.ZhiPuAiContentModel;
import com.voc.service.trhird.model.ZhiPuStatusAiModel;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.core.response.HttpxBinaryResponseContent;
import com.zhipu.oapi.service.v4.batchs.Batch;
import com.zhipu.oapi.service.v4.batchs.BatchCreateParams;
import com.zhipu.oapi.service.v4.batchs.BatchResponse;
import com.zhipu.oapi.service.v4.batchs.QueryBatchResponse;
import com.zhipu.oapi.service.v4.file.FileApiResponse;
import com.zhipu.oapi.service.v4.file.QueryBatchRequest;
import com.zhipu.oapi.service.v4.file.UploadFileRequest;
import lombok.Cleanup;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZhiPuAiApiImpl implements ZhiPuAiApi {

    final Pattern pattern8 = Pattern.compile("vehicle_brand:([^,]+)");
    final Pattern pattern1 = Pattern.compile("vehicle_model:([^,]+)");
    final Pattern pattern = Pattern.compile("scenario:([^,]+)");
    final Pattern pattern2 = Pattern.compile("subject:([^,]+)");
    final Pattern pattern3 = Pattern.compile("aspect:([^,]+)");
    final Pattern pattern4 = Pattern.compile("desc:([^,]+)");
    final Pattern pattern5 = Pattern.compile("sentiment:([^,]+)");
    final Pattern pattern6 = Pattern.compile("intent:([^,]+)");
    final Pattern pattern7 = Pattern.compile("confidence:([^,]+)");


    private static final Logger logger = LoggerFactory.getLogger(ZhiPuAiApiImpl.class);
    @Value("${third.zhipuai.api.batchFileDeleteAuthorization}")
    private String batchFileDeleteAuthorization;
    /*   @Value("${third.zhipuai.api.key}")
       public static void setApiSecretKey(String apiSecretKey) {
           API_SECRET_KEY = apiSecretKey;
       }*/
    @Value("${third.zhipuai.api.key}")
    private String API_SECRET_KEY;

    ClientV4 client;

    public ClientV4 getClient() {
        if (ObjUtil.isNull(client)) {
            client = new ClientV4.Builder(API_SECRET_KEY)
                    .enableTokenCache()
                    .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                    .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                    .build();
        }
        return client;
    }

    @Override
    public FileApiResponse invokeUploadFileApi(UploadFileRequest request) {
        return getClient().invokeUploadFileApi(request);
    }

    @Override
    public QueryBatchResponse batchesListStatus(QueryBatchRequest queryBatchRequest) {
        queryBatchRequest.setLimit(10);
        QueryBatchResponse queryBatchResponse = getClient().batchesList(queryBatchRequest);
        return queryBatchResponse;
    }

    @Override
    public ZhiPuStatusAiModel batchesRetrieveByBatchId(String batchId) {
        logger.info("智谱查询状态信息api入参:{}", batchId);
        BatchResponse batchResponse = getClient().batchesRetrieve(batchId);
        logger.info("智谱查询状态信息api返回:{}", batchResponse);
        ZhiPuStatusAiModel zhiPuAiModel = new ZhiPuStatusAiModel();
        if (batchResponse.isSuccess() && !ObjectUtils.isEmpty(batchResponse.getData())) {
            zhiPuAiModel.setBatchId(batchId);
            Batch data = batchResponse.getData();
            zhiPuAiModel.setStatus(data.getStatus());
            zhiPuAiModel.setErrorFileId(data.getErrorFileId());
            zhiPuAiModel.setOutputFileId(data.getOutputFileId());
            zhiPuAiModel.setInputFileId(data.getInputFileId());
            return zhiPuAiModel;
        }
        return null;
    }

    @Override
    public Pair<List<ZhiPuAiContentModel>, List<String>> downloadFileData(String outFileId) {
      /*  List<ZhiPuAiContentModel> zhiPuAiContentModelList = new ArrayList<>();
        List<String> exceptionList = new ArrayList<>();
        try {
            HttpxBinaryResponseContent httpxBinaryResponseContent = getClient().fileContent(outFileId);
            String filePath = "outputFile." + outFileId;
            File javaIoTmpDir = SystemUtils.getJavaIoTmpDir();
            httpxBinaryResponseContent.streamToFile(javaIoTmpDir.getPath() + filePath, 1000);
            String pathToJson = javaIoTmpDir.getPath() + filePath;
            @Cleanup
            BufferedReader reader = new BufferedReader(new FileReader(pathToJson));
            String line;
            Gson gson = new Gson();
            while ((line = reader.readLine()) != null) {
                ZhiPuAiContentModel zhiPuAiContentModel = null;
                try {
                    DownloadAIFileResultModel model = gson.fromJson(line, DownloadAIFileResultModel.class);
                    zhiPuAiContentModel = new ZhiPuAiContentModel();
                    zhiPuAiContentModel.setCustomId(model.getCustomId());
                    if (!ObjectUtils.isEmpty(model) && !ObjectUtils.isEmpty(model.getResponse())) {
                        ResponseAiModel responseAiModel = model.getResponse();
                        if (responseAiModel.getStatusCode() == 200) {
                            if (!responseAiModel.getBody().getChoices().isEmpty()) {
                                String jsonContent = responseAiModel.getBody().getChoices().get(0).getMessage().getContent();
                                if (StringUtil.isBlank(jsonContent)) {
                                    logger.error("zhiPuAi下载文件数据有错误数据:{}", jsonContent);
                                    continue;
                                }
                                aiModelResultJsonParse(jsonContent.toLowerCase(), gson, zhiPuAiContentModel, zhiPuAiContentModelList);
                            } else {
                                logger.error("zhiPuAi下载文件数据有错误数据:{}", line);
                                throw new Exception("");
                            }
                        } else {
                            logger.error("zhiPuAi下载文件数据有错误数据:{}", line);
                            throw new Exception("");
                        }
                    } else {
                        logger.error("zhiPuAi下载文件数据有错误数据:{}", line);
                        throw new Exception("");
                    }
                } catch (Exception e) {
                    zhiPuAiContentModelList.add(zhiPuAiContentModel);
                    exceptionList.add(line);
                    logger.error("zhiPuAi下载文件数据有错误数据:{}", line);
                }
            }
//            reader.close();
            File file = new File(pathToJson);
            if (file.delete()) {
                logger.info("文件删除成功");
            }
        } catch (
                Exception e) {
            logger.error("file content error", e);
        } *//*finally {
            try {
                if (!ObjectUtils.isEmpty(reader)) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("file content error", e);
            }
        }*//*
        return Pair.of(zhiPuAiContentModelList, exceptionList);*/
        return null;
    }

    @Override
    public void aiModelResultJsonParse(String jsonContent, Gson gson, ZhiPuAiContentModel zhiPuAiContentModel, List<ZhiPuAiContentModel> zhiPuAiContentModelList) throws Exception {
        jsonContent = jsonContent.toLowerCase();
        // 正则表达式确保字符串以 '{"vehicle_brand":' 开头，并且后面不是紧跟一个闭括号 '}'
        String regex = "^\\{\"vehicle_brand\":(?!\\})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonContent);
        if (matcher.find()) {
            ZhiPuAiContentModel.Vehicle sf = gson.fromJson(jsonContent, ZhiPuAiContentModel.Vehicle.class);
            List<ZhiPuAiContentModel.Vehicle> vehicles = new ArrayList<>();
            vehicles.add(sf);
            zhiPuAiContentModel.setVehicleList(vehicles);
            zhiPuAiContentModelList.add(zhiPuAiContentModel);
            return;
        }
        try {
            int index = jsonContent.indexOf("[");
            int lastIndex = jsonContent.lastIndexOf("]");
            if (index != -1) {
                jsonContent = jsonContent.substring(index, lastIndex + 1);
            }
            Type listType = new TypeToken<List<ZhiPuAiContentModel.Vehicle>>() {
            }.getType();
            List<ZhiPuAiContentModel.Vehicle> vehicles = gson.fromJson(jsonContent, listType);
            zhiPuAiContentModel.setVehicleList(vehicles);
            zhiPuAiContentModelList.add(zhiPuAiContentModel);
        } catch (Exception e) {
            //  logger.error("zhiPuAi下载文件异常数据处理:{}", jsonContent);
            this.handleException(zhiPuAiContentModel, jsonContent);
            zhiPuAiContentModelList.add(zhiPuAiContentModel);
        }
    }

    /**
     * 正则处理异常数据解析
     *
     * @param jsonContent
     * @return
     */
    public void handleException(ZhiPuAiContentModel zhiPuAiContentModel, String jsonContent) throws Exception {



        String cleanedString = jsonContent.replaceAll("[^a-zA-Z0-9,:_\\u4e00-\\u9fa5]", "");
        String string = JSON.toJSONString(cleanedString);
        String[] split = string.split("(?=vehicle_brand)");
        List<ZhiPuAiContentModel.Vehicle> vehicleList = new ArrayList<>();
        for (String str : split) {
            ZhiPuAiContentModel.Vehicle vehicle = new ZhiPuAiContentModel.Vehicle();
            List<ZhiPuAiContentModel.Viewpoint> viewpoints = new ArrayList<>();
            Matcher matcher1 = pattern1.matcher(str);
            while (matcher1.find()) {
                if (StringUtil.isNotEmpty(matcher1.group(1))) {
                    vehicle.setVehicleModel(matcher1.group(1));
                }
            }

            Matcher matcher8 = pattern8.matcher(str);
            while (matcher8.find()) {
                if (StringUtil.isNotEmpty(matcher8.group(1))) {
                    vehicle.setVehicleBrand(matcher8.group(1));
                }
            }
            List<String> SCENARIO = new ArrayList<>();
            List<String> SUBJECT = new ArrayList<>();
            List<String> ASPECT = new ArrayList<>();
            List<String> DESC = new ArrayList<>();
            List<String> SENTIMENT = new ArrayList<>();
            List<String> INTENT = new ArrayList<>();
            List<String> CONFIDENCE = new ArrayList<>();
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                if (StringUtil.isNotEmpty(matcher.group(1))) {
                    SCENARIO.add(matcher.group(1));
                }
            }
            Matcher matcher2 = pattern2.matcher(str);
            while (matcher2.find()) {
                if (StringUtil.isNotEmpty(matcher2.group(1))) {
                    SUBJECT.add(matcher2.group(1));
                }
            }
            Matcher matcher3 = pattern3.matcher(str);
            while (matcher3.find()) {
                if (StringUtil.isNotEmpty(matcher3.group(1))) {
                    ASPECT.add(matcher3.group(1));
                }
            }
            Matcher matcher4 = pattern4.matcher(str);
            while (matcher4.find()) {
                if (StringUtil.isNotEmpty(matcher4.group(1))) {
                    DESC.add(matcher4.group(1));
                }
            }
            Matcher matcher5 = pattern5.matcher(str);
            while (matcher5.find()) {
                if (StringUtil.isNotEmpty(matcher5.group(1))) {
                    SENTIMENT.add(matcher5.group(1));
                }
            }
            Matcher matcher6 = pattern6.matcher(str);
            while (matcher6.find()) {
                if (StringUtil.isNotEmpty(matcher6.group(1))) {
                    INTENT.add(matcher6.group(1));
                }
            }
            Matcher matcher7 = pattern7.matcher(str);
            while (matcher7.find()) {
                if (StringUtil.isNotEmpty(matcher7.group(1))) {
                    CONFIDENCE.add(matcher7.group(1));
                }
            }
            List<String> list = SUBJECT.size() > DESC.size() ? SUBJECT : DESC;
            if (CollectionUtil.isNotEmpty(list)) {
                for (int i = 0; i < list.size(); i++) {
                    ZhiPuAiContentModel.Viewpoint viewpoint = new ZhiPuAiContentModel.Viewpoint();
                    if (i < SCENARIO.size()) {
                        viewpoint.setScenario(SCENARIO.get(i));
                    }
                    if (i < SUBJECT.size()) {
                        viewpoint.setSubject(SUBJECT.get(i));
                    }
                    if (i < ASPECT.size()) {
                        viewpoint.setAspect(ASPECT.get(i));
                    }
                    if (i < DESC.size()) {
                        viewpoint.setDesc(DESC.get(i));
                    }
                    if (i < SENTIMENT.size()) {
                        viewpoint.setSentiment(SENTIMENT.get(i));
                    }
                    if (i < CONFIDENCE.size()) {
                        viewpoint.setConfidence(CONFIDENCE.get(i));
                    }
                    if (i < INTENT.size()) {
                        viewpoint.setIntent(INTENT.get(i));
                    }
                    viewpoints.add(viewpoint);
                    vehicle.setViewpoints(viewpoints);
                }
            }
            if (!ObjectUtils.isEmpty(vehicle) && CollectionUtil.isNotEmpty(vehicle.getViewpoints())) {
                vehicleList.add(vehicle);
            }
        }
        if (CollectionUtil.isNotEmpty(vehicleList)) {
            zhiPuAiContentModel.setVehicleList(vehicleList);
        } else {
            throw new Exception("异常数据正则没有匹配到数据");
        }
    }

    public BatchResponse batchesCreate(String inputFileId) {
        BatchCreateParams batchCreateParams = new BatchCreateParams(
                "24h",
                "/v4/chat/completions",
                inputFileId,
                new HashMap<String, String>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }}
        );
        return getClient().batchesCreate(batchCreateParams);
        //         output: BatchResponse(code=200, msg=调用成功, success=true, data=Batch(id=batch_1791021399316246528, completionWindow=24h, createdAt=1715847751822, endpoint=/v4/chat/completions, inputFileId=20240514_ea19d21b-d256-4586-b0df-e80a45e3c286, object=batch, status=validating, cancelledAt=null, cancellingAt=null, completedAt=null, errorFileId=null, errors=null, expiredAt=null, expiresAt=null, failedAt=null, finalizingAt=null, inProgressAt=null, metadata={key1=value1, key2=value2}, outputFileId=null, requestCounts=BatchRequestCounts(completed=0, failed=0, total=0), error=null))
    }

    @Override
    public boolean batchFileDelete(String inputFileId) {
        try {
            // 创建URL对象
            URL url = new URL("https://bigmodel.cn/api/file/v1/" + inputFileId + "/batch");
            // 打开连接
            @Cleanup("disconnect")
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法为DELETE
            connection.setRequestMethod("DELETE");
            aiModelResultJsonParse(connection);
            // 发送请求
            connection.connect();
            // 读取响应内容
            @Cleanup
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
//            in.close();
            // 打印结果
            logger.debug("删除batch文件:" + response);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {

        }

    }

    private void aiModelResultJsonParse(HttpURLConnection connection) {
        // 设置请求头
        connection.setRequestProperty("accept", "application/json, text/plain, */*");
        connection.setRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        connection.setRequestProperty("authorization", batchFileDeleteAuthorization);
        connection.setRequestProperty("cache-control", "no-cache");
        connection.setRequestProperty("pragma", "no-cache");
        connection.setRequestProperty("priority", "u=1, i");
        connection.setRequestProperty("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"");
        connection.setRequestProperty("sec-ch-ua-mobile", "?0");
        connection.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        connection.setRequestProperty("sec-fetch-dest", "empty");
        connection.setRequestProperty("sec-fetch-mode", "cors");
        connection.setRequestProperty("sec-fetch-site", "same-origin");
        connection.setRequestProperty("cookie", "sensorsdata2015jssdkchannel=%7B%22prop%22%3A%7B%22_sa_channel_landing_url%22%3A%22%22%7D%7D; bigmodel_token_production=eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX3R5cGUiOiJQRVJTT05BTCIsInVzZXJfaWQiOjMzMjE1NiwidXNlcl9rZXkiOiI1YzM2OWUyMC1lYWJmLTQ3ZDUtODhkZi01OWVhNTczYzMwNTIiLCJjdXN0b21lcl9pZCI6IjU4MDcxNzA2NjI3NDg5ODc1IiwidXNlcm5hbWUiOiIxODYyMTc4ODYzMiJ9.iubPSeAVTWO-rK3acolw-ikEO2_VV4MgQjAu3etDXtc81lXDirJoUnoXt6VeVT9VCT68RL4uqyhHViRiAPQocg; bigmodel_expires_in=Tue%20Aug%2006%202024%2014:51:54%20GMT+0800%20(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4); sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2258071706627489875%22%2C%22first_id%22%3A%2218f9e445d9b4d3-0f5e52bc15a1bf8-4c657b58-1599065-18f9e445d9c1f%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E5%BC%95%E8%8D%90%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC%22%2C%22%24latest_referrer%22%3A%22https%3A%2F%2Fbigmodel.cn%2Fconsole%2Fbatch%2Fdataset%22%2C%22%24latest_utm_source%22%3A%22zpqyweb%22%2C%22%24latest_utm_campaign%22%3A%22zpqyweb%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMThmOWU0NDVkOWI0ZDMtMGY1ZTUyYmMxNWExYmY4LTRjNjU3YjU4LTE1OTkwNjUtMThmOWU0NDVkOWMxZiIsIiRpZGVudGl0eV9sb2dpbl9pZCI6IjU4MDcxNzA2NjI3NDg5ODc1In0%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%2258071706627489875%22%7D%2C%22%24device_id%22%3A%2218f9e445d9b4d3-0f5e52bc15a1bf8-4c657b58-1599065-18f9e445d9c1f%22%7D");
        connection.setRequestProperty("Referer", "https://bigmodel.cn/console/batch/dataset");
        connection.setRequestProperty("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Override
    public String downloadFilePathByFileId(String fileId) {
       /* Path tempFile;
        try {
            // 通过客户端获取文件内容
            HttpxBinaryResponseContent httpxBinaryResponseContent = getClient().fileContent(fileId);
            String fileName = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMATTER) + RandomUtil.randomNumbers(3);
            tempFile = Files.createTempFile(fileName, ".jsonl");
            // 将文件内容流式传输到指定路径，并设置超时时间
            httpxBinaryResponseContent.streamToFile(tempFile.toString(), 1000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return tempFile.toString();*/
        return null;
    }
}
