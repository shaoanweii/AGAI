package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.IInsAccountLexiconService;
import com.voc.service.insights.engine.api.IInsClosedRuleService;
import com.voc.service.insights.engine.api.data.InsDataResourceDescService;
import com.voc.service.insights.engine.api.data.InsDataResourceService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.config.InsDataResourceConfig;
import com.voc.service.insights.engine.data.entity.InsAccountLexiconEntity;
import com.voc.service.insights.engine.data.entity.InsDataResourceDescEntity;
import com.voc.service.insights.engine.data.entity.InsDataResourceEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataResourceConvertService;
import com.voc.service.insights.engine.data.mapper.InsAccountLexiconMapper;
import com.voc.service.insights.engine.data.mapper.InsDataResourceDescMapper;
import com.voc.service.insights.engine.data.mapper.InsDataResourceMapper;
import com.voc.service.insights.engine.enums.DataResourceType;
import com.voc.service.insights.engine.enums.RuleStatusType;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import com.voc.service.insights.engine.vo.InsDataResourceDetailVo;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 资源库(InsDataResource)表业务层
 *
 * @author leiww
 * @since 2024-04-02 16:37:38
 */
@Service
public class InsDataResourceServiceImpl extends ServiceImpl<InsDataResourceMapper, InsDataResourceEntity> implements InsDataResourceService {
    @Resource
    private InsDataResourceConvertService convertService;
    @Autowired
    private IInsClosedRuleService closedRuleService;
    private static final Logger log = LoggerFactory.getLogger(InsDataResourceServiceImpl.class);
    @Autowired
    private InsDataResourceDescService dataResourceDescService;
    @Autowired
    private IInsAccountLexiconService lexiconService;
    @Autowired
    private InsDataResourceDescMapper dataResourceDescMapper;
    @Autowired
    private InsAccountLexiconMapper accountLexiconMapper;
    @Autowired
    private InsDataResourceConfig insDataResourceConfig;

