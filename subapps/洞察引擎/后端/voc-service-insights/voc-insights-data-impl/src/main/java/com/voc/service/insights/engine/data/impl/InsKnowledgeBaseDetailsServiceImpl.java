package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibService;
import com.voc.service.insights.engine.api.knowledgeBase.InsKnowledgeBaseDetailsService;
import com.voc.service.insights.engine.common.util.ExcelUtil;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.data.impl.converts.InsKnowledgeBaseConvertService;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseDetailsMapper;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseMapper;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseDetailsModel;
import com.voc.service.insights.engine.model.knowledgeBase.KnowledgeBaseDetailFilterModel;
import com.voc.service.insights.engine.producer.CleanCacheEventProducer;
import com.voc.service.insights.engine.vo.TagLibVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseDetailsVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseTemplateVo;
import com.voc.service.logs.dto.MessageDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)表服务实现类
 *
 * @author makejava
 * @since 2024-09-06 14:51:57
 */
@Service
public class InsKnowledgeBaseDetailsServiceImpl extends ServiceImpl<InsKnowledgeBaseDetailsMapper, InsKnowledgeBaseDetails> implements InsKnowledgeBaseDetailsService {
    @Resource
    private InsKnowledgeBaseMapper insKnowledgeBaseMapper;
    @Resource
    private InsKnowledgeBaseConvertService knowledgeBaseConvertService;
    @Autowired
    IInsTagLibService tagLibService;

//    @Autowired
//    MilvusService milvusService;

    @Autowired
    CleanCacheEventProducer cleanCacheEventProducer;

    private QueryWrapper<InsKnowledgeBaseDetails> createQueryWrapper(KnowledgeBaseDetailFilterModel model) {
        QueryWrapper<InsKnowledgeBaseDetails> qw = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(model.getBusinessTag())) {
            List<TagLibVo> tagLibCategoryVos = tagLibService.findTagLibByIds(model.getBusinessTag());
            List<String> businessTags = tagLibCategoryVos.stream().map(TagLibVo::getTagName).collect(Collectors.toList());
            model.setBusinessTag(businessTags);
        }

        if (ObjectUtils.isNotEmpty(model.getQualityTag())) {
            List<TagLibVo> tagLibCategoryVos = tagLibService.findTagLibByIds(model.getQualityTag());
            List<String> collect = tagLibCategoryVos.stream().map(TagLibVo::getTagName).collect(Collectors.toList());
            model.setQualityTag(collect);
        }

        if (ObjectUtils.isNotEmpty(model.getScenarioTag())) {
            List<TagLibVo> tagLibCategoryVos = tagLibService.findTagLibByIds(model.getScenarioTag());
            List<String> collect = tagLibCategoryVos.stream().map(TagLibVo::getTagName).collect(Collectors.toList());
            model.setScenarioTag(collect);
        }

        qw.lambda().eq(StrUtil.isNotBlank(model.getKnowledgeBaseId()), InsKnowledgeBaseDetails::getKnowledgeBaseId, model.getKnowledgeBaseId())
                .like(StrUtil.isNotBlank(model.getOpinion()), InsKnowledgeBaseDetails::getOpinion, model.getOpinion())
                .in(ObjectUtils.isNotEmpty(model.getSentiment()), InsKnowledgeBaseDetails::getSentiment, model.getSentiment())
                .in(ObjectUtils.isNotEmpty(model.getIntention()), InsKnowledgeBaseDetails::getIntention, model.getIntention())
                .in(ObjectUtils.isNotEmpty(model.getIds()), InsKnowledgeBaseDetails::getId, model.getIds())
                .and(ObjectUtils.isNotEmpty(model.getBusinessTag()), wrapper -> {
                    for (String s : model.getBusinessTag()) {
                        wrapper.or().like(InsKnowledgeBaseDetails::getBusinessTag, s);
                    }
                }).and(ObjectUtils.isNotEmpty(model.getQualityTag()), wrapper -> {
                    for (String s : model.getQualityTag()) {
                        wrapper.or().like(InsKnowledgeBaseDetails::getQualityTag, s);
                    }
                }).and(ObjectUtils.isNotEmpty(model.getScenarioTag()), wrapper -> {
                    for (String s : model.getScenarioTag()) {
                        wrapper.or().like(InsKnowledgeBaseDetails::getScenarioTag, s);
                    }
                })
                .eq(InsKnowledgeBaseDetails::getDataValidity, "1")
        ;

