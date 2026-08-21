package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsLabelCorrectionRecordService;
import com.voc.service.insights.engine.api.ILabelCorrectionInfoService;
import com.voc.service.insights.engine.api.model.InsCqCaLabelCorrectionRecordModel;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.entity.AysPostprocessDataEntity;
import com.voc.service.insights.engine.entity.InsLabelCorrectionRecordEntity;
import com.voc.service.insights.engine.mapper.AysLabelPostprocessDataMapper;
import com.voc.service.insights.engine.mapper.InsLabelCorrectionRecordMapper;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.InsCqCaUpdateLabelRecordModel;
import com.voc.service.insights.engine.model.model.InsLabelCorrectionInfoModel;
import com.voc.service.insights.engine.vo.CorrectionInfo;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionGroupDataVo;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionInfoVo;
import com.voc.service.insights.engine.vo.InsCqCaLabelCorrectionRecordPageVo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class InsLabelCorrectionRecordServiceImpl extends ServiceImpl<InsLabelCorrectionRecordMapper, InsLabelCorrectionRecordEntity> implements IInsLabelCorrectionRecordService {

    private static final Logger log = LoggerFactory.getLogger(InsLabelCorrectionRecordServiceImpl.class);

    @Autowired
    InsPostprocessDataImpl insPostprocessDataImpl;

    @Autowired
    private AysLabelPostprocessDataMapper aysPostprocessDataMapper;

    @Autowired
    private ILabelCorrectionInfoService iLabelCorrectionInfoService;


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public PageInfo queryLabelCorrectionList(InsCqCaLabelCorrectionRecordQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsLabelCorrectionRecordEntity> labelCorrectionList = this.baseMapper.queryLabelCorrectionList(model);
        PageInfo page = new PageInfo<>(labelCorrectionList);
        if (CollectionUtil.isEmpty(labelCorrectionList)) {
            return page;
        }
        List<InsCqCaLabelCorrectionRecordPageVo> pageVoList = this.convertPageVo(labelCorrectionList);
        page.setList(pageVoList);
        return page;
    }

    private List<InsCqCaLabelCorrectionRecordPageVo> convertPageVo(List<InsLabelCorrectionRecordEntity> labelCorrectionList) {
        List<InsCqCaLabelCorrectionRecordPageVo> cqCaLabelCorrectionRecordPageVos = new ArrayList<>();
        for (InsLabelCorrectionRecordEntity entity : labelCorrectionList) {
            InsCqCaLabelCorrectionRecordPageVo pageVo = InsCqCaLabelCorrectionRecordPageVo.builder()
                    .id(entity.getId())
                    .correctionInfo(entity.getCorrectionTime())
                    .correctionCount(entity.getCorrectionCount())
                    .createUser(entity.getOperateUser())
                    .createTime(entity.getOperateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .auditTime(entity.getAuditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .auditUser(entity.getAuditUser())
                    .auditStatusCode(entity.getAuditStatus())
                    .auditStatus(entity.getAuditStatusText()).build();
            cqCaLabelCorrectionRecordPageVos.add(pageVo);
        }
        return cqCaLabelCorrectionRecordPageVos;
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean insertLabelCorrection(InsCqCaLabelCorrectionRecordModel model) {
        List<String> newIdList = model.getNewId();
        log.info("数据纠错条数: {}", newIdList.size());
        String id = IdWorker.getId();
        String startTime = model.getStartTime().replace("-", "");
        String endTime = model.getEndTime().replace("-", "");
        int insert = this.baseMapper.insert(InsLabelCorrectionRecordEntity.builder()
                .id(id)
                .errorType(model.getErrorType().toString())
                .correctionCount(newIdList.size() + "")
                .correctionTime("数据纠错" + startTime
                        + "-" + endTime)
                .correctionInfo(JSONUtil.toJsonStr(getNonEmptyFieldsAsMap(model)))
                .auditStatus("0")
                .operateUser(ServiceContextHolder.getUser().getFirstname())
                .operateUserId(ServiceContextHolder.getUsername())
                .operateTime(LocalDateTime.now()).build()
        );
        if (insert > 0) {
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    Boolean b = iLabelCorrectionInfoService.batchInsert(id, newIdList);
                    log.info("数据纠错新增批量插入数据: {}", b);
                } catch (Exception e) {
                    log.error("数据纠错新增批量插入数据失败", e);
                }
            });
        }
        return Boolean.TRUE;
    }

    public Map<String, String> getNonEmptyFieldsAsMap(InsCqCaLabelCorrectionRecordModel model) {
        return Stream.of(
                        new Object[][]{
                                {"brandCode", model.getBrandCode()},
                                {"brandName", model.getBrandName()},
                                {"carSeriesCode", model.getCarSeriesCode()},
                                {"carSeriesName", model.getCarSeriesName()},
                                {"topicCode", model.getTopicCode()},
                                {"topicName", model.getTopicName()},
                                {"sentiment", model.getSentiment()},
                                {"intention", model.getIntention()},
                                {"usageScenarioFirst", model.getUsageScenarioFirst()},
                                {"usageScenarioSecond", model.getUsageScenarioSecond()}
                        })
                .filter(entry -> entry[1] != null && !((String) entry[1]).isEmpty())
                .collect(Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (String) entry[1]
                ));
    }

    @Override
    public PageInfo queryDataInfo(InsCqCaLabelCorrectionRecordQueryModel model) {
        return insPostprocessDataImpl.queryAnalysisData(model);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public InsCqCaCorrectionInfoVo queryCorrectionInfo(InsCqCaLabelCorrectionRecordQueryModel model) {
        QueryWrapper<InsLabelCorrectionRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", model.getId());
        InsLabelCorrectionRecordEntity entity = this.baseMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(entity)) {
            return new InsCqCaCorrectionInfoVo();
        }
        List<CorrectionInfo> comparisonList;
        if (StringUtils.isNotBlank(entity.getCorrectionData())) {
            comparisonList = JSONUtil.toList(entity.getCorrectionData(), CorrectionInfo.class);
        } else {
            // 在 queryCorrectionInfo 方法中替换原有代码
            List<InsCqCaCorrectionGroupDataVo> insCqCaCorrectionGroupDataVos = insPostprocessDataImpl.queryGroupData(model);
            InsCqCaLabelCorrectionRecordModel updatedModel = JSONUtil.toBean(entity.getCorrectionInfo(), InsCqCaLabelCorrectionRecordModel.class);

            /// 获取修改前的值 - 根据 fieldName 进行分组处理
            Map<String, List<InsCqCaCorrectionGroupDataVo>> fieldGroupMap = insCqCaCorrectionGroupDataVos.stream()
                    .collect(Collectors.groupingBy(InsCqCaCorrectionGroupDataVo::getFieldName));

            String beforeBrandCode = getDisplayValueByFieldName(fieldGroupMap, "brand_code");
            String beforeCarSeriesCode = getDisplayValueByFieldName(fieldGroupMap, "car_series_code");
            String beforeTopicCode = getDisplayValueByFieldName(fieldGroupMap, "topic_text");
            String beforeSentiment = getDisplayValueByFieldName(fieldGroupMap, "sentiment");
            String beforeIntention = getDisplayValueByFieldName(fieldGroupMap, "intention");

            // 获取修改后的值（从 list 中获取第一个元素）
            String afterBrandCode = "";
            String afterCarSeriesCode = "";
            String afterTopicCode = "";
            String afterSentiment = "";
            String afterIntention = "";

            if (ObjectUtils.isNotEmpty(updatedModel)) {
                afterBrandCode = updatedModel.getBrandName();
                afterCarSeriesCode = updatedModel.getCarSeriesName();
                afterTopicCode = updatedModel.getTopicName();
                afterSentiment = updatedModel.getSentiment();
                afterIntention = updatedModel.getIntention();
            }

            comparisonList = Stream.of(
                            new Object[][]{
                                    {"品牌", beforeBrandCode, afterBrandCode},
                                    {"车系", beforeCarSeriesCode, afterCarSeriesCode},
                                    {"情感", beforeSentiment, afterSentiment},
                                    {"意图", beforeIntention, afterIntention},
                                    {"观点", beforeTopicCode, afterTopicCode},

                            })
                    .map(arr -> createCorrectionInfo((String) arr[0], (String) arr[1], (String) arr[2]))
                    .collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(comparisonList)) {
                entity.setCorrectionData(JSONUtil.toJsonStr(comparisonList));
            }
            this.baseMapper.updateById(entity);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return InsCqCaCorrectionInfoVo.builder()
                .correctionInfoList(comparisonList)
                .errorType(entity.getErrorType())
                .submitUserName(entity.getOperateUser())
                .submitUserEmployeeID(entity.getOperateUserId())
                .submitTime(entity.getOperateTime().format(formatter))
                .auditUserName(entity.getAuditUser())
                .auditUserEmployeeID(entity.getAuditUserId())
                .auditTime(entity.getAuditTime().format(formatter)).build();
    }

    /**
     * 根据字段名获取显示值
     *
     * @param fieldGroupMap 按字段名分组的数据映射
     * @param fieldName     字段名
     * @return 显示值
     */
    private String getDisplayValueByFieldName(Map<String, List<InsCqCaCorrectionGroupDataVo>> fieldGroupMap, String fieldName) {
        List<InsCqCaCorrectionGroupDataVo> dataList = fieldGroupMap.getOrDefault(fieldName, Collections.emptyList());
        if (CollectionUtil.isEmpty(dataList)) {
            return "";
        }

        // 获取所有非空值并去重
        Set<String> distinctValues = dataList.stream()
                .map(InsCqCaCorrectionGroupDataVo::getGroupValue) // 假设有一个 getValue() 方法获取字段值
                .filter(Objects::nonNull)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());

        // 如果没有值或只有一个唯一值，返回该值；如果有多个不同值，返回 "*"
        if (distinctValues.isEmpty()) {
            return "";
        } else if (distinctValues.size() == 1) {
            return distinctValues.iterator().next();
        } else {
            return "*";
        }
    }

    /**
     * 创建对比数据Map
     *
     * @param type   类型名称
     * @param before 修改前的值
     * @param after  修改后的值
     * @return 对比数据Map
     */
    private CorrectionInfo createCorrectionInfo(String type, String before, String after) {
        CorrectionInfo info = new CorrectionInfo();
        info.setField(type);
        info.setDataValueBefore(before);
        info.setDataValueAfter(after);
        return info;
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<String> queryCreateUserList(InsCqCaLabelCorrectionRecordQueryModel model) {
        List<InsLabelCorrectionRecordEntity> labelCorrectionList = this.baseMapper.queryLabelCorrectionList(model);
        if (CollectionUtil.isEmpty(labelCorrectionList)) {
            return null;
        }
        return labelCorrectionList.stream().map(InsLabelCorrectionRecordEntity::getOperateUser).distinct().collect(Collectors.toList());
    }


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean auditLabelCorrection(InsCqCaUpdateLabelRecordModel model) {
        QueryWrapper<InsLabelCorrectionRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", model.getIdList());
        List<InsLabelCorrectionRecordEntity> entityList = this.baseMapper.selectList(queryWrapper);
        if (ObjectUtils.isEmpty(entityList)) {
            return true;
        }
        List<InsLabelCorrectionRecordEntity> resultData = new ArrayList<>();
        if (model.getAuditStatus().equals("1")) {
            resultData = insPostprocessDataImpl.updateAnalysisData(entityList);
        }
        if (model.getAuditStatus().equals("1") && CollUtil.isEmpty(resultData)) {
            return false;
        }else{
            resultData = entityList;
        }

        for (InsLabelCorrectionRecordEntity entity : resultData) {
            entity.setAuditStatus(model.getAuditStatus());
            entity.setAuditUser(ServiceContextHolder.getUser().getFirstname());
            entity.setAuditUserId(ServiceContextHolder.getUsername());
            entity.setAuditTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        return this.updateBatchById(entityList);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean del(InsCqCaLabelCorrectionRecordQueryModel model) {
        int i = this.baseMapper.deleteById(model.getId());
        if (i == 1) {
            return iLabelCorrectionInfoService.del(model.getId());
        }
        return false;
    }
}
