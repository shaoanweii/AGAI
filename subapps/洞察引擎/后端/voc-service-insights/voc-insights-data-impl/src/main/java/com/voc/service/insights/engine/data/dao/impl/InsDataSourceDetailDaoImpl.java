package com.voc.service.insights.engine.data.dao.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsDataSourceDetailDao;
import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.data.mapper.InsDataSourceDescMapper;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultVo;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 上午10:50
 * @描述:
 **/
@Repository
public class InsDataSourceDetailDaoImpl extends ServiceImpl<InsDataSourceDescMapper, InsDataSourceDescEntity> implements InsDataSourceDetailDao {
    private static final Logger log = LoggerFactory.getLogger(InsDataSourceDetailDaoImpl.class);
    @Autowired
    InsDataSourceDescMapper insDataSourceDescMapper;
    @Autowired
    IAysCoreServiceClient analysisCoreServiceClient;

    @Getter
    @Value("${feign.default.token:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYW5hbHlzaXNfYXBpIiwiaWRlbnRpdHlfdHlwZSI6ImJhc2UiLCJhcHBfaWQiOiJhbmFseXNpcyIsInVzZXJuYW1lIjoiY1Fkb094bmg2eVEwMW5lc2ZLTlhVNjFKQmx5RFg3dHc4YXhod0JjNVl4aXl1MC9CYjdDQWZwQjJ5QTFxYjQ4QiIsInN1YiI6ImFuYWx5c2lzX2FwaSIsImlhdCI6MTcxMDQxMTQyMSwiZXhwIjo0MDc1NjExNDIxfQ.G1kAeqwp0udBimnDdIAqL1nSIcgV0u6YrU0bb5OchJ0}")
    String defaultToken;