        return qw;
    }

    /**
     * 通过ID查询单条数据
     *
     * @param 主键
     * @return 实例对象
     */
    @Override
    public Result queryById(String id) {
        return Result.OK(super.getById(id));
    }

    /**
     * 分页查询
     *
     * @param model 筛选条件
     * @return 查询结果
     */
    @Override
    public Result queryByPage(KnowledgeBaseDetailFilterModel model) {
        QueryWrapper<InsKnowledgeBaseDetails> queryWrapper = this.createQueryWrapper(model);
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsKnowledgeBaseDetails> entityList = super.list(queryWrapper);
        List<InsKnowledgeBaseDetailsVo> list = knowledgeBaseConvertService.convertToDetailList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    /**
     * 新增数据
     *
     * @param insKnowledgeBaseDetails 实例对象
     * @return 实例对象
     */
    @Override
    public Result insert(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return Result.OK(super.saveOrUpdate(knowledgeBaseConvertService.convertToDetail(insKnowledgeBaseDetails)));
    }

    /**
     * 修改数据
     *
     * @param insKnowledgeBaseDetails 实例对象
     * @return 实例对象
     */
    @Override
    public Result update(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        Assert.hasLength(insKnowledgeBaseDetails.getId(), "id不允许为空");
//        Assert.hasLength(insKnowledgeBaseDetails.getOpinion(),"观点名称:不允许为空");
        InsKnowledgeBaseDetails update = knowledgeBaseConvertService.convertToDetail(insKnowledgeBaseDetails);
        InsKnowledgeBaseDetails old = super.getById(insKnowledgeBaseDetails.getId());
        if (StrUtil.isNotEmpty(insKnowledgeBaseDetails.getOpinion()) && !old.getOpinion().equals(update.getOpinion())) {
//            milvusService.delete(old.getCollectionName(), old.getVectorId());
            update.setVectorState("0");
        }
        update.setUpdateTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        super.saveOrUpdate(update);
        try {
            cleanCacheEventProducer.pushEvent(MessageDTO.builder().type("opinion").data(true).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.OK(insKnowledgeBaseDetails);
    }

    /**
     * 通过主键删除数据
     *
     * @param 主键
     * @return 是否成功
     */
    @Override
    public Result deleteById(String id) {
        Assert.hasLength(id, "知识库数据id不允许为空");
        InsKnowledgeBaseDetails entity = super.getById(id);
        InsKnowledgeBase knowledgeBase = insKnowledgeBaseMapper.selectById(entity.getKnowledgeBaseId());
        if (ObjectUtils.isNotEmpty(knowledgeBase.getCollectionName()) && ObjectUtils.isNotEmpty(entity.getVectorId())) {
//            milvusService.delete(knowledgeBase.getCollectionName(), entity.getVectorId());
        }
        return Result.OK(super.removeById(id));
    }

    @Override
    public void batchDelete(List<String> ids) {
        Assert.noNullElements(ids, "知识库数据ids不允许为空");
        InsKnowledgeBaseDetails entity = super.getById(ids.get(0));
        InsKnowledgeBase knowledgeBase = insKnowledgeBaseMapper.selectById(entity.getKnowledgeBaseId());
        if (ObjectUtils.isNotEmpty(knowledgeBase.getCollectionName()) && ObjectUtils.isNotEmpty(entity.getVectorId())) {
//            milvusService.delete(knowledgeBase.getCollectionName(), entity.getVectorId());
        }
        QueryWrapper<InsKnowledgeBaseDetails> deleteQuery = new QueryWrapper<>();
        deleteQuery.lambda().in(InsKnowledgeBaseDetails::getId, ids);
        super.remove(deleteQuery);
    }

    @Override
    public Result batchMove(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        Assert.hasLength(insKnowledgeBaseDetails.getKnowledgeBaseId(), "知识库id不允许为空");
        Assert.noNullElements(insKnowledgeBaseDetails.getIds(), "要移动的知识库数据id不允许为空");
        QueryWrapper<InsKnowledgeBaseDetails> query = new QueryWrapper<>();
        query.lambda().in(InsKnowledgeBaseDetails::getId, insKnowledgeBaseDetails.getIds());
        List<InsKnowledgeBaseDetails> moveList = super.list(query);
        if (ObjectUtils.isNotEmpty(moveList)) {
            UpdateWrapper<InsKnowledgeBaseDetails> updateWrapper = null;
            try {
                Map<String, List<InsKnowledgeBaseDetails>> movemap = moveList.stream().collect(Collectors.groupingBy(InsKnowledgeBaseDetails::getKnowledgeBaseId));
                for (String baseId : movemap.keySet()) {
                    InsKnowledgeBase base = insKnowledgeBaseMapper.selectById(baseId);
                    List<InsKnowledgeBaseDetails> list = movemap.get(baseId);
                    list.stream().forEach(e -> {
                        if (ObjectUtils.isNotEmpty(base.getCollectionName()) && ObjectUtils.isNotEmpty(e.getVectorId())) {
//                            milvusService.delete(base.getCollectionName(), e.getVectorId());
                        }
                    });
                }
                updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda()
                        .in(InsKnowledgeBaseDetails::getId, insKnowledgeBaseDetails.getIds())
                        .set(InsKnowledgeBaseDetails::getKnowledgeBaseId, insKnowledgeBaseDetails.getKnowledgeBaseId())
                        .set(InsKnowledgeBaseDetails::getVectorState, "0");
            } catch (Exception e) {
                log.error("批量移动数据异常", e);
                e.printStackTrace();
                return Result.error("批量移动数据异常");
            }
            return Result.OK(super.update(updateWrapper));
        } else{
            return Result.OK();
        }
    }

    @Override
    public Result batchSynchronous(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        Assert.hasLength(insKnowledgeBaseDetails.getKnowledgeBaseId(), "知识库id不允许为空");
        Assert.noNullElements(insKnowledgeBaseDetails.getIds(), "要复制的知识库数据id不允许为空");
        QueryWrapper<InsKnowledgeBaseDetails> query = new QueryWrapper<>();
        query.lambda().in(InsKnowledgeBaseDetails::getId, insKnowledgeBaseDetails.getIds());
        List<InsKnowledgeBaseDetails> moveList = super.list(query);
        if (ObjectUtils.isNotEmpty(moveList)) {
            try {
                moveList.stream().forEach(e -> {
                    e.setKnowledgeBaseId(insKnowledgeBaseDetails.getKnowledgeBaseId());
                    e.setVectorState("0");
                    e.setId(null);
                    super.save(e);
                });
            } catch (Exception e) {
                log.error("批量同步数据异常", e);
                e.printStackTrace();
                return Result.error("批量同步数据异常");
            }
            return Result.OK();
        } else{
            return Result.OK();
        }
    }

    @Override
    public Result batchEdit(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        Assert.noNullElements(insKnowledgeBaseDetails.getIds(), "批量编辑的id不允许为空");
//        Assert.hasLength(insKnowledgeBaseDetails.getOpinion(),"观点名称:不允许为空");
        UpdateWrapper<InsKnowledgeBaseDetails> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().
                in(InsKnowledgeBaseDetails::getId, insKnowledgeBaseDetails.getIds())
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getTopic()), InsKnowledgeBaseDetails::getTopic, insKnowledgeBaseDetails.getTopic())
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getBusinessTag()), InsKnowledgeBaseDetails::getBusinessTag, insKnowledgeBaseDetails.getBusinessTag())
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getQualityTag()), InsKnowledgeBaseDetails::getQualityTag, insKnowledgeBaseDetails.getQualityTag())
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getScenarioTag()), InsKnowledgeBaseDetails::getScenarioTag, insKnowledgeBaseDetails.getScenarioTag())
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getSentiment()), InsKnowledgeBaseDetails::getSentiment, insKnowledgeBaseDetails.getSentiment())
                .set(InsKnowledgeBaseDetails::getUpdateTime, LocalDateTime.now(ZoneId.of("Asia/Shanghai")))
                .set(StrUtil.isNotBlank(insKnowledgeBaseDetails.getIntention()), InsKnowledgeBaseDetails::getIntention, insKnowledgeBaseDetails.getIntention())
        ;
        try {
            cleanCacheEventProducer.pushEvent(MessageDTO.builder().type("opinion").data(true).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.OK(super.update(updateWrapper));
    }

    @Override
    public void knowledgeBaseDetailsExport(KnowledgeBaseDetailFilterModel insKnowledgeBaseDetails, HttpServletResponse response) {
        try {
            Assert.hasLength(insKnowledgeBaseDetails.getKnowledgeBaseId(), "知识库id不允许为空");
            List<InsKnowledgeBaseDetails> entityList = this.list(this.createQueryWrapper(insKnowledgeBaseDetails));
            List<InsKnowledgeBaseTemplateVo> vos = knowledgeBaseConvertService.convertToOriginDataVoList(entityList);
            ExcelUtil.writeExcel(response, vos, "知识库数据.xlsx", "知识库数据", InsKnowledgeBaseTemplateVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadKnowledgeBase(HttpServletResponse response, String fileName) {
        List<InsKnowledgeBaseTemplateVo> list = new ArrayList<>();
        try {
            final String suffix = FileUtil.getSuffix(fileName);
            if ("xlsx".equals(suffix)) {
                ExcelUtil.writeExcel(response, list, fileName, fileName.substring(0, fileName.lastIndexOf(".")), InsKnowledgeBaseTemplateVo.class, null);
            } else {
                ExcelUtil.writeExcel(response, list, fileName, fileName.substring(0, fileName.lastIndexOf(".")), InsKnowledgeBaseTemplateVo.class, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
