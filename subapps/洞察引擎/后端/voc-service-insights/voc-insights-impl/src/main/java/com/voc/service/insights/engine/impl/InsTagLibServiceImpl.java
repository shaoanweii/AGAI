package com.voc.service.insights.engine.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.api.IInsTagLibService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsTagLibDao;
import com.voc.service.insights.engine.entity.InsTagLibEntity;
import com.voc.service.insights.engine.enums.TagAttribute;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.model.InsTagLibModel;
import com.voc.service.insights.engine.vo.DictInfoVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 上午10:14
 * @描述:
 **/
@Service
public class InsTagLibServiceImpl implements IInsTagLibService {

    private static final Logger log = LoggerFactory.getLogger(InsTagLibServiceImpl.class);
    @Autowired
    private InsConvertMapperService convertMapperService;
    @Autowired
    private InsTagLibDao tagLibDao;
    @Autowired
    private IInsDictService dictService;

    @Override
    public String saveTagLib(InsTagLibModel tagLibModel) {
        this.checkParams(tagLibModel);
        log.debug("开始标签名称重复校验");
        Boolean checkedTagLibName = tagLibDao.checkTagLibName(tagLibModel.getTagName(), null, tagLibModel.getTagParentId());
        if (checkedTagLibName) {
            throw new BussinessException(InsCommonErrorEnum.TAGLIB_EXIST);
        }
        log.debug("标签名称重复校验结束");
        final String username = ServiceContextHolder.getUsername();
        log.debug("转换前:tagLibModel{}", tagLibModel);
        InsTagLibEntity insTagLibEntity = convertMapperService.tagLibModelConvertEntity(tagLibModel);
        final String id = IdWorker.getId();
        insTagLibEntity.setId(id);
        insTagLibEntity.setCreateUser(username);
        insTagLibEntity.setCreateTime(LocalDateTime.now());
        insTagLibEntity.setTagCode(this.getTagCode(tagLibModel.getTagType(), tagLibModel.getTagParentId()));
        log.debug("转换后:insTagLibEntity{}", insTagLibEntity);
        tagLibDao.saveTagLib(insTagLibEntity);
        return id;
    }

