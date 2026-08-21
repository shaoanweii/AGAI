package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.clients.IAnalysisDataServiceClient;
import com.voc.service.analysis.model.ModifyDataModel;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.components.mybatis.util.ClientMappings;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.common.util.ExcelUtil;
import com.voc.service.insights.engine.dao.InsTagLibClientDao;
import com.voc.service.insights.engine.dao.InsTagLibDao;
import com.voc.service.insights.engine.entity.InsAttributeLabelEntity;
import com.voc.service.insights.engine.entity.InsTagLibClientEntity;
import com.voc.service.insights.engine.entity.InsTagLibEntity;
import com.voc.service.insights.engine.enums.*;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.TagClientExcelListener;
import com.voc.service.insights.engine.mapper.InsAttributeLabelMapper;
import com.voc.service.insights.engine.mapper.InsTagLibClientMapper;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午9:32
 * @描述:
 **/
@Service
public class InsTagLibClientServiceImpl implements IInsTagLibClientService {
    private static final Logger log = LoggerFactory.getLogger(InsTagLibClientServiceImpl.class);
    @Autowired
    private InsTagLibClientDao tagLibClientDao;
    @Autowired
    private InsConvertMapperService convertMapperService;
    @Autowired
    private ClientMappings clientMappings;
    @Autowired
    private InsTagLibDao tagLibDao;
    @Autowired
    private IInsDictService dictService;
    @Autowired
    private IAnalysisDataServiceClient dataServiceClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment environment;
    @Autowired
    private InsAttributeLabelMapper attributeLabelMapper;
    @CreateCache(area = "VDP", name = ":tagLibCategory", expire = 60 * 12, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, List<TagLibCategoryVo>> tagLibCategoryCache;
    @Value("${ins.project.id:4cb464bb8f604284dd83c92356fd62a4}")
    private String projectId;
    @Value("${ins.cqca.training.url:http://10.63.6.133:8250/api/ai/opinion-synonyms/transfer}")
    private String topicMergeNotifyUrl;
    @Value("${ins.cqca.training.apiKey:oiqhyerfila;shdf08y5082hnasdo;h}")
    private String topicMergeNotifyApiKey;
    @CreateCache(area = "VDP", name = ":insights:tagLib", expire = 60 * 1, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String,List<TagLibCategoryVo>> tagLibCache;

    final String STARROCKS_CLIENT_DB = "dndc";

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public String saveTagLibClient(InsTagLibClientModel tagLibClientModel) {
        this.checkParams(tagLibClientModel);
        log.debug("开始标签名称重复校验");
        if (TagAttribute.FINAL_CATEGORY.getCode().equalsIgnoreCase(tagLibClientModel.getIdentifier())) {
            Boolean checkTagLibClientName = tagLibClientDao.checkTagLibName(tagLibClientModel.getTagName(), null, tagLibClientModel.getTagType(),TagAttribute.FINAL_CATEGORY.getCode());
            if (checkTagLibClientName) {
                throw new BussinessException(InsCommonErrorEnum.TAGLIB_EXIST);
            }
        }

        log.debug("客户标签名称重复校验结束");
        final String username = ServiceContextHolder.getUserId();
        log.debug("转换前:tagLibModel{}", tagLibClientModel);
        InsTagLibClientEntity insTagLibClientEntity = convertMapperService.tagLibClientModelConvertEntity(tagLibClientModel);
        final String id = IdWorker.getId();
//        final String code = clientMappings.getMappings().get(tagLibClientModel.getAppClient());
        insTagLibClientEntity.setId(id);
        insTagLibClientEntity.setCreateUser(username);
        insTagLibClientEntity.setUpdateUser(username);
        insTagLibClientEntity.setCreateTime(LocalDateTime.now());
        insTagLibClientEntity.setUpdateTime(LocalDateTime.now());
        insTagLibClientEntity.setTagAttribute(TagAttribute.CATEGORY.getCode());
        Integer currentCategoryLevel = resolveCurrentCategoryLevel(tagLibClientModel, insTagLibClientEntity);
        if (insTagLibClientEntity.getLevel() == null && currentCategoryLevel != null) {
            insTagLibClientEntity.setLevel(currentCategoryLevel);
        }
        final String tagCode = this.getTagCode(tagLibClientModel.getTagType(), tagLibClientModel.getTagParentId(), "cqca");
        insTagLibClientEntity.setTagCode(tagCode);
//        insTagLibClientEntity.setSort(Integer.valueOf(tagCode.substring(tagCode.lastIndexOf("_") + 1)));
        log.debug("转换后:insTagLibEntity{}", insTagLibClientEntity);
        tagLibClientDao.saveTagLibClient(insTagLibClientEntity);
        moveParentDirectFinalCategoryIfNecessary(tagLibClientModel, id, currentCategoryLevel);
        this.removeTagLibeCache(ServiceContextHolder.getClientId());
        return id;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void updateTagLibClient(InsTagLibClientModel tagLibClientModel) {
        //单独参数校验
        Assert.hasLength(tagLibClientModel.getId(), "标签id不能为空");
        this.checkParams(tagLibClientModel);
        log.debug("开始标签名称重复校验");
        if (TagAttribute.FINAL_CATEGORY.getCode().equalsIgnoreCase(tagLibClientModel.getIdentifier())) {
            Boolean checkedTagLibName = tagLibClientDao.checkTagLibName(tagLibClientModel.getTagName(), tagLibClientModel.getId(), tagLibClientModel.getTagType(),TagAttribute.FINAL_CATEGORY.getCode());
            if (checkedTagLibName) {
                throw new BussinessException(InsCommonErrorEnum.TAGLIB_EXIST);
            }
        }

        log.debug("标签名称重复校验结束");
        InsTagLibClientEntity tagLibClientById = tagLibClientDao.findTagLibClientById(tagLibClientModel.getId());
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientById), "标签信息不存在");
        boolean tagStatusChanged = !Objects.equals(tagLibClientModel.getTagStatus(), tagLibClientById.getTagStatus());
//        if (!tagLibClientModel.getTagParentId().equalsIgnoreCase(tagLibClientById.getTagParentId())) {
////            final String code = clientMappings.getMappings().get(tagLibClientModel.getAppClient());
//            tagLibClientModel.setTagCode(this.getTagCode(tagLibClientModel.getTagType(), tagLibClientModel.getTagParentId(), "cqca"));
//        }
        final String username = ServiceContextHolder.getUserId();
        log.debug("转换前:tagLibModel{}", tagLibClientModel);
        InsTagLibClientEntity insTagLibEntity = convertMapperService.tagLibClientModelConvertEntity(tagLibClientModel);
        Integer currentCategoryLevel = resolveCurrentCategoryLevel(tagLibClientModel, insTagLibEntity);
        if (insTagLibEntity.getLevel() == null && currentCategoryLevel != null) {
            insTagLibEntity.setLevel(currentCategoryLevel);
        }
        Integer targetCategoryLevel = ObjectUtils.isNotEmpty(insTagLibEntity.getLevel()) ? insTagLibEntity.getLevel() : currentCategoryLevel;
        boolean tagNameChanged = !Objects.equals(tagLibClientModel.getTagName(), tagLibClientById.getTagName());
        boolean tagParentChanged = !Objects.equals(tagLibClientModel.getTagParentId(), tagLibClientById.getTagParentId());
        boolean tagLevelChanged = ObjectUtils.isNotEmpty(targetCategoryLevel) && !Objects.equals(targetCategoryLevel, tagLibClientById.getLevel());
        insTagLibEntity.setUpdateTime(LocalDateTime.now());
        insTagLibEntity.setUpdateUser(username);
        log.debug("转换后:insTagLibEntity{}", insTagLibEntity);
        tagLibClientDao.updateTagLibClient(insTagLibEntity);
        moveParentDirectFinalCategoryIfNecessary(tagLibClientModel, tagLibClientModel.getId(), targetCategoryLevel);
        List<InsTagLibClientEntity> affectedTagEntities = List.of();
        if (tagStatusChanged) {
            affectedTagEntities = updateTagLibStatus(List.of(tagLibClientModel.getId()), tagLibClientModel.getTagStatus());
        }
        boolean needSyncResultData = canSyncTagResultData(tagLibClientById) && (tagNameChanged || tagParentChanged || tagLevelChanged);
        if (needSyncResultData || CollectionUtil.isNotEmpty(affectedTagEntities)) {
            List<InsTagLibClientEntity> finalAffectedTagEntities = affectedTagEntities;
            Integer finalTargetCategoryLevel = targetCategoryLevel;
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    // 确保异步线程也使用 StarRocks 数据源（避免默认落到 MySQL 导致 CTE/cast 语法不兼容）
                    com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.push(STARROCKS_CLIENT_DB);
                    if (needSyncResultData) {
                        this.pushUpdatedTagResultData(tagLibClientById, tagLibClientModel, finalTargetCategoryLevel);
                    }
                    if (CollectionUtil.isNotEmpty(finalAffectedTagEntities)) {
                        this.pushTagStatusResultData(finalAffectedTagEntities, tagLibClientModel.getTagStatus());
                    }
                } catch (Exception e) {
                    log.error("更新标签后推送结果数据失败, id:{}, tagStatus:{}, tagParentId:{}",
                            tagLibClientModel.getId(), tagLibClientModel.getTagStatus(), tagLibClientModel.getTagParentId(), e);
                }
            });
        }
        this.removeTagLibeCache(ServiceContextHolder.getClientId());
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void deleteTagLibClient(InsTagLibClientModel tagLibClientModel) {
        //单独参数校验
        Assert.hasLength(tagLibClientModel.getId(), "标签id不能为空");
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        InsTagLibClientEntity tagLibClientById = tagLibClientDao.findTagLibClientById(tagLibClientModel.getId());
        if(ObjectUtils.isNotEmpty(tagLibClientById)&&tagLibClientById.getTagStatus().equals("1")){
            throw new BussinessException(InsCommonErrorEnum.REMOVE_TAGLIB_ERROR);
        }
        if(ObjectUtils.isNotEmpty(tagLibClientById)&&tagLibClientById.getTagAttribute().equals(TagAttribute.CATEGORY.getCode())){
            //当前要删除的数据是标签分类数据
            List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findDownAllTagLibHierarchical(Arrays.asList(tagLibClientById.getId()));
            if(ObjectUtils.isNotEmpty(tagLibClientList)&&tagLibClientList.size()>1){
                throw new BussinessException(InsCommonErrorEnum.REMOVE_TAGLIB_CATEGORY_ERROR);
            }

        }
        tagLibClientDao.deleteTagLibClient(tagLibClientModel.getId());
        this.removeTagLibeCache(ServiceContextHolder.getClientId());
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public PageInfo findTagLibClientList(InsTagLibClientModel tagLibClientModel) {
        //单独参数校验
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        PageHelper.startPage(tagLibClientModel.getPageNum(), tagLibClientModel.getPageSize());
        tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        List<InsTagLibClientEntity> tagLibClientList;
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentId()) && !tagLibClientModel.getTagParentId().equals("0")) {
            tagLibClientList = tagLibClientDao.findDownTagLibHierarchical(Arrays.asList(tagLibClientModel.getTagParentId()), ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatusList()) ? tagLibClientModel.getTagStatusList() : null
                    , ObjectUtils.isNotEmpty(tagLibClientModel.getTagName()) ? tagLibClientModel.getTagName() : null
                    , tagLibClientModel.getIds(),ObjectUtils.isNotEmpty(tagLibClientModel.getCodes())?tagLibClientModel.getCodes():null,null);
        } else {
            tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        }
        PageInfo pageInfo = new PageInfo<>(tagLibClientList);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            log.info("暂无标签信息");
            return pageInfo;
        }
        List<InsTagLibClientEntity> tagLibClientHierarchical = new ArrayList<>();
        if (tagLibClientModel.getPageSize() == 10000) {
            tagLibClientModel.setTagAttribute(TagAttribute.CATEGORY.getCode());
            tagLibClientHierarchical = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        } else {
            List<String> tagLibIds = tagLibClientList.stream().filter(e -> !e.getId().startsWith("-")).map(e -> e.getId()).collect(Collectors.toList());
            tagLibClientHierarchical = tagLibClientDao.findTagLibClientHierarchical(tagLibIds);
        }

        Map<Object, List<InsTagLibClientEntity>> map = tagLibClientHierarchical.stream().collect(Collectors.groupingBy(e -> e.getId()));
        List<TagLibClientVo> collect = tagLibClientList.stream().map(e -> {
            TagLibClientVo tagLibClientVo = convertMapperService.tagLibClientEntityConvertVo(e);
            //标签类型
            final String tagType = tagLibClientVo.getTagType();
            //末级标签的code
//            final String tagCode = tagLibClientVo.getTagCode();
            String tagParentId = tagLibClientVo.getTagParentId();
//            String code = tagCode.substring(tagCode.lastIndexOf("_")+1, tagCode.length() - 3);
            StringBuffer buffer = new StringBuffer();
            while (!tagParentId.equals("0")) {
                if (tagParentId.startsWith("-")) {
                    break;
                }
                if (map.containsKey(tagParentId)) {
                    List<InsTagLibClientEntity> insTagLibEntity = map.get(tagParentId);
                    if (insTagLibEntity.size() > 1) {
                        InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().filter(k -> k.getTagType().equalsIgnoreCase(tagType)).findFirst().get();
                        String tagName = "#".concat(insTagLibEntity1.getTagName());
                        buffer.insert(0, tagName);
                    } else {
                        InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().findFirst().get();
                        String tagName = "#".concat(insTagLibEntity1.getTagName());
                        buffer.insert(0, tagName);
                        tagParentId = insTagLibEntity1.getTagParentId();
                    }

//                    code = code.substring(0, code.length()-3);
                }
            }
//            if(map.containsKey(code)){
//                List<InsTagLibClientEntity> insTagLibEntity = map.get(code);
//                if(insTagLibEntity.size()>1){
//                    InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().filter(k -> k.getTagType().equalsIgnoreCase(tagType)).findFirst().get();
//                    buffer.insert(0, insTagLibEntity1.getTagName());
//                }else {
//                    InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().findFirst().get();
//                    buffer.insert(0, insTagLibEntity1.getTagName());
//                }
//            }
//            String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(tagLibClientVo.getId());
//            String[] split = tagLibNameHierarchical.split("#");
//            StringBuffer buffer = new StringBuffer();
//            for (int i = split.length - 1; i >= 0; i--) {
//                buffer.append(split[i]);
//                if (i > 0) {
//                    buffer.append("#");
//                }
//            }
            String codes = buffer.toString();
            if (codes.startsWith("#")) {
                codes = codes.substring(1);
            }
            tagLibClientVo.setTagLibNameHierarchical(codes);
            return tagLibClientVo;
        }).collect(Collectors.toList());
        List<TagLibClientVo> collect1 = collect.stream().filter(e -> !e.getId().equals("0")).collect(Collectors.toList());
        pageInfo.setList(collect1);
        pageInfo.setTotal(pageInfo.getTotal()-1);
        return pageInfo;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public TagLibClientVo findTagLibClient(InsTagLibClientModel tagLibClientModel) {
        //单独参数校验
        Assert.hasLength(tagLibClientModel.getId(), "标签id不能为空");
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        InsTagLibClientEntity tagLibClientById = tagLibClientDao.findTagLibClientById(tagLibClientModel.getId());
        TagLibClientVo tagLibClientVo = convertMapperService.tagLibClientEntityConvertVo(tagLibClientById);
        String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(tagLibClientVo.getId());
        String[] split = tagLibNameHierarchical.split("#");
        StringBuffer buffer = new StringBuffer();
        for (int i = split.length - 1; i >= 0; i--) {
            buffer.append(split[i]);
            if (i > 0) {
                buffer.append("#");
            }
        }
        tagLibClientVo.setTagLibNameHierarchical(buffer.toString());
        return tagLibClientVo;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibClientVo> findTagLibClientVoList(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getIds()), "标签id不能为空");
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            log.info("暂无标签信息");
            return Collections.EMPTY_LIST;
        }
        List<TagLibClientVo> collect = tagLibClientList.stream().map(e -> {
            TagLibClientVo tagLibClientVo = convertMapperService.tagLibClientEntityConvertVo(e);
            String tagLibNameHierarchical = tagLibClientDao.findTagLibClientNameHierarchical(tagLibClientVo.getId());
            String[] split = tagLibNameHierarchical.split("#");
            StringBuffer buffer = new StringBuffer();
            for (int i = split.length - 1; i >= 0; i--) {
                buffer.append(split[i]);
                if (i > 0) {
                    buffer.append("#");
                }
            }
            tagLibClientVo.setTagLibNameHierarchical(buffer.toString());
            return tagLibClientVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void copyTagLibClient(InsTagLibClientModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        Assert.notEmpty(tagLibClientModel.getTagParentIds(), "标签id集合不能为空");
        List<InsTagLibEntity> insTagLibEntities = tagLibDao.UpwardFindTagLibHierarchical(tagLibClientModel.getTagParentIds());
        Assert.notEmpty(insTagLibEntities, "标签不存在");
        final List<InsTagLibEntity> tagLibEntities = insTagLibEntities.stream().filter(e -> TagAttribute.FINAL_LABEL.getCode().equalsIgnoreCase(e.getTagAttribute())).collect(Collectors.toList());
        for (int i = 0; i < tagLibEntities.size(); i++) {
            InsTagLibEntity insTagLibEntity = tagLibEntities.get(i);
            InsTagLibClientEntity tagLibClientByName = tagLibClientDao.findTagLibClientByName(insTagLibEntity.getTagName(), insTagLibEntity.getTagParentId(), tagLibClientModel.getAppClient());
            Assert.isNull(tagLibClientByName, "当前调用标签中" + insTagLibEntity.getTagName() + "已存在，请修改后重新提交");
        }
        final String username = ServiceContextHolder.getUsername();
        List<InsTagLibClientEntity> collect = insTagLibEntities.stream().map(e -> {
            InsTagLibClientEntity insTagLibClientEntity = new InsTagLibClientEntity();
            BeanUtils.copyProperties(e, insTagLibClientEntity);
            final String code = clientMappings.getMappings().get(tagLibClientModel.getAppClient());
            insTagLibClientEntity.setTagCode(code.concat("_f_").concat(e.getTagCode()));
            insTagLibClientEntity.setAppClient(tagLibClientModel.getAppClient());
            insTagLibClientEntity.setCreateTime(LocalDateTime.now());
            insTagLibClientEntity.setCreateUser(username);
            insTagLibClientEntity.setTagStatus(tagLibClientModel.getTagStatus());
            return insTagLibClientEntity;
        }).collect(Collectors.toList());
        tagLibClientDao.saveBatchTagLibClient(collect, tagLibClientModel.getAppClient());
        //添加增量客户
        insTagLibEntities.stream().forEach(e -> {
            List<String> appClient = e.getAppClient();
            if (ObjectUtils.isNotEmpty(appClient)) {
                if (!appClient.contains(tagLibClientModel.getAppClient())) {
                    appClient.add(tagLibClientModel.getAppClient());
                }
            } else {
                appClient = new ArrayList<>();
                appClient.add(tagLibClientModel.getAppClient());
            }
            e.setAppClient(appClient);
        });
        tagLibDao.updateBatchTagLib(insTagLibEntities);
    }

    @Override
    public Map<String, List<DictInfoVo>> findTagLibRelatedItems(InsTagLibClientModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getTagType(), "标签类型不能为空");

        List<DictInfoVo> relatedItems = dictService.findRelatedItems(tagLibClientModel.getTagType());
        if (ObjectUtils.isEmpty(relatedItems)) {
            return null;
        }
        Map<String, List<DictInfoVo>> collect = relatedItems.stream().collect(Collectors.groupingBy(DictInfoVo::getTypeCode, LinkedHashMap::new, Collectors.toList()));
        return collect;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findCategoryList(InsTagLibClientModel tagLibClientModel) {
        // 查询当前条件下的全部分类节点。
        List<InsTagLibClientEntity> categoryEntityList = tagLibClientDao.findCategoryList(tagLibClientModel);
        if (CollectionUtil.isEmpty(categoryEntityList)) {
            return Collections.emptyList();
        }
        // 查询当前类型下的全部末级分类，用于计算末级数量和末级观点归属。
        List<InsTagLibClientEntity> finalCategoryEntityList = listTagLibByIdentifier(tagLibClientModel, TagAttribute.FINAL_CATEGORY.getCode());
        // 进一步查询哪些末级分类下挂载了末级观点。
        Set<String> finalTopicParentIds = findFinalTopicParentIds(finalCategoryEntityList.stream()
                .map(InsTagLibClientEntity::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet()));
        // 按标签类型分组，分别构造各类型分类树。
        Map<String, List<InsTagLibClientEntity>> categoryMapByType = categoryEntityList.stream()
                .filter(category -> ObjectUtils.isNotEmpty(category.getTagType()))
                .collect(Collectors.groupingBy(InsTagLibClientEntity::getTagType, LinkedHashMap::new, Collectors.toList()));
        // 按标签类型分组末级分类，便于后续按类型计算末级数量与末级观点标记。
        Map<String, List<InsTagLibClientEntity>> finalCategoryMapByType = CollectionUtil.isEmpty(finalCategoryEntityList)
                ? Collections.emptyMap()
                : finalCategoryEntityList.stream()
                .filter(category -> ObjectUtils.isNotEmpty(category.getTagType()))
                .collect(Collectors.groupingBy(InsTagLibClientEntity::getTagType, LinkedHashMap::new, Collectors.toList()));

        return categoryMapByType.entrySet().stream()
                .map(entry -> {
                    List<TagLibCategoryVo> treeList = buildCategoryTreeByType(entry.getValue(),
                            finalCategoryMapByType.getOrDefault(entry.getKey(), Collections.emptyList()),
                            finalTopicParentIds,
                            tagLibClientModel.getTagName());
                    if (CollectionUtil.isEmpty(treeList)) {
                        return null;
                    }
                    return wrapCategoryTreeWithRoot(entry.getKey(), treeList);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(category -> getCategoryRootSort(category.getTagType())))
                .collect(Collectors.toList());
    }

    private List<InsTagLibClientEntity> listTagLibByIdentifier(InsTagLibClientModel tagLibClientModel, String identifier) {
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InsTagLibClientEntity::getIdentifier, identifier);
        if (StrUtil.isNotBlank(tagLibClientModel.getTagType())) {
            queryWrapper.eq(InsTagLibClientEntity::getTagType, tagLibClientModel.getTagType());
        }
        if (CollectionUtil.isNotEmpty(tagLibClientModel.getTagTypeList())) {
            queryWrapper.in(InsTagLibClientEntity::getTagType, tagLibClientModel.getTagTypeList());
        }
        queryWrapper.orderByAsc(InsTagLibClientEntity::getTagType)
                .orderByAsc(InsTagLibClientEntity::getLevel)
                .orderByAsc(InsTagLibClientEntity::getCreateTime);
        return tagLibClientMapper.selectList(queryWrapper);
    }

    private List<TagLibCategoryVo> buildCategoryTreeByType(List<InsTagLibClientEntity> categoryEntityList,
                                                           List<InsTagLibClientEntity> finalCategoryEntityList,
                                                           Set<String> finalTopicParentIds,
                                                           String tagName) {
        if (CollectionUtil.isEmpty(categoryEntityList)) {
            return Collections.emptyList();
        }
        Map<String, InsTagLibClientEntity> categoryEntityMap = categoryEntityList.stream()
                .filter(category -> ObjectUtils.isNotEmpty(category.getId()))
                .collect(Collectors.toMap(InsTagLibClientEntity::getId, category -> category, (left, right) -> left, LinkedHashMap::new));
        boolean hasKeyword = StrUtil.isNotBlank(tagName);
        Set<String> matchedCategoryIds = collectMatchedCategoryIds(categoryEntityList, tagName);
        Set<String> visibleCategoryIds = collectVisibleCategoryIds(categoryEntityList, categoryEntityMap, matchedCategoryIds, hasKeyword);
        if (CollectionUtil.isEmpty(visibleCategoryIds)) {
            return Collections.emptyList();
        }
        Map<String, Integer> leafCountMap = buildLeafCountMap(categoryEntityMap, finalCategoryEntityList);
        Set<String> categoryIdsWithFinalTopic = buildCategoryHasFinalTopicIds(categoryEntityMap, finalCategoryEntityList, finalTopicParentIds);
        Set<String> directFinalCategoryParentIds = CollectionUtil.isEmpty(finalCategoryEntityList)
                ? Collections.emptySet()
                : finalCategoryEntityList.stream()
                .map(InsTagLibClientEntity::getTagParentId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        List<TagLibCategoryVo> categoryVoList = convertMapperService.tagLibClientEntityListConvertCategoryVoList(
                categoryEntityList.stream()
                        .filter(category -> visibleCategoryIds.contains(category.getId()))
                        .collect(Collectors.toList()));
        categoryVoList.forEach(category -> {
            category.setLeafCount(leafCountMap.getOrDefault(category.getId(), 0));
            category.setHasFinalCategory(directFinalCategoryParentIds.contains(category.getId()));
            category.setHasFinalTopic(categoryIdsWithFinalTopic.contains(category.getId()));
        });
        return buildCategoryTreeByType(categoryVoList);
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public IPage<TagLibClientVo> findExperienceCodeList(InsTagLibClientModel tagLibClientModel) {
        // 体验代码查询必须基于分类节点展开。
        Assert.hasLength(tagLibClientModel.getTagParentId(), "分类Id不能为空");
        // 分页查询分类节点下的末级分类（体验代码）。
        Page<InsTagLibClientEntity> page = new Page<>(tagLibClientModel.getPageNum(), tagLibClientModel.getPageSize());
        IPage<InsTagLibClientEntity> experienceCodePage = tagLibClientDao.findExperienceCodeList(page, tagLibClientModel);
        // 组装返回分页对象。
        IPage<TagLibClientVo> resultPage = new Page<>();
//        resultPage.setTotal(tagLibClientDao.countExperienceCodeList(tagLibClientModel));
        resultPage.setTotal(experienceCodePage.getTotal());
        resultPage.setSize(experienceCodePage.getSize());
        resultPage.setCurrent(experienceCodePage.getCurrent());
        if (CollectionUtil.isEmpty(experienceCodePage.getRecords())) {
            return resultPage;
        }
        // 转换末级分类返回结构，并补充是否存在末级观点标记。
        List<TagLibClientVo> records = buildTagLibClientVoList(experienceCodePage.getRecords());
        populateFinalTopicFlag(records);
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @SwitchClientDS
    public List<TagLibCategoryVo> findTagLibClientCategoryTree(String clientId, String tagLibType) {
        Assert.hasLength(clientId, "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(clientId), "应用客户不存在");
        InsTagLibClientModel tagLibClientModel = InsTagLibClientModel.builder().tagAttribute(TagAttribute.CATEGORY.getCode()).tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            return Collections.EMPTY_LIST;
        }
        //递归树
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);

        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        return topTagLibList;
    }

    private List<TagLibClientVo> buildTagLibClientVoList(List<InsTagLibClientEntity> tagLibClientList) {
        if (CollectionUtil.isEmpty(tagLibClientList)) {
            return Collections.emptyList();
        }
        List<String> tagLibIds = tagLibClientList.stream().map(InsTagLibClientEntity::getTagParentId).collect(Collectors.toList());
        List<InsTagLibClientEntity> tagLibClientHierarchical = tagLibClientDao.findTagLibClientHierarchical(tagLibIds);
        Map<String, List<InsTagLibClientEntity>> tagLibMap = CollectionUtil.isEmpty(tagLibClientHierarchical)
                ? Collections.emptyMap()
                : tagLibClientHierarchical.stream().collect(Collectors.groupingBy(InsTagLibClientEntity::getId));
        return tagLibClientList.stream().map(entity -> {
            TagLibClientVo tagLibClientVo = convertMapperService.tagLibClientEntityConvertVo(entity);
            String tagType = tagLibClientVo.getTagType();
            tagLibClientVo.setTagTypeName(resolveCategoryRootName(tagType));
            tagLibClientVo.setTagLibNameHierarchical(resolveTagLibNameHierarchical(entity, tagLibMap));
            return tagLibClientVo;
        }).collect(Collectors.toList());
    }

    private String resolveTagLibNameHierarchical(InsTagLibClientEntity entity,
                                                 Map<String, List<InsTagLibClientEntity>> tagLibMap) {
        String tagLibNameHierarchical = buildTagLibNameHierarchicalFromMap(entity, tagLibMap);
        if (StrUtil.isBlank(tagLibNameHierarchical)) {
            tagLibNameHierarchical = reverseTagLibNameHierarchical(tagLibClientDao.findTagLibClientNameHierarchical(entity.getId()));
        }
        return tagLibNameHierarchical;
    }

    private String buildTagLibNameHierarchicalFromMap(InsTagLibClientEntity entity,
                                                      Map<String, List<InsTagLibClientEntity>> tagLibMap) {
        if (entity == null || CollectionUtil.isEmpty(tagLibMap)) {
            return "";
        }
        String tagParentId = entity.getTagParentId();
        String tagType = entity.getTagType();
        StringBuilder buffer = new StringBuilder();
        while (ObjectUtils.isNotEmpty(tagParentId) && !"0".equals(tagParentId)) {
            if (tagParentId.startsWith("-")) {
                break;
            }
            if (!tagLibMap.containsKey(tagParentId)) {
                break;
            }
            List<InsTagLibClientEntity> hierarchicalList = tagLibMap.get(tagParentId);
            InsTagLibClientEntity hierarchical = hierarchicalList.size() > 1
                    ? hierarchicalList.stream()
                    .filter(item -> item.getTagType().equalsIgnoreCase(tagType))
                    .findFirst()
                    .orElse(hierarchicalList.get(0))
                    : hierarchicalList.get(0);
            buffer.insert(0, "#".concat(hierarchical.getTagName()));
            tagParentId = hierarchical.getTagParentId();
        }
        return normalizeTagLibNameHierarchical(buffer.toString());
    }

    private String reverseTagLibNameHierarchical(String tagLibNameHierarchical) {
        if (StrUtil.isBlank(tagLibNameHierarchical)) {
            return "";
        }
        String[] split = tagLibNameHierarchical.split("#");
        StringBuilder buffer = new StringBuilder();
        for (int i = split.length - 1; i >= 0; i--) {
            if (StrUtil.isBlank(split[i])) {
                continue;
            }
            if (buffer.length() > 0) {
                buffer.append("#");
            }
            buffer.append(split[i]);
        }
        return buffer.toString();
    }

    private String normalizeTagLibNameHierarchical(String tagLibNameHierarchical) {
        if (StrUtil.isBlank(tagLibNameHierarchical)) {
            return "";
        }
        return tagLibNameHierarchical.startsWith("#")
                ? tagLibNameHierarchical.substring(1)
                : tagLibNameHierarchical;
    }

    private List<TagLibCategoryVo> buildCategoryTreeByType(List<TagLibCategoryVo> categoryVos) {
        if (CollectionUtil.isEmpty(categoryVos)) {
            return Collections.emptyList();
        }
        Map<String, List<TagLibCategoryVo>> childCategoryMap = categoryVos.stream()
                .collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId, LinkedHashMap::new, Collectors.toList()));
        Set<String> categoryIds = categoryVos.stream()
                .map(TagLibCategoryVo::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        List<TagLibCategoryVo> rootCategoryList = categoryVos.stream()
                .filter(category -> isRootCategory(category, categoryIds))
                .collect(Collectors.toList());
        this.buildCategoryTree(rootCategoryList, childCategoryMap);
        return unwrapVirtualRootCategory(rootCategoryList);
    }

    private void moveParentDirectFinalCategoryIfNecessary(InsTagLibClientModel tagLibClientModel,
                                                          String currentCategoryId,
                                                          Integer currentCategoryLevel) {
        if (!shouldMoveParentDirectFinalCategory(tagLibClientModel) || currentCategoryLevel == null) {
            return;
        }
        List<String> directFinalCategoryIds = listDirectFinalCategoryIds(tagLibClientModel.getTagParentId(), tagLibClientModel.getTagType());
        if (CollectionUtil.isEmpty(directFinalCategoryIds)) {
            return;
        }
        tagLibClientDao.batchMoveTagLibClient(directFinalCategoryIds, currentCategoryId, currentCategoryLevel);
    }

    private boolean shouldMoveParentDirectFinalCategory(InsTagLibClientModel tagLibClientModel) {
        if (tagLibClientModel == null || !Boolean.TRUE.equals(tagLibClientModel.getHasFinalCategory())) {
            return false;
        }
        if ("0".equals(tagLibClientModel.getTagParentId())) {
            return false;
        }
        if (TagAttribute.FINAL_CATEGORY.getCode().equalsIgnoreCase(tagLibClientModel.getIdentifier())) {
            return false;
        }
        return StrUtil.isBlank(tagLibClientModel.getTagAttribute())
                || TagAttribute.CATEGORY.getCode().equalsIgnoreCase(tagLibClientModel.getTagAttribute());
    }

    private List<String> listDirectFinalCategoryIds(String tagParentId, String tagType) {
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InsTagLibClientEntity::getTagParentId, tagParentId)
                .eq(InsTagLibClientEntity::getIdentifier, TagAttribute.FINAL_CATEGORY.getCode());
        if (StrUtil.isNotBlank(tagType)) {
            queryWrapper.eq(InsTagLibClientEntity::getTagType, tagType);
        }
        return tagLibClientMapper.selectList(queryWrapper).stream()
                .map(InsTagLibClientEntity::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private Integer resolveCurrentCategoryLevel(InsTagLibClientModel tagLibClientModel,
                                                InsTagLibClientEntity insTagLibClientEntity) {
        if (insTagLibClientEntity != null && insTagLibClientEntity.getLevel() != null) {
            return insTagLibClientEntity.getLevel();
        }
        if (tagLibClientModel != null && tagLibClientModel.getLevel() != null) {
            return tagLibClientModel.getLevel();
        }
        if (tagLibClientModel == null || "0".equals(tagLibClientModel.getTagParentId())) {
            return null;
        }
        InsTagLibClientEntity parentCategory = tagLibClientDao.findTagLibClientById(tagLibClientModel.getTagParentId());
        if (parentCategory == null || parentCategory.getLevel() == null) {
            return null;
        }
        return parentCategory.getLevel() + 1;
    }

    private Set<String> collectMatchedCategoryIds(List<InsTagLibClientEntity> categoryEntityList, String tagName) {
        if (StrUtil.isBlank(tagName)) {
            return Collections.emptySet();
        }
        return categoryEntityList.stream()
                .filter(category -> StrUtil.contains(category.getTagName(), tagName))
                .map(InsTagLibClientEntity::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> collectVisibleCategoryIds(List<InsTagLibClientEntity> categoryEntityList,
                                                  Map<String, InsTagLibClientEntity> categoryEntityMap,
                                                  Set<String> matchedCategoryIds,
                                                  boolean hasKeyword) {
        if (CollectionUtil.isEmpty(categoryEntityList)) {
            return Collections.emptySet();
        }
        if (!hasKeyword) {
            return categoryEntityList.stream()
                    .map(InsTagLibClientEntity::getId)
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (CollectionUtil.isEmpty(matchedCategoryIds)) {
            return Collections.emptySet();
        }
        Set<String> ancestorCategoryIds = collectAncestorCategoryIds(matchedCategoryIds, categoryEntityMap);
        return categoryEntityList.stream()
                .filter(category -> ancestorCategoryIds.contains(category.getId()))
                .map(InsTagLibClientEntity::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> collectAncestorCategoryIds(Set<String> matchedCategoryIds,
                                                   Map<String, InsTagLibClientEntity> categoryEntityMap) {
        Set<String> ancestorCategoryIds = new LinkedHashSet<>();
        for (String matchedCategoryId : matchedCategoryIds) {
            String currentCategoryId = matchedCategoryId;
            while (ObjectUtils.isNotEmpty(currentCategoryId)) {
                if (!ancestorCategoryIds.add(currentCategoryId)) {
                    break;
                }
                InsTagLibClientEntity currentCategory = categoryEntityMap.get(currentCategoryId);
                if (currentCategory == null) {
                    break;
                }
                currentCategoryId = currentCategory.getTagParentId();
            }
        }
        return ancestorCategoryIds;
    }

    private Map<String, Integer> buildLeafCountMap(Map<String, InsTagLibClientEntity> categoryEntityMap,
                                                   List<InsTagLibClientEntity> finalCategoryEntityList) {
        Map<String, Integer> leafCountMap = new HashMap<>();
        if (CollectionUtil.isEmpty(finalCategoryEntityList)) {
            return leafCountMap;
        }
        Map<String, InsTagLibClientEntity> allEntityMap = new HashMap<>(categoryEntityMap);
        finalCategoryEntityList.stream()
                .filter(category -> ObjectUtils.isNotEmpty(category.getId()))
                .forEach(category -> allEntityMap.putIfAbsent(category.getId(), category));
        finalCategoryEntityList.forEach(category -> accumulateLeafCount(category, allEntityMap, categoryEntityMap.keySet(), leafCountMap));
        return leafCountMap;
    }

    private Set<String> buildCategoryHasFinalTopicIds(Map<String, InsTagLibClientEntity> categoryEntityMap,
                                                      List<InsTagLibClientEntity> finalCategoryEntityList,
                                                      Set<String> finalTopicParentIds) {
        if (CollectionUtil.isEmpty(finalCategoryEntityList) || CollectionUtil.isEmpty(finalTopicParentIds)) {
            return Collections.emptySet();
        }
        Map<String, InsTagLibClientEntity> allEntityMap = new HashMap<>(categoryEntityMap);
        finalCategoryEntityList.stream()
                .filter(category -> ObjectUtils.isNotEmpty(category.getId()))
                .forEach(category -> allEntityMap.putIfAbsent(category.getId(), category));
        Set<String> categoryIdsWithFinalTopic = new LinkedHashSet<>();
        finalCategoryEntityList.stream()
                .filter(category -> finalTopicParentIds.contains(category.getId()))
                .forEach(category -> accumulateFinalTopicCategoryIds(category, allEntityMap, categoryEntityMap.keySet(), categoryIdsWithFinalTopic));
        return categoryIdsWithFinalTopic;
    }

    private void accumulateFinalTopicCategoryIds(InsTagLibClientEntity finalCategory,
                                                 Map<String, InsTagLibClientEntity> allEntityMap,
                                                 Set<String> categoryIds,
                                                 Set<String> categoryIdsWithFinalTopic) {
        String parentId = finalCategory.getTagParentId();
        while (ObjectUtils.isNotEmpty(parentId)) {
            InsTagLibClientEntity currentCategory = allEntityMap.get(parentId);
            if (currentCategory == null) {
                break;
            }
            if (categoryIds.contains(currentCategory.getId())) {
                categoryIdsWithFinalTopic.add(currentCategory.getId());
            }
            parentId = currentCategory.getTagParentId();
            if ("0".equals(parentId)) {
                break;
            }
        }
    }

    private void accumulateLeafCount(InsTagLibClientEntity category,
                                     Map<String, InsTagLibClientEntity> allEntityMap,
                                     Set<String> categoryIds,
                                     Map<String, Integer> leafCountMap) {
        String parentId = category.getTagParentId();
        while (ObjectUtils.isNotEmpty(parentId)) {
            InsTagLibClientEntity currentCategory = allEntityMap.get(parentId);
            if (currentCategory == null) {
                break;
            }
            if (categoryIds.contains(currentCategory.getId())) {
                leafCountMap.merge(currentCategory.getId(), 1, Integer::sum);
            }
            parentId = currentCategory.getTagParentId();
            if ("0".equals(parentId)) {
                break;
            }
        }
    }

    private boolean isRootCategory(TagLibCategoryVo category, Set<String> categoryIds) {
        if (category == null) {
            return false;
        }
        String parentId = category.getTagParentId();
        return ObjectUtils.isEmpty(parentId) || "0".equals(parentId) || !categoryIds.contains(parentId);
    }

    private void buildCategoryTree(List<TagLibCategoryVo> categoryList, Map<String, List<TagLibCategoryVo>> childCategoryMap) {
        if (CollectionUtil.isEmpty(categoryList)) {
            return;
        }
        for (TagLibCategoryVo category : categoryList) {
            List<TagLibCategoryVo> childList = childCategoryMap.get(category.getId());
            if (CollectionUtil.isNotEmpty(childList)) {
                buildCategoryTree(childList, childCategoryMap);
            }
            category.setChild(childList);
        }
    }

    private int getLeafCount(TagLibCategoryVo category) {
        return category == null || category.getLeafCount() == null ? 0 : category.getLeafCount();
    }

    private TagLibCategoryVo wrapCategoryTreeWithRoot(String tagType, List<TagLibCategoryVo> treeList) {
        if (CollectionUtil.isEmpty(treeList)) {
            return null;
        }
        return TagLibCategoryVo.builder()
                .id("0")
                .tagCode(tagType)
                .tagType(tagType)
                .tagName(resolveCategoryRootName(tagType))
                .tagParentId("-1")
                .leafCount(treeList.stream().mapToInt(this::getLeafCount).sum())
                .hasFinalTopic(treeList.stream().anyMatch(category -> Boolean.TRUE.equals(category.getHasFinalTopic())))
                .child(treeList)
                .build();
    }

    private void populateFinalTopicFlag(List<TagLibClientVo> tagLibClientVos) {
        if (CollectionUtil.isEmpty(tagLibClientVos)) {
            return;
        }
        Set<String> finalCategoryIds = tagLibClientVos.stream()
                .map(TagLibClientVo::getId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        Set<String> finalTopicParentIds = findFinalTopicParentIds(finalCategoryIds);
        tagLibClientVos.forEach(tagLibClientVo ->
                tagLibClientVo.setHasFinalTopic(finalTopicParentIds.contains(tagLibClientVo.getId())));
    }

    private Set<String> findFinalTopicParentIds(Collection<String> finalCategoryIds) {
        if (CollectionUtil.isEmpty(finalCategoryIds)) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(InsTagLibClientEntity::getTagParentId)
                .eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode())
                .in(InsTagLibClientEntity::getTagParentId, finalCategoryIds)
                .groupBy(InsTagLibClientEntity::getTagParentId);
        return tagLibClientMapper.selectList(queryWrapper).stream()
                .map(InsTagLibClientEntity::getTagParentId)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int getCategoryRootSort(String tagType) {
        if (ObjectUtils.isEmpty(tagType)) {
            return Integer.MAX_VALUE;
        }
        return switch (tagType.toUpperCase(Locale.ROOT)) {
            case "CA" -> 1;
            case "JOUR" -> 2;
            case "PRO" -> 3;
            case "NPS" -> 4;
            case "VRT" -> 5;
            case "CPT" -> 6;
            default -> Integer.MAX_VALUE;
        };
    }

    private String resolveCategoryRootName(String tagType) {
        if (ObjectUtils.isEmpty(tagType)) {
            return "";
        }
        return switch (tagType.toUpperCase(Locale.ROOT)) {
            case "NPS" -> "NPS";
            case "VRT" -> "VRT";
            case "CPT" -> "CPT";
            case "PRO" -> "商品化属性";
            case "CA" -> "全领域业务";
            case "JOUR" -> "用户全旅程";
            default -> {
                TagLibeType tagLibeType = TagLibeType.getByCode(tagType);
                yield tagLibeType == null ? tagType : tagLibeType.getText();
            }
        };
    }

    private List<TagLibCategoryVo> unwrapVirtualRootCategory(List<TagLibCategoryVo> rootCategoryList) {
        if (CollectionUtil.isEmpty(rootCategoryList)) {
            return Collections.emptyList();
        }
        List<TagLibCategoryVo> result = new ArrayList<>();
        for (TagLibCategoryVo rootCategory : rootCategoryList) {
            if (isVirtualRoot(rootCategory)) {
                if (CollectionUtil.isNotEmpty(rootCategory.getChild())) {
                    result.addAll(rootCategory.getChild());
                }
                continue;
            }
            result.add(rootCategory);
        }
        return result;
    }

    private boolean isVirtualRoot(TagLibCategoryVo category) {
        return category != null
                && "0".equals(category.getTagParentId())
                && ObjectUtils.isNotEmpty(category.getId())
                && category.getId().startsWith("-");
    }

    @Override
    @SwitchClientDS
    public List<TagLibCategoryVo> allLibClientCategoryTree(String clientId, String tagLibType) {
        Assert.hasLength(clientId, "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(clientId), "应用客户不存在");
        InsTagLibClientModel tagLibClientModel = InsTagLibClientModel.builder().tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            return Collections.EMPTY_LIST;
        }
        //递归树
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);

        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        return topTagLibList;
    }

    @Override
    public List<TagLibCategoryVo> findTagLibTree(String tagLibType) {
        InsTagLibModel tagLibModel = InsTagLibModel.builder().tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
        List<InsTagLibEntity> tagLibList = tagLibDao.findTagLibList(tagLibModel);
        if (ObjectUtils.isEmpty(tagLibList)) {
            return Collections.EMPTY_LIST;
        }
        //递归树
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibEntityListConvertCategoryVoList(tagLibList);

        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        return topTagLibList;
    }

    @Override
    @SwitchClientDS
    public List<TagLibCategoryVo> findTagLibClientTree(String clientId, String tagLibType) {
        Assert.hasLength(clientId, "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(clientId), "应用客户不存在");
        final String key = this.getKey(clientId);
        List<TagLibCategoryVo> tagLibCategoryVos1 = tagLibCategoryCache.computeIfAbsent(key, k -> {
            try {
                InsTagLibClientModel tagLibClientModel = InsTagLibClientModel.builder().tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
                List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
                if (ObjectUtils.isEmpty(tagLibClientList)) {
                    return null;
                }
                //递归树
                List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);

                //获取顶级渠道
                List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
                //将全部渠道放入map中，用于递归时使用，减少数据库查询
                Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
                this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
                return topTagLibList;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        });

        return tagLibCategoryVos1;
    }

    private String getKey(String clientId){
        return StrUtil.format(":{}:tagLibClient:{}",ServiceContextHolder.getSystemId(), clientId);
    }


    @Override
    public void removeTagLibeCache(String clientId) {
        log.info("删除标签缓存");
        tagLibCategoryCache.remove(this.getKey(clientId));
        tagLibCache.remove(this.getKey(clientId));
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibClientTreeVo> findAllFinalTagLibClientVoList(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagType()), "标签类型不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getAppClient()), "应用客户不能为空");
        tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        List<InsTagLibClientEntity> tagLibClientList;
        if (CollectionUtil.isEmpty(tagLibClientModel.getTagStatusList())){
            tagLibClientModel.setTagStatusList(Lists.newArrayList("1"));
        }
        if ((ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentId()) && !tagLibClientModel.getTagParentId().equals("0"))) {
            tagLibClientList = tagLibClientDao.findDownTagLibHierarchical(Arrays.asList(tagLibClientModel.getTagParentId()), ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatusList()) ? tagLibClientModel.getTagStatusList() : null
                    , ObjectUtils.isNotEmpty(tagLibClientModel.getTagName()) ? tagLibClientModel.getTagName() : null, tagLibClientModel.getIds(), ObjectUtils.isNotEmpty(tagLibClientModel.getCodes())?tagLibClientModel.getCodes():null,null);
        } else if(ObjectUtils.isNotEmpty(tagLibClientModel.getCodes())){
            tagLibClientList = tagLibClientDao.findDownTagLibHierarchical(null, ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatusList()) ? tagLibClientModel.getTagStatusList() : null
                    , ObjectUtils.isNotEmpty(tagLibClientModel.getTagName()) ? tagLibClientModel.getTagName() : null, tagLibClientModel.getIds(), ObjectUtils.isNotEmpty(tagLibClientModel.getCodes())?tagLibClientModel.getCodes():null,ObjectUtils.isNotEmpty(tagLibClientModel.getTagType())?tagLibClientModel.getTagType():null);
        }else {
            tagLibClientList = tagLibClientDao.findFinalTagLibClientBaseList(tagLibClientModel);
        }

        return tagLibClientList.stream().map(e -> TagLibClientTreeVo.builder()
                .id(e.getId())
                .tagName(e.getTagName())
                .tagCode(e.getTagCode())
                .tagStatus(e.getTagStatus())
                .emotion(ObjectUtils.isNotEmpty(e.getEmotion()) ? e.getEmotion() : null)
                .intention(ObjectUtils.isNotEmpty(e.getIntention())?e.getIntention():null)
                .build()).collect(Collectors.toList());
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public InsTagLibVo findAllDisableTagLibClient(InsTagLibClientModel tagLibClientModel) {
        tagLibClientModel.setTagStatus("0");
        //获取禁用标签
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if(ObjectUtils.isEmpty(tagLibClientList)){
            return InsTagLibVo.builder().build();
        }
        //末级禁用标签
        List<TagLibClientTreeVo> finalTagLibList = tagLibClientList.stream().filter(e -> ObjectUtils.isNotEmpty(e.getTagAttribute()) && e.getTagAttribute().equals(TagAttribute.FINAL_LABEL.getCode()))
                .map(e->{
            return TagLibClientTreeVo.builder().id(e.getId()).tagName(e.getTagName()).tagCode(e.getTagCode()).tagStatus(e.getTagStatus()).build();
        }).collect(Collectors.toList());
        //根据父级id分组
        Map<String, List<InsTagLibClientEntity>> collect = tagLibClientList.stream().filter(e -> ObjectUtils.isNotEmpty(e.getTagAttribute()) && e.getTagAttribute().equals(TagAttribute.CATEGORY.getCode())).collect(Collectors.groupingBy(InsTagLibClientEntity::getTagParentId));
        InsTagLibVo build = InsTagLibVo.builder().finalTagLib(finalTagLibList).build();
        return build;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findTagLibClientTree(InsTagLibClientModel tagLibClientModel) {
        ArrayList<Integer> collect = IntStream.range(1, tagLibClientModel.getLevel()+1).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<TagLibCategoryVo> tagLib = tagLibClientDao.findTagLib(collect, ObjectUtils.isNotEmpty(tagLibClientModel.getTagType())? Arrays.asList(tagLibClientModel.getTagType()):null, tagLibClientModel.getTagAttribute(), tagLibClientModel.getTagStatusList());
        if (ObjectUtils.isEmpty(tagLib)) {
            return Collections.EMPTY_LIST;
        }
        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLib.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagName())){
            topTagLibList = topTagLibList.stream().filter(e -> e.getTagName().equals(tagLibClientModel.getTagName())).toList();
        }
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLib.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        List<TagLibCategoryVo> collect1 = topTagLibList.stream().sorted(Comparator.comparingInt(TagLibCategoryVo::getSort)).collect(Collectors.toList());
        return collect1;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> getTagLibClientTree(InsTagLibClientModel tagLibClientModel) {
        ArrayList<Integer> collect = IntStream.range(1, tagLibClientModel.getLevel()+1).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<String> list = null;
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagType())){
            list = Arrays.asList(tagLibClientModel.getTagType());
        }else{
            list = Arrays.asList(TagLibeType.USER_JOURNEY.getCode(), TagLibeType.DOMAIN.getCode());
        }
        List<TagLibCategoryVo> tagLib = tagLibClientDao.findTagLib(collect,list,tagLibClientModel.getTagAttribute(),tagLibClientModel.getTagStatusList());
        if (ObjectUtils.isEmpty(tagLib)) {
            return Collections.EMPTY_LIST;
        }
        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLib.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagName())){
            topTagLibList = topTagLibList.stream().filter(e -> e.getTagName().equals(tagLibClientModel.getTagName())).toList();
        }
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLib.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        Map<String, List<TagLibCategoryVo>> collect1 = topTagLibList.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagType));
        List<TagLibCategoryVo> tagLibCategoryVoList = list.stream().map(e -> {
            TagLibeType byCode = TagLibeType.getByCode(e);
            TagLibCategoryVo build = TagLibCategoryVo.builder()
                    .id("0")
                    .tagCode(byCode.getCode())
                    .tagName(byCode.getText())
                    .tagParentId("-1")
                    .build();
            if (ObjectUtils.isNotEmpty(collect1) && collect1.containsKey(e)) {
                build.setChild(collect1.get(e));
            }
            return build;
        }).collect(Collectors.toList());
        return tagLibCategoryVoList;
    }

    @Override
    @SwitchClientDS
    public void test(String clientId, String tagType) {
        List<InsTagLibClientEntity> downAllTagLibHierarchical = tagLibClientDao.findDownAllTagLibHierarchical(Arrays.asList(tagType));
        Map<Integer, List<InsTagLibClientEntity>> collect = downAllTagLibHierarchical.stream().collect(Collectors.groupingBy(InsTagLibClientEntity::getLevel));
        collect.entrySet().stream().forEach(e->{
            Integer key = e.getKey();
            List<InsTagLibClientEntity> value = e.getValue();
            List<String> list = value.stream().map(k -> k.getTagName()).toList();
            log.info("{}级标签共{}个,【{}】",key, value.size(),StrUtil.join(",", list));
        });
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagClientVo> findAllUpTagLibHierarchicalByTagId(InsTagLibClientModel tagLibClientModel) {
        return tagLibClientDao.findAllUpTagLibHierarchicalByTagId(tagLibClientModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insTopicModel.appClient")
    public List<InsTopicOperatorVo> findTopicOperatorList(InsTopicModel insTopicModel, Boolean isAllVisible) {
        log.info("查询观点操作人列表开始, appClient={}, isAllVisible={}", insTopicModel.getAppClient(), isAllVisible);
        List<String> userIds = new ArrayList<>();
        if (Boolean.TRUE.equals(isAllVisible)) {
            List<String> operatorUserIds = tagLibClientMapper.findTopicOperatorUserIds();
            if (ObjectUtils.isEmpty(operatorUserIds)) {
                return List.of();
            }
            userIds.addAll(operatorUserIds);
        } else {
            userIds.add(ServiceContextHolder.getUserId());
        }
        String systemId = ServiceContextHolder.getSystemId();
        List<InsTopicOperatorVo> result = tagLibClientMapper.findVisibleTopicOperatorList(userIds, systemId);
        log.info("查询观点操作人列表结束, size={}", result == null ? 0 : result.size());
        return result;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public IPage<TopicVo> findAllTopicList(InsTopicModel tagLibClientModel) {
        IPage<InsTagLibClientEntity> page = new Page<>(tagLibClientModel.getPageNum(), tagLibClientModel.getPageSize());
        if (!appendTopicCodesByTagCodes(tagLibClientModel)) {
            return new Page<>();
        }

        IPage<InsTagLibClientEntity> insTagLibClientEntityIPage = tagLibClientDao.findlAllTopic(page,tagLibClientModel);
        IPage<TopicVo> topicPage = new Page<>();
        topicPage.setTotal(insTagLibClientEntityIPage.getTotal());
        topicPage.setSize(insTagLibClientEntityIPage.getSize());
        topicPage.setCurrent(insTagLibClientEntityIPage.getCurrent());
        if(ObjectUtils.isNotEmpty(insTagLibClientEntityIPage.getRecords())){
            final List<InsTagLibClientEntity> records = insTagLibClientEntityIPage.getRecords();
            Set<String> collect = records.stream().map(e -> e.getTagCode()).collect(Collectors.toSet());
            final List<TagClientVo> allUpTagLibHierarchicalByTopicCode = tagLibClientDao.findAllUpTagLibHierarchicalByTopicCode(collect);
            Map<String, List<TagClientVo>> collect1 = allUpTagLibHierarchicalByTopicCode.stream().collect(Collectors.groupingBy(TagClientVo::getFifthCode));
            List<TopicVo> topicVos = records.stream().map(e -> {
                TopicVo topicVo = convertMapperService.tagLibClientEntityListConvertTopicVoList(e);
                topicVo.setSynonyms(e.getSynonyms());
                final String topicCode = topicVo.getTopicCode();
                if (ObjectUtils.isNotEmpty(collect1) && collect1.containsKey(topicCode)) {
                    final List<TagClientVo> tagClientVos = collect1.get(topicCode);
                    if (ObjectUtils.isNotEmpty(tagClientVos)) {
                        Map<String, TagClientVo> collect2 = tagClientVos.stream().collect(Collectors.toMap(TagClientVo::getType, v -> v, (v1, v2) -> v1));
                        if (collect2.containsKey(TagLibeType.USER_JOURNEY.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.USER_JOURNEY.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setJour(tagClientVo);
                        }
                        if (collect2.containsKey(TagLibeType.DOMAIN.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.DOMAIN.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setCa(tagClientVo);
                        }
                        if (collect2.containsKey(TagLibeType.VRT.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.VRT.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setVrt(tagClientVo);
                        }
                        if (collect2.containsKey(TagLibeType.CPT.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.CPT.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setCpt(tagClientVo);
                        }
                        if (collect2.containsKey(TagLibeType.COMMODITY_ATTR.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.COMMODITY_ATTR.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setPro(tagClientVo);
                        }
                        if (collect2.containsKey(TagLibeType.NPS.getCode())) {
                            TagClientVo tagClientVo = collect2.get(TagLibeType.NPS.getCode());
                            if(ObjectUtils.isNotEmpty( tagClientVo.getFourthCode())&&tagClientVo.getFourthCode().equals(tagClientVo.getFifthCode())){
                                tagClientVo.setFourthCode(null);
                                tagClientVo.setFourthName(null);
                                tagClientVo.setFourthId(null);
                            }
                            topicVo.setNps(tagClientVo);
                        }
                    }
                }
                if(ObjectUtils.isNotEmpty(e.getUpdateUser())){
                    topicVo.setOperateUser( e.getUpdateUser());
                }else{
                     topicVo.setOperateUser( e.getCreateUser());
                }
                return topicVo;
            }).sorted(Comparator.comparing(TopicVo::getCreateTime).reversed()).collect(Collectors.toList());
            topicPage.setRecords(topicVos);
        }
        return topicPage;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibTopicVo> findTopicList(InsTopicModel tagLibClientModel) {
        if (!appendTopicCodesByTagCodes(tagLibClientModel)) {
            return List.of();
        }
        return tagLibClientDao.findTopicList(tagLibClientModel);
    }

    private boolean appendTopicCodesByTagCodes(InsTopicModel tagLibClientModel) {
        if (ObjectUtils.isEmpty(tagLibClientModel.getTagCodes())) {
            return true;
        }
        List<InsTagLibClientEntity> downTagLibHierarchical = tagLibClientDao.findDownTagLibHierarchical(
                null, null, null, null, tagLibClientModel.getTagCodes(), tagLibClientModel.getTagType());
        if (ObjectUtils.isEmpty(downTagLibHierarchical)) {
            return false;
        }
        List<String> relationTopicCodes = downTagLibHierarchical.stream()
                .map(InsTagLibClientEntity::getTagCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtils.isEmpty(tagLibClientModel.getTopicCodes())) {
            tagLibClientModel.setTopicCodes(relationTopicCodes);
            return true;
        }
        List<String> topicCodes = new ArrayList<>(tagLibClientModel.getTopicCodes());
        topicCodes.addAll(relationTopicCodes);
        tagLibClientModel.setTopicCodes(topicCodes.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        return true;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void batchChangeTopicStatus(InsTopicModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getTagStatus(), "状态不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTopicCodes()), "标签编码不能为空");
        log.info("开始批量更新观点状态, topicCount:{}, tagStatus:{}", tagLibClientModel.getTopicCodes().size(), tagLibClientModel.getTagStatus());
        tagLibClientDao.batchChangeTopicStatus(tagLibClientModel);
        ModifyDataModel.ModifyAttrs attrs = ModifyDataModel.ModifyAttrs.builder()
                .field("abandon")
                .value("1".equals(tagLibClientModel.getTagStatus())?"0":"1")
                .build();
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultDataByField("topic", tagLibClientModel.getTopicCodes(), Collections.singletonList(attrs));
            } catch (Exception e) {
                log.error("批量更新观点状态后推送结果数据失败, topicCodes:{}, tagStatus:{}", tagLibClientModel.getTopicCodes(), tagLibClientModel.getTagStatus(), e);
            }
        });
    }

    @Override
    @SwitchClientDS(objectAttribute = "insTopicModel.appClient")
    public void saveTopic(InsTopicModel insTopicModel) {
        final String username = ServiceContextHolder.getUser().getFirstname();
        log.info("开始保存观点, tagCode:{}, tagName:{}, experienceCount:{}", insTopicModel.getTagCode(), insTopicModel.getTagName(),
                CollectionUtil.isEmpty(insTopicModel.getExperienceCode()) ? 0 : insTopicModel.getExperienceCode().size());
        Assert.hasLength( insTopicModel.getTagName(), "观点名称不能为空");
        Assert.isTrue( insTopicModel.getTagName().length()<=50,  "观点名称不能超过50个字符");
        this.checkTopicParam( insTopicModel);
        TopicAttributeLabelInfo topicAttributeLabelInfo = this.buildTopicAttributeLabelInfo(insTopicModel.getAttributeLabelIds());
        List<InsTagLibClientEntity> topic = null;
        if(ObjectUtils.isEmpty(insTopicModel.getTagCode())){
            //新增 校验名称唯一性
            Boolean topicCount = tagLibClientDao.findTopicCount(insTopicModel.getTagName());
            if(topicCount){
                throw new BussinessException(InsCommonErrorEnum.TOPIC_EXIST);
            }
        }else{
            InsTopicModel build = InsTopicModel.builder().tagCode(insTopicModel.getTagCode()).build();
            //编辑 根据编码查询，若查询出的观点名称与当前一致，则继续往下进行，若不一致，则校验名称唯一性
            topic = tagLibClientDao.findTopic(build);
            if(ObjectUtils.isEmpty(topic)){
                throw new BussinessException(InsCommonErrorEnum.TOPIC_NOT_EXIST);
            }else{
                String s = topic.stream().map(e -> e.getTagName()).findAny().get();
                if(!s.equals(insTopicModel.getTagName())){
                    //名称不一致，校验名称唯一性
                    Boolean topicCount = tagLibClientDao.findTopicCount(insTopicModel.getTagName());
                    if(topicCount){
                        throw new BussinessException(InsCommonErrorEnum.TOPIC_EXIST);
                    }
                }
            }
        }

        if(ObjectUtils.isEmpty( topic)){
            InsTopicModel build = InsTopicModel.builder().tagName(insTopicModel.getTagName()).build();
            topic = tagLibClientDao.findTopic(build);
        }
        List<InsTagLibClientEntity> topics = new ArrayList<>();
        String topicCode = "";
        Map<String, List<InsTagLibClientEntity>> map = new HashMap<>();
        if(ObjectUtils.isNotEmpty(topic)){
            InsTagLibClientEntity insTagLibClientEntity = topic.stream().findAny().get();
            topicCode = insTagLibClientEntity.getTagCode();
            map = topic.stream().collect(Collectors.groupingBy(InsTagLibClientEntity::getTagType));
        }else{
            topicCode = this.getSoleTagCode("cqca");
        }
        List<InsTopicExperienceCodeModel> experienceCode = insTopicModel.getExperienceCode();
        Map<String, List<InsTagLibClientEntity>> finalMap = map;
        String finalTopicCode = topicCode;
        experienceCode.stream().forEach(e->{
            final String type = e.getType();
            final String parentId = e.getParentId();
            if(ObjectUtils.isEmpty(finalMap)){
                //新观点
                InsTagLibClientEntity tagLibClientEntity = convertMapperService.topicModelConvertTagLibClientEntity(insTopicModel);
                tagLibClientEntity.setId(IdWorker.getId());
                tagLibClientEntity.setTagCode(finalTopicCode);
                tagLibClientEntity.setCreateUser(username);
                tagLibClientEntity.setCreateTime(LocalDateTime.now());
                tagLibClientEntity.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
                tagLibClientEntity.setTagParentId(parentId);
                tagLibClientEntity.setTagType(type);
                tagLibClientEntity.setLevel(5);
                this.fillTopicAttributeLabelInfo(tagLibClientEntity, topicAttributeLabelInfo);
                topics.add(tagLibClientEntity);
            }else{
                //观点已存在，判断关联的标签体系下是否存在，若存在则更新，若不存在则新增
                if(finalMap.containsKey(type)){
                    //当前类型下存在当前观点
                    final List<InsTagLibClientEntity> insTagLibClientEntities = finalMap.get(type);
                    insTagLibClientEntities.stream().forEach(k->{
                        BeanUtils.copyProperties(insTopicModel,k);
                        k.setUpdateUser(username);
                        k.setUpdateTime(LocalDateTime.now());
                        k.setTagParentId(parentId);
                        k.setTagType(type);
                        k.setTagCode(finalTopicCode);
                        this.fillTopicAttributeLabelInfo(k, topicAttributeLabelInfo);
                        topics.add(k);
                    });
                }else{
                    //当前类型下不存在当前观点
                    InsTagLibClientEntity tagLibClientEntity = convertMapperService.topicModelConvertTagLibClientEntity(insTopicModel);
                    tagLibClientEntity.setId(IdWorker.getId());
                    tagLibClientEntity.setTagCode(finalTopicCode);
                    tagLibClientEntity.setCreateUser(username);
                    tagLibClientEntity.setCreateTime(LocalDateTime.now());
                    tagLibClientEntity.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
                    tagLibClientEntity.setTagParentId(parentId);
                    tagLibClientEntity.setTagType(type);
                    tagLibClientEntity.setLevel(5);
                    this.fillTopicAttributeLabelInfo(tagLibClientEntity, topicAttributeLabelInfo);
                    topics.add(tagLibClientEntity);
                }
            }
        });
        if(ObjectUtils.isNotEmpty(topics)){
            //删除原有观点
            if(ObjectUtils.isNotEmpty(topic)){
                List<String> ids = topic.stream().map(e -> e.getId()).collect(Collectors.toList());
                if(ObjectUtils.isNotEmpty( ids)){
                    tagLibClientDao.deleteBatchTagLibClient(ids);
                }
            }
            //新增观点
            tagLibClientDao.saveBatchTagLibClient(topics,null);
        }

        // 保存观点时需要同时同步基础属性和体验码层级字段；若标签体系被删减，则对应字段置空。
        List<ModifyDataModel.ModifyAttrs> attrs = buildSaveTopicResultAttrs(insTopicModel, topic);
        if (CollectionUtil.isEmpty(attrs)) {
            log.info("保存观点后无需要同步的结果数据字段, topicCode:{}", finalTopicCode);
            return;
        }
        log.info("保存观点需同步结果数据字段, topicCode:{}, attrCount:{}", finalTopicCode, attrs.size());
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultData(
                        Collections.singletonList(ModifyDataModel.FilterEntity.builder().field("topic").value(finalTopicCode).build()),
                        attrs
                );
            } catch (Exception e) {
                log.error("保存观点后推送结果数据失败, topicCode:{}", finalTopicCode, e);
            }
        });
    }

    @Override
    @SwitchClientDS(objectAttribute = "insTopicModel.appClient")
    public void batchUpdateTopic(InsTopicModel insTopicModel) {
        final String username = ServiceContextHolder.getUser().getFirstname();
        Assert.isTrue(ObjectUtils.isNotEmpty(insTopicModel.getTopicCodes()), "观点编码不能为空");
//        this.checkTopicParam( insTopicModel);
        log.info("开始批量更新观点, topicCount:{}, experienceCount:{}", insTopicModel.getTopicCodes().size(),
                CollectionUtil.isEmpty(insTopicModel.getExperienceCode()) ? 0 : insTopicModel.getExperienceCode().size());
        // 先校验观点是否存在，避免出现无效更新与空推送。
        List<InsTagLibClientEntity> topic = tagLibClientDao.findTopic(insTopicModel);
        if (ObjectUtils.isEmpty(topic)) {
            throw new BussinessException("观点不存在,请联系管理员");
        }
        TopicAttributeLabelInfo topicAttributeLabelInfo = this.buildTopicAttributeLabelInfo(insTopicModel.getAttributeLabelIds());
        if (ObjectUtils.isEmpty(insTopicModel.getExperienceCode())) {
            tagLibClientDao.batchUpdateTopic(insTopicModel, topicAttributeLabelInfo.getScenarioAttr(), topicAttributeLabelInfo.getAttributeLabelIds());
        } else {
            // 传入体验码时，仅维护观点在各标签体系下的挂载关系。
            List<InsTagLibClientEntity> topics = new ArrayList<>();
            Map<String, List<InsTagLibClientEntity>> map = topic.stream().collect(Collectors.groupingBy(InsTagLibClientEntity::getTagType));
            Map<String, InsTagLibClientEntity> topicMap = topic.stream().collect(Collectors.toMap(InsTagLibClientEntity::getTagCode, v -> v, (v1, v2) -> v1));
            final List<InsTopicExperienceCodeModel> experienceCode = insTopicModel.getExperienceCode();
            experienceCode.stream().forEach(e -> {
                //现分类
                final String type = e.getType();
                final String parentId = e.getParentId();

                if (map.containsKey(type)) {
                    final List<InsTagLibClientEntity> insTagLibClientEntities = map.get(type);
                    //当前类型下存在待编辑的观点
                    insTagLibClientEntities.stream().forEach(k -> {
                        k.setUpdateUser(username);
                        k.setUpdateTime(LocalDateTime.now());
                        k.setTagParentId(parentId);
                        k.setTagType(type);
                        k.setSynonyms(insTopicModel.getSynonyms());
                        this.fillTopicAttributeLabelInfo(k, topicAttributeLabelInfo);
                        topics.add(k);
                    });
                } else {
                    //当前类型下不存在待编辑的观点,对所有
                    List<String> topicCodes = insTopicModel.getTopicCodes();
                    topicCodes.stream().forEach(k -> {
                        if(topicMap.containsKey( k)){
                            InsTagLibClientEntity tagLibClientEntity = topicMap.get(k);
                            InsTagLibClientEntity build = InsTagLibClientEntity.builder().build();
                            BeanUtils.copyProperties(tagLibClientEntity,build);
                            build.setId(IdWorker.getId());
                            build.setCreateUser(username);
                            build.setCreateTime(LocalDateTime.now());
                            build.setTagParentId(parentId);
                            build.setTagType(type);
                            build.setSynonyms(insTopicModel.getSynonyms());
                            this.fillTopicAttributeLabelInfo(build, topicAttributeLabelInfo);
                            topics.add(build);
                        }
                    });
                }
            });

            if (ObjectUtils.isNotEmpty(topics)) {
//                //删除原有观点
//                List<String> ids = topic.stream().map(e -> e.getId()).collect(Collectors.toList());
//                if(ObjectUtils.isNotEmpty( ids)){
//                    tagLibClientDao.deleteBatchTagLibClient(ids);
//                }
                //新增观点
                tagLibClientDao.saveBatchTagLibClient(topics, null);
            }
        }

        // 根据入参构建需要同步到结果数据的字段集合。
        List<ModifyDataModel.ModifyAttrs> attrs = buildTopicUpdateResultAttrs(insTopicModel);
        if (CollectionUtil.isEmpty(attrs)) {
            log.info("批量更新观点后无需要同步的结果数据字段, topicCodes:{}", insTopicModel.getTopicCodes());
            return;
        }
        log.info("批量更新观点需同步结果数据字段, count:{}", attrs.size());

        // 落库完成后异步推送结果数据，降低接口耗时。
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultDataByField("topic", insTopicModel.getTopicCodes(), attrs);
            } catch (Exception e) {
                log.error("批量更新观点后推送结果数据失败, topicCodes:{}", insTopicModel.getTopicCodes(), e);
            }
        });
    }

    @Override
    @SwitchClientDS(objectAttribute = "insTopicModel.appClient")
    public void batchMergeTopic(InsTopicModel insTopicModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(insTopicModel.getTopicCodes()), "待合并观点编码不能为空");
        Assert.hasLength(insTopicModel.getTagCode(), "目标观点编码不能为空");

        String targetTopicCode = StrUtil.trim(insTopicModel.getTagCode());
        List<String> sourceTopicCodes = insTopicModel.getTopicCodes().stream()
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .distinct()
                .collect(Collectors.toList());
        Assert.isTrue(ObjectUtils.isNotEmpty(sourceTopicCodes), "待合并观点编码不能为空");

        LinkedHashSet<String> allTopicCodes = new LinkedHashSet<>(sourceTopicCodes);
        allTopicCodes.add(targetTopicCode);
        List<InsTagLibClientEntity> topicList = tagLibClientDao.findTopic(InsTopicModel.builder()
                .topicCodes(new ArrayList<>(allTopicCodes))
                .build());
        if (ObjectUtils.isEmpty(topicList)) {
            throw new BussinessException(InsCommonErrorEnum.TOPIC_NOT_EXIST);
        }
        Set<String> existedTopicCodes = topicList.stream()
                .map(InsTagLibClientEntity::getTagCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (!existedTopicCodes.contains(targetTopicCode) || !existedTopicCodes.containsAll(sourceTopicCodes)) {
            throw new BussinessException(InsCommonErrorEnum.TOPIC_NOT_EXIST);
        }
        Map<String, InsTagLibClientEntity> topicMap = topicList.stream()
                .filter(entity -> StrUtil.isNotBlank(entity.getTagCode()))
                .collect(Collectors.toMap(InsTagLibClientEntity::getTagCode, entity -> entity, (entity1, entity2) -> entity1));

        List<String> disableTopicCodes = sourceTopicCodes.stream()
                .filter(code -> !Objects.equals(code, targetTopicCode))
                .collect(Collectors.toList());
        List<TopicMergeNotifyPayload> topicMergeNotifyPayload = buildTopicMergeNotifyPayload(disableTopicCodes, targetTopicCode, topicMap, insTopicModel);
        if (ObjectUtils.isNotEmpty(disableTopicCodes)) {
            tagLibClientDao.batchChangeTopicStatus(InsTopicModel.builder()
                    .topicCodes(disableTopicCodes)
                    .tagStatus("0")
                    .build());
        }

        tagLibClientDao.batchChangeTopicStatus(InsTopicModel.builder()
                .topicCodes(Collections.singletonList(targetTopicCode))
                .tagStatus("1")
                .build());
        if (CollectionUtil.isNotEmpty(topicMergeNotifyPayload)) {
            ServiceContextHolder.getExecutor().execute(() -> notifyAiTopicMerge(topicMergeNotifyPayload));
        }
    }

    private List<TopicMergeNotifyPayload> buildTopicMergeNotifyPayload(List<String> sourceTopicCodes, String targetTopicCode,
                                                                       Map<String, InsTagLibClientEntity> topicMap, InsTopicModel insTopicModel) {
        if (CollectionUtil.isEmpty(sourceTopicCodes) || StrUtil.isBlank(targetTopicCode) || ObjectUtils.isEmpty(topicMap)) {
            return List.of();
        }
        InsTagLibClientEntity targetTopic = topicMap.get(targetTopicCode);
        if (ObjectUtils.isEmpty(targetTopic)) {
            return List.of();
        }
        String operator = resolveTopicMergeOperator(insTopicModel);
        return sourceTopicCodes.stream()
                .map(topicMap::get)
                .filter(Objects::nonNull)
                .map(sourceTopic -> {
                    TopicMergeNotifyPayload payloadItem = new TopicMergeNotifyPayload();
                    payloadItem.setSourceStandardOpinionId(StrUtil.nullToEmpty(sourceTopic.getTagCode()));
                    payloadItem.setSourceStandardOpinion(StrUtil.nullToEmpty(sourceTopic.getTagName()));
                    payloadItem.setTargetStandardOpinionId(StrUtil.nullToEmpty(targetTopic.getTagCode()));
                    payloadItem.setTargetStandardOpinion(StrUtil.nullToEmpty(targetTopic.getTagName()));
                    payloadItem.setOperator(operator);
                    return payloadItem;
                })
                .collect(Collectors.toList());
    }

    private String resolveTopicMergeOperator(InsTopicModel insTopicModel) {
        if (ObjectUtils.isNotEmpty(insTopicModel) && StrUtil.isNotBlank(insTopicModel.getOperateUser())) {
            return StrUtil.trim(insTopicModel.getOperateUser());
        }
        String operator = StrUtil.trim(ServiceContextHolder.getUser().getFirstname());
        if (StrUtil.isNotBlank(operator)) {
            return operator;
        }
        operator = StrUtil.trim(ServiceContextHolder.getUsername());
        if (StrUtil.isNotBlank(operator)) {
            return operator;
        }
        return StrUtil.nullToEmpty(ServiceContextHolder.getUserId());
    }

    private void notifyAiTopicMerge(List<TopicMergeNotifyPayload> topicMergeNotifyPayload) {
        if (CollectionUtil.isEmpty(topicMergeNotifyPayload)) {
            return;
        }
        if (isDevProfile()) {
            log.info("当前环境为dev，跳过观点合并后AI通知, payloadSize:{}", topicMergeNotifyPayload.size());
            return;
        }
        if (StrUtil.isBlank(topicMergeNotifyUrl)) {
            log.warn("观点合并后AI通知地址未配置, payloadSize:{}", topicMergeNotifyPayload.size());
            return;
        }
        try {
            log.info("开始调用AI通知，入参:{}",topicMergeNotifyPayload);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", topicMergeNotifyApiKey);
            HttpEntity<List<TopicMergeNotifyPayload>> requestEntity = new HttpEntity<>(topicMergeNotifyPayload, headers);
            TopicMergeNotifyResponse response = restTemplate.postForObject(topicMergeNotifyUrl, requestEntity, TopicMergeNotifyResponse.class);
            if (ObjectUtils.isEmpty(response)) {
                log.error("观点合并后AI通知返回为空, payloadSize:{}, notifyUrl:{}", topicMergeNotifyPayload.size(), topicMergeNotifyUrl);
                return;
            }
            if (!"200".equals(StrUtil.trim(response.getCode()))) {
                log.error("观点合并后AI通知返回失败, payloadSize:{}, notifyUrl:{}, response:{}", topicMergeNotifyPayload.size(), topicMergeNotifyUrl, response);
                return;
            }
            log.info("观点合并后AI通知成功, payloadSize:{}, notifyUrl:{}, response:{}", topicMergeNotifyPayload.size(), topicMergeNotifyUrl, response);
        } catch (Exception e) {
            log.error("观点合并后AI通知失败, payloadSize:{}, notifyUrl:{}", topicMergeNotifyPayload.size(), topicMergeNotifyUrl, e);
        }
    }

    private boolean isDevProfile() {
        if (ObjectUtils.isEmpty(environment)) {
            return false;
        }
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equalsIgnoreCase(StrUtil.trim(profile)));
    }

    private TopicAttributeLabelInfo buildTopicAttributeLabelInfo(List<String> attributeLabelIds) {
        if (CollectionUtil.isEmpty(attributeLabelIds)) {
            return new TopicAttributeLabelInfo(null, null);
        }
        List<String> validAttributeLabelIds = attributeLabelIds.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(validAttributeLabelIds)) {
            return new TopicAttributeLabelInfo(null, null);
        }
        LambdaQueryWrapper<InsAttributeLabelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InsAttributeLabelEntity::getId, validAttributeLabelIds);
        List<InsAttributeLabelEntity> attributeLabelEntityList = attributeLabelMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(attributeLabelEntityList)) {
            return new TopicAttributeLabelInfo(null, null);
        }
        Map<String, String> attributeLabelNameMap = attributeLabelEntityList.stream()
                .filter(e -> StrUtil.isNotBlank(e.getId()) && StrUtil.isNotBlank(e.getName()))
                .collect(Collectors.toMap(InsAttributeLabelEntity::getId, InsAttributeLabelEntity::getName, (v1, v2) -> v1));
        List<String> matchedAttributeLabelIds = validAttributeLabelIds.stream()
                .filter(attributeLabelNameMap::containsKey)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(matchedAttributeLabelIds)) {
            return new TopicAttributeLabelInfo(null, null);
        }
        String scenarioAttr = matchedAttributeLabelIds.stream()
                .map(attributeLabelNameMap::get)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining("、"));
        return new TopicAttributeLabelInfo(StrUtil.emptyToNull(scenarioAttr), matchedAttributeLabelIds);
    }

    private void fillTopicAttributeLabelInfo(InsTagLibClientEntity tagLibClientEntity, TopicAttributeLabelInfo topicAttributeLabelInfo) {
        if (ObjectUtils.isEmpty(tagLibClientEntity) || ObjectUtils.isEmpty(topicAttributeLabelInfo)) {
            return;
        }
        tagLibClientEntity.setScenarioAttr(topicAttributeLabelInfo.getScenarioAttr());
        tagLibClientEntity.setAttributeLabelIds(topicAttributeLabelInfo.getAttributeLabelIds());
    }

    private static class TopicAttributeLabelInfo {
        private final String scenarioAttr;
        private final List<String> attributeLabelIds;

        private TopicAttributeLabelInfo(String scenarioAttr, List<String> attributeLabelIds) {
            this.scenarioAttr = scenarioAttr;
            this.attributeLabelIds = attributeLabelIds;
        }

        public String getScenarioAttr() {
            return scenarioAttr;
        }

        public List<String> getAttributeLabelIds() {
            return attributeLabelIds;
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insTopicModel.appClient")
    public InsTopicVo findTopicByCode(InsTopicModel insTopicModel) {
        Assert.hasLength(insTopicModel.getTagCode(),   "观点编码不能为空");
        final List<InsTagLibClientEntity> topic = tagLibClientDao.findTopic(insTopicModel);
        if(ObjectUtils.isEmpty( topic)){
            log.info( "未查询到该编码观点");
            return  null;
        }
        InsTagLibClientEntity insTagLibClientEntity = topic.stream().findAny().get();
        InsTopicVo insTopicVo = convertMapperService.tagLibClientEntityConvertTopicVo(insTagLibClientEntity);
        insTopicVo.setSynonyms(insTagLibClientEntity.getSynonyms());
        Map<String, List<InsTagLibClientEntity>> collect = topic.stream().collect(Collectors.groupingBy(InsTagLibClientEntity::getTagType));
        List<InsTopicExperienceCodeModel> collect1 = collect.entrySet().stream().map(e -> {
            String key = e.getKey();
            List<InsTagLibClientEntity> value = e.getValue();
            InsTagLibClientEntity insTagLibClientEntity1 = value.stream().findAny().get();
            return InsTopicExperienceCodeModel.builder()
                    .type(key)
                    .parentId(insTagLibClientEntity1.getTagParentId())
                    .build();
        }).collect(Collectors.toList());
        insTopicVo.setExperienceCode( collect1);
        return insTopicVo;
    }

    private void checkTopicParam(InsTopicModel insTopicModel){
        if(ObjectUtils.isNotEmpty(insTopicModel.getTagDescription())){
            Assert.isTrue( insTopicModel.getTagDescription().length()<=100,  "观点描述不能超过100个字符");
        }
        Assert.isTrue(ObjectUtils.isNotEmpty(insTopicModel.getExperienceCode()),  "关联体验代码不能为空");
//        List<InsTopicExperienceCodeModel> experienceCode = insTopicModel.getExperienceCode();
//        List<InsTopicExperienceCodeModel> collect = experienceCode.stream().filter(e -> e.getType().equals(TagLibeType.DOMAIN.getCode())).collect(Collectors.toList());
//        Assert.isTrue(ObjectUtils.isNotEmpty(collect), "体验代码必须包含全领域业务");
//        Assert.hasLength(insTopicModel.getMappingCode(),  "智慧交互中心编码不能为空");
//        Assert.isTrue(insTopicModel.getMappingCode().length()<=50,  "智慧交互中心编码不能超过50个字符");
        Assert.hasLength(insTopicModel.getEmotion(),  "情感不能为空");
        Assert.hasLength(insTopicModel.getIntention(),  "意图不能为空");
//        Assert.hasLength(insTopicModel.getTagIssueSeverity(),  "问题程度不能为空");
        Assert.hasLength( insTopicModel.getTagBusinessDomain(),  "业务领域不能为空");
        Assert.hasLength(insTopicModel.getTagComplaintFlagNeedingReply(),   "是否需回复不能为空");
        Assert.hasLength(insTopicModel.getTagNeedForvclosedLoop(),   "是否需闭环不能为空");
        Assert.hasLength(insTopicModel.getTagStatus(),  "状态不能为空");
    }

    @Override
    @SwitchClientDS
    public List<TagLibCategoryVo> findTagLibClientTreeLevel(String clientId, String tagLibType) {
        Assert.hasLength(clientId, "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(clientId), "应用客户不存在");
        InsTagLibClientModel tagLibClientModel = InsTagLibClientModel.builder().tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            return Collections.EMPTY_LIST;
        }
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);
        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        topTagLibList.stream().forEach(e -> {
            final String id = e.getId();
            if (tagLibbCategoryMap.containsKey(id)) {
                List<TagLibCategoryVo> tagLibCategoryVos1 = tagLibbCategoryMap.get(id);
                e.setChild(tagLibCategoryVos1);
            }
        });
        return topTagLibList;
    }
    @Override
    @SwitchClientDS
    public List<TagLibCategoryVo> findTagLibTwoLevel(String clientId, List<Integer> level) {
        Assert.hasLength(clientId, "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(clientId), "应用客户不存在");
        final String key = this.getKey(clientId);
        return tagLibCache.computeIfAbsent(key, k -> tagLibClientDao.findTagLib(level,null, null, null));
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<String> findCalledTagLibClient(InsTagLibClientModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.hasLength(tagLibClientModel.getTagType(), "标签类型不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        tagLibClientModel.setTagCode(clientMappings.getMappings().get(tagLibClientModel.getAppClient()));
        List<String> calledTagLibClient = tagLibClientDao.findCalledTagLibClient(tagLibClientModel);
        if (ObjectUtils.isEmpty(calledTagLibClient)) {
            log.warn("暂无已调用标签");
            return List.of();
        }
        return calledTagLibClient;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findDownTagLibHierarchical(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentIds()), "标签id集合不能为空");
        List<InsTagLibClientEntity> insTagLibEntities = tagLibClientDao.findDownTagLibHierarchical(tagLibClientModel.getTagParentIds(), null, null, tagLibClientModel.getIds(), null,null);
        if (ObjectUtils.isEmpty(insTagLibEntities)) {
            return List.of();
        }
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(insTagLibEntities);
        return tagLibCategoryVos;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findUpTagLibHierarchical(InsTagLibClientModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getCodes()), "标签code集合不能为空");
        List<String> codes = tagLibClientDao.findTaglibCodeByName(tagLibClientModel.getCodes(),ObjectUtils.isNotEmpty(tagLibClientModel.getTagAttribute())?tagLibClientModel.getTagAttribute():null);
        tagLibClientModel.setCodes(codes);
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientHierarchicalByCodes(tagLibClientModel.getCodes(), null);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            return Collections.EMPTY_LIST;
        }
        //递归树
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);

        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList = tagLibCategoryVos.stream().filter(e -> "0".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        return topTagLibList;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<String> findAllTagLibClientIds(InsTagLibClientModel tagLibClientModel) {
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.hasLength(tagLibClientModel.getTagType(), "标签类型不能为空");
        tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        final List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            log.warn("暂无【{}类型】标签", tagLibClientModel.getTagType());
        }
        final List<String> names = tagLibClientList.stream().map(InsTagLibClientEntity::getTagName).collect(Collectors.toList());
        return names;
    }

    @Autowired
    private InsTagLibClientMapper tagLibClientMapper;

    @Override
    public void analyzeExcelData(List<TagLibExcelModel> list) {
        String clientId = ServiceContextHolder.getClientId();
        List<InsTagLibClientEntity> tagLibClientEntities  = new ArrayList<>();
        list.forEach(e -> {
            String firstId = "";
//            final InsTagLibEntity tagLibByCode = tagLibDao.findTagLibByName(e.getFirstName(), "0");
////            final String firstCode = ObjectUtils.isEmpty(e.getFirstCode())?this.getTagCode(e.getTagType(), "0", "cqca"):e.getFirstCode();
//            if (ObjectUtils.isEmpty(tagLibByCode)) {
//                final String firstCode = this.getTagCode(e.getTagType(), "0", "cqca");
//                firstId = IdWorker.getId();
//                InsTagLibEntity build = InsTagLibEntity.builder()
//                        .id(firstId)
//                        .tagParentId("0")
//                        .tagType(e.getTagType())
//                        .tagName(e.getFirstName())
//                        .tagCode(firstCode)
//                        .tagStatus("1")
//                        .level(1)
//                        .tagAttribute(TagAttribute.CATEGORY.getCode())
//                        .createTime(LocalDateTime.now())
//                        .build();
//                tagLibDao.saveTagLib(build);
//            } else {
//                firstId = tagLibByCode.getId();
//            }
//
//            String secondId = "";
//            final InsTagLibEntity tagLibByCode1 = tagLibDao.findTagLibByName(e.getSecondName(), firstId);
////            final String secondCode = ObjectUtils.isEmpty(e.getSecondCode())?this.getTagCode(e.getTagType(), firstId, "cqca"):e.getSecondCode();
////            final InsTagLibEntity tagLibByCode1 = tagLibDao.findTagLibByCode(secondCode);
//            if (ObjectUtils.isEmpty(tagLibByCode1)) {
//                final String secondCode = this.getTagCode(e.getTagType(), firstId, "cqca");
//                secondId = IdWorker.getId();
//                InsTagLibEntity build = InsTagLibEntity.builder()
//                        .id(secondId)
//                        .tagParentId(firstId)
//                        .tagType(e.getTagType())
//                        .tagName(e.getSecondName())
//                        .tagCode(secondCode)
//                        .tagStatus("1")
//                        .level(2)
//                        .tagAttribute(TagAttribute.CATEGORY.getCode())
//                        .createTime(LocalDateTime.now())
//                        .build();
//                tagLibDao.saveTagLib(build);
//            } else {
//                secondId = tagLibByCode1.getId();
//            }
//
//            String threeId = "";
//            final InsTagLibEntity tagLibByCode2 = tagLibDao.findTagLibByName(e.getThirdName(), secondId);
////            final String threeCode = ObjectUtils.isEmpty(e.getThirdCode())?this.getTagCode(e.getTagType(), secondId, "cqca"):e.getThirdCode();
////            final InsTagLibEntity tagLibByCode2 = tagLibDao.findTagLibByCode(threeCode);
//            if (ObjectUtils.isEmpty(tagLibByCode2)) {
//                final String threeCode = this.getTagCode(e.getTagType(), secondId, "cqca");
//                threeId = IdWorker.getId();
//                InsTagLibEntity build = InsTagLibEntity.builder()
//                        .id(threeId)
//                        .tagParentId(secondId)
//                        .tagType(e.getTagType())
//                        .tagName(e.getThirdName())
//                        .tagCode(threeCode)
//                        .tagStatus("1")
//                        .level(3)
//                        .tagAttribute(TagAttribute.CATEGORY.getCode())
//                        .createTime(LocalDateTime.now())
////                        .emotion(ObjectUtils.isNotEmpty(e.getEmotion())?e.getEmotion():null)
////                        .intention(ObjectUtils.isNotEmpty(e.getIntention())?e.getIntention():null)
////                        .tagAccuracy(ObjectUtils.isNotEmpty(e.getCodePrecision())?e.getCodePrecision():null)
////                        .tagCustomerIssueClassification(ObjectUtils.isNotEmpty(e.getCustomerIssue())?e.getCustomerIssue():null)
////                        .tagIssueSeverity(ObjectUtils.isNotEmpty(e.getQuestionDegree())?e.getQuestionDegree():null)
////                        .tagCodeStatus(ObjectUtils.isNotEmpty(e.getCodeStatus())?e.getCodeStatus():null)
////                        .userJourney2(ObjectUtils.isNotEmpty(e.getCarScenario())?e.getCarScenario():null)
////                        .eventClarity(ObjectUtils.isNotEmpty(e.getEventClarity())?e.getEventClarity():null)
////                        .tagBusinessDomain(ObjectUtils.isNotEmpty(e.getBusinessDomain())?e.getBusinessDomain():null)
////                        .scenarioAttr(ObjectUtils.isNotEmpty(e.getSceneAttribute())?e.getSceneAttribute():null)
////                        .tagHighValueFlag(ObjectUtils.isNotEmpty(e.getPush())?e.getPush():null)
////                        .tagComplaintFlagNeedingReply(ObjectUtils.isNotEmpty(e.getReply())?e.getReply():null)
////                        .tagHighQualityVocFlag(ObjectUtils.isNotEmpty(e.getHighQualityVoc())?e.getHighQualityVoc():null)
////                        .tagNeedForvclosedLoop(ObjectUtils.isNotEmpty(e.getNeedForvclosedLoop())?e.getNeedForvclosedLoop():null)
////                        .d2cResponsibleDept(ObjectUtils.isNotEmpty(e.getResponsibleDept())?e.getResponsibleDept():null)
////                        .d2cAccountableDept(ObjectUtils.isNotEmpty(e.getAccountableDept())?Arrays.asList(e.getAccountableDept()):null)
////                        .d2cCcDept(ObjectUtils.isNotEmpty(e.getCcDept())?Arrays.asList(e.getCcDept()):null)
////                        .tagNewEnergyOrFuel(ObjectUtils.isNotEmpty(e.getEnergyAttribute())?e.getEnergyAttribute():null)
//                        .build();
//                tagLibDao.saveTagLib(build);
//            } else {
//                threeId = tagLibByCode2.getId();
////                log.warn("标签已存在");
//            }
//
//            String fourId = "";
//            if(ObjectUtils.isNotEmpty(e.getFourName())){
//                final InsTagLibEntity tagLibByCode3 = tagLibDao.findTagLibByName(e.getFourName(), threeId);
//
////            final String fourCode = ObjectUtils.isEmpty(e.getFourCode())?this.getTagCode(e.getTagType(), threeId, "cqca"):e.getFourCode();
////            final InsTagLibEntity tagLibByCode3 = tagLibDao.findTagLibByCode(fourCode);
//                if (ObjectUtils.isEmpty(tagLibByCode3)) {
//                    final String fourCode = this.getTagCode(e.getTagType(), threeId, "cqca");
//                    fourId = IdWorker.getId();
//                    InsTagLibEntity build = InsTagLibEntity.builder()
//                            .id(fourId)
//                            .tagParentId(threeId)
//                            .tagType(e.getTagType())
//                            .tagName(e.getFourName())
//                            .tagCode(fourCode)
//                            .tagStatus("1")
//                            .level(4)
////                        .tagAttribute(TagAttribute.FINAL_LABEL.getCode())
//                            .tagAttribute(TagAttribute.CATEGORY.getCode())
//                            .createTime(LocalDateTime.now())
////                        .emotion(ObjectUtils.isNotEmpty(e.getEmotion())?e.getEmotion():null)
////                        .intention(ObjectUtils.isNotEmpty(e.getIntention())?e.getIntention():null)
////                        .tagAccuracy(ObjectUtils.isNotEmpty(e.getCodePrecision())?e.getCodePrecision():null)
////                        .tagCustomerIssueClassification(ObjectUtils.isNotEmpty(e.getCustomerIssue())?e.getCustomerIssue():null)
////                        .tagIssueSeverity(ObjectUtils.isNotEmpty(e.getQuestionDegree())?e.getQuestionDegree():null)
////                        .tagCodeStatus(ObjectUtils.isNotEmpty(e.getCodeStatus())?e.getCodeStatus():null)
////                        .userJourney2(ObjectUtils.isNotEmpty(e.getCarScenario())?e.getCarScenario():null)
////                        .eventClarity(ObjectUtils.isNotEmpty(e.getEventClarity())?e.getEventClarity():null)
////                        .tagBusinessDomain(ObjectUtils.isNotEmpty(e.getBusinessDomain())?e.getBusinessDomain():null)
////                        .scenarioAttr(ObjectUtils.isNotEmpty(e.getSceneAttribute())?e.getSceneAttribute():null)
////                        .tagHighValueFlag(ObjectUtils.isNotEmpty(e.getPush())?e.getPush():null)
////                        .tagComplaintFlagNeedingReply(ObjectUtils.isNotEmpty(e.getReply())?e.getReply():null)
////                        .tagHighQualityVocFlag(ObjectUtils.isNotEmpty(e.getHighQualityVoc())?e.getHighQualityVoc():null)
////                        .tagNeedForvclosedLoop(ObjectUtils.isNotEmpty(e.getNeedForvclosedLoop())?e.getNeedForvclosedLoop():null)
////                        .d2cResponsibleDept(ObjectUtils.isNotEmpty(e.getResponsibleDept())?e.getResponsibleDept():null)
////                        .d2cAccountableDept(ObjectUtils.isNotEmpty(e.getAccountableDept())?Arrays.asList(e.getAccountableDept()):null)
////                        .d2cCcDept(ObjectUtils.isNotEmpty(e.getCcDept())?Arrays.asList(e.getCcDept()):null)
////                        .tagNewEnergyOrFuel(ObjectUtils.isNotEmpty(e.getEnergyAttribute())?e.getEnergyAttribute():null)
//                            .build();
//                    tagLibDao.saveTagLib(build);
//                } else {
//                    fourId = tagLibByCode3.getId();
////                log.warn("标签已存在");
//                }
//            }
//
//
////            final InsTagLibEntity tagLibByCode4 = tagLibDao.findTagLibByName(e.getFiveName(), null);
////            final InsTagLibEntity tagLibByCode4 = tagLibDao.findTagLibByCode(fiveCode);
//            if(ObjectUtils.isNotEmpty(e.getFiveName())){
//                InsTopicModel build1 = InsTopicModel.builder().tagName(e.getFiveName()).build();
//                List<InsTagLibClientEntity> topic = tagLibClientDao.findTopic(build1);
//                String fiveCode = "";
//                if(ObjectUtils.isNotEmpty( topic)){
//                    InsTagLibClientEntity insTagLibClientEntity = topic.stream().findFirst().orElse( null);
//                    if(ObjectUtils.isNotEmpty(insTagLibClientEntity)){
//                        fiveCode = insTagLibClientEntity.getTagCode();
//                    }
//                }else{
//                    fiveCode = this.getSoleTagCode( "cqca");
//                }
//                InsTagLibEntity build = InsTagLibEntity.builder()
//                        .id(IdWorker.getId())
//                        .tagParentId(ObjectUtils.isNotEmpty(fourId)?fourId:threeId)
//                        .tagType(e.getTagType())
//                        .tagName(e.getFiveName())
//                        .tagCode(fiveCode)
//                        .tagStatus("1")
//                        .level(5)
//                        .tagAttribute(TagAttribute.FINAL_LABEL.getCode())
//                        .createTime(LocalDateTime.now())
//                        .emotion(ObjectUtils.isNotEmpty(e.getEmotion())?e.getEmotion():null)
//                        .intention(ObjectUtils.isNotEmpty(e.getIntention())?e.getIntention():null)
//                        .tagAccuracy(ObjectUtils.isNotEmpty(e.getCodePrecision())?e.getCodePrecision():null)
//                        .tagCustomerIssueClassification(ObjectUtils.isNotEmpty(e.getCustomerIssue())?e.getCustomerIssue():null)
//                        .tagIssueSeverity(ObjectUtils.isNotEmpty(e.getQuestionDegree())?e.getQuestionDegree():null)
//                        .tagCodeStatus(ObjectUtils.isNotEmpty(e.getCodeStatus())?e.getCodeStatus():null)
//                        .userJourney2(ObjectUtils.isNotEmpty(e.getCarScenario())?e.getCarScenario():null)
//                        .eventClarity(ObjectUtils.isNotEmpty(e.getEventClarity())?e.getEventClarity():null)
//                        .tagBusinessDomain(ObjectUtils.isNotEmpty(e.getBusinessDomain())?e.getBusinessDomain():null)
//                        .scenarioAttr(ObjectUtils.isNotEmpty(e.getSceneAttribute())?e.getSceneAttribute():null)
//                        .tagHighValueFlag(ObjectUtils.isNotEmpty(e.getPush())?e.getPush():null)
//                        .tagComplaintFlagNeedingReply(ObjectUtils.isNotEmpty(e.getReply())?e.getReply():null)
////                    .tagHighQualityVocFlag(ObjectUtils.isNotEmpty(e.getHighQualityVoc())?e.getHighQualityVoc():null)
//                        .tagNeedForvclosedLoop(ObjectUtils.isNotEmpty(e.getNeedForvclosedLoop())?e.getNeedForvclosedLoop():null)
//                        .d2cResponsibleDept(ObjectUtils.isNotEmpty(e.getResponsibleDept())?e.getResponsibleDept():null)
//                        .d2cAccountableDept(ObjectUtils.isNotEmpty(e.getAccountableDept())?Arrays.asList(e.getAccountableDept()):null)
//                        .d2cCcDept(ObjectUtils.isNotEmpty(e.getCcDept())?Arrays.asList(e.getCcDept()):null)
////                    .tagNewEnergyOrFuel(ObjectUtils.isNotEmpty(e.getEnergyAttribute())?e.getEnergyAttribute():null)
//                        .build();
//                tagLibDao.saveTagLib(build);
//            }
            InsTagLibClientEntity build = InsTagLibClientEntity
                    .builder()
                    .id(e.getId())
                    .scenarioAttr(ObjectUtils.isEmpty(e.getScenarioAttr())?null:e.getScenarioAttr())
                    .eventClarity(ObjectUtils.isEmpty(e.getEventClarity())?null:e.getEventClarity())
                    .emotion(ObjectUtils.isEmpty(e.getEmotion())?null:e.getEmotion())
                    .intention(ObjectUtils.isEmpty(e.getIntention())?null:e.getIntention())
                    .tagCustomerIssueClassification(ObjectUtils.isEmpty(e.getTagCustomerIssueClassification())?null:e.getTagCustomerIssueClassification())
                    .tagCodeStatus(ObjectUtils.isEmpty(e.getTagCodeStatus())?null:e.getTagCodeStatus())
                    .tagBusinessDomain(ObjectUtils.isEmpty(e.getBusinessDomain())?null:e.getBusinessDomain())
                    .tagHighValueFlag(ObjectUtils.isEmpty(e.getTagHighValueFlag())?null:e.getTagHighValueFlag())
                    .updateTime(LocalDateTime.now())
                    .build();
            tagLibClientEntities.add(build);
        });
        tagLibClientDao.updateBatch(tagLibClientEntities,clientId);
    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), TagLibExcelModel.class, new TagClientExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void batchDeleteTagLibClient(InsTagLibClientModel tagLibClientModel) {
        Assert.notNull(tagLibClientModel.getIds(), "标签ID集合不能为空");
        tagLibClientDao.deleteBatchTagLibClient(tagLibClientModel.getIds());
        this.removeReportTagLibeCache();
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void batchMoveTagLibClient(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getIds()), "标签ID集合不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentId()), "标签父级ID不能为空");
        log.info("开始批量移动标签, idCount:{}, tagParentId:{}, level:{}", tagLibClientModel.getIds().size(),
                tagLibClientModel.getTagParentId(), tagLibClientModel.getLevel());
        // 先查询移动前的标签信息，后续查询结果数据时必须使用待移动标签自身编码。
        List<InsTagLibClientEntity> moveTagEntities = findTagLibClientByIds(tagLibClientModel.getIds());
        tagLibClientDao.batchMoveTagLibClient(tagLibClientModel.getIds(), tagLibClientModel.getTagParentId(),ObjectUtils.isNotEmpty(tagLibClientModel.getLevel())?tagLibClientModel.getLevel():null);
        if (CollectionUtil.isNotEmpty(moveTagEntities)) {
            // 根据目标父级反查完整上级编码，用于同步结果数据中的1~4级标签字段。
            TagClientVo parentHierarchy = findParentHierarchyByTagId(tagLibClientModel.getTagParentId());
            if (ObjectUtils.isNotEmpty(parentHierarchy)) {
                // 异步推送结果数据，避免批量移动接口长时间阻塞。
                ServiceContextHolder.getExecutor().execute(() -> {
                    try {
                        this.pushTagMoveResultData(moveTagEntities, parentHierarchy, tagLibClientModel.getLevel());
                    } catch (Exception e) {
                        log.error("批量移动标签后推送结果数据失败, ids:{}, tagParentId:{}, level:{}", tagLibClientModel.getIds(), tagLibClientModel.getTagParentId(), tagLibClientModel.getLevel(), e);
                    }
                });
            } else {
                log.info("批量移动标签未命中可同步的父级层级编码, tagParentId:{}, level:{}", tagLibClientModel.getTagParentId(), tagLibClientModel.getLevel());
            }
        }
        this.removeReportTagLibeCache();
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void batchUpdateStatusTagLibClient(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getIds()), "标签ID集合不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatus()), "标签状态不能为空");
        log.info("开始批量更新标签状态, idCount:{}, tagStatus:{}", tagLibClientModel.getIds().size(), tagLibClientModel.getTagStatus());
        // 递归收集本次状态变更涉及的分类标签，保证落库与推送范围一致。
        List<InsTagLibClientEntity> affectedTagEntities = updateTagLibStatus(tagLibClientModel.getIds(), tagLibClientModel.getTagStatus());
        if (CollectionUtil.isNotEmpty(affectedTagEntities)) {
            log.info("批量更新标签状态命中受影响标签, count:{}", affectedTagEntities.size());
            // 状态落库后异步推送结果数据中的abandon字段。
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    this.pushTagStatusResultData(affectedTagEntities, tagLibClientModel.getTagStatus());
                } catch (Exception e) {
                    log.error("批量更新标签状态后推送结果数据失败, ids:{}, tagStatus:{}", tagLibClientModel.getIds(), tagLibClientModel.getTagStatus(), e);
                }
            });
        }
        this.removeReportTagLibeCache();
    }

    private List<InsTagLibClientEntity> updateTagLibStatus(List<String> ids, String tagStatus) {
        LinkedHashSet<String> updateIds = new LinkedHashSet<>(ids);
        LinkedHashMap<String, InsTagLibClientEntity> affectedTagMap = new LinkedHashMap<>();
        List<InsTagLibClientEntity> currentTagEntities = findTagLibClientByIds(ids);
        if (CollectionUtil.isNotEmpty(currentTagEntities)) {
            currentTagEntities.stream()
                    .filter(this::canSyncTagResultData)
                    .filter(entity -> ObjectUtils.isNotEmpty(entity.getId()))
                    .forEach(entity -> affectedTagMap.putIfAbsent(entity.getId(), entity));
        }
        // 同步收集所有下级分类标签，确保标签状态与结果数据保持一致。
        List<InsTagLibClientEntity> downAllTagLibHierarchical = tagLibClientDao.findDownAllTagLibHierarchical(ids);
        if (CollectionUtil.isNotEmpty(downAllTagLibHierarchical)) {
            downAllTagLibHierarchical.stream()
                    .filter(this::canSyncTagResultData)
                    .filter(entity -> ObjectUtils.isNotEmpty(entity.getId()))
                    .forEach(entity -> {
                        updateIds.add(entity.getId());
                        affectedTagMap.putIfAbsent(entity.getId(), entity);
                    });
        }
        log.info("批量更新标签状态实际落库标签总数:{}, tagStatus:{}", updateIds.size(), tagStatus);
        tagLibClientDao.batchUpdateStatusTagLibClient(new ArrayList<>(updateIds), tagStatus);
        return new ArrayList<>(affectedTagMap.values());
    }

    private void pushTagStatusResultData(List<InsTagLibClientEntity> affectedTagEntities, String tagStatus) {
        List<ModifyDataModel.ModifyAttrs> attrs = Collections.singletonList(ModifyDataModel.ModifyAttrs.builder()
                .field("abandon")
                .value("1".equals(tagStatus)?"0":"1")
                .build());
        int pushCount = 0;
        for (InsTagLibClientEntity entity : distinctSyncTagEntities(affectedTagEntities)) {
            List<ModifyDataModel.FilterEntity> filters = buildTagCodeFilters(entity.getTagType(), entity.getLevel(), entity.getTagCode());
            if (CollectionUtil.isEmpty(filters)) {
                continue;
            }
            this.modifyResultData(filters, attrs);
            pushCount++;
        }
        if (pushCount == 0) {
            log.warn("标签状态变更未命中可推送的过滤条件，不作任何处理");
            return;
        }
        log.info("标签状态变更结果数据推送完成, filterCount:{}, tagStatus:{}", pushCount, tagStatus);
    }

    private void pushTagMoveResultData(List<InsTagLibClientEntity> moveTagEntities, TagClientVo parentHierarchy, Integer targetLevel) {
        log.info("开始推送标签移动结果数据, moveTagCount:{}, targetLevel:{}", moveTagEntities.size(), targetLevel);
        int pushCount = 0;
        for (InsTagLibClientEntity entity : distinctSyncTagEntities(moveTagEntities)) {
            Integer currentLevel = entity.getLevel();
            Integer moveTargetLevel = ObjectUtils.isNotEmpty(targetLevel) ? targetLevel : currentLevel;
            LinkedHashMap<String, String> attrsMap = new LinkedHashMap<>();
            buildMoveTagModifyAttrs(entity.getTagType(), moveTargetLevel, parentHierarchy)
                    .forEach(attr -> attrsMap.put(attr.getField(), attr.getValue()));
            if (!Objects.equals(currentLevel, moveTargetLevel)) {
                putTagLevelResultAttrs(attrsMap, entity.getTagType(), currentLevel, null, null);
                putTagLevelResultAttrs(attrsMap, entity.getTagType(), moveTargetLevel, entity.getTagCode(), entity.getTagName());
            }
            if (attrsMap.isEmpty()) {
                log.info("标签移动后无需更新结果数据字段, id:{}, tagType:{}, level:{}", entity.getId(), entity.getTagType(), moveTargetLevel);
                continue;
            }
            List<ModifyDataModel.FilterEntity> filters = buildTagCodeFilters(entity.getTagType(), currentLevel, entity.getTagCode());
            if (CollectionUtil.isEmpty(filters)) {
                continue;
            }
            this.modifyResultData(filters, buildModifyAttrs(attrsMap));
            pushCount++;
        }
        if (pushCount == 0) {
            log.info("本次批量移动未命中可用于推送的标签编码");
            return;
        }
        log.info("标签移动结果数据推送完成, filterCount:{}, targetLevel:{}", pushCount, targetLevel);
    }

    private void modifyResultDataByField(String field, Collection<String> values, List<ModifyDataModel.ModifyAttrs> attrs) {
        if (StrUtil.isBlank(field) || CollectionUtil.isEmpty(values) || CollectionUtil.isEmpty(attrs)) {
            return;
        }
        int pushCount = 0;
        for (String value : values.stream().filter(StrUtil::isNotBlank).distinct().toList()) {
            this.modifyResultData(
                    Collections.singletonList(ModifyDataModel.FilterEntity.builder().field(field).value(value).build()),
                    attrs
            );
            pushCount++;
        }
        log.info("按字段推送结果数据完成, field:{}, valueCount:{}", field, pushCount);
    }

    private void modifyResultData(List<ModifyDataModel.FilterEntity> filters, ModifyDataModel.ModifyAttrs resultDataModel) {
        this.modifyResultData(filters, Collections.singletonList(resultDataModel));
    }

    private void modifyResultData(List<ModifyDataModel.FilterEntity> filters, List<ModifyDataModel.ModifyAttrs> resultDataModels) {
        if (CollectionUtil.isEmpty(filters) || CollectionUtil.isEmpty(resultDataModels)) {
            return;
        }
        final String requestId = IdWorker.getId();
        ModifyDataModel build = ModifyDataModel.builder()
                .requestId(requestId)
                .type(2)
                .filters(filters)
                .attrs(resultDataModels)
                .build();
        log.info("开始推送标签相关数据,请求id:{}, filters:{}, attrs:{}", requestId, filters, resultDataModels);
        Result<?> result = dataServiceClient.modifyResultdata(build);
        if (ObjectUtils.isEmpty(result) || !"200".equals(result.getCode()) || ObjectUtils.isEmpty(result.getResult())) {
            log.error("调用数据清洗服务更新标签相关数据接口异常, requestId:{}, filters:{}", requestId, filters);
        } else {
            log.info("调用数据清洗服务更新标签相关数据成功, requestId:{}", requestId);
        }
    }

    private List<InsTagLibClientEntity> distinctSyncTagEntities(List<InsTagLibClientEntity> tagEntities) {
        if (CollectionUtil.isEmpty(tagEntities)) {
            return List.of();
        }
        LinkedHashMap<String, InsTagLibClientEntity> entityMap = new LinkedHashMap<>();
        for (InsTagLibClientEntity entity : tagEntities) {
            if (!canSyncTagResultData(entity)) {
                continue;
            }
            String key = StrUtil.join(":", entity.getTagType(), entity.getLevel(), entity.getTagCode());
            entityMap.putIfAbsent(key, entity);
        }
        return new ArrayList<>(entityMap.values());
    }

    private List<InsTagLibClientEntity> findTagLibClientByIds(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return List.of();
        }
        return tagLibClientDao.findTagLibClientList(InsTagLibClientModel.builder().ids(ids).build());
    }

    private boolean canSyncTagResultData(InsTagLibClientEntity tagEntity) {
        if (ObjectUtils.isEmpty(tagEntity)) {
            return false;
        }
        String tagAttribute = tagEntity.getTagAttribute();
        return (TagAttribute.CATEGORY.getCode().equalsIgnoreCase(tagAttribute)
                || TagAttribute.FINAL_CATEGORY.getCode().equalsIgnoreCase(tagAttribute))
                && ObjectUtils.isNotEmpty(tagEntity.getTagType())
                && ObjectUtils.isNotEmpty(tagEntity.getTagCode())
                && ObjectUtils.isNotEmpty(tagEntity.getLevel());
    }

    private void pushUpdatedTagResultData(InsTagLibClientEntity originalTagEntity,
                                          InsTagLibClientModel currentTagModel,
                                          Integer targetLevel) {
        if (!canSyncTagResultData(originalTagEntity)) {
            return;
        }
        Integer sourceLevel = originalTagEntity.getLevel();
        Integer currentLevel = ObjectUtils.isNotEmpty(targetLevel) ? targetLevel : sourceLevel;
        List<ModifyDataModel.FilterEntity> filters = buildTagCodeFilters(
                originalTagEntity.getTagType(),
                sourceLevel,
                originalTagEntity.getTagCode()
        );
        if (CollectionUtil.isEmpty(filters)) {
            log.warn("更新标签未命中可推送的过滤条件，不作任何处理, id:{}, tagCode:{}, level:{}",
                    originalTagEntity.getId(), originalTagEntity.getTagCode(), sourceLevel);
            return;
        }

        LinkedHashMap<String, String> attrsMap = new LinkedHashMap<>();
        boolean tagNameChanged = !Objects.equals(originalTagEntity.getTagName(), currentTagModel.getTagName());
        boolean tagParentChanged = !Objects.equals(originalTagEntity.getTagParentId(), currentTagModel.getTagParentId());
        boolean levelChanged = !Objects.equals(sourceLevel, currentLevel);

        if (tagParentChanged || levelChanged) {
            if (ObjectUtils.isNotEmpty(currentLevel) && currentLevel > 1) {
                TagClientVo parentHierarchy = findParentHierarchyByTagId(currentTagModel.getTagParentId());
                if (ObjectUtils.isNotEmpty(parentHierarchy)) {
                    buildMoveTagModifyAttrs(originalTagEntity.getTagType(), currentLevel, parentHierarchy)
                            .forEach(attr -> attrsMap.put(attr.getField(), attr.getValue()));
                } else {
                    log.info("更新标签时未命中可同步的父级层级编码, id:{}, tagParentId:{}, targetLevel:{}",
                            originalTagEntity.getId(), currentTagModel.getTagParentId(), currentLevel);
                }
            }
        }

        if (levelChanged) {
            putTagLevelResultAttrs(attrsMap, originalTagEntity.getTagType(), sourceLevel, null, null);
            putTagLevelResultAttrs(attrsMap, originalTagEntity.getTagType(), currentLevel,
                    originalTagEntity.getTagCode(), currentTagModel.getTagName());
        } else if (tagNameChanged) {
            putTagLevelResultAttrs(attrsMap, originalTagEntity.getTagType(), sourceLevel,
                    originalTagEntity.getTagCode(), currentTagModel.getTagName());
        }

        if (attrsMap.isEmpty()) {
            log.info("更新标签后无需要同步的结果数据字段, id:{}", originalTagEntity.getId());
            return;
        }
        log.info("更新标签结果数据推送开始, id:{}, attrCount:{}", originalTagEntity.getId(), attrsMap.size());
        this.modifyResultData(filters, buildModifyAttrs(attrsMap));
    }

    private void putTagLevelResultAttrs(Map<String, String> attrsMap,
                                        String tagType,
                                        Integer level,
                                        String code,
                                        String name) {
        if (ObjectUtils.isEmpty(level)) {
            return;
        }
        String codeField = resolveTagCodeField(tagType, level);
        if (ObjectUtils.isNotEmpty(codeField)) {
            attrsMap.put(codeField, code);
        }
        String nameField = resolveTagNameField(tagType, level);
        if (ObjectUtils.isNotEmpty(nameField)) {
            attrsMap.put(nameField, name);
        }
    }

    private List<ModifyDataModel.FilterEntity> buildTagCodeFilters(String tagType, Integer level, String tagCode) {
        if (StrUtil.isBlank(tagType) || ObjectUtils.isEmpty(level) || StrUtil.isBlank(tagCode)) {
            return List.of();
        }
        String codeField = resolveTagCodeField(tagType, level);
        if (StrUtil.isBlank(codeField)) {
            return List.of();
        }
        return Collections.singletonList(ModifyDataModel.FilterEntity.builder().field(codeField).value(tagCode).build());
    }

    private TagClientVo findParentHierarchyByTagId(String tagParentId) {
        if (ObjectUtils.isEmpty(tagParentId) || "-1".equals(tagParentId) || "0".equals(tagParentId)) {
            return null;
        }
        // 查询目标父级的完整上级编码链，供移动后回填结果数据使用。
        List<TagClientVo> parentHierarchies = tagLibClientDao.findAllUpTagLibHierarchicalByTagId(
                InsTagLibClientModel.builder().ids(Collections.singletonList(tagParentId)).build()
        );
        if (CollectionUtil.isEmpty(parentHierarchies)) {
            return null;
        }
        return parentHierarchies.get(0);
    }

    private List<ModifyDataModel.ModifyAttrs> buildMoveTagModifyAttrs(String tagType, Integer targetLevel, TagClientVo parentHierarchy) {
        if (ObjectUtils.isEmpty(targetLevel) || targetLevel <= 1 || ObjectUtils.isEmpty(parentHierarchy)) {
            return List.of();
        }
        // 标签移动后，仅需回填目标层级之前的上级编码和名称字段。
        Map<Integer, String> levelCodeMap = new LinkedHashMap<>();
        levelCodeMap.put(1, parentHierarchy.getFirstCode());
        levelCodeMap.put(2, parentHierarchy.getSecondCode());
        levelCodeMap.put(3, parentHierarchy.getThirdCode());
        levelCodeMap.put(4, parentHierarchy.getFourthCode());
        Map<Integer, String> levelNameMap = new LinkedHashMap<>();
        levelNameMap.put(1, parentHierarchy.getFirstName());
        levelNameMap.put(2, parentHierarchy.getSecondName());
        levelNameMap.put(3, parentHierarchy.getThirdName());
        levelNameMap.put(4, parentHierarchy.getFourthName());

        LinkedHashMap<String, String> attrsMap = new LinkedHashMap<>();
        for (int level = 1; level < targetLevel && level <= 4; level++) {
            String codeField = resolveTagCodeField(tagType, level);
            String code = levelCodeMap.get(level);
            if (ObjectUtils.isNotEmpty(codeField) && ObjectUtils.isNotEmpty(code)) {
                attrsMap.put(codeField, code);
            }
            String nameField = resolveTagNameField(tagType, level);
            String name = levelNameMap.get(level);
            if (ObjectUtils.isNotEmpty(nameField) && ObjectUtils.isNotEmpty(name)) {
                attrsMap.put(nameField, name);
            }
        }
        return buildModifyAttrs(attrsMap);
    }

    private List<ModifyDataModel.ModifyAttrs> buildTopicUpdateResultAttrs(InsTopicModel insTopicModel) {
        LinkedHashMap<String, String> attrsMap = new LinkedHashMap<>();
        if (CollectionUtil.isEmpty(insTopicModel.getExperienceCode())) {
            // 未调整体验码时，同步观点自身基础属性到结果数据。
            putTopicBaseResultAttrs(attrsMap, insTopicModel);
        } else {
            // 调整体验码时，仅同步各标签体系对应的层级编码字段。
            insTopicModel.getExperienceCode().forEach(experience -> {
                if (ObjectUtils.isEmpty(experience.getType()) || ObjectUtils.isEmpty(experience.getParentId())) {
                    return;
                }
                TagClientVo parentHierarchy = findParentHierarchyByTagId(experience.getParentId());
                if (ObjectUtils.isEmpty(parentHierarchy)) {
                    log.info("观点体验码未命中可同步的父级层级编码, type:{}, parentId:{}", experience.getType(), experience.getParentId());
                    return;
                }
                putTopicHierarchyAttrs(attrsMap, experience.getType(), parentHierarchy);
            });
        }

        return buildModifyAttrs(attrsMap);
    }

    private List<ModifyDataModel.ModifyAttrs> buildSaveTopicResultAttrs(InsTopicModel insTopicModel, List<InsTagLibClientEntity> originalTopics) {
        LinkedHashMap<String, String> attrsMap = new LinkedHashMap<>();
        // 保存观点时，同步观点基础属性。
        putTopicBaseResultAttrs(attrsMap, insTopicModel);

        if (CollectionUtil.isNotEmpty(insTopicModel.getExperienceCode())) {
            LinkedHashSet<String> currentTypes = new LinkedHashSet<>();
            // 同步本次提交的体验码层级编码。
            insTopicModel.getExperienceCode().forEach(experience -> {
                if (ObjectUtils.isEmpty(experience.getType()) || ObjectUtils.isEmpty(experience.getParentId())) {
                    return;
                }
                currentTypes.add(experience.getType());
                TagClientVo parentHierarchy = findParentHierarchyByTagId(experience.getParentId());
                if (ObjectUtils.isEmpty(parentHierarchy)) {
                    log.info("保存观点时未命中可同步的父级层级编码, type:{}, parentId:{}", experience.getType(), experience.getParentId());
                    return;
                }
                putTopicHierarchyAttrs(attrsMap, experience.getType(), parentHierarchy);
            });

            // 若编辑时移除了某套标签体系，则将该体系对应的结果数据字段清空。
            LinkedHashSet<String> removedTypes = CollectionUtil.isEmpty(originalTopics) ? new LinkedHashSet<>() :
                    originalTopics.stream()
                            .map(InsTagLibClientEntity::getTagType)
                            .filter(ObjectUtils::isNotEmpty)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
            removedTypes.removeAll(currentTypes);
            removedTypes.forEach(type -> clearTopicHierarchyAttrs(attrsMap, type));
        }

        return buildModifyAttrs(attrsMap);
    }

    private void putTopicBaseResultAttrs(Map<String, String> attrsMap, InsTopicModel insTopicModel) {
        putResultAttrIfPresent(attrsMap, "sentiment", insTopicModel.getEmotion());
        putResultAttrIfPresent(attrsMap, "intention", insTopicModel.getIntention());
        putResultAttrIfPresent(attrsMap, "abandon", ObjectUtils.isNotEmpty(insTopicModel.getTagStatus())?("1".equals(insTopicModel.getTagStatus())?"0":"1"):null);
        putResultAttrIfPresent(attrsMap, "topic_text", insTopicModel.getTagName());
    }

    private void putResultAttrIfPresent(Map<String, String> attrsMap, String field, String value) {
        if (ObjectUtils.isNotEmpty(field) && ObjectUtils.isNotEmpty(value)) {
            attrsMap.put(field, value);
        }
    }

    private List<ModifyDataModel.ModifyAttrs> buildModifyAttrs(Map<String, String> attrsMap) {
        return attrsMap.entrySet().stream()
                .map(entry -> ModifyDataModel.ModifyAttrs.builder().field(entry.getKey()).value(entry.getValue()).build())
                .collect(Collectors.toList());
    }

    private void putTopicHierarchyAttrs(Map<String, String> attrsMap, String tagType, TagClientVo parentHierarchy) {
        putTopicHierarchyAttr(attrsMap, tagType, 1, parentHierarchy.getFirstCode(), parentHierarchy.getFirstName());
        putTopicHierarchyAttr(attrsMap, tagType, 2, parentHierarchy.getSecondCode(), parentHierarchy.getSecondName());
        putTopicHierarchyAttr(attrsMap, tagType, 3, parentHierarchy.getThirdCode(), parentHierarchy.getThirdName());
        putTopicHierarchyAttr(attrsMap, tagType, 4, parentHierarchy.getFourthCode(), parentHierarchy.getFourthName());
    }

    private void clearTopicHierarchyAttrs(Map<String, String> attrsMap, String tagType) {
        clearTopicHierarchyAttr(attrsMap, tagType, 1);
        clearTopicHierarchyAttr(attrsMap, tagType, 2);
        clearTopicHierarchyAttr(attrsMap, tagType, 3);
        clearTopicHierarchyAttr(attrsMap, tagType, 4);
    }

    private void clearTopicHierarchyAttr(Map<String, String> attrsMap, String tagType, int level) {
        String codeField = resolveTagCodeField(tagType, level);
        if (ObjectUtils.isNotEmpty(codeField)) {
            attrsMap.put(codeField, null);
        }
        String nameField = resolveTagNameField(tagType, level);
        if (ObjectUtils.isNotEmpty(nameField)) {
            attrsMap.put(nameField, null);
        }
    }

    private void putTopicHierarchyAttr(Map<String, String> attrsMap, String tagType, int level, String code, String name) {
        String codeField = resolveTagCodeField(tagType, level);
        if (ObjectUtils.isNotEmpty(codeField) && ObjectUtils.isNotEmpty(code)) {
            attrsMap.put(codeField, code);
        }
        String nameField = resolveTagNameField(tagType, level);
        if (ObjectUtils.isNotEmpty(nameField) && ObjectUtils.isNotEmpty(name)) {
            attrsMap.put(nameField, name);
        }
    }

    private String resolveTagNameField(String tagType, int level) {
        return switch (tagType) {
            case "CA" -> switch (level) {
                case 1 -> "dom_tag_first";
                case 2 -> "dom_tag_second";
                case 3 -> "dom_tag_three";
                case 4 -> "dom_tag_four";
                default -> null;
            };
            case "JOUR" -> switch (level) {
                case 1 -> "ujy_tag_first";
                case 2 -> "ujy_tag_second";
                case 3 -> "ujy_tag_three";
                case 4 -> "ujy_tag_four";
                default -> null;
            };
            case "CPT" -> switch (level) {
                case 1 -> "cpt_tag_first";
                case 2 -> "cpt_tag_second";
                case 3 -> "cpt_tag_three";
                case 4 -> "cpt_tag_four";
                default -> null;
            };
            case "NPS" -> switch (level) {
                case 1 -> "nps_tag_first";
                case 2 -> "nps_tag_second";
                case 3 -> "nps_tag_three";
                case 4 -> "nps_tag_four";
                default -> null;
            };
            case "VRT" -> switch (level) {
                case 1 -> "vtr_tag_first";
                case 2 -> "vtr_tag_second";
                case 3 -> "vtr_tag_three";
                case 4 -> "vtr_tag_four";
                default -> null;
            };
            case "PRO" -> switch (level) {
                case 1 -> "cma_tag_first";
                case 2 -> "cma_tag_second";
                case 3 -> "cma_tag_three";
                case 4 -> "cma_tag_four";
                default -> null;
            };
            default -> null;
        };
    }

    private String resolveTagCodeField(String tagType, int level) {
        return switch (tagType) {
            case "CA" -> switch (level) {
                case 1 -> "dom_tag_first_code";
                case 2 -> "dom_tag_second_code";
                case 3 -> "dom_tag_three_code";
                case 4 -> "dom_tag_four_code";
                default -> null;
            };
            case "JOUR" -> switch (level) {
                case 1 -> "ujy_tag_first_code";
                case 2 -> "ujy_tag_second_code";
                case 3 -> "ujy_tag_three_code";
                case 4 -> "ujy_tag_four_code";
                default -> null;
            };
            case "CPT" -> switch (level) {
                case 1 -> "cpt_tag_first_code";
                case 2 -> "cpt_tag_second_code";
                case 3 -> "cpt_tag_three_code";
                case 4 -> "cpt_tag_four_code";
                default -> null;
            };
            case "NPS" -> switch (level) {
                case 1 -> "nps_tag_first_code";
                case 2 -> "nps_tag_second_code";
                case 3 -> "nps_tag_three_code";
                case 4 -> "nps_tag_four_code";
                default -> null;
            };
            case "VRT" -> switch (level) {
                case 1 -> "vtr_tag_first_code";
                case 2 -> "vtr_tag_second_code";
                case 3 -> "vtr_tag_three_code";
                case 4 -> "vtr_tag_four_code";
                default -> null;
            };
            case "PRO" -> switch (level) {
                case 1 -> "cma_tag_first_code";
                case 2 -> "cma_tag_second_code";
                case 3 -> "cma_tag_three_code";
                case 4 -> "cma_tag_four_code";
                default -> null;
            };
            default -> null;
        };
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findAllFinalTagLib(InsTagLibClientModel tagLibClientModel) {
//        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getAppClient()), "clientId不能为空");
//        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentIds()), "tagParentId不能为空");
        if (CollectionUtil.isEmpty(tagLibClientModel.getTagStatusList())){
            tagLibClientModel.setTagStatusList(Lists.newArrayList("1"));
        }
        List<InsTagLibClientEntity> tagLibCategoryEntity = tagLibClientDao.findDownTagLibHierarchical(tagLibClientModel.getTagParentIds(), tagLibClientModel.getTagStatusList(), null, tagLibClientModel.getIds(), null,null);
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibCategoryEntity);
        this.removeReportTagLibeCache();
        return tagLibCategoryVos;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public List<TagLibCategoryVo> findClientCategoryTree(InsTagLibClientModel tagLibClientModel) {
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getAppClient()), "clientId不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        InsTagLibClientModel tagLibClientModels = InsTagLibClientModel.builder().tagAttribute(TagAttribute.CATEGORY.getCode()).tagName(ObjectUtils.isEmpty(tagLibClientModel.getTagName()) ? null : tagLibClientModel.getTagName()).tagType(ObjectUtils.isEmpty(tagLibClientModel.getTagType()) ? null : tagLibClientModel.getTagType()).build();
        List<InsTagLibClientEntity> tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModels);
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            return Collections.EMPTY_LIST;
        }
        //递归树
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(tagLibClientList);

        //获取顶级渠道
        List<TagLibCategoryVo> topTagLibList;
        topTagLibList = tagLibCategoryVos.stream().filter(e -> "-1".equalsIgnoreCase(e.getTagParentId())).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(topTagLibList)) {
            return tagLibCategoryVos;
        }
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap = tagLibCategoryVos.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        this.tagLibClientCategoryTree(topTagLibList, tagLibbCategoryMap);
        if (ObjectUtils.isNotEmpty(topTagLibList)) {
            topTagLibList = topTagLibList.stream().sorted(Comparator.comparing(TagLibCategoryVo::getId,Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        return topTagLibList;
    }

    @Override
    @SwitchClientDS(objectAttribute = "tagLibClientModel.appClient")
    public void batchDownloadTagLibClient(InsTagLibClientModel tagLibClientModel, HttpServletResponse response) {
        //单独参数校验
        Assert.hasLength(tagLibClientModel.getAppClient(), "应用客户不能为空");
        Assert.isTrue(clientMappings.getMappings().containsKey(tagLibClientModel.getAppClient()), "应用客户不存在");
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagType()), "标签类型不能为空");
        tagLibClientModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        List<InsTagLibClientEntity> tagLibClientList;
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagParentId()) && !tagLibClientModel.getTagParentId().equalsIgnoreCase("0")) {
            tagLibClientList = tagLibClientDao.findDownTagLibHierarchical(Arrays.asList(tagLibClientModel.getTagParentId()), ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatusList()) ? tagLibClientModel.getTagStatusList() : null
                    , ObjectUtils.isNotEmpty(tagLibClientModel.getTagName()) ? tagLibClientModel.getTagName() : null,tagLibClientModel.getIds()
                    , ObjectUtils.isNotEmpty(tagLibClientModel.getCodes())?tagLibClientModel.getCodes() : null,null);
        } else {
            tagLibClientList = tagLibClientDao.findTagLibClientList(tagLibClientModel);
        }
        List<TagLibClientQYTemplateVo> collect = new ArrayList<>();
        if (ObjectUtils.isEmpty(tagLibClientList)) {
            log.info("暂无标签信息");

        } else {
//            tagLibClientList = tagLibClientList.stream().filter(e -> !e.getTagParentId().startsWith("-")).collect(Collectors.toList());
            List<String> tagLibIds = tagLibClientList.stream().map(e -> e.getId()).collect(Collectors.toList());
            List<InsTagLibClientEntity> tagLibClientHierarchical = tagLibClientDao.findTagLibClientHierarchical(tagLibIds);
            Map<Object, List<InsTagLibClientEntity>> map = tagLibClientHierarchical.stream().collect(Collectors.groupingBy(e -> e.getId()));
            collect = tagLibClientList.stream().map(e -> {
                TagLibClientVo tagLibClientVo = convertMapperService.tagLibClientEntityConvertVo(e);
                //标签类型
                TagLibClientQYTemplateVo build = TagLibClientQYTemplateVo.builder()
                        .tagType(ObjectUtils.isNotEmpty(e.getTagType()) ? TagLibeType.getByCode(e.getTagType()).getText() : "")
                        .tagDesc(e.getTagDescription())
                        .status(ObjectUtils.isNotEmpty(e.getTagStatus()) ? TagLibelStatus.getByCode(e.getTagStatus()).getText() : "")
//                        .finalTag(e.getTagName())
                        .build();
                if (ObjectUtils.isNotEmpty(e.getTagName()) && !e.getTagName().contains("全部分类") && ObjectUtils.isNotEmpty(e.getTagType()) && e.getTagType().equals(TagLibeType.QY.getCode())) {
                    build.setFinalTag(e.getTagName());
                } else if (ObjectUtils.isNotEmpty(e.getTagName()) && !e.getTagName().contains("全部分类") && ObjectUtils.isNotEmpty(e.getTagType()) && !e.getTagType().equals(TagLibeType.QY.getCode())) {
                    build.setFourTypeCategory(e.getTagName());
                }

                if (ObjectUtils.isNotEmpty(e.getCarType())) {
                    List<String> carType = e.getCarType();
                    List<String> collect1 = carType.stream().map(k -> CarType.getByCode(k).getText()).collect(Collectors.toList());
                    build.setCarType(String.join("、", collect1));
                }
                if (ObjectUtils.isNotEmpty(e.getSeriousness())) {
                    build.setSeriousness(SeriousnessEnum.getByCode(e.getSeriousness()).getText());
                }

                if (ObjectUtils.isNotEmpty(e.getEnergyType())) {
                    List<String> energyCategory = e.getEnergyType();
                    List<String> collect1 = energyCategory.stream().map(k -> EnergyCategoryEnum.getByCode(k).getText()).collect(Collectors.toList());
                    build.setEnergyCategory(String.join("、", collect1));
                }

//                if (ObjectUtils.isNotEmpty(e.getUserJourney())) {
//                    List<String> userJourney = e.getUserJourney();
//                    List<String> collect1 = userJourney.stream().map(k -> {
//                        UserJoureyEnum byCode = UserJoureyEnum.getByCode(k);
//                        return byCode.getText();
//                    }).collect(Collectors.toList());
//                    build.setUserJourney(String.join("、", collect1));
//                }

                final String tagType = tagLibClientVo.getTagType();
                String tagParentId = tagLibClientVo.getTagParentId();
                StringBuffer buffer = new StringBuffer();
                while (ObjectUtils.isNotEmpty(tagParentId)) {
                    if (map.containsKey(tagParentId)) {
                        List<InsTagLibClientEntity> insTagLibEntity = map.get(tagParentId);
                        if (insTagLibEntity.size() > 1) {
                            InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().filter(k -> k.getTagType().equalsIgnoreCase(tagType)).findFirst().get();
                            String tagName = "#".concat(insTagLibEntity1.getTagName());
                            buffer.insert(0, tagName);
                        } else {
                            InsTagLibClientEntity insTagLibEntity1 = insTagLibEntity.stream().findFirst().get();
                            String tagName = "#".concat(insTagLibEntity1.getTagName());
                            buffer.insert(0, tagName);
                            tagParentId = insTagLibEntity1.getTagParentId();
                        }
                    } else {
                        break;
                    }
                }

                String codes = buffer.toString();
                if (codes.contains("全部分类")) {
                    codes = codes.substring(6);
                } else if (codes.startsWith("#")) {
                    codes = codes.substring(1);
                }
                String[] split = codes.split("#");
                for (int i = 0; i < split.length; i++) {
                    if (i == 0) {
                        build.setFirstTypeCategory(split[i]);
                    } else if (i == 1) {
                        build.setSecondTypeCategory(split[i]);
                    } else if (i == 2) {
                        build.setThreeTypeCategory(split[i]);
                    } else if (i == 3) {
                        build.setFourTypeCategory(split[i]);
                    }
                }
                return build;
            }).collect(Collectors.toList());
        }
        List<TagLibClientQYTemplateVo> collect1 = collect.stream().filter(e -> ObjectUtils.isNotEmpty(e.getTagType())).collect(Collectors.toList());
        String fileName = TagLibeType.getByCode(tagLibClientModel.getTagType()).getText();
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagType())&&!tagLibClientModel.getTagType().equals(TagLibeType.QY.getCode())){
            List<TagLibClientTemplateVo> tagLibClientTemplateVos = convertMapperService.tagLibClientQYTemplateConvertTemplateVoList(collect1);
            try {
                ExcelUtil.writeExcel(response, tagLibClientTemplateVos, fileName+"标签.xlsx", fileName+"标签", TagLibClientTemplateVo.class, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                ExcelUtil.writeExcel(response, collect1, fileName+"标签.xlsx", fileName+"标签", TagLibClientQYTemplateVo.class, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    @Override
    public void removeReportTagLibeCache() {
        this.removeTagLibeCache(ServiceContextHolder.getClientId());
    }


    /**
     * @param tagLibbCategoryMap
     * @param topTagLibList
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:34
     * @描述 组建标签分类树
     **/
    void tagLibClientCategoryTree(List<TagLibCategoryVo> topTagLibList, Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap) {
        if (ObjectUtils.isEmpty(topTagLibList)) {
            return;
        }
        for (TagLibCategoryVo tagLibCategoryVo : topTagLibList) {
            if (tagLibCategoryVo.getId().startsWith("-")) {
                break;
            }
            List<TagLibCategoryVo> tagLibCategoryVos = tagLibbCategoryMap.get(tagLibCategoryVo.getId());
            this.tagLibClientCategoryTree(tagLibCategoryVos, tagLibbCategoryMap);
            if(ObjectUtils.isNotEmpty(tagLibCategoryVos)){
                tagLibCategoryVos = tagLibCategoryVos.stream().sorted(Comparator.comparing(TagLibCategoryVo::getSort)).collect(Collectors.toList());
            }
            tagLibCategoryVo.setChild(tagLibCategoryVos);
        }
    }

    private void checkParams(InsTagLibClientModel tagLibClientModel) {
        log.debug("开始入参校验:");
        Assert.hasLength(tagLibClientModel.getTagType(), "标签类型不能为空");
        Assert.hasLength(tagLibClientModel.getTagName(), "标签名称不能为空");
        Assert.isTrue(tagLibClientModel.getTagName().length() <= 50, "标签名称长度不能超过50");
        Assert.hasLength(tagLibClientModel.getTagParentId(), "标签所属分类不能为空");
        Assert.hasLength(tagLibClientModel.getTagStatus(), "标签状态不允许为空");
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagDescription())) {
            Assert.isTrue(tagLibClientModel.getTagDescription().length() <= 1000, "标签描述长度不能超过1000");
        }
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getSynonyms())) {
            Assert.isTrue(tagLibClientModel.getSynonyms().length() <= 10000, "同义词长度不能超过10000");
        }
        log.debug("入参检验结束");
    }

    /**
     * 获取新增标签code
     *
     * @param type 标签类型
     * @param pid  父标签code
     * @return
     */
    public String getTagCode(String type, String pid, String clientCode) {
        Assert.hasLength(type, "type类型不允许为空");
        Assert.hasLength(pid, "父标签pCode不允许为空");
        String ROOT_PID_VALUE = "0";
        String tagCode = null;
        /*
         * 分成三种情况
         * 1.数据库无数据
         * 2.添加子节点，无兄弟元素
         * 3.添加子节点有兄弟元素
         * */
        //找同类 确定上一个最大的code值
        List<InsTagLibClientEntity> list = tagLibClientDao.findTagLibByQueryWrapper(type, pid);
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(pid)) {
                //情况1 根节点
                tagCode = clientCode + "1001";
            } else {
                //情况2  首个子节点
                List<InsTagLibClientEntity> tagLibChildNodeByParentId = tagLibClientDao.findTagLibChildNodeByParentId(pid);
                Assert.isTrue(ObjectUtils.isNotEmpty(tagLibChildNodeByParentId), "父级分类或标签不存在");
                InsTagLibClientEntity insTagLibEntity = tagLibChildNodeByParentId.stream().findFirst().get();
                tagCode = insTagLibEntity.getTagCode() + "001";
            }
        } else {
            //情况3
            String oldCode = list.get(0).getTagCode();

            long newCode = Long.parseLong(oldCode.substring(4, oldCode.length()));
            newCode += 1;
            tagCode = clientCode + newCode;
        }
        return tagCode;
    }


    public String getSoleTagCode(String clientCode){
        String tagCode = null;
        String maxCode = tagLibClientDao.findMaxCode();
        if(ObjectUtils.isEmpty(maxCode)){
            //首个根节点
            tagCode =  clientCode + "1001";
        }else{
            long newCode = Long.parseLong(maxCode.substring(4,maxCode.length()));
            newCode += 1;
            tagCode = clientCode + newCode;
        }

        return tagCode;
    }
}
