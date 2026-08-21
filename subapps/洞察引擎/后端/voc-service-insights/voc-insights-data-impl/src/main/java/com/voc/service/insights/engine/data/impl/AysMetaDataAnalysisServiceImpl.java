package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.api.AysExtAttrsMappingValuesService;
import com.voc.service.insights.engine.api.IAysMetaDataAnalysisService;
import com.voc.service.insights.engine.api.ILargeDigitaFilesService;
import com.voc.service.insights.engine.api.model.ProjectRawDataParamModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.common.enums.PreDataStatus;
import com.voc.service.insights.engine.data.entity.AysMetaDataAnalysisEntity;
import com.voc.service.insights.engine.data.mapper.AysMetaDataAnalysisMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Title: AysMetaDataAnalysisServiceImpl
 * @Package: com.voc.service.analysis.core.v2.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 11:49
 * @Version:1.0
 */
@Service
public class AysMetaDataAnalysisServiceImpl
        extends ServiceImpl<AysMetaDataAnalysisMapper, AysMetaDataAnalysisEntity>
        implements IAysMetaDataAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AysMetaDataAnalysisServiceImpl.class);
    @Autowired
    ILargeDigitaFilesService largeDigitaFilesService;
    @Autowired
    AysExtAttrsMappingValuesService extAttrsMappingValuesService;


    /**
     * 导出原始数据 本地上传和数据集成
     *
     * @param paramModel
     */
    @Override
    public void exportRawDataResultTask(RawDataParamModel paramModel) throws Exception {
//        final Map<String, String> extAttrsMap = extAttrsMappingValuesService.getAttrs(paramModel.getClientId());
//        log.info("{}", JSONUtil.toJsonStr(extAttrsMap));
//        List<List<String>> heads = new ArrayList<>();
//        heads.add(Arrays.asList("voc_id"));
//        extAttrsMap.values().stream().forEach(head -> heads.add(Arrays.asList(head)));
//        heads.add(Arrays.asList("数据状态"));
//        long total = this.baseMapper.pageMetaDataAnalysisListCount(paramModel);
//        log.info("本地上传/系统集成->待导出的原始数据总数据量:{}",total);
//        AysMetaDataAnalysisMapper baseMapper_ = this.baseMapper;
//        final String taskId = paramModel.getTaskId();
//
//        largeDigitaFilesService.start(
//                //TODO ckcui  新增文件名参数
//                paramModel.getFileName(),
//                taskId,
//                total,
//                heads,
//                page -> {
//                    RawDataParamModel cloneModel = RawDataParamModel.builder().build();
//                    BeanUtil.copyProperties(paramModel, cloneModel);
//
//
//                    if (page.getPageNum() == 1) {
//                        cloneModel.setPageNum(page.getPageNum()-1);
//                    } else {
//                        cloneModel.setPageNum((page.getPageNum()-1)*page.getPageSize());
//                    }
//                    cloneModel.setPageSize(page.getPageSize());
//                    List<Map<String, Object>> data = baseMapper_.pageMetaDataAnalysisList(cloneModel);
//
//                    List<List<String>> rows = new ArrayList<>();
//                    data.stream().forEach(map -> {
//                        List<String> row = new ArrayList<>();
//                        //voc_new_id
//                        if(map.containsKey("new_id")){
//                            Object o = map.get("new_id");
//                            row.add(ObjectUtils.isEmpty(o)? "" : o.toString());
//                        }
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
//                        if(map.containsKey("data_status")){
//                            Object o = map.get("data_status");
//                            row.add(ObjectUtils.isEmpty(o) ? "" : PreDataStatus.getByCode((Integer) o).getText());
//                        }
//                    });
//
//                    return rows;
//                });
//
////        largeDigitaFilesService.getFileList(LargeDigitaFilesModel.builder().userId(ServiceContextHolder.getUserId()).build());
    }


    /***
     * 导出原始数据 项目原始数据
     * @param paramModel
     * @throws Exception
     */
    @Override
    public void exportProjectRawDataResultTask(ProjectRawDataParamModel paramModel) throws Exception {
//        final Map<String, String> extAttrsMap = extAttrsMappingValuesService.getAttrs(paramModel.getClientId());
//        log.info("{}", JSONUtil.toJsonStr(extAttrsMap));
//        List<List<String>> heads = new ArrayList<>();
//        heads.add(Arrays.asList("voc_id"));
//        heads.add(Arrays.asList("本品车系"));
//        heads.add(Arrays.asList("竞品车系"));
//        heads.add(Arrays.asList("同时提及车系"));
//        extAttrsMap.values().stream().forEach(head -> heads.add(Arrays.asList(head)));
//
//        long total = this.baseMapper.projectMetaDataAnalysisListCount(paramModel);
//        log.info("项目-原始数据->待导出的总数据量:{}",total);
//        AysMetaDataAnalysisMapper baseMapper_ = this.baseMapper;
//        final String taskId = paramModel.getTaskId();
//        largeDigitaFilesService.start(
//                paramModel.getFileName(),
//                taskId,
//                total,
//                heads, page -> {
//                    ProjectRawDataParamModel cloneModel = ProjectRawDataParamModel.builder().build();
//                    BeanUtil.copyProperties(paramModel, cloneModel);
//
//                    if (page.getPageNum() == 1) {
//                        cloneModel.setPageNum(page.getPageNum()-1);
//                    } else {
//                        cloneModel.setPageNum((page.getPageNum()-1)*page.getPageSize());
//                    }
//                    cloneModel.setPageSize(page.getPageSize());
//                    log.info("导出分页参数:{},{}",page.getPageNum(),page.getPageSize());
//                    List<Map<String, Object>> data = baseMapper_.projectMetaDataAnalysisList(cloneModel);
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
//
//                        if (ObjectUtils.isNotEmpty(map.get("carSeriesName"))) {
//
//                            Object carSeriesName = map.get("carSeriesName");
//                            JSONArray jsonArray = new JSONArray(carSeriesName);
//                            Set<String> allCarSerieslist = new HashSet<>();
//                            for (int i = 0; i < jsonArray.size(); i++) {
//                                String o1 = jsonArray.getStr(i);
//                                if (StringUtils.isNotEmpty(o1) && !o1.equals("null")) {
//                                    allCarSerieslist.add(o1.toString());
//                                }
//                            }
//                            if (CollUtil.isNotEmpty(paramModel.getOwnCarSeries())) {
//                                List<String> list = allCarSerieslist.stream().filter(a -> paramModel.getOwnCarSeries().contains(a)).toList();
//                                if (CollUtil.isNotEmpty(list)) {
//                                    row.add(String.join(",", list));
//                                }
//                            }else{
//                                row.add("");
//                            }
//                            if (CollUtil.isNotEmpty(paramModel.getCompetitorsCarSeries())) {
//                                List<String> list = allCarSerieslist.stream().filter(a -> paramModel.getCompetitorsCarSeries().contains(a)).toList();
//                                if (CollUtil.isNotEmpty(list)) {
//                                    row.add(String.join(",", list));
//                                }
//                            }else{
//                                row.add("");
//                            }
//
//                            if (ObjectUtils.isNotEmpty(map.get("mentionCarSeries"))) {
//                                JSONArray objects = JSONUtil.parseArray(map.get("mentionCarSeries"));
//                                Set<String> mentionCarSeriesList = new HashSet<>();
//                                for (int i = 0; i < objects.size(); i++) {
//                                    String o1 = objects.getStr(i);
//                                    if (StringUtils.isNotEmpty(o1) && !o1.equals("null")) {
//                                        JSONArray array = JSONUtil.parseArray(o1);
//                                        for (int j = 0; j < array.size(); j++) {
//                                            String o2 = array.getStr(j);
//                                            if (StringUtils.isNotEmpty(o2) && !o2.equals("null")) {
//                                                mentionCarSeriesList.add(o2.toString());
//                                            }
//                                        }
//                                    }
//                                }
//                                List<String> mentionlist = mentionCarSeriesList.stream().filter(a -> !paramModel.getOwnCarSeries().contains(a)).toList();
//                                row.add(StringUtils.join(mentionlist, ","));
//                            }
//                        }
//
//                        if (map.containsKey("bizExtAttrs")) {
//                            JSONObject biz_ext_attrs_json = JSONUtil.parseObj(map.get("bizExtAttrs"));
//                            JSONObject biz_ext_attrs2_json = JSONUtil.parseObj(map.get("bizExtAttrs2"));
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
//                            log.error("未找到bizExtAttrs字段");
//                        }
//                    });
//
//                    return rows;
//                });

//        largeDigitaFilesService.getFileList(LargeDigitaFilesModel.builder().userId(ServiceContextHolder.getUserId()).build());
    }

}
