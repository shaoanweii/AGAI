package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IConditionFilters;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.api.IInsRegulationInfoService;
import com.voc.service.insights.engine.api.data.InsDataResourceService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.constant.InsCommonConstant;
import com.voc.service.insights.engine.data.dao.InsRegulationDetailsDao;
import com.voc.service.insights.engine.data.dao.InsRegulationInfoDao;
import com.voc.service.insights.engine.data.dao.InsValidateRuleDao;
import com.voc.service.insights.engine.data.entity.InsRegulationDetailEntity;
import com.voc.service.insights.engine.data.entity.InsRegulationInfoEntity;
import com.voc.service.insights.engine.data.entity.InsValidateRuleEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataConvertMapperService;
import com.voc.service.insights.engine.entity.InsTableInfoEntity;
import com.voc.service.insights.engine.enums.*;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 09:52
 * @描述:
 **/
@Service
public class InsRegulationInfoServiceImpl implements IInsRegulationInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsRegulationInfoServiceImpl.class);
    @Autowired
    InsDataConvertMapperService convertMapperService;
    @Autowired
    InsRegulationInfoDao regulationInfoDao;
    @Autowired
    InsRegulationDetailsDao regulationDetailsDao;

    @Autowired
    InsValidateRuleDao validateRuleDao;
    @Autowired
    IInsDictService dictService;

    @Autowired
    InsDataResourceService dataResourceService;
    @Autowired
    IInsChannelInfoService channelInfoService;

    private static final String START_VALIDATE_KEY = "{}:startValidate:{}";
    private static final String START_TEST_KEY = "{}:startTest:{}";



    @CreateCache(area = "VDP", name = ":",  cacheType = CacheType.REMOTE)
    private Cache<String, String> startValidateCache;

    @CreateCache(area = "VDP", name = ":",  cacheType = CacheType.REMOTE)
    private Cache<String, String> startTestCache;

    @Override
    public void saveRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        // 必填项校验
        this.checkParameter(regulationInfoModel);
        // 当前用户
        final String username = ServiceContextHolder.getUsername();
        // 检验当前应用客户下规则名称是否存在
        Boolean checked = this.checkRegulationName(regulationInfoModel);
        if(checked){
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "新增规则信息失败:当前应用客户下规则名称已存在");
        }
        // 实体转换
        RuleWeight ruleWeight = RuleWeight.getByCode(regulationInfoModel.getRegulationWeight());
        regulationInfoModel.setRegulationWeight(String.valueOf(ruleWeight.getText()));
        InsRegulationInfoEntity insRegulationInfoEntity = convertMapperService.regulationInfoModelConvertEntity(regulationInfoModel);
        final String id = IdWorker.getId();
        final LocalDateTime now = LocalDateTime.now();
        if(ObjectUtils.isNotEmpty(regulationInfoModel.getId())){
            insRegulationInfoEntity.setId(regulationInfoModel.getId());
        }else {
            insRegulationInfoEntity.setId(id);
        }
        insRegulationInfoEntity.setCreateTime(now);
        insRegulationInfoEntity.setCreateUser(username);
        insRegulationInfoEntity.setUpdateTime(now);
        // 根据规则类型获取关联的表名
//        Set<String> staticTableNames = regulationInfoDao.findStaticTableNames(regulationInfoModel.getRegulationType());
//
//        String tableName = staticTableNames.stream().filter(e->ObjectUtils.isNotEmpty(e)).findFirst().orElse(null);
//        if(ObjectUtils.isNotEmpty(tableName)){
//            insRegulationInfoEntity.setRelevancyTable(tableName);
//        }
        // 生成规则编码
//        String string = String.valueOf(DateTime.now().getTime());
//        Random random = new Random();
        insRegulationInfoEntity.setRegulationType(regulationInfoModel.getRegulationType());
//        insRegulationInfoEntity.setRegulationWeight(ruleWeight.getText());
//        List<String> channelHierarchical = regulationInfoDao.findChannelHierarchical(regulationInfoModel.getClientId(), regulationInfoModel.getChannel(), false);
//        insRegulationInfoEntity.setChannel(channelHierarchical);
        //保存规则信息
        regulationInfoDao.saveRegulationInfo(insRegulationInfoEntity);
        //规则条件
        List<InsRegulationDetailsModel> regulationCondition = regulationInfoModel.getRegulationConditions();
        List<InsRegulationDetailEntity> insRegulationDetailEntities = this.dataAssembly(regulationCondition, InsCommonConstant.REGULATION_CONDITIONS, id, username, now);
        //规则执行动作
        List<InsRegulationDetailsModel> regulationPerformAction = regulationInfoModel.getRegulationPerformAction();
        List<InsRegulationDetailEntity> performActionEntities = this.dataAssembly(regulationPerformAction, InsCommonConstant.REGULATION_PERFORM_ACTION, id, username, now);
        //数据合并
        List<InsRegulationDetailEntity> regulationDetailEntities = CollUtil.unionAll(insRegulationDetailEntities, performActionEntities);
        //保存规则详情信息
        regulationDetailsDao.saveRegulationDetails(regulationDetailEntities,insRegulationInfoEntity.getClientId() );
    }



    @Override
    public void updateRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        // 必填项校验
        this.checkParameter(regulationInfoModel);
        //单独参数校验
        Assert.hasLength(regulationInfoModel.getId(),"id不允许为空");

        final String username = ServiceContextHolder.getUsername();
        // 检验当前应用客户下规则名称是否存在
        Boolean checked = this.checkRegulationName(regulationInfoModel);
        if(checked){
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "更新规则信息失败:当前应用客户下规则名称已存在");
        }

        final InsRegulationInfoEntity regulationInfo = regulationInfoDao.findRegulationInfo(regulationInfoModel);
        Assert.isTrue(ObjectUtils.isNotEmpty(regulationInfo),"规则信息不存在");
        final LocalDateTime now = LocalDateTime.now();
        // 更新规则类型 若规则类型发生变化，则根据新的规则类型生成规则编码，否则规则类型沿用之前的编码
        final String regulationType = regulationInfo.getRegulationType();
        BeanUtils.copyProperties(regulationInfoModel,regulationInfo);
        if(!regulationType.contains(regulationInfoModel.getRegulationType())){
//            String string = String.valueOf(DateTime.now());
//            Random random = new Random();
            regulationInfo.setRegulationType(regulationInfoModel.getRegulationType());
        }else {
            regulationInfo.setRegulationType(regulationType);
        }

        //更新规则信息
        RuleWeight ruleWeight = RuleWeight.getByCode(regulationInfoModel.getRegulationWeight());
        regulationInfoModel.setRegulationWeight(String.valueOf(ruleWeight.getText()));
        InsRegulationInfoEntity insRegulationInfoEntity = convertMapperService.regulationInfoModelConvertEntity(regulationInfoModel);
        insRegulationInfoEntity.setUpdateTime(now);
        insRegulationInfoEntity.setUpdateUser(username);
