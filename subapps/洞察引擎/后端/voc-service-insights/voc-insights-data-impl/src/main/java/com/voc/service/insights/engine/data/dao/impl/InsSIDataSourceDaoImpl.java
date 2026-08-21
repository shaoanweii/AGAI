package com.voc.service.insights.engine.data.dao.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.StringUtil;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.IIntegrationServiceClient;
import com.voc.service.insights.engine.data.dao.InsSIDataSourceDao;
import com.voc.service.insights.engine.data.entity.InsSIDataSourceEntity;
import com.voc.service.insights.engine.data.mapper.InsSIDataSourceMapper;
import com.voc.service.insights.engine.model.InsDataSourceRequestModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.AttrMappingVo;
import com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/28 下午4:08
 * @描述:
 **/
@Repository
public class InsSIDataSourceDaoImpl extends ServiceImpl<InsSIDataSourceMapper, InsSIDataSourceEntity> implements InsSIDataSourceDao {
    private static final Logger log = LoggerFactory.getLogger(InsSIDataSourceDaoImpl.class);
    @Autowired
    InsSIDataSourceMapper insSIDataSourceMapper;

    @Autowired
    IIntegrationServiceClient integrationServiceClient;

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public InsDataSourceResultVo getDataSourceResult(InsDataSourceRequestModel dataSourceRequestModel) {
//        try {
//            log.debug("调用数据接收服务开始，入参:{}", JSONObject.toJSONString(dataSourceRequestModel));
//            Result<InsDataSourceResultVo> result = integrationServiceClient.getDataSourceResult(dataSourceRequestModel);
//            if(!"200".equals(result.getCode())){
//                log.error("调用数据接收服务[integration]异常:{}",result.getMessage());
//                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
//            }
//            if(ObjectUtils.isEmpty(result.getResult())){
//                return null;
//            }
//            final Object rawData = result.getResult();
//            InsDataSourceResultVo dataSourceResultVo = JSONUtil.toBean(JSONUtil.parseObj(rawData), InsDataSourceResultVo.class);
//            log.debug("调用数据接收服务结束");
//            return dataSourceResultVo;
//        }catch (BussinessException exception){
//            throw exception;
//        }catch (Exception e){
//            throw e;
//        }
//        final Integer total = insSIDataSourceMapper.findDataSourceResult(dataSourceRequestModel);
//        dataSourceRequestModel.setStatus("1");
//        Integer successCount = insSIDataSourceMapper.findDataSourceResult(dataSourceRequestModel);
        return InsDataSourceResultVo.builder().totalCount(0).verificationSuccessCount(0).build();
    }

