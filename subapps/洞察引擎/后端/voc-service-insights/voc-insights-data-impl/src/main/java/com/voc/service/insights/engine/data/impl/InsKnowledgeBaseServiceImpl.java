package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.ttl.TtlWrappers;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.api.IUploadFileService;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.PinyinConverter;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.IOnnxVectorServiceClient;
import com.voc.service.insights.engine.api.knowledgeBase.InsKnowledgeBaseService;
import com.voc.service.insights.engine.data.dao.InsKnowledgeBaseDetailsDao;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.data.impl.converts.InsKnowledgeBaseConvertService;
import com.voc.service.insights.engine.data.listener.KnowledgeBaseExcelListener;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseDetailsMapper;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseMapper;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseTemplateVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseValidateVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseVo;
import lombok.Cleanup;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识库表(InsKnowledgeBase)表服务实现类
 *
 * @author makejava
 * @since 2024-09-06 14:51:58
 */
@Service
public class InsKnowledgeBaseServiceImpl extends ServiceImpl<InsKnowledgeBaseMapper,InsKnowledgeBase> implements InsKnowledgeBaseService {
    private static final Logger log = LoggerFactory.getLogger(InsKnowledgeBaseServiceImpl.class);
    @Resource
    private InsKnowledgeBaseConvertService knowledgeBaseConvertService;
    @Resource
    private InsKnowledgeBaseDetailsDao insKnowledgeBaseDetailsDao;
    @Autowired
    InsKnowledgeBaseDetailsMapper knowledgeBaseDetailsMapper;
    @Autowired
    IOnnxVectorServiceClient onnxVectorServiceClient;
//    @Autowired
//    MilvusService milvusService;
    @Autowired
    IUploadFileService uploadFileService;
    private QueryWrapper<InsKnowledgeBase> createQueryWrapper(InsKnowledgeBaseModel model) {
        InsKnowledgeBase entity =  knowledgeBaseConvertService.convertTo(model);
        entity.setCount(null);
        QueryWrapper<InsKnowledgeBase> qw = new QueryWrapper<>(entity);
        model.orderBy(qw);
        return qw;
    }
    /**
     * 通过ID查询单条数据
     *
     * @param id
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
    public Result<?> queryByPage(InsKnowledgeBaseModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        model.setClientId(null);
        List<InsKnowledgeBase> entityList = this.list(this.createQueryWrapper(model));
        // 使用并行流进行多线程处理
        entityList.parallelStream().forEach(e -> {
            QueryWrapper<InsKnowledgeBaseDetails> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(InsKnowledgeBaseDetails::getKnowledgeBaseId, e.getId());
            wrapper.lambda().eq(InsKnowledgeBaseDetails::getDataValidity, "1");
            e.setCount(Math.toIntExact(knowledgeBaseDetailsMapper.selectCount(wrapper)));
        });
        List<InsKnowledgeBaseVo> list =  knowledgeBaseConvertService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
      
    }


    @Override
    public Result listSelect(InsKnowledgeBaseModel insKnowledgeBase) {
        QueryWrapper<InsKnowledgeBase> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().select(InsKnowledgeBase::getId,InsKnowledgeBase::getName);
        List<InsKnowledgeBase> entityList = this.list(queryWrapper);
        return Result.OK(entityList);
    }

    /**
     * 新增数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 实例对象
     */
    @Override
    public Result insert(InsKnowledgeBaseModel insKnowledgeBase) {
        insKnowledgeBase.setId(IdWorker.getId());
        String  collectionName ="KB_"+ PinyinConverter.convertToPinyin(insKnowledgeBase.getName())+"_"+ RandomUtil.randomNumbers(3);
        /*List<String> strings=new ArrayList<>();
        strings.add("导航死机");
        List<Float> floats=onnxVectorServiceClient.getEmbedding(strings);
        log.error("导航死机=========向量：{}" ,JSONUtil.toJsonStr(floats));*/
//        String  collectionName ="test_"+ RandomUtil.randomNumbers(3);
//        milvusService.createCollection(collectionName, 768,insKnowledgeBase.getName());
        insKnowledgeBase.setCollectionName(collectionName);
        super.save(knowledgeBaseConvertService.convertTo(insKnowledgeBase));
        return Result.OK(insKnowledgeBase);
    }


