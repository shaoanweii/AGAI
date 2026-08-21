package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.IInsTagInfoService;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.dao.InsTagInfoDao;
import com.voc.service.insights.engine.entity.InsTagInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsTagClientMapper;
import com.voc.service.insights.engine.mapper.InsTagInfoMapper;
import com.voc.service.insights.engine.model.InsTagInfoBatchModel;
import com.voc.service.insights.engine.model.InsTagInfoModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.InsTagInfoListVo;
import com.voc.service.insights.engine.vo.TagClientCustomerVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:09
 * @描述:
 **/
@Service
public class InsTagInfoServiceImpl extends ServiceImpl<InsTagInfoMapper, InsTagInfoEntity> implements IInsTagInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsTagInfoServiceImpl.class);
    @Autowired
    InsTagInfoDao tagInfoDao;
    @Autowired
    InsConvertMapperService convertMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    InsTagInfoMapper tagInfoMapper;
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    InsTagClientMapper insTagClientMapper;

    @Override
    @Cached(area="VDP" ,name = ":users:", key = "':C{appId}:C{userId}:tokens:C{token}:tags:'+#type", expire = 60 * 60, cacheType = CacheType.BOTH )
    public List<InsTagInfoModel> findTageInfoByType(String type) {
        if (StrUtil.isEmpty(type)) {
            log.info("获取全部标签");
        } else {
            log.info("获取{}标签", InsightsConstants.BUSINESS_TAG_TYPE.equalsIgnoreCase(type) ? "业务标签" :
                    InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type) ? "质量标签" : type);
        }

        log.trace("读取数据库");
        List<InsTagInfoEntity> tageInfoByType = tagInfoDao.findTagInfoByType(type.toUpperCase());
        if (ObjectUtils.isEmpty(tageInfoByType)) {
            return Collections.EMPTY_LIST;
        }
        List<InsTagInfoModel> list = convertMapper.tageInfoEntityListConvertModelList(tageInfoByType);
        return list;
    }


    @Override
    public Result<?> queryInsTagInfo(InsTagInfoQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsTagInfoListVo> listVos = tagInfoMapper.queryInsTagInfo(model);
        PageInfo<InsTagInfoListVo> page = new PageInfo<>(listVos);
        List<String> codeList = listVos.stream().map(InsTagInfoListVo::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(codeList)) {
//            List<InsTagClientEntity> tagClientList = insTagClientMapper.selectList(new QueryWrapper<InsTagClientEntity>().in("code", codeList).eq("enable", 1));
//            Map<String, List<InsTagClientEntity>> map = tagClientList.stream().collect(Collectors.groupingBy(InsTagClientEntity::getCode));
            List<TagClientCustomerVo> tagClientList = insTagClientMapper.queryTagClientCustomerVo(codeList);
            Map<String, List<TagClientCustomerVo>> map = tagClientList.stream().collect(Collectors.groupingBy(TagClientCustomerVo::getTagId));
            listVos.stream().forEach(s -> {
                if (CollUtil.isNotEmpty(map.get(s.getId()))) {
//                    List<InsTagClientModel> clientList = insConvertMapperService.convertTagClientModelToList(map.get(s.getTopicCode()));
//                    List<String> clientList = map.get(s.getTopicCode()).stream().map(InsTagClientEntity::getClientId).collect(Collectors.toList());
                    List<String> clientList = map.get(s.getId()).stream().map(TagClientCustomerVo::getAbbreviation).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(clientList)) {
                        s.setApplyList(clientList);
                    }
                }
            });
        }
        page.setList(listVos);
        return Result.OK(page);
    }

    @Override
    public void insert(InsTagInfoModel model) {
        //必填项校验
        this.checkParameter(model);
        //单独参数校验
        Assert.hasLength(model.getEnable(), "状态不允许为空");
        if (model.getType().equals(InsightsConstants.QUALITY_TAG_TYPE)) {
            Assert.hasLength(model.getSeriousness(), "严重性不允许为空");
        }
        if (ObjectUtils.isEmpty(model.getId())) {
            model.setId(IdWorker.getId());
        }
        InsTagInfoEntity insTagInfoEntity = insConvertMapperService.converTo(model);
        //校验标签名称是否重复
        checkName(insTagInfoEntity);
        if (ObjectUtils.isEmpty(insTagInfoEntity.getCode())) {
            insTagInfoEntity.setCode(getTagCode(model.getType(), model.getParentId()));
        }
        insTagInfoEntity.setCreateTime(LocalDateTime.now());
        insTagInfoEntity.setUpdateTime(LocalDateTime.now());
        tagInfoMapper.insert(insTagInfoEntity);
    }

    @Override
    public void insertBatch(InsTagInfoBatchModel model) {
        Assert.hasLength(model.getType(), "标签类型不允许为空");
        Assert.hasLength(model.getParentId(), "所属分类不允许为空");
        Assert.hasLength(model.getLabelType(), "新增类型不允许为空");
        AtomicReference<String> code = new AtomicReference<>(getTagCode(model.getType(), model.getParentId()));
        //分类
        if ("Category".equals(model.getLabelType())) {
            List<InsTagInfoModel> tagInfos = model.getTagInfos();
            tagInfos.stream().forEach(s -> {
                BeanUtil.copyProperties(model, s);
                s.setId(IdWorker.getId());
                if (ObjectUtils.isEmpty(s.getCode())) {
                    s.setCode(code.get());
                }
                code.set(nextTagCode(model.getType(), code.get()));
                s.setCreateTime(LocalDateTime.now());
                s.setUpdateTime(LocalDateTime.now());
            });
            this.saveBatch(insConvertMapperService.convertTagInfoEntityToList(tagInfos));
        } else {
            InsTagInfoModel insTagInfoModel = new InsTagInfoModel();
            BeanUtil.copyProperties(model, insTagInfoModel);
            InsTagInfoEntity entity = insConvertMapperService.converTo(insTagInfoModel);
            //校验标签名称是否重复
            checkName(entity);
            entity.setId(IdWorker.getId());
            if (ObjectUtils.isEmpty(entity.getCode())) {
                entity.setCode(code.get());
            }
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            this.save(entity);
        }

    }

    @Override
    public void update(InsTagInfoModel model) {
        //必填项校验
        this.checkParameter(model);
        //单独参数校验
        Assert.hasLength(model.getId(), "id不允许为空");
        InsTagInfoEntity insTagInfoEntity = insConvertMapperService.converTo(model);
        //校验标签名称是否重复
//        checkName(insTagInfoEntity);
        insTagInfoEntity.setUpdateTime(LocalDateTime.now());
        tagInfoMapper.updateById(insTagInfoEntity);
    }

    @Override
    public void deleteByIds(List<Serializable> ids) {
        tagInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public InsTagInfoModel queryById(Serializable id) {
        InsTagInfoEntity insTagInfoEntity = tagInfoMapper.selectById(id);
        return insConvertMapperService.converTo(insTagInfoEntity);
    }

    @Override
    public InsTagInfoListVo queryVoById(Serializable id) {
        return tagInfoMapper.queryInsTagInfoVoById(id);
    }

    @Override
    public List<InsTagInfoModel> queryByIdList(List<String> idList) {
        List<InsTagInfoModel> insTagInfoModelList = new ArrayList<>();
        List<InsTagInfoEntity> insTagInfoEntities = this.listByIds(idList);
        for (InsTagInfoEntity insTagInfoEntity : insTagInfoEntities) {
            InsTagInfoModel insTagInfoModel = insConvertMapperService.converTo(insTagInfoEntity);
            insTagInfoModelList.add(insTagInfoModel);
        }
        return insTagInfoModelList;
    }

//    @Override
//    public void tagConvert(MultipartFile file, HttpServletResponse response, String tagType) {
//        try {
//            @Cleanup
//            InputStream fileIs = file.getInputStream();
//            Map<String, List<TagConvertTemplate>> map = new HashMap<>();
//            EasyExcel.read(fileIs, TagConvertTemplate.class,new ExcelListenerTest(this,tagType,map)).sheet().doRead();
//            List<TagConvertTemplate> tag = map.get("tag");
//            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + tagType+"_car_dim.xlsx");
//            EasyExcel.write(response.getOutputStream(),TagConvertTemplate.class).needHead(true).excelType(ExcelTypeEnum.XLSX).sheet("carDim").doWrite(tag);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Override
//    public void tagGetCode(List<TagConvertTemplate> list, String tagType, Map<String, List<TagConvertTemplate>> map) {
//        List<InsTagInfoEntity> insTagInfoEntities = this.baseMapper.findAllTaginfo();
//        Map<String, InsTagInfoEntity> top = insTagInfoEntities.stream().filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toMap(InsTagInfoEntity::getName, Function.identity()));
//        List<InsTagInfoEntity> collect = insTagInfoEntities.stream().filter(e -> !"0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
//        list.stream().forEach(e->{
//            String businessTagName = "";
//            if(tagType.equalsIgnoreCase("BIZ")){
//                businessTagName =  e.getBusiness_tag_name();
//            }else {
////                businessTagName = e.getQuality_tag_name();
//            }
//            String[] split = businessTagName.split("#");
//            StringBuffer buffer = new StringBuffer();
//            String parentId = "0";
//            for (int i = 0; i < split.length; i++) {
//                String s = split[i];
//                if(i == 0){
//                    if(top.containsKey(s)){
//                        InsTagInfoEntity insTagInfoEntity = top.get(s);
//                        buffer.append(insTagInfoEntity.getCode());
//                        parentId = insTagInfoEntity.getId();
//                    }
//                }else{
//                    String finalParentId = parentId;
//                    List<InsTagInfoEntity> collect1 = collect.stream().filter(k -> k.getName().equals(s) && k.getParentId().equals(finalParentId)).collect(Collectors.toList());
//                    if(ObjectUtils.isNotEmpty(collect1)){
//                        buffer.append(collect1.get(0).getCode());
//                        parentId = collect1.get(0).getId();
//                    }else{
//                        System.out.println("未匹配到："+businessTagName);
//                    }
//                }
//                if (i < split.length - 1) {
//                    buffer.append("#");
//                }
//            }
//            if(tagType.equalsIgnoreCase("BIZ")){
//                e.setBusiness_tag_code(buffer.toString());
//            }else {
////                e.setQuality_tag_code(buffer.toString());
//            }
//
//        });
////        EasyExcel.write(response.getOutputStream(), head).excelType(ExcelTypeEnum.XLSX).sheet("单条分析结果").doWrite(records);
//        map.put("tag",list);
//    }

    /**
     * 必填项校验
     *
     * @param model
     */
    private void checkParameter(InsTagInfoModel model) {
        Assert.hasLength(model.getName(), "标签名称不允许为空");
        Assert.hasLength(model.getType(), "标签类型不允许为空");
        Assert.hasLength(model.getParentId(), "所属分类不允许为空");
    }

    /**
     * 校验标签名称是否已有重复
     *
     * @param insTagInfoEntity
     */
    private void checkName(InsTagInfoEntity insTagInfoEntity) {
        InsTagInfoEntity entity = tagInfoMapper.selectOne(new QueryWrapper<>(insTagInfoEntity).lambda().eq(InsTagInfoEntity::getName, insTagInfoEntity.getName()));
        Assert.isNull(entity, "标签名称重复");
    }

    /**
     * 获取新增标签code
     *
     * @param type  标签类型
     * @param pCode 父标签code
     * @return
     */
    public String getTagCode(String type, String pCode) {
        Assert.hasLength(type, "type类型不允许为空");
        Assert.hasLength(pCode, "父标签pCode不允许为空");
        String ROOT_PID_VALUE = "0";
        String tagCode = null;
        String pre = "GWM";
        if (InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type)) {
            pre = "Q";
        }

        /*
         * 分成三种情况
         * 1.数据库无数据
         * 2.添加子节点，无兄弟元素
         * 3.添加子节点有兄弟元素
         * */
        //找同类 确定上一个最大的code值
        LambdaQueryWrapper<InsTagInfoEntity> query = new LambdaQueryWrapper<InsTagInfoEntity>().eq(InsTagInfoEntity::getParentId, pCode).eq(InsTagInfoEntity::getType, type).isNotNull(InsTagInfoEntity::getCode).orderByDesc(InsTagInfoEntity::getCode);
//        query.eq(InsTagInfoEntity::getEnable,1);
        List<InsTagInfoEntity> list = this.list(query);
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(pCode)) {
                //情况1
                tagCode = pre + "1001";
            } else {
                //情况2
                tagCode = pCode + "001";
            }
        } else {
            //情况3
            String oldCode = list.get(0).getCode();
            int len = InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type) ? 1 : 3;
            int newCode = Integer.parseInt(oldCode.substring(len, oldCode.length()));
            newCode += 1;
            tagCode = pre + newCode;
        }
        return tagCode;
    }

    public String nextTagCode(String type, String code) {
        Assert.hasLength(type, "type类型不允许为空");
        Assert.hasLength(code, "父标签pCode不允许为空");

        String pre = "GWM";
        if (InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type)) {
            pre = "Q";
        }
        int len = InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type) ? 1 : 3;
        int newCode = Integer.parseInt(code.substring(len, code.length()));
        newCode += 1;
        code = pre + newCode;
        return code;
    }

}
