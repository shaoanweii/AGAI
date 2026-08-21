package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheInvalidateContainer;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.data.InsDataResourceDescService;
import com.voc.service.insights.engine.api.data.InsDataResourceService;import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsDataResourceDescDao;
import com.voc.service.insights.engine.data.entity.InsDataResourceDescEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataResourceDescConvertService;
import com.voc.service.insights.engine.data.listener.DataResourceExcelListener;
import com.voc.service.insights.engine.data.mapper.InsDataResourceDescMapper;
import com.voc.service.insights.engine.mapper.InsClosedRuleConditionMapper;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.model.data.InsDataResourceExcelModel;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import lombok.Cleanup;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源详情(InsDataResourceDesc)表业务层
 *
 * @author leiww
 * @since 2024-04-02 17:00:19
 */
@Service
public class InsDataResourceDescServiceImpl extends ServiceImpl<InsDataResourceDescMapper, InsDataResourceDescEntity> implements InsDataResourceDescService {
    @Resource
    private InsDataResourceDescConvertService convertService;
    @Autowired
    InsDataResourceDescDao dataResourceDescDao;
    @Autowired
    InsClosedRuleConditionMapper closedRuleConditionMapper;

    private static final Logger log = LoggerFactory.getLogger(InsDataResourceDescServiceImpl.class);

    private QueryWrapper<InsDataResourceDescEntity> createQueryWrapper(InsDataResourceDescModel model) {
        QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<InsDataResourceDescEntity> lambdaQueryWrapper = queryWrapper.lambda();
        if (StrUtil.isNotBlank(model.getNameDescFilter())) {
            lambdaQueryWrapper.like(InsDataResourceDescEntity::getName, model.getNameDescFilter());
        }
        if (StrUtil.isNotBlank(model.getNotIdFilter())) {
            lambdaQueryWrapper.ne(InsDataResourceDescEntity::getId, model.getNotIdFilter());
        }
        if (CollUtil.isNotEmpty(model.getStatusFilters())) {
            lambdaQueryWrapper.in(InsDataResourceDescEntity::getStatus, model.getStatusFilters());
        }
        if(ObjectUtils.isNotEmpty(model.getResourceId())){
            lambdaQueryWrapper.eq(InsDataResourceDescEntity::getResourceId, model.getResourceId());
        }
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(InsDataResourceDescModel model) {
        Assert.hasLength(model.getResourceId(), "资源组id不允许为空");
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsDataResourceDescEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsDataResourceDescModel> list = convertService.convertEntityToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }


    @Override
    @CacheInvalidateContainer({
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Disabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled,NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled,Disabled'")
    })
    public Boolean insert(InsDataResourceDescModel model) {
//        this.checkParameter(model);
        Assert.hasLength(model.getName(), "详情内容不允许为空");
        Assert.hasLength(model.getResourceId(), "资源组id不允许为空");
        String[] split = model.getName().split(",");
        List<String> names = new ArrayList<>(Arrays.asList(split));
        QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsDataResourceDescEntity::getResourceId, model.getResourceId());
        queryWrapper.lambda().in(InsDataResourceDescEntity::getName, names);
        final List<InsDataResourceDescEntity> list1 = this.list(queryWrapper);
        if(ObjectUtils.isNotEmpty(list1)){
            log.info("已存在数据源详情：{}", list1);
            List<String> collect = list1.stream().map(InsDataResourceDescEntity::getName).collect(Collectors.toList());
            names.removeAll( collect);
            if(ObjectUtils.isEmpty(names)){
                log.info("本次添加的关键字已全部存在，不做任何处理");
                return true;
            }
        }else{
            log.info("本次添加的关键字均不存在，开始保存");
        }
        List<InsDataResourceDescEntity> collect1 = names.stream().map(e -> {
            return InsDataResourceDescEntity.builder()
                    .id(IdWorker.getId())
                    .resourceId(model.getResourceId())
                    .name(e)
                    .status(model.getStatus())
                    .createBy(model.getCreateBy())
                    .createTime(LocalDateTime.now())
                    .build();
        }).collect(Collectors.toList());
        boolean saveBatch = this.saveBatch(collect1);
        if(saveBatch){
            log.info("批量保存成功");
            return true;
        }else{
            throw new BussinessException(InsCommonErrorEnum.SAVE_DATA_RESOURCE_ERROR);
        }
    }