    /**
     * 修改数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 实例对象
     */
    @Override
    public Result update(InsKnowledgeBaseModel insKnowledgeBase) {
        super.saveOrUpdate(knowledgeBaseConvertService.convertTo(insKnowledgeBase));
        return Result.OK(insKnowledgeBase);
    }

    /**
     * 通过主键删除数据
     *
     * @param id
     * @return 是否成功
     */
    @Override
    public Result deleteById(String id) {
        Assert.hasLength(id,"id不允许为空");

        QueryWrapper<InsKnowledgeBaseDetails> detailsQueryWrapper = new QueryWrapper<>();
        detailsQueryWrapper.lambda().eq(InsKnowledgeBaseDetails::getKnowledgeBaseId, id);
        if (knowledgeBaseDetailsMapper.selectCount(detailsQueryWrapper) > 0) {
         return Result.error("该知识库中还有数据，不能删除！");
        }
        knowledgeBaseDetailsMapper.delete(detailsQueryWrapper);
        InsKnowledgeBase insKnowledgeBase = super.getById(id);
        if (ObjectUtils.isNotEmpty(insKnowledgeBase.getCollectionName())) {
//            milvusService.removeCollection(insKnowledgeBase.getCollectionName());
        }
        return Result.OK(super.removeById(id));
    }
    private String getFileName(String name) {
        return ServiceContextHolder.getSystemId().concat("/").concat("file-temp").concat("/").concat(name);
    }

