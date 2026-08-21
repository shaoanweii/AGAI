package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysBatchUpdateService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.entity.AysBatchUpdateEntity;
import com.voc.service.analysis.core.v2.mapper.AysBatchUpdateMapper;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.ModifyDataModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AysBatchUpdateServiceImpl
        extends ServiceImpl<AysBatchUpdateMapper, AysBatchUpdateEntity>
        implements IAysBatchUpdateService {
    private static final Logger log = LoggerFactory.getLogger(AysBatchUpdateServiceImpl.class);
    @Resource
    private FlowExecutor flowExecutor;

    @Autowired
    AnalysisConfig config;


    @XxlJob("modify_result_data")
    public void modifyResultdata() throws Exception {
        this.modifyResultdata(1);
    }

    @XxlJob("modify_result_data_d")
    public void modifyResultdataD() throws Exception {
        this.modifyResultdata(2);
    }

    public void modifyResultdata(final int type) throws Exception {
        Assert.isTrue(Arrays.asList(1, 2).contains(type), "【修改结果表功能】参数错误");
        //1、实现读取数据库中未完成的数据范围
        QueryWrapper<AysBatchUpdateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AysBatchUpdateEntity::getStatus, 0);
        queryWrapper.lambda().eq(AysBatchUpdateEntity::getType, type);
        queryWrapper.lambda().orderByAsc(AysBatchUpdateEntity::getCreateTime);
        queryWrapper.last("limit ".concat(String.valueOf(config.getModifyResultDataRowsSize())));
        log.info("【修改结果表功能】开始处理数据，数据处理条数：{}", config.getModifyResultDataRowsSize());
        List<AysBatchUpdateEntity> list = this.baseMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            log.info("【修改结果表功能】没有需要处理的数据");
            return;
        }
        log.info("【修改结果表功能】开始处理数据，数据范围：{}", list.size());
        //2、根据数据范围实现实体表的批量更新
        Set<String> ids = new HashSet<>();
        for (AysBatchUpdateEntity item : list) {
            try {
                final List<JSONObject> modifyAttrsJson = JSONUtil.toList(JSONUtil.parseArray(item.getAttrs()), JSONObject.class);
                List<ModifyDataModel.ModifyAttrs> modifyAttrs = modifyAttrsJson.stream()
                        .map(obj -> JSONUtil.toBean(obj, ModifyDataModel.ModifyAttrs.class))
                        .collect(Collectors.toList());

                List<ModifyDataModel.ModifyAttrs> filters = List.of();
                if (2 == type && ObjectUtil.isNotNull(item.getFilters())) {
                    final List<JSONObject> filterJson = JSONUtil.toList(JSONUtil.parseArray(item.getFilters()), JSONObject.class);
                    filters = filterJson.stream()
                            .map(obj -> JSONUtil.toBean(obj, ModifyDataModel.ModifyAttrs.class))
                            .collect(Collectors.toList());
                }


                this.batchUpdateResultData(StrUtil.split(item.getIds(), ","), filters, modifyAttrs);
                ids.add(item.getId());
                Thread.sleep(500);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        }

        try {
            if (CollUtil.isNotEmpty(ids)) {
                //3、修改处理数据的状态
                this.baseMapper.updateStatusToDone(new ArrayList<>(ids));
                log.info("【修改结果表功能】批量更新状态成功，更新记录数：{}", ids.size());
            } else {
                log.warn("【修改结果表功能】没有需要处理的数据");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 实现从临时表读取数据，写入到最终表功能
     *
     * @throws Exception
     */
    @XxlJob("move_result_data_to_final_table")
    public void moveResultDataToFinalTable() throws Exception {
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
        context.setWorkId(DigestUtil.md5Hex(IdWorker.getId()));

        try {
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");
            LiteflowResponse response = flowExecutor.execute2Resp("move_result_data_to_final_table", context, context.getWorkId());
            if (!response.isSuccess()) {
                log.error("workId:{}{}", context.getWorkId(), response.getCause());
                throw new Exception(response.getCause().getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
            stopWatch.stop();
            log.info("move_result_data_to_final_table 处理总耗时：{} -> workId:{}"
                    , stopWatch.prettyPrint(java.util.concurrent.TimeUnit.MILLISECONDS), context.getWorkId());
        }
    }

    @Override
    @SwitchClientDS
    public void save(ModifyDataModel model) throws Exception {
        try {
            AysBatchUpdateEntity entity = AysBatchUpdateEntity.builder()
                    .id(DigestUtil.md5Hex(IdWorker.getId()))
                    .build();
            BeanUtil.copyProperties(model, entity, CopyOptions.create().setIgnoreProperties("ids", "attrs"));

            entity.setIds(CollUtil.join(model.getIds(), ","));
            entity.setAttrs(JSONUtil.toJsonStr(model.getAttrs(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false)));
            entity.setFilters(JSONUtil.toJsonStr(model.getFilters()));
            this.save(entity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    private Boolean batchUpdateResultData(List<String> ids, List<ModifyDataModel.ModifyAttrs> filters, List<ModifyDataModel.ModifyAttrs> modifyAttrs) throws Exception {
        if (CollUtil.isEmpty(ids) & CollUtil.isEmpty(modifyAttrs)) {
            throw new Exception("【修改结果表功能】参数错误");
        }

        try {
            Map<String, String> updateValues = MapUtil.newHashMap();
            modifyAttrs.forEach(attr -> {
                updateValues.put(attr.getField(), attr.getValue());
            });
            Map<String, String> filterValues = MapUtil.newHashMap();
            filters.forEach(attr -> {
                filterValues.put(attr.getField(), attr.getValue());
            });

            this.baseMapper.batchUpdateResultData(ids, filterValues, updateValues);
            return true;
        } catch (Exception e) {
            log.error("【修改结果表功能】异常：", e);
            throw new RuntimeException(e);
        }
    }
}