    @Override
    @CacheInvalidateContainer({
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Disabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled,NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled,Disabled'")
    })
    public Boolean update(InsDataResourceDescModel model) {
//        this.checkParameter(model);
//        Assert.hasLength(model.getName(), "详情内容不允许为空");
        Assert.hasLength(model.getResourceId(), "资源组id不允许为空");
        Assert.hasLength(model.getId(), "id不允许为空");
        if("Disabled".equals(model.getStatus())){
            Integer quoteCount = closedRuleConditionMapper.findQuoteCount(Arrays.asList(model.getId()));
            if(quoteCount>0){
                throw new BussinessException(InsCommonErrorEnum.QUOTE_COUNT_NOT_ZERO);
            }
        }
        return this.updateById(convertService.convertTo(model));
    }


    @Override
    @CacheInvalidateContainer({
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Disabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled,NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled,Disabled'")
    })
    public Boolean deleteByIdResourceId(InsDataResourceDescModel model) {
        Assert.hasLength(model.getId(), "id不允许为空");
        Assert.hasLength(model.getResourceId(), "资源id不允许为空");
        InsDataResourceDescModel dataResourceDescModel = InsDataResourceDescModel.builder().id(model.getId()).resourceId(model.getResourceId()).build();
        return this.baseMapper.delete(this.createQueryWrapper(dataResourceDescModel)) > 0;
    }

    @Override
    public InsDataResourceDescModel queryById(InsDataResourceDescModel model) {
        Assert.hasLength(model.getId(), "id不允许为空");
        InsDataResourceDescEntity entity = this.getById(model.getId());
        return convertService.convertTo(entity);
    }

    @Override
    public List<ResourceDescDto> queryByParam(InsDataResourceDescModel model) {
        //标准
        final String customer = model.getCustomer();
        model.setCustomer(null);
        List<InsDataResourceDescEntity> list = this.list(this.createQueryWrapper(model));
        List<ResourceDescDto> collect = list.stream().map(e -> convertService.convertToDto(e)).collect(Collectors.toList());
        model.setCustomer(customer);
//        List<ResourceDescDto> resourceDescDtos = dataResourceDescDao.queryCustomByParam(model);
//        collect.addAll(resourceDescDtos);
        log.debug("end");
        return collect;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:resources:", key = "':C{appId}:'+#model.resourceId + ':' + #status", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<ResourceDescDto> queryByResourceId(InsDataResourceDescModel model) {
        Assert.hasLength(model.getResourceId(), "资源组resourceId不允许为空");
        Assert.isTrue(CollUtil.isNotEmpty(model.getStatusFilters()), "状态不允许为空");
        InsDataResourceDescModel query = InsDataResourceDescModel.builder()
                .resourceId(model.getResourceId())
                .statusFilters(model.getStatusFilters()).build();
        List<InsDataResourceDescEntity> list = this.list(this.createQueryWrapper(query));
        return list.stream().map(e -> convertService.convertToDto(e)).collect(Collectors.toList());
    }

