package com.voc.service.insights.engine.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsCommonDataBaseDao;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/28 上午9:34
 * @描述:
 **/
@Repository
public class InsCommonDataBaseDaoImpl implements InsCommonDataBaseDao {

    private static final Logger log = LoggerFactory.getLogger(InsCommonDataBaseDaoImpl.class);
    @Autowired
    IAysCoreServiceClient analysisCoreServiceClient;

    @Override
    public PageInfo getCommonDataList(InsCommonDataBaseModel commonDataBaseModel) {
        try {
            log.info("调用数据清洗服务获取公域数据开始，入参:{}", JSONObject.toJSONString(commonDataBaseModel));
            Result<?> result = analysisCoreServiceClient.getCommonDataList(commonDataBaseModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }

            final Object validDataCondition = result.getResult();
            String jsonString = JSON.toJSONString(validDataCondition, SerializerFeature.WriteMapNullValue);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            final PageInfo ruleInfoVo = JSONObject.toJavaObject(jsonObject, PageInfo.class);
            log.info("调用数据清洗服务获取公域数据结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }
}