    @Override
    public InsKnowledgeBaseValidateVo checkUploadDataSource(InsKnowledgeBaseModel knowledgeBaseModel) throws Exception {
        Assert.hasLength(knowledgeBaseModel.getFileName(),"文件名称不允许为空");
        Assert.hasLength(knowledgeBaseModel.getId(),"知识库Id不允许为空");
//        Assert.hasLength(knowledgeBaseModel.getInputBatchId(),"导入批次id不允许为空");
        InsKnowledgeBaseValidateVo vo = new InsKnowledgeBaseValidateVo();
        String message = "";
        Map<String,Integer> map = new ConcurrentHashMap<>();
        final String batchId =IdWorker.getId();
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        try {
            @Cleanup
            InputStream objectInputStream = uploadFileService.getObjectInputStream(this.getFileName(knowledgeBaseModel.getFileName()));
            long start = System.currentTimeMillis();
            EasyExcel.read(objectInputStream, InsKnowledgeBaseTemplateVo.class,new KnowledgeBaseExcelListener(this,map,batchId,knowledgeBaseModel.getId(),fail,success)).sheet().doRead();
            long end = System.currentTimeMillis();
            log.info("读取耗时：{}", TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
            long start1 = System.currentTimeMillis();
            Integer failTotal = ObjectUtils.isNotEmpty(map.get("fail"))?map.get("fail"):0;
            Integer successTotal = ObjectUtils.isNotEmpty(map.get("success"))?map.get("success"):0;
            Integer total = failTotal+successTotal;
            vo.setInputBatchId(batchId);
            vo.setId(knowledgeBaseModel.getId());
            vo.setFileName(knowledgeBaseModel.getFileName());
            vo.setSuccess(String.valueOf(successTotal));
            vo.setTotal(String.valueOf(total));
            if(successTotal.equals(total)){
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条";
            }else if(successTotal < total&& successTotal > 0){
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条，系统仅导入有效数据";
            }else if(successTotal==0){
                message = "校验完成，共：" + total + "条，有效数据：" + success+"条,请重新上传";
            }
            vo.setMessage(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception("校验失败，请重新校验");
        }

        return vo;
    }

    @Override
    public Map<String, Integer> analyzeExcelData(List<InsKnowledgeBaseTemplateVo> list, String batchId, String knowledgeBaseId, AtomicInteger fail, AtomicInteger success, Map<String, Integer> map) {
        if(ObjectUtils.isNotEmpty(list)){
            List<InsKnowledgeBaseDetails> knowledgeBaseDetails = new ArrayList<>();
            long start1 = System.currentTimeMillis();
            InsKnowledgeBase knowledgeBase = super.getById(knowledgeBaseId);
            list.stream().forEach(e->{
                InsKnowledgeBaseDetails klbtemplate2Details = knowledgeBaseConvertService.klbtemplate2Details(e);
                klbtemplate2Details.setId(IdWorker.getId());
                klbtemplate2Details.setKnowledgeBaseId(knowledgeBaseId);
                klbtemplate2Details.setInputBatchId(batchId);
                klbtemplate2Details.setCollectionName(knowledgeBase.getCollectionName());
                klbtemplate2Details.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                if(ObjectUtils.isEmpty(e.getContent())||ObjectUtils.isEmpty(e.getContent())||ObjectUtils.isEmpty(e.getOpinion())){
                    log.warn("当前数据无效,必填项:[{}]为空",ObjectUtils.isEmpty(e.getContent())?"content":ObjectUtils.isEmpty(e.getOpinion())?"opinion":"");
                    klbtemplate2Details.setDataValidity("0");
                    fail.getAndIncrement();
                }else {
                    klbtemplate2Details.setDataValidity("1");
                    success.getAndIncrement();
                }
                knowledgeBaseDetails.add(klbtemplate2Details);
            });
            long end1 = System.currentTimeMillis();
            log.info("知识库数据批量处理完成，总耗时:{}",TimeUnit.MILLISECONDS.toSeconds(end1 - start1) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end1 - start1) + "秒" : (end1 - start1) + "毫秒");
            long start = System.currentTimeMillis();
            if(ObjectUtils.isNotEmpty(knowledgeBaseDetails)){
                List<List<InsKnowledgeBaseDetails>> split;
                if(knowledgeBaseDetails.size()>4){
                    split = CollUtil.split(knowledgeBaseDetails, knowledgeBaseDetails.size() / 4);
                }else {
                    split = CollUtil.split(knowledgeBaseDetails, 1);
                }
                List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
                split.stream().forEach(sub-> {
                    futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                        insKnowledgeBaseDetailsDao.saveBatch(sub);
                        return null;
                    })));
                });
                try {
                    CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

            }
            long end = System.currentTimeMillis();
            log.info("数据源数据批量新增完成，总耗时:{}",TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
        }
        map.put("fail",fail.get());
        map.put("success",success.get());
        return map;
    }

    @Override
    public void saveUploadData(InsKnowledgeBaseModel knowledgeBaseModel) throws Exception{
        Assert.hasLength(knowledgeBaseModel.getId(),"知识库id不允许为空");
        Assert.hasLength(knowledgeBaseModel.getInputBatchId(),"导入批次id不允许为空");
        QueryWrapper<InsKnowledgeBaseDetails> query = new QueryWrapper<>();
        query.lambda().eq(InsKnowledgeBaseDetails::getKnowledgeBaseId, knowledgeBaseModel.getId());
        query.lambda().eq(InsKnowledgeBaseDetails::getInputBatchId, knowledgeBaseModel.getInputBatchId());
        query.lambda().eq(InsKnowledgeBaseDetails::getDataValidity, "1");
        query.lambda().select(InsKnowledgeBaseDetails::getId);
        List<Object> inputBatchIds = knowledgeBaseDetailsMapper.selectObjs(query);
            if(ObjectUtils.isNotEmpty(inputBatchIds)) {
                UpdateWrapper<InsKnowledgeBaseDetails> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda().set(InsKnowledgeBaseDetails::getVectorState, "0")
                        .in(InsKnowledgeBaseDetails::getId,inputBatchIds);
                knowledgeBaseDetailsMapper.update(null, updateWrapper);
            }
    }


}
