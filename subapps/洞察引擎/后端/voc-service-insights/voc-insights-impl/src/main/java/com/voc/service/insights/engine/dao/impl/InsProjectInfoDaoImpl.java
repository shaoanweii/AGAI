package com.voc.service.insights.engine.dao.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.voc.service.insights.engine.api.clients.IRiskWarningServiceClient;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsProjectInfoDao;
import com.voc.service.insights.engine.entity.InsProjectInfoEntity;
import com.voc.service.insights.engine.mapper.InsProjectInfoMapper;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;
import com.voc.service.insights.engine.vo.InsOriginDataListVo;
import com.voc.service.insights.engine.vo.InsResultDataListVo;
import com.voc.service.insights.engine.vo.InsRiskWarningResultData;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/14 上午10:43
 * @描述:
 **/
@Repository
public class InsProjectInfoDaoImpl extends ServiceImpl<InsProjectInfoMapper, InsProjectInfoEntity>  implements InsProjectInfoDao {
    private static final Logger log = LoggerFactory.getLogger(InsProjectInfoDaoImpl.class);
    @Autowired
    InsProjectInfoMapper projectInfoMapper;
    @Autowired
    IAysCoreServiceClient analysisCoreServiceClient;
    @Autowired
    IRiskWarningServiceClient riskWarningServiceClient;


    @Override
    @SwitchClientDS
    public void saveProjectInfo(String clientId, InsProjectInfoEntity projectEntity) {
        try {
            boolean save = this.save(projectEntity);
            if(save){
                log.info("保存项目成功");
            }else {
                throw new BussinessException(InsCommonErrorEnum.SAVE_PROJECT_ERROR);
            }
        }catch (Exception e){
            log.error("保存项目异常:{}",e);
            throw new BussinessException(InsCommonErrorEnum.SAVE_PROJECT_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateProjectInfo(String clientId, InsProjectInfoEntity projectEntity) {
        try {
            int updated = projectInfoMapper.updateById(projectEntity);
            if(updated>0){
                log.info("更新项目成功");
            }else {
                throw new BussinessException(InsCommonErrorEnum.UPDATE_PROJECT_ERROR);
            }
        }catch (Exception e){
            log.error("更新项目异常:{}",e);
            throw new RuntimeException(e);
        }

    }

    @Override
    @SwitchClientDS(objectAttribute = "insProjectInfoModel.clientId")
    public List<InsProjectInfoEntity> findProjectList(InsProjectInfoModel insProjectInfoModel) {
        return projectInfoMapper.findProjectList(insProjectInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insProjectInfoModel.clientId")
    public InsProjectInfoEntity findProjectInfo(InsProjectInfoModel insProjectInfoModel) {
        return projectInfoMapper.findProjectInfo(insProjectInfoModel);
    }

    @Override
    public PageInfo findRawData(InsDataSourceModel dataSourceModel) {
        try {
            log.info("调用数据清洗服务获取项目原始数据开始，入参:{}", JSONObject.toJSONString(dataSourceModel));
            Result<?> result = analysisCoreServiceClient.getProjectRawData(dataSourceModel);
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
            log.debug("调用数据清洗服务获取项目原始数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public PageInfo findResultData(InsDataSourceModel dataSourceModel) {
        try {
            log.info("调用数据清洗服务获取项目结果数据开始，入参:{}", JSONObject.toJSONString(dataSourceModel));
            Result<?> result = analysisCoreServiceClient.getProjectRawDataResult(dataSourceModel);
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
            log.debug("调用数据清洗服务获取项目结果数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsOriginDataListVo> exportProjectRawDataResult(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取需导出的原始数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.exportProjectRawDataResult(insDataSourceModel);
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
            List<InsOriginDataListVo> originDataVo = jsonArray.toJavaList(InsOriginDataListVo.class);
            log.debug("调用数据清洗服务获取需导出的原始数据结束，结果:{}", JSONObject.toJSONString(originDataVo));
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsResultDataListVo> exportProjectResultData(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取需导出的结果数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = analysisCoreServiceClient.exportProjectResultData(insDataSourceModel);
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
            List<InsResultDataListVo> originDataVo = jsonArray.toJavaList(InsResultDataListVo.class);
            log.debug("调用数据清洗服务获取需导出的结果数据结束，结果:{}", JSONObject.toJSONString(originDataVo));
            return originDataVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public InsDataSourceSearchCriteriaVo findSearchCriteria(InsDataSourceModel dataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取查询条件开始，入参:{}", JSONObject.toJSONString(dataSourceModel));
            Result<?> result = analysisCoreServiceClient.findSearchCriteria(dataSourceModel);
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
//            throw exception;
            log.error("调用数据清洗服务异常:{}",exception);
        }catch (Exception e){
//            throw e;
            log.error("调用数据清洗服务异常:{}",e);
        }
        return new InsDataSourceSearchCriteriaVo();
    }

    @Override
    public PageInfo findRiskWarningData(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取项目结果数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = riskWarningServiceClient.findRiskWarningData(insDataSourceModel);
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
            log.debug("调用数据清洗服务获取项目结果数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<InsRiskWarningResultData> exportRiskWarningData(InsDataSourceModel insDataSourceModel) {
        try {
            log.debug("调用数据清洗服务获取需导出的风险预警数据开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<?> result = riskWarningServiceClient.exportRiskWarningData(insDataSourceModel);
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
            List<InsRiskWarningResultData> riskWarningResultData = jsonArray.toJavaList(InsRiskWarningResultData.class);
            log.debug("调用数据清洗服务获取需导出的风险预警数据结束，结果:{}", JSONObject.toJSONString(riskWarningResultData));
            return riskWarningResultData;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }
}