    @Override
    @CacheInvalidateContainer({
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Disabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':Enabled,NotEnabled'"),
            @CacheInvalidate(area="VDP" ,name = ":data:resources:", key = "#model.resourceId+':NotEnabled,Enabled'")
    })
    public Boolean updateStatus(InsDataResourceDescModel model) {
        Assert.hasLength(model.getId(), "主键不允许为空");
        Assert.hasLength(model.getResourceId(), "资源组id不允许为空");
        Assert.hasLength(model.getStatus(), "资源组状态不允许为空");
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public void analysisExcel(String resourceId, String clientId, List<InsDataResourceExcelModel> list) {
        if(ObjectUtils.isEmpty(list)){
            return;
        }

        list.stream().forEach(e->{
            this.insert(InsDataResourceDescModel.builder().name(e.getName()).resourceId(resourceId).customer(clientId).build());
        });

    }

    @Override
    public void dataResourceUpload(MultipartFile file, String clientId, String resourceId) {
        try {
            @Cleanup
            InputStream fileIs = file.getInputStream();
            EasyExcel.read(fileIs, InsDataResourceExcelModel.class,new DataResourceExcelListener(this,clientId,resourceId)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<ResourceDescDto> findAllDataResourceDesc(InsDataResourceDescModel model) {
//        Assert.hasLength(model.getCustomer(), "客户id不允许为空");
        QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsDataResourceDescEntity::getStatus, "Enabled");
        List<InsDataResourceDescEntity> list = this.list(queryWrapper);
        if(ObjectUtils.isEmpty(list)){
            log.warn("暂无数据源详情");
            return List.of();
        }
        List<ResourceDescDto> dataResourceDesc = list.stream().map(e -> convertService.convertToDto(e)).collect(Collectors.toList());
//        List<ResourceDescDto> clientDataResourceDesc = dataResourceDescDao.findAllDataResourceDesc(model);
//        if(ObjectUtils.isNotEmpty(clientDataResourceDesc)&&ObjectUtils.isEmpty(dataResourceDesc)){
//            return clientDataResourceDesc;
//        }else if(ObjectUtils.isEmpty(clientDataResourceDesc)&&ObjectUtils.isNotEmpty(dataResourceDesc)){
//            return dataResourceDesc;
//        }else {
//            dataResourceDesc.addAll(clientDataResourceDesc);
//        }
        return dataResourceDesc;
    }

    @Override
    public void changeResourceStatus(InsDataResourceDescModel model) {
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "id集合不允许为空");
        Assert.hasLength(model.getStatus(), "资源组状态不允许为空");
        UpdateWrapper<InsDataResourceDescEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsDataResourceDescEntity::getId, model.getIds());
        updateWrapper.lambda().set(InsDataResourceDescEntity::getStatus, model.getStatus());
        boolean update = this.update(updateWrapper);
        if(update){
            log.info("规则词库详情状态更新成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.UPDATE_DATA_SOURCE_ERROR);
        }
    }

    @Override
    public Map<String, Integer> countByCategoryIds(Set<String> ids) {
        if(ObjectUtils.isEmpty(ids)){
            return Map.of();
        }
        final List<InsDataResourceDescEntity> insDataResourceDescEntities = this.baseMapper.countByResourceIds(ids);
        return insDataResourceDescEntities.stream().collect(Collectors.toMap(InsDataResourceDescEntity::getResourceId, InsDataResourceDescEntity::getCnt));
    }

    private void checkParameter(InsDataResourceDescModel model) {
        Assert.hasLength(model.getName(), "详情内容不允许为空");
        Assert.hasLength(model.getResourceId(), "资源组id不允许为空");
        InsDataResourceDescModel query = InsDataResourceDescModel.builder()
                .notIdFilter(model.getId())
                .resourceId(model.getResourceId())
                .name(model.getName()).build();
        List<InsDataResourceDescEntity> list = this.list(this.createQueryWrapper(query));
        Assert.isTrue(list.isEmpty(), "数据详情不可以重复，请修改后重新提交");
//        InsDataResourceModel insDataResourceModel = resourceService.queryById(InsDataResourceModel.builder().customer(model.getCustomer()).id(model.getResourceId()).build());
//        Assert.notNull(insDataResourceModel, "资源组不存在");
    }
}