    @Override
    public void updateTagLib(InsTagLibModel tagLibModel) {
        //单独参数校验
        Assert.hasLength(tagLibModel.getId(), "标签id不能为空");
        this.checkParams(tagLibModel);
        log.debug("开始标签名称重复校验");
        Boolean checkedTagLibName = tagLibDao.checkTagLibName(tagLibModel.getTagName(), tagLibModel.getId(), tagLibModel.getTagParentId());
        if (checkedTagLibName) {
            throw new BussinessException(InsCommonErrorEnum.TAGLIB_EXIST);
        }
        log.debug("标签名称重复校验结束");
        InsTagLibEntity tagLibById = tagLibDao.findTagLibById(tagLibModel.getId());
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibById), "标签信息不存在");
        if (!tagLibModel.getTagParentId().equalsIgnoreCase(tagLibById.getTagParentId())) {
            tagLibModel.setTagCode(this.getTagCode(tagLibModel.getTagType(), tagLibModel.getTagParentId()));
        }
        final String username = ServiceContextHolder.getUsername();
        log.debug("转换前:tagLibModel{}", tagLibModel);
        InsTagLibEntity insTagLibEntity = convertMapperService.tagLibModelConvertEntity(tagLibModel);
        insTagLibEntity.setUpdateTime(LocalDateTime.now());
        insTagLibEntity.setUpdateUser(username);
        log.debug("转换后:insTagLibEntity{}", insTagLibEntity);
        tagLibDao.updateTagLib(insTagLibEntity);
    }

    @Override
    public PageInfo findTagLibList(InsTagLibModel tagLibModel) {
        PageHelper.startPage(tagLibModel.getPageNum(), tagLibModel.getPageSize());
        tagLibModel.setTagAttribute(TagAttribute.FINAL_LABEL.getCode());
        List<InsTagLibEntity> tagLibList = tagLibDao.findTagLibList(tagLibModel);
        PageInfo pageInfo = new PageInfo<>(tagLibList);
        if (ObjectUtils.isEmpty(tagLibList)) {
            log.info("暂无标签信息");
            return pageInfo;
        }
        List<String> tagLibIds = tagLibList.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<InsTagLibEntity> tagLibHierarchical = tagLibDao.findTagLibHierarchical(tagLibIds);
        Map<String, List<InsTagLibEntity>> map = tagLibHierarchical.stream().collect(Collectors.groupingBy(InsTagLibEntity::getTagCode));
        List<TagLibVo> collect = tagLibList.stream().map(e -> {
            TagLibVo tagLibVo = convertMapperService.tagLibEntityConvertVo(e);
            //标签类型
            final String tagType = tagLibVo.getTagType();
            //末级标签的code
            final String tagCode = tagLibVo.getTagCode();
            String code = tagCode.substring(0, tagCode.length() - 3);
            StringBuffer buffer = new StringBuffer();
            while(code.length()>4){
                if(map.containsKey(code)){
                    List<InsTagLibEntity> insTagLibEntity = map.get(code);
                    if(insTagLibEntity.size()>1){
                        InsTagLibEntity insTagLibEntity1 = insTagLibEntity.stream().filter(k -> k.getTagType().equalsIgnoreCase(tagType)).findFirst().get();
                        String tagName = "#".concat(insTagLibEntity1.getTagName());
                        buffer.insert(0, tagName);
                    }else {
                        InsTagLibEntity insTagLibEntity1 = insTagLibEntity.stream().findFirst().get();
                        String tagName = "#".concat(insTagLibEntity1.getTagName());
                        buffer.insert(0, tagName);
                    }

                    code = code.substring(0, code.length()-3);
                }
            }
            if(map.containsKey(code)){
                List<InsTagLibEntity> insTagLibEntity = map.get(code);
                if(insTagLibEntity.size()>1){
                    InsTagLibEntity insTagLibEntity1 = insTagLibEntity.stream().filter(k -> k.getTagType().equalsIgnoreCase(tagType)).findFirst().get();
                    buffer.insert(0, insTagLibEntity1.getTagName());
                }else {
                    InsTagLibEntity insTagLibEntity1 = insTagLibEntity.stream().findFirst().get();
                    buffer.insert(0, insTagLibEntity1.getTagName());
                }
            }
//            String tagLibNameHierarchical = tagLibDao.findTagLibNameHierarchical(tagLibVo.getId());
//            String[] split = tagLibNameHierarchical.split("#");
//            StringBuffer buffer = new StringBuffer();
//            for (int i = split.length - 1; i >= 0; i--) {
//                buffer.append(split[i]);
//                if (i > 0) {
//                    buffer.append("#");
//                }
//            }
            tagLibVo.setTagLibNameHierarchical(buffer.toString());
            return tagLibVo;
        }).collect(Collectors.toList());
        pageInfo.setList(collect);
        return pageInfo;
    }

    @Override
    public TagLibVo findTagLib(InsTagLibModel tagLibModel) {
        //单独参数校验
        Assert.hasLength(tagLibModel.getId(), "标签id不能为空");
        InsTagLibEntity tagLibById = tagLibDao.findTagLibById(tagLibModel.getId());
        Assert.isTrue(ObjectUtils.isNotEmpty(tagLibById), "标签信息不存在");
        TagLibVo tagLibVo = convertMapperService.tagLibEntityConvertVo(tagLibById);
        String tagLibNameHierarchical = tagLibDao.findTagLibNameHierarchical(tagLibVo.getId());
        String[] split = tagLibNameHierarchical.split("#");
        StringBuffer buffer = new StringBuffer();
        for (int i = split.length - 1; i >= 0; i--) {
            buffer.append(split[i]);
            if (i > 0) {
                buffer.append("#");
            }
        }
        tagLibVo.setTagLibNameHierarchical(buffer.toString());
        return tagLibVo;
    }

    @Override
    public Map<String, List<DictInfoVo>> findTagLibRelatedItems(InsTagLibModel tagLibModel) {
        Assert.hasLength(tagLibModel.getTagType(), "标签类型不能为空");
        List<DictInfoVo> relatedItems = dictService.findRelatedItems(tagLibModel.getTagType());
        if (ObjectUtils.isEmpty(relatedItems)) {
            return null;
        }
        Map<String, List<DictInfoVo>> collect = relatedItems.stream().collect(Collectors.groupingBy(DictInfoVo::getTypeCode, LinkedHashMap::new, Collectors.toList()));
        return collect;
    }

    @Override
    public List<TagLibCategoryVo> findTagLibCategoryTree(String clientId, String tagLibType) {
        InsTagLibModel tagLibModel = InsTagLibModel.builder().tagAttribute(TagAttribute.CATEGORY.getCode()).tagType(ObjectUtils.isEmpty(tagLibType) ? null : tagLibType).build();
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
        this.tagLibCategoryTree(topTagLibList, tagLibbCategoryMap);
        return topTagLibList;
    }

    @Override
    public List<TagLibVo> findTagLibByIds(List<String> ids) {
        List<InsTagLibEntity> tagLibByIds = tagLibDao.findTagLibByIds(ids);
        if(ObjectUtils.isEmpty(tagLibByIds)){
            return List.of();
        }
        List<TagLibVo> tagLibVos =  convertMapperService.tagLibEntityListConvertVo(tagLibByIds);
        return tagLibVos;
    }

    @Override
    public TagLibVo findTagLibByCode(String code) {
        InsTagLibEntity tagLibByCode = tagLibDao.findTagLibByCode(code);
        if(ObjectUtils.isEmpty(tagLibByCode)){
            return null;
        }
        TagLibVo tagLibVo = convertMapperService.tagLibEntityConvertVo(tagLibByCode);
        return tagLibVo;
    }

    /**
     * @param tagLibbCategoryMap
     * @param topTagLibList
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:34
     * @描述 组建标签分类树
     **/
    void tagLibCategoryTree(List<TagLibCategoryVo> topTagLibList, Map<String, List<TagLibCategoryVo>> tagLibbCategoryMap) {
        if (ObjectUtils.isEmpty(topTagLibList)) {
            return;
        }
        for (TagLibCategoryVo tagLibCategoryVo : topTagLibList) {
            List<TagLibCategoryVo> tagLibCategoryVos = tagLibbCategoryMap.get(tagLibCategoryVo.getId());
            this.tagLibCategoryTree(tagLibCategoryVos, tagLibbCategoryMap);
            tagLibCategoryVo.setChild(tagLibCategoryVos);
        }
    }


    private void checkParams(InsTagLibModel tagLibModel) {
        log.debug("开始入参校验:");
        Assert.hasLength(tagLibModel.getTagType(), "标签类型不能为空");
        Assert.hasLength(tagLibModel.getTagAttribute(), "标签属性不能为空");
        Assert.hasLength(tagLibModel.getTagName(), "标签名称不能为空");
        Assert.isTrue(tagLibModel.getTagName().length() <= 50, "标签名称长度不能超过50");
        if (ObjectUtils.isNotEmpty(tagLibModel.getTagNameEn())) {
            Assert.isTrue(tagLibModel.getTagNameEn().length() <= 50, "标签英文名称长度不能超过50");
        }
        Assert.hasLength(tagLibModel.getTagParentId(), "标签所属分类不能为空");
        if (ObjectUtils.isNotEmpty(tagLibModel.getTagDescription())) {
            Assert.isTrue(tagLibModel.getTagDescription().length() <= 1024, "标签描述长度不能超过1024");
        }
//        if(TagAttribute.FINAL_LABEL.getCode().equalsIgnoreCase(tagLibModel.getTagAttribute())){
//            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibModel.getEnergyType()),"能源分类不能为空");
//            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibModel.getCarType()),"车辆类型不能为空");
//            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibModel.getSeriousness()),"严重性等级不能为空");
//            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibModel.getUserJourney()),"用户旅程不能为空");
//            Assert.hasLength(tagLibModel.getTagStatus(),"标签状态不允许为空");
//            Assert.isTrue(Integer.valueOf(tagLibModel.getTagStatus())>=0 && Integer.valueOf(tagLibModel.getTagStatus())<=1,"标签状态只能为0或1");
//        }
        log.debug("入参检验结束");
    }


    /**
     * 获取新增标签code
     *
     * @param type 标签类型
     * @param pid  父标签code
     * @return
     */
    public String getTagCode(String type, String pid) {
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
        List<InsTagLibEntity> list = tagLibDao.findTagLibByQueryWrapper(type, pid);
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(pid)) {
                //情况1 根节点
                tagCode = "1001";
            } else {
                //情况2  首个子节点
                List<InsTagLibEntity> tagLibChildNodeByParentId = tagLibDao.findTagLibChildNodeByParentId(pid);
                Assert.isTrue(ObjectUtils.isNotEmpty(tagLibChildNodeByParentId), "父级分类或标签不存在");
                InsTagLibEntity insTagLibEntity = tagLibChildNodeByParentId.stream().findFirst().get();
                tagCode = insTagLibEntity.getTagCode() + "001";
            }
        } else {
            //情况3
            String oldCode = list.get(0).getTagCode();
            long newCode = Long.parseLong(oldCode);
            newCode += 1;
            tagCode = String.valueOf(newCode);
        }
        return tagCode;
    }
}