    @Override
    @SwitchClientDS
    public void saveBatchDataSourceDetail(List<InsDataSourceDescEntity> dataSourceEntities, String clientId) {
        try {
            insDataSourceDescMapper.insertBatchDataSourceDetail(dataSourceEntities);
            log.info("批量保存数据源详情数据成功");
        }catch (Exception e){
            log.error("异常：{}",e);
            throw new BussinessException(InsCommonErrorEnum.SAVE_BATCH_DATA_SOURCE_DETAIL_ERROR);
        }
//        boolean saveBatch = this.saveBatch(dataSourceEntities);
//        if (saveBatch) {
//            log.info("批量保存数据源详情数据成功");
//        } else {
//            throw new BussinessException(InsCommonErrorEnum.SAVE_BATCH_DATA_SOURCE_DETAIL_ERROR);
//        }
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findDataSourceDetail(String clientId, String dataSourceId) {
        return insDataSourceDescMapper.findDataSourceDetail(dataSourceId);
    }

    @Override
    @SwitchClientDS
    public String findDataSourceName(String clientId, String dataSourceId,String dataName) {
        return insDataSourceDescMapper.findDataSourceName(dataSourceId, dataName);
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findDataSourceDetailMaxStatus(String clientId, List<String> batchIds) {
        return insDataSourceDescMapper.findDataSourceDetailMaxStatus(batchIds);
    }

    @Override
    @SwitchClientDS
    public void deleteDataSourceDetail(String clientId, String batchId) {
        insDataSourceDescMapper.deleteDataSourceDetail(batchId);
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findDataSourceDetailAll(String clientId, String batchId, String dataSourceId, List<String> status) {
        return insDataSourceDescMapper.findDataSourceDetailAll(dataSourceId, batchId,status);
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findDataSourceDetails(String clientId, List<String> batchIds, String dataSourceId, List<String> status) {
        return insDataSourceDescMapper.findDataSourceDetails(dataSourceId, batchIds,status);
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findFailDataSourceDetails(String clientId, List<String> batchIds, String dataSourceId, List<String> status) {
        return insDataSourceDescMapper.findFailDataSourceDetails(dataSourceId, batchIds, status);
    }

    @Override
    @SwitchClientDS
    public void updateDataSourceDetail(String clientId, String batchId, String status) {
        insDataSourceDescMapper.updateDataSourceDetail(batchId, status);
    }

    @Override
    @SwitchClientDS
    public void batchUpdateDataSourceDetail(String clientId, String batchId, String status, List<String> newIds) {
        insDataSourceDescMapper.batchUpdateDataSourceDetailStatus(batchId, status, newIds);
    }

    @Override
    @SwitchClientDS
    public void updateDataSourceDetailStatusAndWorkId(String clientId, String batchId, String status, String workId) {
        insDataSourceDescMapper.updateDataSourceDetailStatusAndWorkId(batchId, status, workId);
    }

    @Override
    public PageInfo getRawData(InsDataSourceModel insDataSourceModel) {
        try {
            log.info("调用数据清洗服务获取原始数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.getRawData(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }

            final Object validDataCondition = result.getResult();
            final PageInfo ruleInfoVo = JSONUtil.toBean(JSONUtil.parseObj(validDataCondition), PageInfo.class);
            log.debug("调用数据清洗服务获取原始数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public PageInfo getRawDataResult(InsDataSourceModel insDataSourceModel) {
        try {
            log.info("调用数据清洗服务获取结果数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.getRawDataResult(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }

            final Object validDataCondition = result.getResult();
            final PageInfo ruleInfoVo = JSONUtil.toBean(JSONUtil.parseObj(validDataCondition), PageInfo.class);
            log.debug("调用数据清洗服务获取结果数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsDataSourceOriginDataVo> exportRawData(InsDataSourceModel insDataSourceModel) {
        try {
            log.info("调用数据清洗服务获取需导出的原始数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            long start = System.currentTimeMillis();
            Result<?> result = analysisCoreServiceClient.exportRawData(insDataSourceModel);
            log.info("调用数据清洗服务获取需导出的原始数据耗时：" + (System.currentTimeMillis() - start));
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            start = System.currentTimeMillis();
            final Object rawData = result.getResult();
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(rawData));
            List<InsDataSourceOriginDataVo> originDataVo = jsonArray.toJavaList(InsDataSourceOriginDataVo.class);
            log.info("解析JSONArray耗时：" + (System.currentTimeMillis() - start));
            log.debug("调用数据清洗服务获取需导出的原始数据结束");
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsDataSourceOriginDataVo> getFailDataList(InsDataSourceModel insDataSourceModel) {
        try {
            log.info("调用数据清洗服务获取需导出的失败数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            long start = System.currentTimeMillis();
            Result<?> result = analysisCoreServiceClient.getFailDataList(insDataSourceModel);
            log.info("调用数据清洗服务获取需导出的失败数据耗时：" + (System.currentTimeMillis() - start));
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            start = System.currentTimeMillis();
            final Object rawData = result.getResult();
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(rawData));
            List<InsDataSourceOriginDataVo> originDataVo = jsonArray.toJavaList(InsDataSourceOriginDataVo.class);
            log.info("解析JSONArray耗时：" + (System.currentTimeMillis() - start));
            log.debug("调用数据清洗服务获取需导出的失败数据结束");
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsDataSourceResultDataVo> exportRawDataResult(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务导出结果数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.exportRawDataResult(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            final Object rawData = result.getResult();
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(rawData));
            List<InsDataSourceResultDataVo> originDataVo = jsonArray.toJavaList(InsDataSourceResultDataVo.class);
            log.debug("调用数据清洗服务导出结果数据结束");
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public String batchPushData(InsDataSourceModel insDataSourceModelList) {
        try {
            log.info("调用数据清洗服务进行数据处理开始");
            log.info("入参:{}", JSONObject.toJSONString(insDataSourceModelList));
            Result<?> result = analysisCoreServiceClient.batchPushData(insDataSourceModelList);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            final Object processingDataCondition = result.getResult();
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(processingDataCondition);
            if(ObjectUtils.isEmpty(jsonObject.get("workId"))){
                log.error("调用数据清洗服务异常:wordId为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:wordId为空");
            }
            if(ObjectUtils.isEmpty(jsonObject.get("requestId"))){
                log.error("调用数据清洗服务异常:requestId为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:requestId为空");
            }
            log.info("调用数据清洗服务进行校验结束");
            return String.valueOf(jsonObject.get("workId"));
        }catch (BussinessException bussinessException){
            throw bussinessException;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public InsDataSourceSearchCriteriaVo getDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取查询条件开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.getSearchCriteria(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            final Object rawData = result.getResult();
            InsDataSourceSearchCriteriaVo sourceSearchCriteriaVo = JSONUtil.toBean(JSONUtil.parseObj(rawData), InsDataSourceSearchCriteriaVo.class);
            log.debug("调用数据清洗服务获取查询条件结束");
            return sourceSearchCriteriaVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findAllDataSourceDetail(String clientId, List<String> dataSourceIds) {
        return insDataSourceDescMapper.findAllDataSourceDetail(dataSourceIds);
    }

    @Override
    @SwitchClientDS
    public Set<String> findDataSourceWorkIds(String clientId, String batchIds, String dataSourceId, List<String> status) {
        return insDataSourceDescMapper.findDataSourceWorkIds(dataSourceId , batchIds,status);
    }

    @Override
    @SwitchClientDS
    public List<InsDataSourceDescEntity> findDataSourceDetailsByBatchIds(String clientId, List<String> batchIds, String dataSourceId) {
        return insDataSourceDescMapper.findDataSourceDetailByBatchIds(batchIds,dataSourceId);
    }

    @Override
    public List<InsDataSourceResultVo> getDataResultStatus(InsDataSourceModel insDataSourceModel) {
        try {
            ServiceContextHolder.setToken(defaultToken);
            log.debug("调用数据清洗服务导出结果数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.getDataResultStatus(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            final Object rawData = result.getResult();
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(rawData));
            List<InsDataSourceResultVo> originDataVo = jsonArray.toJavaList(InsDataSourceResultVo.class);
            log.debug("调用数据清洗服务导出结果数据结束");
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }
}