//        List<String> channelHierarchical = regulationInfoDao.findChannelHierarchical(regulationInfoModel.getClientId(), regulationInfoModel.getChannel(), false);
//        insRegulationInfoEntity.setChannel(channelHierarchical);
        // 根据规则类型获取关联的表名
//        Set<String> staticTableNames = regulationInfoDao.findStaticTableNames(regulationInfoModel.getRegulationType());
//        String tableName = staticTableNames.stream().filter(e->ObjectUtils.isNotEmpty(e)).findFirst().orElse(null);
//        if(ObjectUtils.isNotEmpty(tableName)){
//            insRegulationInfoEntity.setRelevancyTable(tableName);
//        }
        regulationInfoDao.updateRegulationInfo(insRegulationInfoEntity);

        regulationDetailsDao.removeRegulationDetails(regulationInfoModel.getId(), insRegulationInfoEntity.getClientId());
        //规则条件
        List<InsRegulationDetailsModel> regulationCondition = regulationInfoModel.getRegulationConditions();
        List<InsRegulationDetailEntity> insRegulationDetailEntities = this.dataAssembly(regulationCondition, InsCommonConstant.REGULATION_CONDITIONS, regulationInfoModel.getId(), username, now);
        //规则执行动作
        List<InsRegulationDetailsModel> regulationPerformAction = regulationInfoModel.getRegulationPerformAction();
        List<InsRegulationDetailEntity> performActionEntities = this.dataAssembly(regulationPerformAction, InsCommonConstant.REGULATION_PERFORM_ACTION, regulationInfoModel.getId(), username, now);
        //数据合并
        List<InsRegulationDetailEntity> regulationDetailEntities = CollUtil.unionAll(insRegulationDetailEntities, performActionEntities);

        //更新规则详情信息
        regulationDetailsDao.updateRegulationDetails(regulationDetailEntities, insRegulationInfoEntity.getClientId());
    }




    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public void deleteRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        //单独参数校验
        Assert.hasLength(regulationInfoModel.getId(),"id不允许为空");

        Boolean status = regulationInfoDao.checkRegulationStatusById(regulationInfoModel.getId());
        Assert.isTrue(!status,"当前规则为启用状态，无法删除");

        final String username = ServiceContextHolder.getUsername();
        //删除规则信息
        regulationInfoDao.deleteRegulationInfo(regulationInfoModel.getId(),username);
        //删除规则详情信息
        regulationDetailsDao.deleteRegulationDetails(regulationInfoModel.getId(),username);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public PageInfo findRegulationInfoList(InsRegulationInfoModel regulationInfoModel) {
        PageHelper.startPage(regulationInfoModel.getPageNum(),regulationInfoModel.getPageSize());
        List<InsRegulationInfoEntity> regulationInfoList = regulationInfoDao.findRegulationInfoList(regulationInfoModel);
        if(ObjectUtils.isEmpty(regulationInfoList)){
            log.info("暂无规则信息");
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(regulationInfoList);
        List<ChannelInfoVo> allChannelInfo = channelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(ServiceContextHolder.getClientId()).build());
        Map<String, String> channles = allChannelInfo.stream().collect(Collectors.toMap(ChannelInfoVo::getId, ChannelInfoVo::getName));
        List<RegulationInfoVo> regulationInfoVos = convertMapperService.regulationInfoEntityListConvertVoList(regulationInfoList);
        List<String> ids = regulationInfoVos.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<InsValidateRuleEntity> validateInfoListByIds = validateRuleDao.findValidateInfoListByIds(ids);
        Map<String, List<InsValidateRuleEntity>> collect1 = validateInfoListByIds.stream().collect(Collectors.groupingBy(InsValidateRuleEntity::getRegulationId));
        regulationInfoVos.stream().forEach(e->{
            List<String> channelName = new ArrayList<>();
            List<String> channel = e.getChannel();
            channel.stream().forEach(k->{
                if(channles.containsKey(k)){
                    channelName.add(channles.get(k));
                }
            });
            e.setChannelText(channelName);
//            List<InsValidateRuleEntity> validateInfoList = validateRuleDao.findValidateInfoList(e.getId());
            if(!collect1.containsKey(e.getId())){
                e.setSingleValidateStatus("-1");
                e.setFullyValidateStatus("-1");
            }else {
//                validateInfoList.stream().forEach(k->{
//                    e.setSingleValidateStatus(k.getSingleValidateStatus());
//                    e.setFullyValidateStatus(k.getFullValidateStatus());
//                });
                List<InsValidateRuleEntity> validateInfoList = collect1.get(e.getId());
                final Map<String, List<InsValidateRuleEntity>> collect = validateInfoList.stream().collect(Collectors.groupingBy(InsValidateRuleEntity::getSingleOrFullType));
                collect.entrySet().stream().forEach(k->{
                    final List<InsValidateRuleEntity> value = k.getValue();
                    InsValidateRuleEntity validateRuleEntity = null;
                    if(value.size()>1){
                        validateRuleEntity = value.stream().sorted(Comparator.comparing(InsValidateRuleEntity::getCreateTime,Comparator.nullsLast(Comparator.naturalOrder())).reversed()).findFirst().orElse(null);
                    }else {
                        validateRuleEntity = value.get(0);
                    }
                    if("0".equalsIgnoreCase(k.getKey())){
                        if(ObjectUtils.isEmpty(validateRuleEntity)|| ObjectUtils.isEmpty(validateRuleEntity.getSingleValidateStatus())){
                            e.setSingleValidateStatus("-1");
                        }else {
                            e.setSingleValidateStatus(validateRuleEntity.getSingleValidateStatus());
                        }
                    }else {
                        if(ObjectUtils.isEmpty(validateRuleEntity)||ObjectUtils.isEmpty(validateRuleEntity.getFullValidateStatus())){
                            e.setFullyValidateStatus("-1");
                        }else {
                            e.setFullyValidateStatus(validateRuleEntity.getFullValidateStatus());
                        }
                    }

                });
                if(ObjectUtils.isEmpty(e.getSingleValidateStatus())){
                    e.setSingleValidateStatus("-1");
                }
                if(ObjectUtils.isEmpty(e.getFullyValidateStatus())){
                    e.setFullyValidateStatus("-1");
                }
            }
        });
        pageInfo.setList(regulationInfoVos);
        return pageInfo;
    }

    @Override
    public void copyRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        Assert.hasLength(regulationInfoModel.getId(),"id不允许为空");
        final String username = ServiceContextHolder.getUsername();
        final InsRegulationInfoEntity regulationInfo = regulationInfoDao.findRegulationInfo(regulationInfoModel);
        final LocalDateTime now = LocalDateTime.now();
        Assert.isTrue(ObjectUtils.isNotEmpty(regulationInfo),"规则信息不存在");
        InsRegulationInfoEntity regulationInfoEntity = new InsRegulationInfoEntity();
        BeanUtils.copyProperties(regulationInfo,regulationInfoEntity);
        final String regulationName = regulationInfoDao.findRegulationName(regulationInfoModel.getClientId(), regulationInfo.getName());
        if(regulationName.contains("_")){
            String substring = regulationName.substring(regulationName.lastIndexOf("_") + 1);
            Integer suffix = Integer.valueOf(substring);
            regulationInfoEntity.setName(regulationInfo.getName()+"_"+ (suffix+1));
        }else {
            String rename = regulationInfo.getName() + "_1";
            regulationInfoEntity.setName(rename);
        }

        regulationInfoEntity.setId(IdWorker.getId());
        regulationInfoEntity.setCreateTime(now);
        regulationInfoEntity.setCreateUser(username);
        regulationInfoEntity.setStatus(RuleStatusType.NotEnabled.getCode());
        //保存规则信息
        regulationInfoDao.saveRegulationInfo(regulationInfoEntity);

        List<InsRegulationDetailEntity> regulationDetailEntities = regulationDetailsDao.findRegulationDetailsById(regulationInfo.getId(), regulationInfoEntity.getClientId());
        List<InsRegulationDetailEntity> regulationDetailEntityList = new ArrayList<>();
        regulationDetailEntities.stream().forEach(e->{
            InsRegulationDetailEntity regulationDetail = new InsRegulationDetailEntity();
            BeanUtils.copyProperties(e,regulationDetail);
            regulationDetail.setId(IdWorker.getId());
            regulationDetail.setCreateTime(now);
            regulationDetail.setCreateUser(username);
            regulationDetail.setRegulationId(regulationInfoEntity.getId());
            regulationDetailEntityList.add(regulationDetail);
        });
        //保存规则详情信息
        regulationDetailsDao.saveRegulationDetails(regulationDetailEntityList, regulationInfoModel.getClientId());
    }

    @Override
    public RegulationInfoVo findRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        //单独参数校验
        Assert.hasLength(regulationInfoModel.getId(),"id不允许为空");

        //获取规则信息
        final InsRegulationInfoEntity regulationInfo = regulationInfoDao.findRegulationInfo(regulationInfoModel);
        if(ObjectUtils.isEmpty(regulationInfo)){
            log.info("暂无规则信息");
            return null;
        }

        RegulationInfoVo regulationInfoVo = convertMapperService.regulationInfoEntityConvertVo(regulationInfo);
