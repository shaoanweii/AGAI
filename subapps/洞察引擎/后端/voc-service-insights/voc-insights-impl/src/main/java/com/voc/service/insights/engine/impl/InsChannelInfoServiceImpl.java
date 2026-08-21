package com.voc.service.insights.engine.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.constants.EnableStatusEnum;
import com.voc.service.insights.engine.dao.InsChannelInfoDao;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.enums.ChannelType;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.ChannelExcelListener;
import com.voc.service.insights.engine.model.ChannelExcelModel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:40
 * @描述:
 **/
@Service
public class InsChannelInfoServiceImpl implements IInsChannelInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsChannelInfoServiceImpl.class);
    @Autowired
    InsChannelInfoDao channelDao;
    @Autowired
    InsConvertMapperService channelInfoConvert;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.ChannelInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 15:41
     * @描述 获取渠道分布树
     **/
//    @Override
//    public List<ChannelInfoVo> findChannelInfoTree() {
//        List<InsChannelInfoEntity> allChannel = channelDao.findAllChannel();
//        if(ObjectUtils.isEmpty(allChannel)){
//            log.info("无渠道信息");
//            return Collections.EMPTY_LIST;
//        }
//        log.debug("转换前 allChannel:{}", JSONArray.toJSONString(allChannel));
//        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(allChannel);
//        log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
//        //获取顶级渠道
//        List<ChannelInfoVo> topChannel = channelInfoVos.stream().filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
//        //将全部渠道放入map中，用于递归时使用，减少数据库查询
//        Map<String, List<ChannelInfoVo>> channelMap = channelInfoVos.stream().collect(Collectors.groupingBy(ChannelInfoVo::getParentId));
//        this.channelTree(topChannel,channelMap);
//        return topChannel;
//    }

