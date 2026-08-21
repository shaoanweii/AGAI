package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.IInsHighFrequencyOpinionsService;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.constants.AllocationStatusEnum;
import com.voc.service.insights.engine.dao.InsTagLibClientDao;
import com.voc.service.insights.engine.entity.InsAllocationOpinionsEntity;
import com.voc.service.insights.engine.entity.InsHighFrequencyOpinionsEntity;
import com.voc.service.insights.engine.enums.TagAttribute;
import com.voc.service.insights.engine.mapper.InsAllocationOpinionsMapper;
import com.voc.service.insights.engine.mapper.InsHighFrequencyOpinionsMapper;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.TagLibClientVo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class InsHighFrequencyOpinionsServiceImpl extends ServiceImpl<InsHighFrequencyOpinionsMapper, InsHighFrequencyOpinionsEntity> implements IInsHighFrequencyOpinionsService {
    private static final Logger log = LoggerFactory.getLogger(InsHighFrequencyOpinionsServiceImpl.class);

    @Resource
    private InsHighFrequencyOpinionsMapper insHighFrequencyOpinionsMapper;

    @Resource
    private IInsTagLibClientService iInsTagLibClientService;

    @Resource
    private InsTagLibClientDao tagLibClientDao;

    @Resource
    private IInsChannelInfoService iInsChannelInfoService;

    @Resource
    private InsAllocationOpinionsMapper insAllocationOpinionsMapper;

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public PageInfo queryOpinionsList(InsBaseHighFrequencyQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsOpinionsListModel> opinionsListModels = insHighFrequencyOpinionsMapper.queryPageHighFrequencyOpinionsList(model);
        PageInfo page = new PageInfo<>(opinionsListModels);
        if (CollectionUtil.isEmpty(opinionsListModels)) {
            return page;
        }
        List<InsHighFrequencyOpinionsModel> insHighFrequencyOpinionsModels = this.assemblyData(opinionsListModels);
        page.setList(insHighFrequencyOpinionsModels);
        return page;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public InsOpinionsInfoModel queryOpinionsInfo(InsBaseHighFrequencyQueryModel model) {
        InsOpinionsInfoModel insOpinionsInfoModel = new InsOpinionsInfoModel();
        InsHighFrequencyOpinionsEntity entity = this.getById(model.getId());
        if (ObjectUtils.isEmpty(entity)) {
            return insOpinionsInfoModel;
        }
        List<InsAllocationRecordModel> insAllocationRecordModels = insHighFrequencyOpinionsMapper.queryAllocationRecord(entity.getId());
        Long historyTotalFrequency = insHighFrequencyOpinionsMapper.queryHistoryTotalFrequency(entity.getNormalizedOpinions(), entity.getClientId(), entity.getChannelSource());
        insOpinionsInfoModel = this.getOpinionsInfo(entity, insAllocationRecordModels);
        insOpinionsInfoModel.setHistoryTotalFrequency(historyTotalFrequency);
        return insOpinionsInfoModel;
    }

    @SwitchClientDS(objectAttribute = "opinionModel.clientId")
    @Override
    public Boolean addHighFrequencyOpinion(AddHighFrequencyOpinionModel opinionModel) {
        if (CollectionUtil.isEmpty(opinionModel.getAddOpinionInfoModelList())) {
            return Boolean.TRUE;
        }
        List<OpinionInfoModel> addOpinionInfoModelList = opinionModel.getAddOpinionInfoModelList();
        List<InsHighFrequencyOpinionsEntity> insHighFrequencyOpinionsEntities = new ArrayList<>();
        for (OpinionInfoModel model : addOpinionInfoModelList) {
            InsHighFrequencyOpinionsEntity entity = new InsHighFrequencyOpinionsEntity();
            LocalDate now = LocalDate.now();
            entity.setNormalizedOpinions(model.getNormalizedOpinions());
            entity.setCorrespondingOpinions(StringUtils.join(model.getCorrespondingOpinions(), ","));
            entity.setCurrentFrequency(model.getCurrentFrequency());
            entity.setClientId(opinionModel.getClientId());
            entity.setChannelSource(StringUtils.join(model.getChannelSource(), ","));
            entity.setSystemSuggestedBusiness(StringUtils.join(model.getSystemSuggestedBusiness(), ","));
            entity.setSystemSuggestedQuality(StringUtils.join(model.getSystemSuggestedQuality(), ","));
            entity.setAllocationStatus(0);
            entity.setCreateTime(now);
            entity.setUpdateTime(LocalDateTime.now());
            String id = DigestUtil.md5Hex(now + opinionModel.getClientId() + model.getNormalizedOpinions() + model.getCorrespondingOpinions() + model.getChannelSource());
            entity.setId(id);
            insHighFrequencyOpinionsEntities.add(entity);
        }
        return insHighFrequencyOpinionsMapper.saveOrUpdateBatch(insHighFrequencyOpinionsEntities);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public Result<?> allocationOpinions(InsBaseTagInfoModel model) {

        InsHighFrequencyOpinionsEntity entity = this.getById(model.getId());
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(entity)) {
            return Result.error("ID查询不到数据");
        }
        String id;
        try {
            InsTagLibClientModel tagLibClientModel = new InsTagLibClientModel();
            BeanUtil.copyProperties(model, tagLibClientModel);
            tagLibClientModel.setId("");
            tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
            id = iInsTagLibClientService.saveTagLibClient(tagLibClientModel);
        } catch (Exception e) {
            log.error("调用标签新增错误:", e);
            if (e.toString().contains("标签已存在")) {
                return Result.error("标签已存在");
            }
            return Result.error(e.toString());
        }
        if (StringUtils.isNotBlank(id)) {
            entity.setTagId(id);
            entity.setTagType(model.getTagType());
            entity.setTagCategory(model.getTagParentId());
            entity.setAllocationStatus(Integer.valueOf(AllocationStatusEnum.ENABLED.getCode()));
            entity.setOperateTime(LocalDateTime.now());
            InsAllocationOpinionsEntity insAllocationOpinionsEntity = new InsAllocationOpinionsEntity();
            insAllocationOpinionsEntity.setOpinionsId(entity.getId());
            insAllocationOpinionsEntity.setTagId(entity.getTagId());
            insAllocationOpinionsEntity.setTagType(entity.getTagType());
            insAllocationOpinionsEntity.setTagCategory(entity.getTagCategory());
            insAllocationOpinionsEntity.setCreateTime(LocalDateTime.now());
            insAllocationOpinionsMapper.insert(insAllocationOpinionsEntity);
            return Result.OK(this.updateById(entity));
        }
        return Result.OK(Boolean.FALSE);
    }


    /**
     * 获取单条查询详情
     *
     * @param entity
     * @param insAllocationRecordModels
     * @return
     */
    private InsOpinionsInfoModel getOpinionsInfo(InsHighFrequencyOpinionsEntity entity, List<InsAllocationRecordModel> insAllocationRecordModels) {
        InsOpinionsInfoModel insOpinionsInfoModel = new InsOpinionsInfoModel();
        insOpinionsInfoModel.setNormalizedOpinions(entity.getNormalizedOpinions());
        insOpinionsInfoModel.setId(entity.getId());
        insOpinionsInfoModel.setCorrespondingOpinions(entity.getCorrespondingOpinions());
        insOpinionsInfoModel.setCurrentFrequency(entity.getCurrentFrequency().toString());
        if (StringUtils.isNotBlank(entity.getSystemSuggestedBusiness())) {
            insOpinionsInfoModel.setSystemSuggestedBusiness(Collections.singletonList(entity.getSystemSuggestedBusiness()));
        }
        if (StringUtils.isNotBlank(entity.getSystemSuggestedQuality())) {
            insOpinionsInfoModel.setSystemSuggestedQuality(Collections.singletonList(entity.getSystemSuggestedQuality()));
        }
        Map<String, ChannelInfoVo> channelInfoVoMap = null;
        try {
            List<ChannelInfoVo> allChannelInfo = iInsChannelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(entity.getClientId()).build());
            log.info("获取渠道集合:{}", allChannelInfo.size());
            channelInfoVoMap = allChannelInfo.stream().collect(Collectors.toMap(ChannelInfoVo::getId, Function.identity()));
        } catch (Exception e) {
            log.error("获取渠道集合异常:", e);
        }
        if (MapUtil.isNotEmpty(channelInfoVoMap)) {
            String[] split = entity.getChannelSource().split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : split) {
                if (channelInfoVoMap.containsKey(s)) {
                    stringBuilder.append(channelInfoVoMap.get(s).getName()).append(",");
                }
            }
            if (StringUtils.isNotBlank(stringBuilder)) {
                insOpinionsInfoModel.setChannelSource(stringBuilder.substring(0, stringBuilder.length() - 1));
            }
        }
        if (entity.getAllocationStatus() == 0) {
            return insOpinionsInfoModel;
        }
        if (CollectionUtil.isNotEmpty(insAllocationRecordModels)) {
            for (InsAllocationRecordModel insAllocationRecordModel : insAllocationRecordModels) {
                String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(insAllocationRecordModel.getTagId());
                String[] split = tagLibNameHierarchical.split("#");
                StringBuilder buffer = new StringBuilder();
                for (int i = split.length - 1; i >= 0; i--) {
                    buffer.append(split[i]);
                    if (i > 0) {
                        buffer.append("#");
                    }
                }
                insAllocationRecordModel.setTagCategoryName(buffer.toString());
            }
        }
        insOpinionsInfoModel.setAllocationRecord(insAllocationRecordModels);
        InsBaseTagInfoModel insBaseTagInfoModel = new InsBaseTagInfoModel();
        List<TagLibClientVo> insTagInfoModelList = getQueryTagInfo(Collections.singletonList(entity.getTagId()), entity.getClientId());
        if (CollectionUtil.isNotEmpty(insTagInfoModelList)) {
            TagLibClientVo insTagInfoModel = insTagInfoModelList.get(0);
            BeanUtil.copyProperties(insTagInfoModel, insBaseTagInfoModel);
            insOpinionsInfoModel.setInsBaseTagInfoModel(insBaseTagInfoModel);
        }
        return insOpinionsInfoModel;
    }

    /**
     * 组装标签 渠道数据
     *
     * @param opinionsListModels
     * @return
     */
    private List<InsHighFrequencyOpinionsModel> assemblyData(List<InsOpinionsListModel> opinionsListModels) {

        List<InsHighFrequencyOpinionsModel> insHighFrequencyOpinionsModels = new ArrayList<>();
        for (InsOpinionsListModel opinionsListModel : opinionsListModels) {
            InsHighFrequencyOpinionsModel insHighFrequencyOpinionsModel = new InsHighFrequencyOpinionsModel();
            insHighFrequencyOpinionsModel.setNormalizedOpinions(opinionsListModel.getNormalizedOpinions());
            insHighFrequencyOpinionsModel.setCorrespondingOpinions(opinionsListModel.getCorrespondingOpinions());
            insHighFrequencyOpinionsModel.setCurrentFrequency(opinionsListModel.getCurrentFrequency());
            insHighFrequencyOpinionsModel.setAllocationStatus(AllocationStatusEnum.getByCode(opinionsListModel.getAllocationStatus()).getName());
            insHighFrequencyOpinionsModel.setRatio(opinionsListModel.getSeq());
            insHighFrequencyOpinionsModel.setTagType(opinionsListModel.getTagType());
            insHighFrequencyOpinionsModel.setId(opinionsListModel.getId());
            if (opinionsListModel.getAllocationStatus().equals("1")) {
                String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(opinionsListModel.getTagId());
                String[] split = tagLibNameHierarchical.split("#");
                StringBuilder buffer = new StringBuilder();
                for (int i = split.length - 1; i >= 0; i--) {
                    buffer.append(split[i]);
                    if (i > 0) {
                        buffer.append("#");
                    }
                }
                insHighFrequencyOpinionsModel.setTagCategoryName(buffer.toString());
            }
            insHighFrequencyOpinionsModels.add(insHighFrequencyOpinionsModel);
        }
        return insHighFrequencyOpinionsModels;
    }

    /**
     * 调用标签服务
     *
     * @param tagIdList
     * @return
     */
    private List<TagLibClientVo> getQueryTagInfo(List<String> tagIdList, String clientId) {
        try {
            log.info("调用标签入参:{}", tagIdList);
            InsTagLibClientModel tagLibClientModel = new InsTagLibClientModel();
            tagLibClientModel.setAppClient(clientId);
            tagLibClientModel.setIds(tagIdList);
            tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
            List<TagLibClientVo> tagLibClientVoList = iInsTagLibClientService.findTagLibClientVoList(tagLibClientModel);
            log.info("调用标签返回:{}", tagLibClientVoList.size());
            return tagLibClientVoList;
        } catch (Exception e) {
            log.error("调用标签错误:", e);
            return new ArrayList<>();
        }
    }
}