    @Override
    @SwitchClientDS(objectAttribute ="dataSourceResultVo.clientId")
    public void saveOrUpdateDataSource(InsDataSourceResultVo dataSourceResultVo) {
        InsSIDataSourceEntity build = InsSIDataSourceEntity.builder()
                .dataName(dataSourceResultVo.getDate())
                .totalCount(dataSourceResultVo.getTotalCount())
                .verificationSuccessCount(dataSourceResultVo.getVerificationSuccessCount())
                .executeFailCount(Integer.valueOf(dataSourceResultVo.getFailCount()))
                .executeSuccessCount(Integer.valueOf(dataSourceResultVo.getFinishCount()))
                .dataSourceId(dataSourceResultVo.getDataSourceId())
                .build();
        QueryWrapper<InsSIDataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsSIDataSourceEntity::getDataName, dataSourceResultVo.getDate());
        List<InsSIDataSourceEntity> list = this.list(queryWrapper);
//        InsSIDataSourceEntity dataSourceEntity = this.getOne(queryWrapper);
        if(ObjectUtils.isEmpty(list)){
            build.setId(IdWorker.getId());
            build.setCreateTime(LocalDateTime.now());
            this.save( build);
        }else {
            InsSIDataSourceEntity dataSourceEntity = list.stream().findFirst().get();
            build.setUpdateTime(LocalDateTime.now());
            UpdateWrapper<InsSIDataSourceEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(InsSIDataSourceEntity::getDataName, dataSourceEntity.getDataName());
            this.update(build, updateWrapper);
        }
    }

    @Override
    @SwitchClientDS
    public void saveOrUpdateBatchDataSource(String clientId,List<InsDataSourceResultVo> dataSourceResultVoList) {
        Assert.isTrue(ObjectUtils.isNotEmpty(dataSourceResultVoList),"数据源结果集不能为空");
        List<String> collect1 = dataSourceResultVoList.stream().map(e -> e.getCreateTime()).collect(Collectors.toList());
        QueryWrapper<InsSIDataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(InsSIDataSourceEntity::getDataName, collect1);
        List<InsSIDataSourceEntity> list = this.list(queryWrapper);
        if(ObjectUtils.isNotEmpty(list)){
            Map<String, InsSIDataSourceEntity> collect2 = list.stream().collect(Collectors.toMap(e -> e.getDataName(), e -> e, (v1, v2) -> v2));
            List<InsSIDataSourceEntity> collect = dataSourceResultVoList.stream().map(e -> {
                InsSIDataSourceEntity dataSourceEntity = null;
                if (collect2.containsKey(e.getCreateTime())) {
                    dataSourceEntity = collect2.get(e.getCreateTime());
                    dataSourceEntity.setExecuteFailCount(Integer.valueOf(e.getFailCount()));
                    dataSourceEntity.setExecuteSuccessCount(Integer.valueOf(e.getFinishCount()));
                    dataSourceEntity.setStatus("2");
                    dataSourceEntity.setUpdateTime(LocalDateTime.now());
                }
                return dataSourceEntity;
            }).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            this.updateBatchById(collect);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceModel.clientId")
    public List<InsSIDataSourceEntity> findSIDataSourceList(InsDataSourceModel insDataSourceModel) {
        QueryWrapper<InsSIDataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().gt(InsSIDataSourceEntity::getTotalCount, 0);
        queryWrapper.lambda().orderByDesc(InsSIDataSourceEntity::getDataName);
        return this.list(queryWrapper);
    }

    @Override
    @SwitchClientDS(objectAttribute ="insDataSourceModel.clientId")
    public void updateSIDataSource(InsDataSourceModel insDataSourceModel) {
        InsSIDataSourceEntity build = InsSIDataSourceEntity.builder()
                .executeSuccessCount(insDataSourceModel.getExecuteSuccessCount())
                .executeFailCount(insDataSourceModel.getExecuteFailCount())
                .status(insDataSourceModel.getStatus())
                .build();
        QueryWrapper<InsSIDataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsSIDataSourceEntity::getDataName, insDataSourceModel.getDate());
        InsSIDataSourceEntity dataSourceEntity = this.getOne(queryWrapper);
        if(ObjectUtils.isEmpty(dataSourceEntity)){
            build.setId(IdWorker.getId());
            build.setCreateTime(LocalDateTime.now());
        }else {
            build.setId(dataSourceEntity.getId());
            build.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(build);
    }

    @Override
    public List<InsDataSourceOriginDataVo> findVerificationResultByCondition(InsDataSourceRequestModel insDataSourceModel) {
        try {
            log.debug("调用数据接收服务开始，入参:{}", JSONObject.toJSONString(insDataSourceModel));
            Result<List<InsDataSourceResultVo>> result = integrationServiceClient.findVerificationResultByCondition(insDataSourceModel);
            if(!"200".equals(result.getCode())){
                log.error("调用数据接收服务[integration]异常:{}",result.getMessage());
                throw new BussinessException(Integer.valueOf(result.getCode()),result.getMessage());
            }
            if(ObjectUtils.isEmpty(result.getResult())){
                return null;
            }
            final Object rawData = result.getResult();
            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(rawData));
            List<InsDataSourceResultVo> dataSourceResultVo = jsonArray.toJavaList(InsDataSourceResultVo.class);
            List<InsDataSourceOriginDataVo> collect = dataSourceResultVo.stream().map(e -> {
                String data = e.getData();
                String uncompress = StringUtil.uncompress(data);
                JSONObject jsonObject = JSONObject.parseObject(uncompress);
                InsDataSourceOriginDataVo javaObject = jsonObject.toJavaObject(InsDataSourceOriginDataVo.class);
                return javaObject;
            }).collect(Collectors.toList());
            log.debug("调用数据接收服务结束");
            return collect;
        }catch (BussinessException exception){
            throw exception;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    @DS("starrock_dndc")
    public List<AttrMappingVo> findAllAttrMapping() {
        return insSIDataSourceMapper.findAllAttrMapping();
    }
}
