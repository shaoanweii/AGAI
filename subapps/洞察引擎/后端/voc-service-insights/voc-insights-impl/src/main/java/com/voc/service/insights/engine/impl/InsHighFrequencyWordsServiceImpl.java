package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.IInsHighFrequencyWordsService;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.constants.AllocationStatusEnum;
import com.voc.service.insights.engine.dao.InsTagLibClientDao;
import com.voc.service.insights.engine.entity.InsAllocationWordsEntity;
import com.voc.service.insights.engine.entity.InsHighFrequencyWordsEntity;
import com.voc.service.insights.engine.enums.TagAttribute;
import com.voc.service.insights.engine.mapper.InsAllocationWordsMapper;
import com.voc.service.insights.engine.mapper.InsHighFrequencyWordsMapper;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.TagLibClientVo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class InsHighFrequencyWordsServiceImpl extends ServiceImpl<InsHighFrequencyWordsMapper, InsHighFrequencyWordsEntity> implements IInsHighFrequencyWordsService {

    private static final Logger log = LoggerFactory.getLogger(InsHighFrequencyWordsServiceImpl.class);
    @Resource
    private InsHighFrequencyWordsMapper insHighFrequencyWordsMapper;

    @Resource
    private IInsTagLibClientService iInsTagLibClientService;

    @Resource
    private InsTagLibClientDao tagLibClientDao;

    @Resource
    private IInsChannelInfoService iInsChannelInfoService;

    @Resource
    private InsAllocationWordsMapper insAllocationWordsMapper;

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public PageInfo queryWordsList(InsBaseHighFrequencyQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsWordsListModel> entityList = insHighFrequencyWordsMapper.queryPageHighFrequencyWordsList(model);
        PageInfo page = new PageInfo<>(entityList);
        if (CollectionUtil.isEmpty(entityList)) {
            return page;
        }
        List<InsHighFrequencyWordsModel> insHighFrequencyWordsModelList = this.assemblyData(entityList);
        page.setList(insHighFrequencyWordsModelList);
        return page;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public InsWordsInfoModel queryWordsInfo(InsBaseHighFrequencyQueryModel model) {
        InsWordsInfoModel insWordsInfoModel = new InsWordsInfoModel();
        InsHighFrequencyWordsEntity entity = this.getById(model.getId());
        if (ObjectUtils.isEmpty(entity)) {
            return insWordsInfoModel;
        }
        List<InsAllocationRecordModel> insAllocationRecordModels = insHighFrequencyWordsMapper.queryAllocationRecord(entity.getId());
        Long historyTotalFrequency = insHighFrequencyWordsMapper.queryHistoryTotalFrequency(entity.getWordName(), entity.getClientId(), entity.getChannelSource());
        insWordsInfoModel = this.getWordsInfo(entity, insAllocationRecordModels);
        insWordsInfoModel.setHistoryTotalFrequency(String.valueOf(historyTotalFrequency));
        return insWordsInfoModel;
    }

    @SwitchClientDS(objectAttribute = "wordsModel.clientId")
    @Override
    public Boolean addHighFrequencyWords(AddHighFrequencyWordsModel wordsModel) {
        if (CollectionUtil.isEmpty(wordsModel.getAddWordsInfoModelList())) {
            return Boolean.TRUE;
        }
        List<AddWordsInfoModel> modelList = wordsModel.getAddWordsInfoModelList();
        List<InsHighFrequencyWordsEntity> insHighFrequencyWordsEntities = new ArrayList<>();
        for (AddWordsInfoModel model : modelList) {
            InsHighFrequencyWordsEntity entity = new InsHighFrequencyWordsEntity();
            LocalDate now = LocalDate.now();
            entity.setWordName(model.getWordName());
            entity.setClientId(wordsModel.getClientId());
            entity.setChannelSource(model.getChannelSource());
            entity.setSystemSuggestedBusiness(StringUtils.join(model.getSystemSuggestedBusiness(), ","));
            entity.setSystemSuggestedQuality(StringUtils.join(model.getSystemSuggestedQuality(), ","));
            entity.setCurrentFrequency(1L);
            entity.setAllocationStatus(0);
            entity.setCreateTime(now);
            entity.setUpdateTime(LocalDateTime.now());
            String id = DigestUtil.md5Hex(now + wordsModel.getClientId() + model.getWordName() + model.getChannelSource());
            entity.setId(id);
            insHighFrequencyWordsEntities.add(entity);
        }
        return insHighFrequencyWordsMapper.saveOrUpdateBatch(insHighFrequencyWordsEntities);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public Result<?> allocationWords(InsBaseTagInfoModel model) {

        InsHighFrequencyWordsEntity entity = insHighFrequencyWordsMapper.selectById(model.getId());
        if (ObjectUtils.isEmpty(entity)) {
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

            InsAllocationWordsEntity insAllocationWordsEntity = new InsAllocationWordsEntity();
            insAllocationWordsEntity.setWordsId(entity.getId());
            insAllocationWordsEntity.setTagId(entity.getTagId());
            insAllocationWordsEntity.setTagType(entity.getTagType());
            insAllocationWordsEntity.setTagCategory(entity.getTagCategory());
            insAllocationWordsEntity.setCreateTime(LocalDateTime.now());
            insAllocationWordsMapper.insert(insAllocationWordsEntity);
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
    private InsWordsInfoModel getWordsInfo(InsHighFrequencyWordsEntity entity, List<InsAllocationRecordModel> insAllocationRecordModels) {
        InsWordsInfoModel insWordsInfoModel = new InsWordsInfoModel();
        insWordsInfoModel.setWordName(entity.getWordName());
        insWordsInfoModel.setId(entity.getId());
        insWordsInfoModel.setCurrentFrequency(entity.getCurrentFrequency().toString());
        if (StringUtils.isNotBlank(entity.getSystemSuggestedBusiness())) {
            String[] split = entity.getSystemSuggestedBusiness().split(",");
            insWordsInfoModel.setSystemSuggestedBusiness(Arrays.asList(split));
        }
        if (StringUtils.isNotBlank(entity.getSystemSuggestedQuality())) {
            String[] split = entity.getSystemSuggestedQuality().split(",");
            insWordsInfoModel.setSystemSuggestedBusiness(Arrays.asList(split));
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
                insWordsInfoModel.setChannelSource(stringBuilder.substring(0, stringBuilder.length() - 1));
            }
        }
        if (entity.getAllocationStatus() == 0) {
            return insWordsInfoModel;
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
        insWordsInfoModel.setAllocationRecord(insAllocationRecordModels);
        InsBaseTagInfoModel insBaseTagInfoModel = new InsBaseTagInfoModel();
        List<TagLibClientVo> insTagInfoModelList = getQueryTagInfo(Collections.singletonList(entity.getTagId()), entity.getClientId());
        if (CollectionUtil.isNotEmpty(insTagInfoModelList)) {
            TagLibClientVo insTagInfoModel = insTagInfoModelList.get(0);
            BeanUtil.copyProperties(insTagInfoModel, insBaseTagInfoModel);
            insWordsInfoModel.setInsBaseTagInfoModel(insBaseTagInfoModel);
        }
        return insWordsInfoModel;
    }

    /**
     * 组装标签 渠道数据
     *
     * @param insWordsInfoModels
     * @return
     */
    private List<InsHighFrequencyWordsModel> assemblyData(List<InsWordsListModel> insWordsInfoModels) {
        List<InsHighFrequencyWordsModel> insHighFrequencyWordsModelList = new ArrayList<>();
        for (InsWordsListModel insWordsListModel : insWordsInfoModels) {
            InsHighFrequencyWordsModel insHighFrequencyWordsModel = new InsHighFrequencyWordsModel();
            insHighFrequencyWordsModel.setWordName(insWordsListModel.getWordName());
            insHighFrequencyWordsModel.setCurrentFrequency(insWordsListModel.getCurrentFrequency());
            insHighFrequencyWordsModel.setRatio(insWordsListModel.getSeq());
            insHighFrequencyWordsModel.setTagType(insWordsListModel.getTagType());
            insHighFrequencyWordsModel.setAllocationStatus(AllocationStatusEnum.getByCode(insWordsListModel.getAllocationStatus()).getName());
            insHighFrequencyWordsModel.setId(insWordsListModel.getId());
            if (insWordsListModel.getAllocationStatus().equals("1")) {
                String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(insWordsListModel.getTagId());
                String[] split = tagLibNameHierarchical.split("#");
                StringBuilder buffer = new StringBuilder();
                for (int i = split.length - 1; i >= 0; i--) {
                    buffer.append(split[i]);
                    if (i > 0) {
                        buffer.append("#");
                    }
                }
                insHighFrequencyWordsModel.setTagCategoryName(buffer.toString());
            }
            insHighFrequencyWordsModelList.add(insHighFrequencyWordsModel);
        }
        return insHighFrequencyWordsModelList;
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
