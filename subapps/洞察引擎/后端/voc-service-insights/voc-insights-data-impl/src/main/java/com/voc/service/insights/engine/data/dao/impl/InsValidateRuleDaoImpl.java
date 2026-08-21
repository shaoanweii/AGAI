package com.voc.service.insights.engine.data.dao.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsValidateRuleDao;
import com.voc.service.insights.engine.data.entity.InsValidateRuleEntity;
import com.voc.service.insights.engine.data.mapper.InsValidateRuleMapper;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.InsValidateRuleInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/29 10:25
 * @描述:
 **/
@Repository
public class InsValidateRuleDaoImpl extends ServiceImpl<InsValidateRuleMapper, InsValidateRuleEntity> implements InsValidateRuleDao {
    private static final Logger log = LoggerFactory.getLogger(InsValidateRuleDaoImpl.class);
    @Autowired
    IAysCoreServiceClient analysisCoreServiceClient;
    @Autowired
    InsValidateRuleMapper validateRuleMapper;

    @Override
    public InsValidateRuleInfoVo findValidateRuleCondition(InsValidateRuleInfoModel validateRuleInfoModel) {
        try {
            log.debug("调用数据清洗服务获取本次条件范围内数据量开始，入参:{}", JSONObject.toJSONString(validateRuleInfoModel));
            Result<?> result = analysisCoreServiceClient.validDataCondition(validateRuleInfoModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }

            final Object validDataCondition = result.getResult();
            final InsValidateRuleInfoVo ruleInfoVo = JSONUtil.toBean(JSONUtil.parseObj(validDataCondition), InsValidateRuleInfoVo.class);
            log.debug("调用数据清洗服务获取本次条件范围内数据量结束，结果:{}", JSONObject.toJSONString(ruleInfoVo));
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "validateRuleInfoModel.clientId")
    public void startValidateRuleInfo(InsValidateRuleInfoModel validateRuleInfoModel) {
        String wordId = "";
        try {
            log.info("调用数据清洗服务进行校验开始，入参:{}", validateRuleInfoModel);
            Result<?> result = analysisCoreServiceClient.validateFlow(validateRuleInfoModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据清洗服务异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务异常:返回结果为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:返回结果为空");
            }
            final Object validDataCondition = result.getResult();
            final InsValidateRuleInfoVo ruleInfoVo = JSONUtil.toBean(JSONUtil.parseObj(validDataCondition), InsValidateRuleInfoVo.class);
            if(StrUtil.isBlank(ruleInfoVo.getWorkId())){
                log.error("调用数据清洗服务异常:wordId为空");
                throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"调用数据清洗服务异常:wordId为空");
            }
            wordId = ruleInfoVo.getWorkId();
            log.info("调用数据清洗服务进行校验结束，结果:{}",wordId);
        }catch (BussinessException bussinessException){
            throw bussinessException;
        }catch (Exception e){
            throw e;
        }

        List<InsValidateRuleEntity> validateRuleEntities = this.dataAssembly(validateRuleInfoModel,wordId);
        boolean b = this.saveBatch(validateRuleEntities);
        if(!b){
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"保存校验规则信息失败");
        }else {
            log.info("保存校验规则信息成功");
        }

        log.info("开始进行规则校验");
    }

    @Override
    public PageInfo findValidateRuleResult(InsValidateModel insValidateModel) {
        try {
            log.debug("调用数据清洗服务获取校验结果开始，入参:{}", JSONObject.toJSONString(insValidateModel));
            Result<?> result = analysisCoreServiceClient.validResult(insValidateModel);
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
            log.debug("调用数据清洗服务获取校验结果结束，结果:{}", ruleInfoVo);
            return ruleInfoVo;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void pushValidateRuleStatus(InsValidateRuleInfoModel validateRuleInfoModel) {
        final String endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        validateRuleInfoModel.setEndTime(endTime);
        validateRuleMapper.updateValidateStatusByWordId(validateRuleInfoModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insValidateModel.clientId")
    public List<InsValidateRuleEntity> findValidateRuleInfo(InsValidateModel insValidateModel) {
        List<InsValidateRuleEntity> validateRuleEntity = validateRuleMapper.findValidateRuleInfo(insValidateModel);
        if(ObjectUtils.isEmpty(validateRuleEntity)){
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"未找到当前规则信息");
        }
        return validateRuleEntity;
    }

    @Override
    public List<InsValidateRuleEntity> findNewestValidateRuleInfo() {
        return validateRuleMapper.findNewestValidateRuleInfo();
    }

    @Override
    public List<InsValidateRuleEntity> findValidateInfoList(String regulationId) {
        return validateRuleMapper.findValidateInfoList(regulationId);
    }

    @Override
    public List<InsValidateRuleEntity> findValidateInfoListByIds(List<String> regulationIds) {
        return validateRuleMapper.findValidateInfoListByIds(regulationIds);
    }

    @Override
    public String findValidateTypeByWorkId(String workId) {
        return validateRuleMapper.findValidateTypeByWorkId(workId);
    }

    /**
     * @param validateRuleInfoModel
     * @param wordId
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/29 10:33
     * @描述 数据组装
     **/
    private List<InsValidateRuleEntity> dataAssembly(InsValidateRuleInfoModel validateRuleInfoModel, String wordId){
        final LocalDateTime now = LocalDateTime.now();
        final String username = ServiceContextHolder.getUsername();
        List<InsValidateRuleEntity> validateRuleEntities = new ArrayList<>();
        validateRuleInfoModel.getValidRuleIds().stream().forEach(e->{
            InsValidateRuleEntity validateRuleEntity = InsValidateRuleEntity.builder()
                    .id(IdWorker.getId())
                    .regulationId(e)
                    .createTime(now)
                    .workId(wordId)
                    .singleOrFullType(validateRuleInfoModel.getRuleType())
                    .operator(username)
                    .channel(validateRuleInfoModel.getChannel())
                    .contentType(validateRuleInfoModel.getContentType())
                    .build();
            if("0".equalsIgnoreCase(validateRuleInfoModel.getRuleType())){
                validateRuleEntity.setSingleValidateStatus("0");
            }else {
                validateRuleEntity.setFullValidateStatus("0");
            }
            validateRuleEntities.add(validateRuleEntity);
        });

        return validateRuleEntities;
    }

}
