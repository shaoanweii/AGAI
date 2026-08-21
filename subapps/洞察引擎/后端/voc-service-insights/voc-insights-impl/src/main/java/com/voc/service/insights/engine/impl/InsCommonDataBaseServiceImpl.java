package com.voc.service.insights.engine.impl;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.api.IInsTagLibService;
import com.voc.service.insights.engine.api.InsCommonDataBaseService;
import com.voc.service.insights.engine.dao.InsCommonDataBaseDao;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;
import com.voc.service.insights.engine.vo.TagLibVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/28 上午9:32
 * @描述:
 **/
@Service
public class InsCommonDataBaseServiceImpl  implements InsCommonDataBaseService {
    private static final Logger log = LoggerFactory.getLogger(InsCommonDataBaseServiceImpl.class);
    @Autowired
    InsCommonDataBaseDao commonDataBaseDao;
    @Value("${default.common.clientId:764547797eb2e192763f5334028d49c9}")
    String commonClientId;
    @Autowired
    IInsTagLibService tagLibService;

    @Override
    public PageInfo getCommonDataList(InsCommonDataBaseModel commonDataBaseModel) {
        log.debug("入参:{}", commonDataBaseModel);
        if(ObjectUtils.isNotEmpty(commonDataBaseModel.getBusinessEndTag())){
            List<TagLibVo> tagLibCategoryVos = tagLibService.findTagLibByIds(commonDataBaseModel.getBusinessEndTag());
            List<String> collect = tagLibCategoryVos.stream().map(TagLibVo::getTagName).collect(Collectors.toList());
            commonDataBaseModel.setBusinessEndTag(collect);
        }
        if(ObjectUtils.isNotEmpty(commonDataBaseModel.getQualityEndTag())){
            List<TagLibVo> tagLibCategoryVos = tagLibService.findTagLibByIds(commonDataBaseModel.getQualityEndTag());
            List<String> collect = tagLibCategoryVos.stream().map(TagLibVo::getTagName).collect(Collectors.toList());
            commonDataBaseModel.setQualityEndTag(collect);
        }
        commonDataBaseModel.setClientId(commonClientId);
        return commonDataBaseDao.getCommonDataList(commonDataBaseModel);
    }
}