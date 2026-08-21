package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.api.IAysModelResltAnalysisValidService;
import com.voc.service.analysis.api.IAysPostprocessValidDataService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataAnalysisValidEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysModelResltAnalysisValidMapper;
import com.voc.service.analysis.model.AysModelResltDataAnalysisValidModel;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.model.AysValidResltDataModel;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysModelResltAnalysisValidServiceImpl extends ServiceImpl<AysModelResltAnalysisValidMapper, AysModelResltDataAnalysisValidEntity>
        implements IAysModelResltAnalysisValidService {
    private static final Logger logger = LoggerFactory.getLogger(AysModelResltAnalysisValidServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    IAysModelResltAnalysisService resltAnalysisService;
    @Autowired
    IAysPostprocessValidDataService finalValidDataService;
    @Autowired
    AnalysisConfig config;

    @SwitchClientDS
    @Override
    public AysModelResltDataAnalysisValidModel getClientIdByWorkId(String clientId, String workId) {
        Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
        UpdateWrapper<AysModelResltDataAnalysisValidEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().ge(AysModelResltDataAnalysisValidEntity::getWorkId, workId);
        wrapper.last("limit 1");

        AysModelResltDataAnalysisValidEntity entity = this.baseMapper.selectOne(wrapper);
        logger.info("getClientIdByWorkId {}", entity);
        return aysConvertMapperService.converToAysModelResltDataAnalysisValidModel2(entity);
    }

    @SwitchClientDS
    @Override
    public int modifyToDone(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        UpdateWrapper<AysModelResltDataAnalysisValidEntity> wrapper = new UpdateWrapper<>();
        wrapper.in("new_id", ids);
        wrapper.set("done", "1");
        return this.baseMapper.update(null, wrapper);
    }

    @SwitchClientDS
    @Override
    public List<AysModelResltDataAnalysisValidModel> readData(String clientId, AysValidDataModel param) {
        UpdateWrapper<AysModelResltDataAnalysisValidEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(AysModelResltDataAnalysisValidEntity::getWorkId, param.getWorkId());
        wrapper.last("limit 5");

        List<AysModelResltDataAnalysisValidEntity> list = this.baseMapper.selectList(wrapper);
        return aysConvertMapperService.converToAysModelResltDataAnalysisValidModel(list);
    }

    @Override
    public AysValidResltDataModel validDataCondition(AysValidDataModel param) {
        Assert.isTrue(ObjectUtil.isNotNull(param), "getValidResltDataParam cannot be empty");

        logger.info("param: {}", JSONUtil.toJsonStr(param, JSONConfig.create().setIgnoreNullValue(true)));
        /*//开始检验
        if (StrUtil.isEmpty(param.getStartTime()) && StrUtil.isEmpty(param.getEndTime())) {
            AysModelResltDataAnalysisValidEntity entity = this.baseMapper.validDataCondition();
            final String startTime = entity.getStartTime();
            final String endTime = entity.getEndTime();
            logger.info("startTime:{},endTime:{}", startTime, endTime);

            Map<String, String> attrs_def = finalValidDataService.getAttributes();
            return AysValidResltDataModel.builder().startTime(startTime).endTime(endTime).attrs(attrs_def).build();
        } else { //获取本次校验数据数量

        }*/

        Assert.isTrue(StrUtil.isNotBlank(param.getStartTime()), "getStartTime cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getEndTime()), "getEndTime cannot be empty");
        final long total = resltAnalysisService.dataCount(param);
        logger.info("条件范围数据量：{}", total);
        return AysValidResltDataModel.builder().dataCount(total).build();
    }

    @SwitchClientDS
    @Override
    public PageInfo find(String clientId, AysValidDataModel param, int size) {
        //此处设置分批次处理的规则计算的数据集大小
        param.setPageSize(size);

        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<AysModelResltDataAnalysisValidEntity> list = this.baseMapper.find(param);
        PageInfo page = new PageInfo(list);

        List<AysModelResltDataAnalysisValidModel> rs = new ArrayList<>();
        for (AysModelResltDataAnalysisValidEntity aysModelResltDataAnalysisValidEntity : list) {
            AysModelResltDataAnalysisValidModel aysModelResltDataAnalysisValidModel = new AysModelResltDataAnalysisValidModel();
            BeanUtil.copyProperties(aysModelResltDataAnalysisValidEntity, aysModelResltDataAnalysisValidModel);
            rs.add(aysModelResltDataAnalysisValidModel);
        }
        page.setList(rs);

        return page;
    }

    @SwitchClientDS
    @Override
    public long remove(String clientId, final String workId) {
        Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
        logger.info("remove.workId -> {} ", workId);
        QueryWrapper<AysModelResltDataAnalysisValidEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AysModelResltDataAnalysisValidEntity::getWorkId, workId);

        return this.baseMapper.delete(wrapper);
    }

    @SwitchClientDS
    @Override
    public String getOldWorkByWorkId(String clientId, String workId) {
        Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
        UpdateWrapper<AysModelResltDataAnalysisValidEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().ge(AysModelResltDataAnalysisValidEntity::getWorkId, workId);
        wrapper.last("limit 1");

        AysModelResltDataAnalysisValidEntity entity = this.baseMapper.selectOne(wrapper);
        logger.debug("getOldWorkByWorkId {}", entity);
        return entity.getOldWorkId();
    }

    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        return this.baseMapper.removeHistoryData(days);
    }

    @SwitchClientDS
    @Override
    public Set<String> findIincompleteData(String clientId) {
        return this.baseMapper.findIincompleteData();
    }

    @SwitchClientDS
    @Override
    public List<AysModelResltDataAnalysisValidModel> findByIds(String clientId, Set<String> ids) {
        List<AysModelResltDataAnalysisValidEntity> entityList = this.list(
                new QueryWrapper<AysModelResltDataAnalysisValidEntity>()
                        .in("new_id", ids)
        );

        return aysConvertMapperService.cenvertToAysModelResltDataAnalysisValidEntityList(entityList);
    }
}