//    @Override
////    @Cached(name = "users:", key = "'C{userId}:tokens:C{token}:channel'", expire = 60 * 60, cacheType = CacheType.BOTH )
//    public List<ChannelInfoVo> findAllChannelInfo() {
//        log.trace("读取数据库");
//        List<InsChannelInfoEntity> allChannel = channelDao.findAllChannel();
//        if (ObjectUtils.isEmpty(allChannel)) {
//            log.info("无渠道信息");
//            return null;
//        }
//        log.debug("转换前 allChannel:{}", JSONArray.toJSONString(allChannel));
//        List<ChannelInfoVo> list = channelInfoConvert.channelEntityListConvertVoList(allChannel);
//        log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(list));
//        return list;
//    }
    @Override
    public void saveChannel(InsChannelInfoModel insChannelInfoModel) {
        this.checkParameter(insChannelInfoModel);
        log.debug("转换前 insChannelInfoModel:{}", insChannelInfoModel);
        final String id = IdWorker.getId();
        String channelCode = "";
        List<InsChannelInfoEntity> channelInfoByName = channelDao.findChannelInfoByName(insChannelInfoModel.getName(), insChannelInfoModel.getClientId());
        if (ChannelType.CATEGORY.getCode().equalsIgnoreCase(insChannelInfoModel.getType())) {
            log.debug("校验{}下名称是否存在", insChannelInfoModel.getType());
            List<InsChannelInfoEntity> collect = channelInfoByName.stream().filter(e -> e.getType().equalsIgnoreCase(ChannelType.CATEGORY.getCode()) && e.getName().equalsIgnoreCase(insChannelInfoModel.getName()) && e.getParentId().equalsIgnoreCase(insChannelInfoModel.getParentId())).collect(Collectors.toList());
            Assert.isTrue(ObjectUtils.isEmpty(collect), "渠道分类名称已存在");
            List<InsChannelInfoEntity> collect1 = channelInfoByName.stream().filter(e -> e.getType().equalsIgnoreCase(ChannelType.CHANNEL.getCode()) && e.getName().equalsIgnoreCase(insChannelInfoModel.getName())).collect(Collectors.toList());
            Assert.isTrue(ObjectUtils.isEmpty(collect1), "渠道名称已存在");
            channelCode = id;
        } else {
            log.debug("校验渠道分类下{}名称是否存在", insChannelInfoModel.getType());
            List<InsChannelInfoEntity> collect = channelInfoByName.stream().filter(e -> e.getType().equalsIgnoreCase(insChannelInfoModel.getType()) && e.getName().equalsIgnoreCase(insChannelInfoModel.getName())).collect(Collectors.toList());
            Assert.isTrue(ObjectUtils.isEmpty(collect), "渠道名称已存在");
            channelCode = this.createChannelCode(insChannelInfoModel);
        }

        InsChannelInfoEntity insChannelInfoEntity = channelInfoConvert.channelInfoModelConvertEntity(insChannelInfoModel);
        insChannelInfoEntity.setId(id);
        insChannelInfoEntity.setCode(channelCode);
        insChannelInfoEntity.setCreateTime(LocalDateTime.now());
        log.debug("转换后 insChannelInfoEntity:{}", insChannelInfoEntity);
        channelDao.saveChannel(insChannelInfoEntity);
    }

    @Override
    public void updateChannel(InsChannelInfoModel insChannelInfoModel) {
        //单独参数校验
        Assert.hasLength(insChannelInfoModel.getId(), "id不允许为空");
        this.checkParameter(insChannelInfoModel);
        Assert.isTrue(insChannelInfoModel.getId().equalsIgnoreCase(insChannelInfoModel.getParentId()) ? false : true, "父级id不允许与id一致");
        //校验名称
        List<InsChannelInfoEntity> channelInfoByName = channelDao.findChannelInfoByName(insChannelInfoModel.getName(), insChannelInfoModel.getClientId());
        if (ChannelType.CATEGORY.getCode().equalsIgnoreCase(insChannelInfoModel.getType())) {
            log.debug("校验{}下名称是否存在", insChannelInfoModel.getType());
            List<InsChannelInfoEntity> collect = channelInfoByName.stream().filter(e -> e.getName().equalsIgnoreCase(insChannelInfoModel.getName()) && e.getType().equalsIgnoreCase(insChannelInfoModel.getType()) && e.getParentId().equalsIgnoreCase(insChannelInfoModel.getParentId())).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(collect)) {
                List<InsChannelInfoEntity> collect1 = collect.stream().filter(e -> !e.getId().equalsIgnoreCase(insChannelInfoModel.getId())).collect(Collectors.toList());
                Assert.isTrue(ObjectUtils.isEmpty(collect1), "渠道分类名称已存在");
            }
            List<InsChannelInfoEntity> collect1 = channelInfoByName.stream().filter(e -> e.getType().equalsIgnoreCase(ChannelType.CHANNEL.getCode()) && e.getName().equalsIgnoreCase(insChannelInfoModel.getName())).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(collect1)) {
                List<InsChannelInfoEntity> collect2 = collect1.stream().filter(e -> !e.getId().equalsIgnoreCase(insChannelInfoModel.getId())).collect(Collectors.toList());
                Assert.isTrue(ObjectUtils.isEmpty(collect2), "渠道名称已存在");
            }
        } else {
            log.debug("校验渠道分类下{}名称是否存在", insChannelInfoModel.getType());
            List<InsChannelInfoEntity> collect = channelInfoByName.stream().filter(e -> e.getType().equalsIgnoreCase(insChannelInfoModel.getType()) && e.getName().equalsIgnoreCase(insChannelInfoModel.getName())).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(collect)) {
                List<InsChannelInfoEntity> collect1 = collect.stream().filter(e -> !e.getId().equalsIgnoreCase(insChannelInfoModel.getId())).collect(Collectors.toList());
                Assert.isTrue(ObjectUtils.isEmpty(collect1), "渠道名称已存在");
            }
        }

        log.debug("转换前 insChannelInfoModel:{}", insChannelInfoModel);
        InsChannelInfoEntity insChannelInfoEntity = channelInfoConvert.channelInfoModelConvertEntity(insChannelInfoModel);
        log.debug("转换后 insChannelInfoEntity:{}", insChannelInfoEntity);
        insChannelInfoEntity.setUpdateTime(LocalDateTime.now());
        channelDao.updateChannel(insChannelInfoEntity);
    }

    @Override
    public List<ChannelInfoVo> findChannelCategoryTree(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        insChannelInfoModel.setType(ChannelType.CATEGORY.getCode());
        List<InsChannelInfoEntity> allChannelCategory = channelDao.findChannel(insChannelInfoModel);
        if (ObjectUtils.isEmpty(allChannelCategory)) {
            log.info("无渠道分类信息");
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("转换前 allChannelCategory:{}", JSONArray.toJSONString(allChannelCategory));
        }
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(allChannelCategory);
        if(log.isDebugEnabled()) {
        log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
        }
        //获取顶级渠道
        List<ChannelInfoVo> topChannel = channelInfoVos.stream().filter(e -> "-1".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<ChannelInfoVo>> channelMap = channelInfoVos.stream().collect(Collectors.groupingBy(ChannelInfoVo::getParentId));
        this.channelTree(topChannel, channelMap);
        return topChannel;
    }

    @Override
    public PageInfo findChannelInfoByParentId(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        Assert.hasLength(insChannelInfoModel.getParentId(), "父级渠道不允许为空");
        insChannelInfoModel.setType(ChannelType.CHANNEL.getCode());
//        PageHelper.startPage(insChannelInfoModel.getPageNum(), insChannelInfoModel.getPageSize());
        List<InsChannelInfoEntity> allChannelCategory = new ArrayList<>();
        if(insChannelInfoModel.getParentId().equals("0")){
            allChannelCategory = channelDao.findChannel(insChannelInfoModel);
        }else {
           allChannelCategory = channelDao.findChannelInfoByParentId(insChannelInfoModel);
        }
        if (ObjectUtils.isEmpty(allChannelCategory)) {
            log.info("无渠道分类信息");
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(allChannelCategory);
        if(log.isDebugEnabled()) {
            log.debug("转换前 allChannelCategory:{}", JSONArray.toJSONString(allChannelCategory));
        }
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(allChannelCategory);
        if(log.isDebugEnabled()) {
            log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
        }
        pageInfo.setList(channelInfoVos);
        pageInfo.setTotal(pageInfo.getTotal()-1);
        return pageInfo;
    }

    @Override
    public void deleteChannel(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        Assert.hasLength(insChannelInfoModel.getId(), "分类id不允许为空");
        List<InsChannelInfoEntity> downChannelInfoByParentId = channelDao.findDownChannelInfoByParentId(insChannelInfoModel);
        Assert.isTrue(ObjectUtils.isEmpty(downChannelInfoByParentId), "该渠道分类或其子分类下存在渠道信息，请先删除渠道信息");
        channelDao.deleteChannel(insChannelInfoModel);
    }

    @Override
    public List<ChannelInfoVo> findChannelTree(InsChannelInfoModel insChannelInfoModel) {
//        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        if(ObjectUtils.isNotEmpty(insChannelInfoModel.getLevel())){
            ArrayList<Integer> collect = IntStream.range(1, insChannelInfoModel.getLevel()+1).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            insChannelInfoModel.setLevels( collect);
        }
        if(ObjectUtils.isEmpty(insChannelInfoModel.getClientId())){
            insChannelInfoModel.setClientId(ServiceContextHolder.getClientId());
        }
        List<InsChannelInfoEntity> allChannelCategory = channelDao.findChannel(insChannelInfoModel);
        if (ObjectUtils.isEmpty(allChannelCategory)) {
            log.info("无渠道分类信息");
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("转换前 allChannelCategory:{}", JSONArray.toJSONString(allChannelCategory));
        }
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(allChannelCategory);
        if(log.isDebugEnabled()) {
            log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
        }
        if (channelInfoVos.size() == 1) {
            return channelInfoVos;
        }
        //获取顶级渠道
        List<ChannelInfoVo> topChannel = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
        if (topChannel.size() == 1) {
            List<ChannelInfoVo> collect = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).filter(e -> !"0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
            return collect;
        }
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<ChannelInfoVo>> channelMap = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).collect(Collectors.groupingBy(ChannelInfoVo::getParentId));
        this.channelTree(topChannel, channelMap);
        return topChannel;
    }

    @Override
    public List<String> findDownChannelByCode(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        List<InsChannelInfoEntity> downChannelInfoByCode = channelDao.findDownChannelInfoByCode(insChannelInfoModel);
        if(ObjectUtils.isEmpty(downChannelInfoByCode)){
            log.info("无末级渠道信息");
            return List.of();
        }
        List<String> codes = downChannelInfoByCode.stream().filter(e -> ObjectUtils.isNotEmpty(e.getCode())).map(InsChannelInfoEntity::getCode).collect(Collectors.toList());
        return codes;
    }

    @Override
    public List<ChannelInfoVo> findAllChannelInfo(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        List<InsChannelInfoEntity> allChannelInfo = channelDao.findAllChannelInfo(insChannelInfoModel);
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(allChannelInfo);
        return channelInfoVos;
    }

    @Override
    public List<ChannelInfoVo> findAll(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        insChannelInfoModel.setStatus("1");
        List<InsChannelInfoEntity> list = channelDao.findChannel(insChannelInfoModel);
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(list);
        return channelInfoVos;
    }

    @Override
    public List<ChannelInfoVo> upwardFindChannelHierarchical(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        List<InsChannelInfoEntity> upwardFindChannelHierarchical = channelDao.upwardFindChannelHierarchical(insChannelInfoModel);
        if (log.isDebugEnabled()) {
            log.debug("转换前 allChannelCategory:{}", JSONArray.toJSONString(upwardFindChannelHierarchical));
        }
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(upwardFindChannelHierarchical);
        if (log.isDebugEnabled()) {
            log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
        }
        //获取顶级渠道
        List<ChannelInfoVo> topChannel = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
        if (topChannel.size() == 1) {
            List<ChannelInfoVo> collect = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).filter(e -> !"0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
            return collect;
        }
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<ChannelInfoVo>> channelMap = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).collect(Collectors.groupingBy(ChannelInfoVo::getParentId));
        this.channelTree(topChannel, channelMap);
        return topChannel;
    }

    @Override
    public List<ChannelInfoVo> upwardFindChannelHierarchicalTree(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        List<String> channelIdsByChannelCode = channelDao.findChannelIdsByChannelCode(insChannelInfoModel);
        insChannelInfoModel.setChannelIds(channelIdsByChannelCode);
        List<InsChannelInfoEntity> upwardFindChannelHierarchical = channelDao.upwardFindChannelHierarchical(insChannelInfoModel);
        if (log.isDebugEnabled()) {
            log.debug("转换前 allChannelCategory:{}", JSONArray.toJSONString(upwardFindChannelHierarchical));
        }
        List<ChannelInfoVo> channelInfoVos = channelInfoConvert.channelEntityListConvertVoList(upwardFindChannelHierarchical);
        if (log.isDebugEnabled()) {
            log.debug("转换后 channelInfoVos:{}", JSONArray.toJSONString(channelInfoVos));
        }
        //获取顶级渠道
        List<ChannelInfoVo> topChannel = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
        //将全部渠道放入map中，用于递归时使用，减少数据库查询
        Map<String, List<ChannelInfoVo>> channelMap = channelInfoVos.stream().filter(e -> e.getStatus().equalsIgnoreCase("1")).collect(Collectors.groupingBy(ChannelInfoVo::getParentId));
        this.channelTree(topChannel, channelMap);
        return topChannel;
    }

    @Override
    public String findChannelNameByChannelCode(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        Assert.hasLength(insChannelInfoModel.getCode(), "渠道编码不能为空");

        //TODO  改成返回entity
        InsChannelInfoEntity entity = channelDao.findChannelNameByChannelCode(insChannelInfoModel);
        if(entity == null){
            log.error("未找到数据 {}", insChannelInfoModel);
        }

        return entity.getName();
    }

    @Override
    public void analyzeExcelData(List<ChannelExcelModel> list) {
        final String clientId = ServiceContextHolder.getClientId();
        list.forEach(e->{
            final String type = e.getType();
            List<InsChannelInfoEntity> channelInfoByName = channelDao.findChannelInfoByName(type, clientId);
            String pid = "";
            if(ObjectUtils.isEmpty(channelInfoByName)){
                pid = IdWorker.getId();
                String channelCode = this.createChannelCode(InsChannelInfoModel.builder().clientId(clientId).build());
                InsChannelInfoEntity build = InsChannelInfoEntity.builder()
                        .id(pid)
                        .parentId("0")
                        .isCoreChannel(ObjectUtils.isNotEmpty(e.getIsCoreChannel()) ? e.getIsCoreChannel() : null)
                        .name(type)
                        .description(ObjectUtils.isNotEmpty(e.getDescribe()) ? e.getDescribe() : null)
                        .type(ChannelType.CATEGORY.getCode())
                        .code(channelCode)
                        .status("1")
                        .createTime(LocalDateTime.now())
                        .level(1)
                        .dataSourceType(ObjectUtils.isNotEmpty(e.getDataSourceType()) ? e.getDataSourceType() : null)
                        .clientId(clientId)
                        .build();
                channelDao.saveChannel(build);
            }else{
                pid = channelInfoByName.get(0).getId();
            }

            String firstId = "";
            if(ObjectUtils.isNotEmpty(e.getFirstChannel())){
                final String name = e.getFirstChannel();
                InsChannelInfoEntity channelCount = channelDao.findChannelCountByParentIdAndName(InsChannelInfoModel.builder().clientId(clientId).parentId(pid).name(name).build());
//                List<InsChannelInfoEntity> channelInfoByName1 = channelDao.findChannelInfoByName(name, "");
                if(ObjectUtils.isEmpty(channelCount)){
                    firstId = IdWorker.getId();
                    String channelCode = this.createChannelCode(InsChannelInfoModel.builder().type(ChannelType.CATEGORY.getCode()).clientId(clientId).build());
                    InsChannelInfoEntity build = InsChannelInfoEntity.builder()
                            .id(firstId)
                            .parentId(pid)
                            .isCoreChannel(ObjectUtils.isNotEmpty(e.getIsCoreChannel()) ? e.getIsCoreChannel() : null)
                            .name(name)
                            .description(ObjectUtils.isNotEmpty(e.getDescribe()) ? e.getDescribe() : null)
                            .type(ChannelType.CATEGORY.getCode())
//                            .type(ChannelType.CHANNEL.getCode())
                            .code(channelCode)
                            .status("1")
                            .createTime(LocalDateTime.now())
                            .level(2)
//                            .topId(pid)
                            .dataSourceType(ObjectUtils.isNotEmpty(e.getDataSourceType()) ? e.getDataSourceType() : null)
                            .clientId(clientId)
                            .build();
                    channelDao.saveChannel(build);
                }else{
//                    log.warn("渠道已存在");
                    firstId = channelCount.getId();
                }
            }

            String secondId = "";
            if(ObjectUtils.isNotEmpty(e.getSecondChannel())){
                final String name = e.getSecondChannel();
                InsChannelInfoEntity channelCount = channelDao.findChannelCountByParentIdAndName(InsChannelInfoModel.builder().clientId(clientId).parentId(firstId).name(name).build());
//                List<InsChannelInfoEntity> channelInfoByName1 = channelDao.findChannelInfoByName(name, "");
                if(ObjectUtils.isEmpty(channelCount)){
                    secondId = IdWorker.getId();
//                    String channelCode = this.createChannelCode(InsChannelInfoModel.builder().clientId(clientId).build());
                    InsChannelInfoEntity build = InsChannelInfoEntity.builder()
                            .id(secondId)
                            .parentId(firstId)
                            .name(name)
                            .status("1")
                            .description(ObjectUtils.isNotEmpty(e.getDescribe()) ? e.getDescribe() : null)
                            .type(ChannelType.CHANNEL.getCode())
                            .code(e.getCode())
                            .level(3)
                            .clientId(clientId)
                            .topId(pid)
                            .isCoreChannel(ObjectUtils.isNotEmpty(e.getIsCoreChannel()) ? e.getIsCoreChannel() : null)
                            .dataSourceType(ObjectUtils.isNotEmpty(e.getDataSourceType()) ? e.getDataSourceType() : null)
                            .createTime(LocalDateTime.now())
                            .build();
                    channelDao.saveChannel(build);
                }else{
                   log.warn("渠道已存在");
                }
            }

        });
    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), ChannelExcelModel.class, new ChannelExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChannelInfoVo> upwardFindAllChannelHierarchical(String clientId) {
        Assert.hasLength(clientId, "客户id不能为空");
        final List<InsChannelInfoEntity> insChannelInfoEntities = channelDao.upwardFindAllChannelHierarchical(clientId);
        if(ObjectUtils.isEmpty(insChannelInfoEntities)){
            log.info("暂无渠道数据");
            return List.of();
        }
        return channelInfoConvert.channelEntityListConvertVoList(insChannelInfoEntities);
    }


    /**
     * @param insChannelInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/11 下午2:12
     * @描述 必填项校验
     **/
    private void checkParameter(InsChannelInfoModel insChannelInfoModel) {
        Assert.hasLength(insChannelInfoModel.getClientId(), "所属客户不能为空");
        Assert.hasLength(insChannelInfoModel.getType(), "渠道类型不允许为空");
        if (ChannelType.CATEGORY.getCode().equalsIgnoreCase(insChannelInfoModel.getType())) {
            Assert.hasLength(insChannelInfoModel.getName(), "渠道名称不允许为空");
            Assert.isTrue(!"-1".equals(insChannelInfoModel.getParentId()), "默认分类下不允许创建子分类");
        } else if (ChannelType.CHANNEL.getCode().equalsIgnoreCase(insChannelInfoModel.getType())) {
            Assert.hasLength(insChannelInfoModel.getName(), "渠道名称不允许为空");
            Assert.hasLength(insChannelInfoModel.getParentId(), "父级渠道不允许为空");
        }
    }

    /**
     * @param topChannel
     * @param channelMap
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:34
     * @描述 组建渠道树
     **/
    void channelTree(List<ChannelInfoVo> topChannel, Map<String, List<ChannelInfoVo>> channelMap) {
        if (ObjectUtils.isEmpty(topChannel)) {
            return;
        }
        for (ChannelInfoVo channel : topChannel) {
            List<ChannelInfoVo> channelInfoVos = channelMap.get(channel.getId());
            this.channelTree(channelInfoVos, channelMap);
            channel.setChild(channelInfoVos);
        }
    }

    private String createChannelCode(InsChannelInfoModel insChannelInfoModel) {
        String pre = "chl_";
        List<InsChannelInfoEntity> channel = channelDao.findChannel(insChannelInfoModel);
        if(ObjectUtils.isEmpty(channel)){
            return pre+"001";
        }else {
            List<Integer> collect = channel.stream().map(e -> {
                String code = e.getCode();
                String substring = code.substring(code.lastIndexOf("_") + 1);
                return Integer.valueOf(substring);
            }).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            Integer insChannelInfoEntity = collect.stream().findFirst().orElseGet(null);
            int length = String.valueOf(insChannelInfoEntity).length();
            if(length<3){
                return pre+String.format("%03d", (insChannelInfoEntity+1));
            }else{
                return pre+(insChannelInfoEntity+1);
            }
        }
    }
}