    private QueryWrapper<InsDataResourceEntity> createQueryWrapper(InsDataResourceModel model) {
        InsDataResourceEntity entity = convertService.convertTo(model);
        QueryWrapper<InsDataResourceEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsDataResourceEntity> lambdaQueryWrapper = queryWrapper.lambda();
        if (StrUtil.isNotBlank(model.getNameFilter())) {
            lambdaQueryWrapper.like(InsDataResourceEntity::getName, model.getNameFilter());
        }
        if (StrUtil.isNotBlank(model.getNotIdFilter())) {
            lambdaQueryWrapper.ne(InsDataResourceEntity::getId, model.getNotIdFilter());
        }

        if(ObjectUtils.isNotEmpty(model.getTypeList())){
            lambdaQueryWrapper.in(InsDataResourceEntity::getType, model.getTypeList());
        }

        if(ObjectUtils.isNotEmpty(model.getType())){
            lambdaQueryWrapper.eq(InsDataResourceEntity::getType, model.getType());
        }

        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(InsDataResourceModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsDataResourceEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsDataResourceModel> list = convertService.convertEntityToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }


    @Override
    @CacheInvalidate(area="VDP" ,name = ":users:", key = "'C{userId}:tokens:C{token}:DataResource'")
    public Boolean insert(InsDataResourceModel model) {
        this.checkParameter(model);
        model.setId(IdWorker.getId());
        model.setCreateTime(LocalDateTime.now());
        return this.save(convertService.convertTo(model));
    }


    @Override
    @CacheInvalidate(area="VDP" ,name = ":users:", key = "'C{userId}:tokens:C{token}:DataResource'")
    public Boolean update(InsDataResourceModel model) {
        this.checkParameter(model);
        model.setUpdateTime(LocalDateTime.now());
        return this.updateById(convertService.convertTo(model));
    }


    @Override
    public Boolean deleteByIds(InsDataResourceModel model) {
        Assert.hasLength(model.getId(), "id不能为空");
        Assert.hasLength(model.getType(), "类型不能为空");
        Map<String, Integer> map = null;
        if(DataResourceType.DATA_RESOURCE_CLOSED_LOOP.getCode().equals(model.getType())){
            log.info("闭环规则分类");
            map = closedRuleService.countByCategoryIds(Collections.singleton(model.getId()));
        }else if(DataResourceType.DATA_RESOURCE_RULE.getCode().equals(model.getType())){
            log.info("规则词库分类");
            map = dataResourceDescService.countByCategoryIds(Collections.singleton(model.getId()));
        }else if(DataResourceType.DATA_RESOURCE_ACCOUNT.getCode().equals(model.getType())){
            log.info("账号规则分类");
            map = lexiconService.countByResourceIds(Collections.singleton(model.getId()));
        }else{
            throw new BussinessException("分类类型不存在");
        }

        if(ObjectUtils.isNotEmpty(map)&&map.containsKey(model.getId())&&map.get(model.getId())>0){
            throw new BussinessException("当前分类下存在详情信息，请先删除详情信息");
        }

        boolean remove = this.removeById(model.getId());
        if(remove){
            log.info("删除成功");
            return true;
        }else{
            throw new BussinessException(InsCommonErrorEnum.REMOVE_CATEGORY_ERROR);
        }
    }

    @Override
    public InsDataResourceModel queryById(InsDataResourceModel model) {
        Assert.hasLength(model.getId(), "id不能为空");
        InsDataResourceEntity entity = this.getById(model.getId());
        return convertService.convertTo(entity);
    }

    @Override
    public IPage<InsDataResourceModel> queryByParam(InsDataResourceModel model) {
        Page<InsDataResourceEntity> page = new Page<>(model.getPageNum(), model.getPageSize());
        IPage<InsDataResourceEntity> dataResourceList = this.baseMapper.findDataResourceList(page, model);
        IPage<InsDataResourceModel> pages = new Page<>();
        pages.setCurrent(dataResourceList.getCurrent());
        pages.setTotal(dataResourceList.getTotal());
        pages.setSize(dataResourceList.getSize());
        pages.setTotal(dataResourceList.getTotal());
        if(ObjectUtils.isEmpty( dataResourceList.getRecords())){
            log.info("暂无分组信息");
            return pages;
        }


        final List<InsDataResourceEntity> list = dataResourceList.getRecords();
        final Set<String> ids = list.stream().map(InsDataResourceEntity::getId).collect(Collectors.toSet());
        Map<String, Integer> map = null;
        if(DataResourceType.DATA_RESOURCE_CLOSED_LOOP.getCode().equals(model.getType())){
            log.info("闭环规则分类");
            map = closedRuleService.countByCategoryIds(ids);
        }else if(DataResourceType.DATA_RESOURCE_RULE.getCode().equals(model.getType())){
            log.info("规则词库分类");
            map = dataResourceDescService.countByCategoryIds(ids);
        }else if(DataResourceType.DATA_RESOURCE_ACCOUNT.getCode().equals(model.getType())){
            log.info("账号规则分类");
            map = lexiconService.countByResourceIds(ids);
        }else{
            throw new BussinessException("分类类型不存在");
        }
        Map<String, Integer> finalMap = map;
        Set<String> forbiddenDeletionIds = insDataResourceConfig.getForbiddenDeletionIds();
        List<InsDataResourceModel> collect = list.stream().map(e -> {
            InsDataResourceModel insDataResourceModel = convertService.convertTo(e);
            if(ObjectUtils.isNotEmpty(finalMap)&& finalMap.containsKey(e.getId())){
                Integer count = finalMap.get(e.getId());
                insDataResourceModel.setCnt( count);
            }
            insDataResourceModel.setAllowDeletion(!forbiddenDeletionIds.contains(e.getId()));
            return insDataResourceModel;
        }).collect(Collectors.toList());
        pages.setRecords(collect);
        return pages;
    }

    @Override
    public List<InsDataResourceModel> findAllDataResourceList(InsDataResourceModel model) {
        List<InsDataResourceEntity> allDataResourceList = this.baseMapper.findAllDataResourceList(model);
        if(ObjectUtils.isEmpty(allDataResourceList)){
            log.info("暂无数据资源信息");
            return List.of();
        }
        boolean filterByStatus = ObjectUtils.isNotEmpty(model.getStatus());
        Set<String> matchedIds = Collections.emptySet();
        if(filterByStatus){
            Set<String> categoryIds = allDataResourceList.stream().map(InsDataResourceEntity::getId).collect(Collectors.toSet());
            matchedIds = new HashSet<>(this.findEnabledCategoryIds(model.getType(), categoryIds));
        }
        Set<String> finalMatchedIds = matchedIds;
        return allDataResourceList.stream()
                .filter(e -> !filterByStatus || finalMatchedIds.contains(e.getId()))
                .map(convertService::convertTo)
                .collect(Collectors.toList());
    }

    private List<String> findEnabledCategoryIds(String type, Set<String> categoryIds) {
        if(ObjectUtils.isEmpty(categoryIds)){
            return List.of();
        }
        if(DataResourceType.DATA_RESOURCE_CLOSED_LOOP.getCode().equals(type)){
            log.info("闭环规则分类");
            return closedRuleService.findRuleIdsByCategoryIds(categoryIds);
        }else if(DataResourceType.DATA_RESOURCE_RULE.getCode().equals(type)){
            log.info("规则词库分类");
            QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .select(InsDataResourceDescEntity::getResourceId)
                    .in(InsDataResourceDescEntity::getResourceId, categoryIds)
                    .eq(InsDataResourceDescEntity::getStatus, RuleStatusType.Enabled.getCode());
            return dataResourceDescMapper.selectList(queryWrapper).stream()
                    .map(InsDataResourceDescEntity::getResourceId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }else if(DataResourceType.DATA_RESOURCE_ACCOUNT.getCode().equals(type)){
            log.info("账号规则分类");
            QueryWrapper<InsAccountLexiconEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .select(InsAccountLexiconEntity::getResourceId)
                    .in(InsAccountLexiconEntity::getResourceId, categoryIds)
                    .eq(InsAccountLexiconEntity::getStatus, RuleStatusType.Enabled.getCode());
            return accountLexiconMapper.selectList(queryWrapper).stream()
                    .map(InsAccountLexiconEntity::getResourceId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }else{
            throw new BussinessException("分类类型不存在");
        }
    }

    @Override
    @Cached(area="VDP" ,name = ":users:", key = "'C{appId}:C{userId}:tokens:C{token}:DataResource'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<InsDataResourceModel> listAll() {
        List<InsDataResourceEntity> list = this.list();
        return list.stream().map(e -> convertService.convertTo(e)).collect(Collectors.toList());
    }

    @Override
    public List<InsDataResourceModel> findResourceGroupByAppClient(InsDataResourceModel model) {
        Assert.hasLength(model.getCustomer(), "所属客户不允许为空");
        QueryWrapper<InsDataResourceEntity> queryWrapper = new QueryWrapper<>();
        List<InsDataResourceEntity> entityList = this.list(queryWrapper);
        List<InsDataResourceModel> list = convertService.convertEntityToList(entityList);
        return list;
    }

    @Override
    public List<InsDataResourceModel> findAllResourceTree(InsDataResourceModel model) {
//        Assert.hasLength(model.getType(), "类型不允许为空");
        List<InsDataResourceEntity> entityList = this.list(this.createQueryWrapper(model));
        if(ObjectUtils.isEmpty(entityList)){
            log.info("暂无分组信息");
            return List.of();
        }
        Map<String, List<InsDataResourceDetailVo>> detailMap = new HashMap<>();
        List<InsDataResourceModel> list = convertService.convertEntityToList(entityList);
        final List<InsAccountLexiconVo> allAccountLexiconList = lexiconService.findAllAccountLexiconList();
        list.stream().map(InsDataResourceModel::getType).collect(Collectors.toSet()).stream().forEach(e->{
            if(DataResourceType.DATA_RESOURCE_RULE.getCode().equals(e)){
                final List<ResourceDescDto> allDataResourceDesc = dataResourceDescService.findAllDataResourceDesc(new InsDataResourceDescModel());
                if(ObjectUtils.isNotEmpty(allDataResourceDesc)){
                    detailMap.putAll(allDataResourceDesc.stream()
                            .filter(vo -> vo != null && vo.getResourceId() != null)
                            .collect(Collectors.groupingBy(
                                    ResourceDescDto::getResourceId,
                                    Collectors.mapping(
                                            convertService::convertResourceDesToDataResourceDetailVo,
                                            Collectors.toList()
                                    )
                            )));
                }
            }else if(DataResourceType.DATA_RESOURCE_ACCOUNT.getCode().equals(e)){
                if(ObjectUtils.isNotEmpty(allAccountLexiconList)){
                    detailMap.putAll(allAccountLexiconList.stream()
                            .filter(vo -> vo != null && vo.getResourceId() != null)
                            .collect(Collectors.groupingBy(
                                    InsAccountLexiconVo::getResourceId,
                                    Collectors.mapping(
                                            this::convertAccountLexiconToDataResourceDetailVo,
                                            Collectors.toList()
                                    )
                            )));
                }
            }
        });
        if(ObjectUtils.isNotEmpty(detailMap)){
            list.stream().forEach(e->{
                if(ObjectUtils.isNotEmpty(detailMap)&&detailMap.containsKey(e.getId())){
                    e.setKeywordList(detailMap.get(e.getId()));
                }
            });
        }
        return list;
    }

    private InsDataResourceDetailVo convertAccountLexiconToDataResourceDetailVo(InsAccountLexiconVo insAccountLexiconVo) {
        return InsDataResourceDetailVo.builder().id(insAccountLexiconVo.getId()).name(ObjectUtils.isNotEmpty(insAccountLexiconVo.getAccountName())?insAccountLexiconVo.getAccountName():insAccountLexiconVo.getAccountId()).build();
    }

    private void checkParameter(InsDataResourceModel model) {
        Assert.hasLength(model.getName(), "名称不允许为空");
//        Assert.isTrue(!model.getName().isEmpty()
//                && model.getName().length() <= 50, "资源组名长度不符,长度不允许超过50个字");
        Assert.hasLength(model.getType(), "类型不允许为空");
//        Assert.hasLength(model.getCustomer(), "所属客户不允许为空");
        InsDataResourceModel query = InsDataResourceModel.builder()
                .notIdFilter(model.getId())
                .customer(model.getCustomer())
                .type(model.getType())
                .name(model.getName()).build();
        List<InsDataResourceEntity> list = this.list(this.createQueryWrapper(query));
        if(ObjectUtils.isNotEmpty( list)){
            if(DataResourceType.DATA_RESOURCE_CLOSED_LOOP.getCode().equals(model.getType())){
                Assert.isTrue(list.isEmpty(), DataResourceType.DATA_RESOURCE_CLOSED_LOOP.getText()+"分类名称不可以重复，请修改后重新提交");
            }else if(DataResourceType.DATA_RESOURCE_RULE.getCode().equals(model.getType())){
                Assert.isTrue(list.isEmpty(), DataResourceType.DATA_RESOURCE_RULE.getText()+"分类名称不可以重复，请修改后重新提交");
            }else if(DataResourceType.DATA_RESOURCE_ACCOUNT.getCode().equals(model.getType())){
                Assert.isTrue(list.isEmpty(), DataResourceType.DATA_RESOURCE_ACCOUNT.getText()+"分类名称不可以重复，请修改后重新提交");
            }else{
                Assert.isTrue(false, "分类类型不存在");
            }
        }
    }
}