//        RuleWeight ruleWeight = RuleWeight.getByText(Long.valueOf(regulationInfoVo.getRegulationWeight()));
//        if(ObjectUtils.isNotEmpty(ruleWeight)){
//            regulationInfoVo.setRegulationWeight(ruleWeight.getCode());
//        }
//        List<String> channelHierarchical = regulationInfoDao.findChannelHierarchical(regulationInfoVo.getClientId(), regulationInfoVo.getChannel(), false);
//        regulationInfoVo.setChannel(channelHierarchical);
        //获取规则详情信息
        List<InsRegulationDetailEntity> regulationDetailsById = regulationDetailsDao.findRegulationDetailsById(regulationInfoModel.getId(),regulationInfoModel.getClientId() );
        if(ObjectUtils.isEmpty(regulationDetailsById)){
            log.info("暂无规则详情信息");
        }else {
            List<RegulationDetailsVo> regulationDetailsVos = convertMapperService.regulationDetailsEntityListConvertVoList(regulationDetailsById);
            Map<String, List<RegulationDetailsVo>> regulationDetailsMap = regulationDetailsVos.stream().collect(Collectors.groupingBy(RegulationDetailsVo::getDetailType));
            regulationDetailsMap.entrySet().stream().forEach(e->{
                List<RegulationDetailsVo> value = e.getValue();
                value.stream().forEach(k->{
                    List<DictInfoVo> dictInfoByCode = null;
                    //如果是后置规则
                    if(RuleStage.PostRule.getCode().equalsIgnoreCase(regulationInfoVo.getProcessPhase())){
                        dictInfoByCode = dictService.findDictInfoByCode(IConditionFilters.POST_FIELDS);
                    }else {
                        //如果是前置规则并且是文本类型
                        if(RuleContentType.PreRule.getCode().equalsIgnoreCase(regulationInfoVo.getContentType())){
                            dictInfoByCode = dictService.findDictInfoByCode(IConditionFilters.VOC_TEXT_TYPE);
                        }else {
                            //如果是前置规则并且是工单类型
                            dictInfoByCode = dictService.findDictInfoByCode(IConditionFilters.VOC_ORDER_TYPE);
                        }
                    }
                    if(ObjectUtils.isNotEmpty(dictInfoByCode)){
                        Optional<DictInfoVo> vo = dictInfoByCode.stream().filter(item -> item.getTypeCode().equalsIgnoreCase(k.getFieldName())).findFirst();
                        if(vo.isPresent()){
                            k.setFieldNameText(vo.get().getTypeName());
                        }
                    }
                    //如果条件类型是正则表达式
                    if(RuleConditionType.ResourceGroup.getCode().equalsIgnoreCase(k.getConditionType())){
                        List<InsDataResourceModel> insDataResourceModels = dataResourceService.findResourceGroupByAppClient(InsDataResourceModel.builder().customer(regulationInfoVo.getClientId()).build());
                        if(ObjectUtils.isNotEmpty(insDataResourceModels)){
                            InsDataResourceModel insDataResourceModel = insDataResourceModels.stream().filter(item -> item.getId().equalsIgnoreCase(k.getConditionDetail())).findFirst().orElse(null);
                            if(ObjectUtils.isNotEmpty(insDataResourceModel)){
                                k.setConditionDetailText(insDataResourceModel.getName());
                            }
                        }
                    }
                });


                if("0".equalsIgnoreCase(e.getKey())){
                    //规则条件
                    regulationInfoVo.setRegulationConditions(value);
                }else if("1".equalsIgnoreCase(e.getKey())){
                    //执行动作
                    regulationInfoVo.setRegulationPerformAction(value);
                }
            });
        }
        return regulationInfoVo;
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public void disabledOrEnableRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        //单独参数校验
        Assert.hasLength(regulationInfoModel.getId(),"id不允许为空");
        Assert.hasLength(regulationInfoModel.getStatus(),"停用/启用状态不允许为空");
        InsRegulationInfoEntity regulationInfo = regulationInfoDao.findRegulationInfo(regulationInfoModel);
        if(ObjectUtils.isNotEmpty(regulationInfo)){
            regulationInfo.setStatus(regulationInfoModel.getStatus());
            regulationInfoDao.updateRegulationInfo(regulationInfo);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public List<AysRegulationInfoVo> findAllRegulationInfo(InsRegulationInfoModel regulationInfoModel) {
        List<InsRegulationInfoEntity> regulationInfoList = regulationInfoDao.findRegulationInfoList(regulationInfoModel);
        if(ObjectUtils.isEmpty(regulationInfoList)){
            log.info("暂无规则信息");
            return Collections.EMPTY_LIST;
        }
        List<AysRegulationInfoVo> regulationInfoVos = convertMapperService.regulationInfoEntityListConvertAysVoList(regulationInfoList);
        List<InsRegulationDetailEntity> allRegulationDetails = regulationDetailsDao.findAllRegulationDetails(regulationInfoModel);
        if(ObjectUtils.isNotEmpty(allRegulationDetails)){
            regulationInfoVos.stream().forEach(e->{
                final List<InsRegulationDetailEntity> collect = allRegulationDetails.stream().filter(k -> k.getRegulationId().equalsIgnoreCase(e.getId())).collect(Collectors.toList());
                final List<RegulationDetailsVo> regulationDetailsVos = convertMapperService.regulationDetailsEntityListConvertVoList(collect);
                Map<String, List<RegulationDetailsVo>> regulationDetailsMap = regulationDetailsVos.stream().collect(Collectors.groupingBy(RegulationDetailsVo::getDetailType));
                regulationDetailsMap.entrySet().stream().forEach(k->{
                    if("0".equalsIgnoreCase(k.getKey())){
                        e.setRegulationConditions(k.getValue());
                    }else if("1".equalsIgnoreCase(k.getKey())){
                        e.setRegulationPerformAction(k.getValue());
                    }
                });
            });
        }
        return regulationInfoVos;
    }


    @Override
    @SwitchClientDS(objectAttribute = "tableInfoModel.clientId")
    public List<InsTableInfoVo> findTableInfoList(InsTableInfoModel tableInfoModel) {
        Set<String> tableNames = null;
        //获取表名
        if(ObjectUtils.isEmpty(tableInfoModel)||ObjectUtils.isEmpty(tableInfoModel.getClientId())){
            log.info("获取定义好的所有表信息");
            tableNames = regulationInfoDao.findStaticTableNames(null);
        }else if(ObjectUtils.isNotEmpty(tableInfoModel.getClientId())){
            log.info("获取指定客户下的表信息");
            tableNames = regulationInfoDao.findTableNames(tableInfoModel);
        }

        Assert.isTrue(ObjectUtils.isNotEmpty(tableNames),"暂无表信息");

        if(ObjectUtils.isNotEmpty(tableInfoModel.getTableName())){
            tableNames = tableNames.stream().filter(e->e.equals(tableInfoModel.getTableName())).collect(Collectors.toSet());
        }

        Assert.isTrue(ObjectUtils.isNotEmpty(tableNames),"客户未指定当前表作为规则依赖表");

        //获取所需表及表字段信息
        List<InsTableInfoEntity> tableInfoList = regulationInfoDao.findTableInfoList(tableNames,
                ObjectUtils.isEmpty(tableInfoModel)||ObjectUtils.isEmpty(tableInfoModel.getColumns())?null:tableInfoModel.getColumns());
        //根据表名分组
        Map<String, List<InsTableInfoEntity>> collect = tableInfoList.stream().collect(Collectors.groupingBy(InsTableInfoEntity::getTableName));
        List<InsTableInfoVo> tableInfoVos = new ArrayList<>();
        collect.entrySet().stream().forEach(e->{
            // 表名
            final String tableName = e.getKey();
            final List<InsTableInfoEntity> tableInfoEntityList = e.getValue();
            // 所需字段
            final List<String> columns = tableInfoEntityList.stream().map(InsTableInfoEntity::getColumnName).collect(Collectors.toList());
            final InsTableInfoEntity insTableInfoEntity = tableInfoEntityList.stream().findFirst().get();
            //表注释
            final String tableComment = insTableInfoEntity.getTableComment();
            //字段名及对应的字段注释
            Map<String, String> columnsMap = tableInfoEntityList.stream().collect(Collectors.toMap(InsTableInfoEntity::getColumnName, InsTableInfoEntity::getColumnComment,(v1, v2) -> v1));

            //获取表数据
            List<JSONObject> tableData = regulationInfoDao.findTableData(tableName, columns);
            InsTableInfoVo insTableInfoVo = InsTableInfoVo.builder()
                    .tableName(tableName)
                    .tableComment(tableComment)
                    .columnsList(columns)
                    .columnsMap(columnsMap)
                    .data(tableData).build();
            tableInfoVos.add(insTableInfoVo);
        });

        return tableInfoVos;
    }

    @Override
    public List<AysRegulationInfoVo> findRegulationList(InsRegulationInfoModel regulationInfoMode) {
        //单独参数校验
        Assert.hasLength(regulationInfoMode.getClientId(),"客户编码不允许为空");
        regulationInfoMode.setRegulationClassify(null);
        regulationInfoMode.setStatusList(Arrays.asList(RuleStatusType.Enabled.getCode(),RuleStatusType.NotEnabled.getCode()));
        // 标准规则
        List<InsRegulationInfoEntity> standardRuleInfoList = regulationInfoDao.findStandardRuleInfoList(regulationInfoMode);
        //客户自定义规则
//        List<InsRegulationInfoEntity> ruleInfoList = regulationInfoDao.findRuleInfoList(regulationInfoMode);
        List<InsRegulationInfoEntity> ruleInfoList = new ArrayList<>();

        ruleInfoList.addAll(standardRuleInfoList);
        if(ObjectUtils.isEmpty(ruleInfoList)&&ObjectUtils.isEmpty(standardRuleInfoList)){
            log.info("暂无规则信息");
            return Collections.EMPTY_LIST;
        }

        final List<AysRegulationInfoVo> insRuleInfoVos = convertMapperService.regulationInfoEntityListConvertAysVoList(ruleInfoList);
        //客户自定义规则详情
//        List<InsRegulationDetailEntity> allRegulationDetails = regulationDetailsDao.findAllRegulationDetails(regulationInfoMode);
        List<InsRegulationDetailEntity> allRegulationDetails = new ArrayList<>();
        //标准规则详情
        List<InsRegulationDetailEntity> allStandardRegulationDetails = regulationDetailsDao.findAllStandardRegulationDetails();
        allRegulationDetails.addAll(allStandardRegulationDetails);
        final List<RegulationDetailsVo> regulationDetailsVos = convertMapperService.regulationDetailsEntityListConvertVoList(allRegulationDetails);
        Map<String, List<RegulationDetailsVo>> collect2 = regulationDetailsVos.stream().collect(Collectors.groupingBy(RegulationDetailsVo::getRegulationId));


        if(ObjectUtils.isNotEmpty(allRegulationDetails)){
            List<ChannelInfoVo> allChannelInfo = channelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(regulationInfoMode.getClientId()).build());
            Map<String,String> map = new HashMap<>();
            if(ObjectUtils.isNotEmpty(allChannelInfo)){
                map.putAll(allChannelInfo.stream().collect(Collectors.toMap(ChannelInfoVo::getId,ChannelInfoVo::getCode)));
            }
            insRuleInfoVos.stream().forEach(e->{
                List<String> channel = e.getChannel();
                if(ObjectUtils.isNotEmpty(map)&&!channel.contains("all")){
                    List<String> collect1 = channel.stream().filter(k -> map.containsKey(k)).map(k -> map.get(k)).collect(Collectors.toList());
                    e.setChannel(collect1);
                }

                if(collect2.containsKey(e.getId())){
                    List<RegulationDetailsVo> regulationDetailsVos1 = collect2.get(e.getId());
                    Map<String, List<RegulationDetailsVo>> regulationDetailsMap = regulationDetailsVos1.stream().collect(Collectors.groupingBy(RegulationDetailsVo::getDetailType));
                    regulationDetailsMap.entrySet().stream().forEach(k->{
                        if("0".equalsIgnoreCase(k.getKey())){
                            e.setRegulationConditions(k.getValue());
                        }else if("1".equalsIgnoreCase(k.getKey())){
                            e.setRegulationPerformAction(k.getValue());
                        }
                    });
                }

            });
        }else {
            List<ChannelInfoVo> allChannelInfo = channelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(regulationInfoMode.getClientId()).build());

            Map<String,String> map = new HashMap<>();
            if(ObjectUtils.isNotEmpty(allChannelInfo)){
                map.putAll(allChannelInfo.stream().collect(Collectors.toMap(ChannelInfoVo::getId,ChannelInfoVo::getCode)));
            }
            insRuleInfoVos.stream().forEach(e->{
                List<String> channel = e.getChannel();
                if(ObjectUtils.isNotEmpty(map)&&!channel.contains("all")){
                    List<String> collect1 = channel.stream().filter(k -> map.containsKey(k)).map(k -> map.get(k)).collect(Collectors.toList());
                    e.setChannel(collect1);
                }
              if(collect2.containsKey(e.getId())){
                    List<RegulationDetailsVo> regulationDetailsVos1 = collect2.get(e.getId());
                    Map<String, List<RegulationDetailsVo>> regulationDetailsMap = regulationDetailsVos1.stream().collect(Collectors.groupingBy(RegulationDetailsVo::getDetailType));
                    regulationDetailsMap.entrySet().stream().forEach(k->{
                        if("0".equalsIgnoreCase(k.getKey())){
                            e.setRegulationConditions(k.getValue());
                        }else if("1".equalsIgnoreCase(k.getKey())){
                            e.setRegulationPerformAction(k.getValue());
                        }
                    });
                }
            });
        }

        return insRuleInfoVos;
    }

    @Override
    public InsValidateRuleInfoVo findValidateRegulationCondition(InsValidateRuleInfoModel validateRuleInfoModel) {
        this.checkValidateRegulationConditionParameter(validateRuleInfoModel);
        List<String> channelHierarchical = regulationInfoDao.findChannelCodeHierarchical(validateRuleInfoModel.getClientId(), validateRuleInfoModel.getChannel(), false);
        validateRuleInfoModel.setChannel(channelHierarchical);
        InsValidateRuleInfoVo validateRuleInfo = validateRuleDao.findValidateRuleCondition(validateRuleInfoModel);
        return validateRuleInfo;
    }

    @Override
    public void startValidateRegulationInfo(InsValidateRuleInfoModel validateRuleInfoModel) {
        Assert.notNull(validateRuleInfoModel.getStartTime(),"开始时间不允许为空");
        Assert.notNull(validateRuleInfoModel.getEndTime(),"结束时间不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(validateRuleInfoModel.getValidRuleIds()),"规则id不允许为空");
        Assert.hasLength(validateRuleInfoModel.getClientId(),"客户id不允许为空");
//        String s = startValidateCache.get(validateRuleInfoModel.getClientId() + "_" + validateRuleInfoModel.getValidRuleIds());
        String s = startValidateCache.get(this.getStartValidateKey(validateRuleInfoModel.getClientId() + "_" + validateRuleInfoModel.getValidRuleIds()));
        Assert.isTrue(StrUtil.isEmpty(s),"校验规则正在执行中,请勿重复点击");
//        startValidateCache.put(validateRuleInfoModel.getClientId()+"_"+validateRuleInfoModel.getValidRuleIds(),"true");
        startValidateCache.put(this.getStartValidateKey(validateRuleInfoModel.getClientId()+"_"+validateRuleInfoModel.getValidRuleIds()),"true");
        try {
            validateRuleInfoModel.setRuleType("0");
            List<String> channelHierarchical = regulationInfoDao.findChannelCodeHierarchical(validateRuleInfoModel.getClientId(), validateRuleInfoModel.getChannel(), false);
            validateRuleInfoModel.setChannel(channelHierarchical);
            validateRuleDao.startValidateRuleInfo(validateRuleInfoModel);
        }finally {
            startValidateCache.remove(this.getStartValidateKey(validateRuleInfoModel.getClientId()+"_"+validateRuleInfoModel.getValidRuleIds()));
        }
    }

    @Override
    public ValidateRuleResult findValidateRegulationResult(InsValidateModel insValidateModel) {
        Assert.hasLength(insValidateModel.getRulesId(),"规则id不允许为空");
        Assert.hasLength(insValidateModel.getClientId(),"客户id不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insValidateModel.getChannelId()),"渠道不允许为空");
        Assert.hasLength(insValidateModel.getDataType(),"数据类型不允许为空");
        List<InsValidateRuleEntity> validateRuleInfo = validateRuleDao.findValidateRuleInfo(insValidateModel);
        List<InsValidateRuleEntity> collect = validateRuleInfo.stream().filter(e -> insValidateModel.getDataType().equals(e.getSingleOrFullType())).collect(Collectors.toList());
        InsValidateRuleEntity validateRuleEntity = collect.stream().findFirst().orElse(null);
//        List<String> channelHierarchical = regulationInfoDao.findChannelCodeHierarchical(insValidateModel.getClientId(), validateRuleEntity.getChannel(), false);
        insValidateModel.setChannelId(validateRuleEntity.getChannel());
        if(ObjectUtils.isNotEmpty(validateRuleEntity)){
            insValidateModel.setWorkId(validateRuleEntity.getWorkId());
            PageInfo pageInfo = validateRuleDao.findValidateRuleResult(insValidateModel);
            ValidateRuleResult ruleResult = ValidateRuleResult.builder()
                    .finishTime(ObjectUtils.isNotEmpty(validateRuleEntity.getUpdateTime()) ? validateRuleEntity.getUpdateTime() : null)
                    .pageInfo(pageInfo)
                    .build();
            return ruleResult;
        }
        return null;
    }

    @Override
    @SwitchClientDS(objectAttribute = "validateRuleInfoModel.clientId")
    public void pushValidateRegulationStatus(InsValidateRuleInfoModel validateRuleInfoModel) {
        Assert.hasLength(validateRuleInfoModel.getWorkId(),"workId不允许为空");
        Assert.hasLength(validateRuleInfoModel.getValidateStatus(),"校验状态不允许为空");
        String validateType = validateRuleDao.findValidateTypeByWorkId(validateRuleInfoModel.getWorkId());
        Assert.hasLength(validateType,"根据接收到的workId，未查询到开启校验的规则，请检查");
        validateRuleInfoModel.setRuleType(validateType);
        validateRuleDao.pushValidateRuleStatus(validateRuleInfoModel);
    }

    @Override
    public Boolean checkRegulationName(InsRegulationInfoModel regulationInfoMode) {
        //单独参数校验
        Assert.hasLength(regulationInfoMode.getName(),"规则名称不能为空");
        if(ObjectUtils.isEmpty(regulationInfoMode.getId())){
            return regulationInfoDao.checkRegulationName(regulationInfoMode);
        }else {
            InsRegulationInfoEntity regulationInfo = regulationInfoDao.findRegulationInfo(regulationInfoMode);
            if(regulationInfo.getName().equals(regulationInfoMode.getName())){
                return false;
            }else {
                return regulationInfoDao.checkRegulationName(regulationInfoMode);
            }
        }
    }

    @Override
    public void startTestRegulationInfo(InsValidateRuleInfoModel validateRuleInfoModel) {
        Assert.notNull(validateRuleInfoModel.getStartTime(),"开始时间不允许为空");
        Assert.notNull(validateRuleInfoModel.getEndTime(),"结束时间不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(validateRuleInfoModel.getValidRuleIds()),"规则id不允许为空");
        Assert.hasLength(validateRuleInfoModel.getClientId(),"客户id不允许为空");
//        String s = startTestCache.get(validateRuleInfoModel.getClientId() + "_" + validateRuleInfoModel.getValidRuleIds());
        String s = startTestCache.get(this.getStartTestKey(validateRuleInfoModel.getClientId() + "_" + validateRuleInfoModel.getValidRuleIds()));
        Assert.isTrue(StrUtil.isEmpty(s),"规则测试正在执行中,请勿重复点击");
        startTestCache.put(this.getStartTestKey(validateRuleInfoModel.getClientId()+"_"+validateRuleInfoModel.getValidRuleIds()),"true");
        try {
            List<InsRegulationInfoEntity> regulationInfoList = regulationInfoDao.findRegulationInfoList(InsRegulationInfoModel.builder().clientId(validateRuleInfoModel.getClientId()).statusList(Arrays.asList(RuleStatusType.Enabled.getCode(),RuleStatusType.NotEnabled.getCode())).processPhase(RuleStage.PostRule.getCode()).build());
            if(ObjectUtils.isNotEmpty(regulationInfoList)){
                Set<String> enabledRuleIds = regulationInfoList.stream().map(InsRegulationInfoEntity::getId).collect(Collectors.toSet());
                validateRuleInfoModel.setEnabledRuleIds(enabledRuleIds);
            }
            validateRuleInfoModel.setRuleType("1");
            List<String> channelHierarchical = regulationInfoDao.findChannelCodeHierarchical(validateRuleInfoModel.getClientId(), validateRuleInfoModel.getChannel(), false);
            validateRuleInfoModel.setChannel(channelHierarchical);
            validateRuleDao.startValidateRuleInfo(validateRuleInfoModel);
        }finally {
            startTestCache.remove(this.getStartTestKey(validateRuleInfoModel.getClientId()+"_"+validateRuleInfoModel.getValidRuleIds()));
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public PageInfo findResourceGroupRegulationList(InsRegulationInfoModel regulationInfoModel) {
        Assert.hasLength(regulationInfoModel.getResourceGroupId(),"资源组id不允许为空");
        Assert.hasLength(regulationInfoModel.getClientId(),"客户id不允许为空");
        PageHelper.startPage(regulationInfoModel.getPageNum(),regulationInfoModel.getPageSize());
        List<InsRegulationInfoEntity> resourceGroupRegulationList = regulationInfoDao.findResourceGroupRegulationList(regulationInfoModel);
        PageInfo pageInfo = new PageInfo(resourceGroupRegulationList);
        List<RegulationInfoVo> regulationInfoVos = convertMapperService.regulationInfoEntityListConvertVoList(resourceGroupRegulationList);
        pageInfo.setList(regulationInfoVos);
        return pageInfo;
    }

    @Override
    @SwitchClientDS(objectAttribute = "regulationInfoModel.clientId")
    public List<RegulationInfoVo> findResourceGroupRegulationStatusCount(InsRegulationInfoModel regulationInfoModel) {
        Assert.hasLength(regulationInfoModel.getResourceGroupId(),"资源组id不允许为空");
        List<InsRegulationInfoEntity> resourceGroupRegulationStatusCount = regulationInfoDao.findResourceGroupRegulationStatusCount(regulationInfoModel);
        List<RegulationInfoVo> regulationInfoVos = convertMapperService.regulationInfoEntityListConvertVoList(resourceGroupRegulationStatusCount);
        return regulationInfoVos;
    }

    @Override
    public List<InsValidateInfoVo> findNewestValidateRuleInfo() {
        List<InsValidateRuleEntity> newestValidateRuleInfo = validateRuleDao.findNewestValidateRuleInfo();
        if(ObjectUtils.isEmpty(newestValidateRuleInfo)){
            return Collections.EMPTY_LIST;
        }
        List<InsValidateInfoVo> insValidateInfoVos = convertMapperService.validateInfoEntityListConvertVoList(newestValidateRuleInfo);
        return insValidateInfoVos;
    }


    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/28 10:18
     * @描述  必填项校验
     * @param regulationInfoModel
     * @return void
     **/
    private void checkParameter(InsRegulationInfoModel regulationInfoModel){
        Assert.hasLength(regulationInfoModel.getClientId(),"客户不允许为空");
        Assert.hasLength(regulationInfoModel.getName(),"规则名称不允许为空");
        Assert.isTrue(regulationInfoModel.getName().length()<=50,"规则名称长度不能超过50");
        if(ObjectUtils.isNotEmpty(regulationInfoModel.getDescription())){
            Assert.isTrue(regulationInfoModel.getDescription().length()<=200,"规则描述长度不能超过200");
        }
        Assert.hasLength(regulationInfoModel.getProcessPhase(),"处理阶段不允许为空");
        Assert.hasLength(regulationInfoModel.getRegulationType(),"规则类型不允许为空");
        Assert.hasLength(regulationInfoModel.getContentType(),"内容类型不允许为空");
        Assert.notEmpty(regulationInfoModel.getChannel(),"数据渠道不允许为空");
        Assert.hasLength(regulationInfoModel.getMatchingRule(),"匹配规则不允许为空");
        Assert.hasLength(regulationInfoModel.getRegulationWeight(),"权重不允许为空");
        Assert.notEmpty(regulationInfoModel.getRegulationConditions(),"规则条件不允许为空");
        List<InsRegulationDetailsModel> regulationCondition = regulationInfoModel.getRegulationConditions();
        for(InsRegulationDetailsModel regulationDetailsModel:regulationCondition){
            Assert.hasLength(regulationDetailsModel.getFieldName(),"规则条件详情信息中字段不允许为空");
            Assert.hasLength(regulationDetailsModel.getVariableValue(),"规则条件详情信息中变量值不允许为空");
            Assert.hasLength(regulationDetailsModel.getLogicalOperator(),"规则条件详情信息中逻辑运算符不允许为空");
            if(!(regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Empty.getCode())||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotEmpty.getCode()))){
                Assert.hasLength(regulationDetailsModel.getConditionType(),"规则条件详情信息中条件类型不允许为空");
                Assert.hasLength(regulationDetailsModel.getConditionDetail(),"规则条件详情信息中条件详情不允许为空");
                Assert.hasLength(regulationDetailsModel.getSerialNumber(),"规则条件详情信息中序号不允许为空");
                if(regulationDetailsModel.getConditionType().equalsIgnoreCase(InsCommonConstant.REGULATION_REGEX)){
                    try {
                        Pattern.compile(regulationDetailsModel.getConditionDetail());
                    }catch (Exception e){
                        Assert.isTrue(false,"规则条件详情信息中正则表达式不合法");
                    }
                }
                if(regulationDetailsModel.getConditionType().equalsIgnoreCase(InsCommonConstant.REGULATION_REGEX)){
                    Assert.isTrue(
                            regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Contain.getCode())
                    || regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotContain.getCode())
                    ||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Empty.getCode())
                    ||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotEmpty.getCode())
                    ,"规则条件详情信息中资源组类型只允许存在为空、不为空、包含、不包含");
                }
            }
        }

        if(ObjectUtils.isNotEmpty(regulationInfoModel.getRegulationPerformAction())){
            Set<String> set = new HashSet<>();
            for(InsRegulationDetailsModel regulationDetailsModel: regulationInfoModel.getRegulationPerformAction()){
                Assert.hasLength(regulationDetailsModel.getFieldName(),"规则执行条件详情信息中字段不允许为空");
                Assert.isTrue(set.add(regulationDetailsModel.getFieldName()),"规则执行条件详情信息中字段内容不允许重复");
                Assert.hasLength(regulationDetailsModel.getVariableValue(),"规则执行条件详情信息中变量值不允许为空");
                Assert.hasLength(regulationDetailsModel.getLogicalOperator(),"规则执行条件详情信息中逻辑运算符不允许为空");
                Assert.hasLength(regulationDetailsModel.getConditionType(),"规则执行条件详情信息中条件类型不允许为空");
                Assert.hasLength(regulationDetailsModel.getConditionDetail(),"规则执行条件详情信息中条件详情不允许为空");
                Assert.hasLength(regulationDetailsModel.getSerialNumber(),"规则执行条件详情信息中序号不允许为空");
            }
        }
    }


    private void checkValidateRegulationConditionParameter(InsValidateRuleInfoModel validateRuleInfoModel){
        Assert.notNull(validateRuleInfoModel.getStartTime(),"开始时间不允许为空");
        Assert.notNull(validateRuleInfoModel.getEndTime(),"结束时间不允许为空");
        Assert.hasLength(validateRuleInfoModel.getContentType(),"内容类型不允许为空");
        Assert.hasLength(validateRuleInfoModel.getMatchingRule(),"匹配规则不允许为空");
        Assert.notEmpty(validateRuleInfoModel.getChannel(),"渠道不允许为空");
        Assert.notEmpty(validateRuleInfoModel.getAttrs(),"渠道不允许为空");
        List<RegulationDetailsVo> attrs = validateRuleInfoModel.getAttrs();
        for(RegulationDetailsVo regulationDetailsModel:attrs){
            Assert.hasLength(regulationDetailsModel.getFieldName(),"规则条件详情信息中字段不允许为空");
            Assert.hasLength(regulationDetailsModel.getVariableValue(),"规则条件详情信息中变量值不允许为空");
            Assert.hasLength(regulationDetailsModel.getLogicalOperator(),"规则条件详情信息中逻辑运算符不允许为空");
            if(!(regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Empty.getCode())||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotEmpty.getCode()))){
                Assert.hasLength(regulationDetailsModel.getConditionType(),"规则条件详情信息中条件类型不允许为空");
                Assert.hasLength(regulationDetailsModel.getConditionDetail(),"规则条件详情信息中条件详情不允许为空");
                Assert.hasLength(regulationDetailsModel.getSerialNumber(),"规则条件详情信息中序号不允许为空");
                if(regulationDetailsModel.getConditionType().equalsIgnoreCase(InsCommonConstant.REGULATION_REGEX)){
                    try {
                        Pattern.compile(regulationDetailsModel.getConditionDetail());
                    }catch (Exception e){
                        Assert.isTrue(false,"规则条件详情信息中正则表达式不合法");
                    }
                }
                if(regulationDetailsModel.getConditionType().equalsIgnoreCase(InsCommonConstant.REGULATION_REGEX)){
                    Assert.isTrue(
                            regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Contain.getCode())
                                    || regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotContain.getCode())
                                    ||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.Empty.getCode())
                                    ||regulationDetailsModel.getLogicalOperator().equalsIgnoreCase(RuleLogicalOperator.NotEmpty.getCode())
                            ,"规则条件详情信息中资源组类型只允许存在为空、不为空、包含、不包含");
                }
            }
        }

    }


    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/1 15:42
     * @描述   数据组装
     * @param regulationConditionOrPerformAction
     * @param detailType
     * @param regulationId
     * @param username
     * @param now
     * @return java.util.List<com.voc.service.insights.engine.data.entity.InsRegulationDetailEntity>
     **/
    private List<InsRegulationDetailEntity> dataAssembly(List<InsRegulationDetailsModel> regulationConditionOrPerformAction,String detailType,String regulationId,String username,LocalDateTime now){
        if(ObjectUtils.isEmpty(regulationConditionOrPerformAction)){
            regulationConditionOrPerformAction = CollUtil.newArrayList();
        }
        List<InsRegulationDetailEntity> insRegulationDetailEntities = convertMapperService.regulationDetailsModelListConvertEntityList(regulationConditionOrPerformAction);
        insRegulationDetailEntities.stream().forEach(e->{
            e.setId(IdWorker.getId());
            e.setRegulationId(regulationId);
            e.setCreateTime(now);
            e.setCreateUser(username);
            e.setDetailType(detailType);
        });
        return insRegulationDetailEntities;
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/1 15:42
     * @描述   数据重组
     * @param regulationDetails
     * @param regulationConditionOrPerformAction
     * @param regulationId
     * @param username
     * @return void
     **/
    private List<InsRegulationDetailEntity> dataReconstitution(List<InsRegulationDetailEntity> regulationDetails,List<InsRegulationDetailsModel> regulationConditionOrPerformAction,String regulationId,String username){
        List<InsRegulationDetailEntity> insRegulationDetailEntities = CollUtil.newArrayList();
        if(ObjectUtils.isNotEmpty(regulationConditionOrPerformAction)){
            insRegulationDetailEntities = convertMapperService.regulationDetailsModelListConvertEntityList(regulationConditionOrPerformAction);
        }
        //新增的规则详情
        List<InsRegulationDetailEntity> newDetails = insRegulationDetailEntities.stream().filter(e -> ObjectUtils.isEmpty(e.getId())).collect(Collectors.toList());
        //已存在的规则详情
        List<InsRegulationDetailEntity> oldDetails = insRegulationDetailEntities.stream().filter(e -> ObjectUtils.isNotEmpty(e.getId())).collect(Collectors.toList());

        if(ObjectUtils.isNotEmpty(newDetails)){
            newDetails.stream().forEach(e->{
                e.setId(IdWorker.getId());
                e.setRegulationId(regulationId);
                e.setCreateTime(LocalDateTime.now());
                e.setCreateUser(username);
            });
        }

        regulationDetails.stream().forEach(e->{
            InsRegulationDetailEntity regulationDetail = oldDetails.stream().filter(k -> ObjectUtils.isNotEmpty(k.getId())).filter(k -> k.getId().equalsIgnoreCase(e.getId())).findFirst().orElse(null);
            if(ObjectUtils.isEmpty(regulationDetail)){
                //删除规则
                e.setDelFlag(1);
//                e.setStatus(0);
            }else {
                BeanUtils.copyProperties(regulationDetail,e);
            }
        });
        regulationDetails.addAll(newDetails);
        return regulationDetails;
    }

    private String getStartValidateKey(String... params){
        return StrUtil.format(START_VALIDATE_KEY, ServiceContextHolder.getSystemId(),params);
    }
    private String getStartTestKey(String... params){
        return StrUtil.format(START_TEST_KEY, ServiceContextHolder.getSystemId(),params);
    }
}
