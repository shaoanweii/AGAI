package com.voc.service.insights.engine.data.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.api.AysExtAttrsMappingValuesService;
import com.voc.service.insights.engine.api.IAysPostprocessDataService;
import com.voc.service.insights.engine.api.ILargeDigitaFilesService;
import com.voc.service.insights.engine.api.model.ProjectResultDataParamModel;
import com.voc.service.insights.engine.api.model.ResultDataParamModel;
import com.voc.service.insights.engine.data.entity.AysPostprocessDataEntity;
import com.voc.service.insights.engine.data.mapper.AysPostprocessDataMapper;
import com.voc.service.insights.engine.enums.TagLibeType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysPostprocessDataServiceImpl extends ServiceImpl<AysPostprocessDataMapper, AysPostprocessDataEntity>
        implements IAysPostprocessDataService {
    private static final Logger log = LoggerFactory.getLogger(AysPostprocessDataServiceImpl.class);

    @Autowired
    ILargeDigitaFilesService largeDigitaFilesService;
    @Autowired
    AysExtAttrsMappingValuesService extAttrsMappingValuesService;


    /**
     * 导出结果数据 本地上传和数据集成
     *
     * @param paramModel
     */
    @Override
    public void exportResultDataTask(ResultDataParamModel paramModel) throws Exception {
//        final Map<String, String> extAttrsMap = extAttrsMappingValuesService.getAttrs(paramModel.getClientId());
//        log.info("{}", JSONUtil.toJsonStr(extAttrsMap));
//        List<List<String>> heads = new ArrayList<>();
//        heads.add(Arrays.asList("voc_id"));
//        heads.add(Arrays.asList("品牌"));
//        heads.add(Arrays.asList("车系"));
//        heads.add(Arrays.asList("声音片段"));
//        heads.add(Arrays.asList("标签类型"));
//        heads.add(Arrays.asList("一级标签"));
//        heads.add(Arrays.asList("二级标签"));
//        heads.add(Arrays.asList("三级标签"));
//        heads.add(Arrays.asList("四级标签"));
//        heads.add(Arrays.asList("观点标签"));
//        heads.add(Arrays.asList("相似观点"));
//        heads.add(Arrays.asList("情感"));
//        heads.add(Arrays.asList("意图"));
//        extAttrsMap.values().stream().forEach(head -> heads.add(Arrays.asList(head)));
//        heads.add(Arrays.asList("数据状态"));
//        long total = this.baseMapper.pagePostprocessDataListCount(paramModel);
//        log.info("本地上传/系统集成->待导出的结果总数据量:{}",total);
//        AysPostprocessDataMapper baseMapper_ = this.baseMapper;
//        final String taskId = paramModel.getTaskId();
//        largeDigitaFilesService.start(
//                //TODO ckcui  新增文件名参数
//                paramModel.getFileName(),
//                taskId,
//                total,
//                heads, page -> {
//                    ResultDataParamModel cloneModel = ResultDataParamModel.builder().build();
//                    BeanUtil.copyProperties(paramModel, cloneModel);
//
//                    if (page.getPageNum() == 1) {
//                        cloneModel.setPageNum(page.getPageNum()-1);
//                    } else {
//                        cloneModel.setPageNum((page.getPageNum()-1)*page.getPageSize());
//                    }
//
//                    cloneModel.setPageSize(page.getPageSize());
//                    List<Map<String, Object>> data = baseMapper_.pagePostprocessDataList(cloneModel);
//
//                    List<List<String>> rows = new ArrayList<>();
//                    data.stream().forEach(map -> {
//                        List<String> row = new ArrayList<>();
//                        //voc_new_id
//                        if(map.containsKey("new_id")){
//                            Object o = map.get("new_id");
//                            row.add(ObjectUtils.isEmpty(o)? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //品牌
//                        if(map.containsKey("brand_code_name")){
//                            Object o = map.get("brand_code_name");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        List<String> allCarSeriesList = new ArrayList<>();
//                        //车系
//                        if(map.containsKey("car_series_name")){
//                            Object o = map.get("car_series_name");
//                            if(ObjectUtils.isNotEmpty(o)&&String.valueOf(o).contains(",")){
//                                allCarSeriesList = Arrays.asList(StringUtils.split(o.toString(), ","));
//                                if(CollUtil.isNotEmpty(allCarSeriesList)){
//                                    row.add(String.join(",", allCarSeriesList));
//                                }else {
//                                    row.add("");
//                                }
//                            }else if(ObjectUtils.isNotEmpty(o)&&!String.valueOf(o).contains(",")){
//                                row.add(o.toString());
//                            }else{
//                                row.add("");
//                            }
//                        }else{
//                            row.add("");
//                        }
//
//                        //声音片段
//                        if(map.containsKey("original_text_scene")){
//                            Object o = map.get("original_text_scene");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //标签类型
//                        if(map.containsKey("label_type")){
//                            Object o = map.get("label_type");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : TagLibeType.getByCode((String) o).getText());
//                        }else{
//                            row.add("");
//                        }
//                        //一级标签
//                        if(map.containsKey("label_type_level_first")){
//                            Object o = map.get("label_type_level_first");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //二级标签
//                        if(map.containsKey("label_type_level_second")){
//                            Object o = map.get("label_type_level_second");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //三级标签
//                        if(map.containsKey("label_type_level_three")){
//                            Object o = map.get("label_type_level_three");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //四级标签
//                        if(map.containsKey("label_type_level_four")){
//                            Object o = map.get("label_type_level_four");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //观点标签
//                        if(map.containsKey("topic")){
//                            Object o = map.get("topic");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //相似观点
//                        if(map.containsKey("similar_topic")){
//                            Object o = map.get("similar_topic");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //情感
//                        if(map.containsKey("sentiment")){
//                            Object o = map.get("sentiment");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //意图
//                        if(map.containsKey("intention_type")){
//                            Object o = map.get("intention_type");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//
//                        if (map.containsKey("biz_ext_attrs")) {
//                            JSONObject biz_ext_attrs_json = JSONUtil.parseObj(map.get("biz_ext_attrs"));
//                            JSONObject biz_ext_attrs2_json = JSONUtil.parseObj(map.get("biz_ext_attrs2"));
//
//                            extAttrsMap.keySet().stream().forEach(key -> {
//                                final String value;
//                                if ("voc_content".equalsIgnoreCase(key)) {
//                                    value = biz_ext_attrs2_json.get(key) == null ? "" : biz_ext_attrs2_json.get(key).toString();
//                                } else {
//                                    value = biz_ext_attrs_json.get(key) == null ? "" : biz_ext_attrs_json.get(key).toString();
//                                }
//                                row.add(value);
//                            });
//                            rows.add(row);
//                        } else {
//                            log.error("未找到biz_ext_attrs字段");
//                        }
//
//                        if(map.containsKey("abandon")){
//                            Object o = map.get("abandon");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : "1".equals(o.toString()) ? "过滤数据" : "统计数据");
//                        }
//                    });
//
//                    return rows;
//                });
    }

    /**
     * 导出结果数据 项目结果数据
     *
     * @param paramModel
     */
    @Override
    public void exportProjectResultDataTask(ProjectResultDataParamModel paramModel) throws Exception {
//        final Map<String, String> extAttrsMap = extAttrsMappingValuesService.getAttrs(paramModel.getClientId());
//        log.info("{}", JSONUtil.toJsonStr(extAttrsMap));
//        List<List<String>> heads = new ArrayList<>();
//        heads.add(Arrays.asList("voc_id"));
//        heads.add(Arrays.asList("品牌"));
//        heads.add(Arrays.asList("车系"));
//        heads.add(Arrays.asList("本品车系"));
//        heads.add(Arrays.asList("竞品车系"));
//        heads.add(Arrays.asList("同时提及车系"));
//        heads.add(Arrays.asList("声音片段"));
//        heads.add(Arrays.asList("标签类型"));
//        heads.add(Arrays.asList("一级标签"));
//        heads.add(Arrays.asList("二级标签"));
//        heads.add(Arrays.asList("三级标签"));
//        heads.add(Arrays.asList("四级标签"));
//        heads.add(Arrays.asList("观点标签"));
//        heads.add(Arrays.asList("相似观点"));
//        heads.add(Arrays.asList("情感"));
//        heads.add(Arrays.asList("意图"));
//
//        extAttrsMap.values().stream().forEach(head -> heads.add(Arrays.asList(head)));
//
//        long total = this.baseMapper.pageProjectPostDataCount(paramModel);
//        log.info("项目-结果数据->待导出的总数据量:{}",total);
//        AysPostprocessDataMapper baseMapper_ = this.baseMapper;
//        final String taskId = paramModel.getTaskId();
//        largeDigitaFilesService.start(
//                paramModel.getFileName(),
//                taskId,
//                total,
//                heads, page -> {
//                    ProjectResultDataParamModel cloneModel = ProjectResultDataParamModel.builder().build();
//                    BeanUtil.copyProperties(paramModel, cloneModel);
//
//                    if (page.getPageNum() == 1) {
//                        cloneModel.setPageNum(page.getPageNum()-1);
//                    } else {
//                        cloneModel.setPageNum((page.getPageNum()-1)*page.getPageSize());
//                    }
//
//                    cloneModel.setPageSize(page.getPageSize());
//                    List<Map<String, Object>> data = baseMapper_.pageProjectPostDataList(cloneModel);
//
//                    List<List<String>> rows = new ArrayList<>();
//                    data.stream().forEach(map -> {
//                        List<String> row = new ArrayList<>();
//                        //voc_new_id
//                        if(map.containsKey("new_id")){
//                            Object o = map.get("new_id");
//                            row.add(ObjectUtils.isEmpty(o)? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //品牌
//                        if(map.containsKey("brand_code_name")){
//                            Object o = map.get("brand_code_name");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        List<String> allCarSeriesList = new ArrayList<>();
//                        //车系
//                        if(map.containsKey("car_series_name")){
//                            Object o = map.get("car_series_name");
//                            if(ObjectUtils.isNotEmpty(o)&&String.valueOf(o).contains(",")){
//                                allCarSeriesList = Arrays.asList(StringUtils.split(o.toString(), ","));
//                                if(CollUtil.isNotEmpty(allCarSeriesList)){
//                                    row.add(String.join(",", allCarSeriesList));
//                                }else {
//                                    row.add("");
//                                }
//                            }else if(ObjectUtils.isNotEmpty(o)&&!String.valueOf(o).contains(",")){
//                                row.add(o.toString());
//                                allCarSeriesList.add(o.toString());
//                            }else{
//                                row.add("");
//                            }
//                        }else{
//                            row.add("");
//                        }
//
//                        //本品车系
//                        if(ObjectUtils.isNotEmpty(allCarSeriesList)){
//                            row.add(String.join(",", allCarSeriesList));
//                        }else{
//                            row.add("");
//                        }
//                        //竞品车系
//                        if(ObjectUtils.isNotEmpty(allCarSeriesList)){
//                            if(ObjectUtils.isNotEmpty(cloneModel.getCompetitorsCarSeries())){
//                                List<String> list = allCarSeriesList.stream().filter(a -> cloneModel.getCompetitorsCarSeries().contains(a)).toList();
//                                if(ObjectUtils.isNotEmpty(list)){
//                                    row.add(String.join(",", list));
//                                }else{
//                                    row.add("");
//                                }
//                            }else{
//                                row.add("");
//                            }
//                        }else{
//                            row.add("");
//                        }
//                        //同时提及车系
//                        if(map.containsKey("mentionCarSeries")){
//                            Object o = map.get("mentionCarSeries");
//                            if(ObjectUtils.isNotEmpty(o)&&!String.valueOf(o).equals("\"[]\"")){
//                                JSONArray objects = JSONUtil.parseArray(o);
//                                List<String> list = new ArrayList<>();
//                                for (int i = 0; i < objects.size(); i++) {
//                                    String o1 = objects.getStr(i);
//                                    if (StringUtils.isNotEmpty(o1) && !o1.equals("null")) {
//                                        list.add(o1.toString());
//                                    }
//                                }
//                                List<String> mentionlist = list.stream().filter(a -> !cloneModel.getOwnCarSeries().contains(a)).toList();
//                                row.add(StringUtils.join(mentionlist, ","));
//                            }else{
//                                row.add("");
//                            }
//                        }else{
//                            row.add("");
//                        }
//
//                        //声音片段
//                        if(map.containsKey("original_text_scene")){
//                            Object o = map.get("original_text_scene");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //标签类型
//                        if(map.containsKey("label_type")){
//                            Object o = map.get("label_type");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : TagLibeType.getByCode((String) o).getText());
//                        }else{
//                            row.add("");
//                        }
//                        //一级标签
//                        if(map.containsKey("label_type_level_first")){
//                            Object o = map.get("label_type_level_first");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //二级标签
//                        if(map.containsKey("label_type_level_second")){
//                            Object o = map.get("label_type_level_second");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //三级标签
//                        if(map.containsKey("label_type_level_three")){
//                            Object o = map.get("label_type_level_three");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //四级标签
//                        if(map.containsKey("label_type_level_four")){
//                            Object o = map.get("label_type_level_four");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //观点标签
//                        if(map.containsKey("topic")){
//                            Object o = map.get("topic");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //相似观点
//                        if(map.containsKey("similar_topic")){
//                            Object o = map.get("similar_topic");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //情感
//                        if(map.containsKey("sentiment")){
//                            Object o = map.get("sentiment");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//                        //意图
//                        if(map.containsKey("intention_type")){
//                            Object o = map.get("intention_type");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : o.toString());
//                        }else{
//                            row.add("");
//                        }
//
//                        if (map.containsKey("biz_ext_attrs")) {
//                            JSONObject biz_ext_attrs_json = JSONUtil.parseObj(map.get("biz_ext_attrs"));
//                            JSONObject biz_ext_attrs2_json = JSONUtil.parseObj(map.get("biz_ext_attrs2"));
//
//                            extAttrsMap.keySet().stream().forEach(key -> {
//                                final String value;
//                                if ("voc_content".equalsIgnoreCase(key)) {
//                                    value = biz_ext_attrs2_json.get(key) == null ? "" : biz_ext_attrs2_json.get(key).toString();
//                                } else {
//                                    value = biz_ext_attrs_json.get(key) == null ? "" : biz_ext_attrs_json.get(key).toString();
//                                }
//                                row.add(value);
//                            });
//                            rows.add(row);
//                        } else {
//                            log.error("未找到biz_ext_attrs字段");
//                        }
//                    });
//
//                    return rows;
//                });
    }


}
