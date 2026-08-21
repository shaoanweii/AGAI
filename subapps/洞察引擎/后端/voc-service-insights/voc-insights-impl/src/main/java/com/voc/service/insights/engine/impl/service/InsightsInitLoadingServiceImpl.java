package com.voc.service.insights.engine.impl.service;

import com.voc.service.common.api.ISystemInitLoadingService;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @Title: SystemInitLoadingServiceImpl
 * @Package: com.voc.service.common.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/22 12:13
 * @Version:1.0
 */
@Service
@Primary
public class InsightsInitLoadingServiceImpl implements ISystemInitLoadingService {
    private static final Logger log = LoggerFactory.getLogger(InsightsInitLoadingServiceImpl.class);
    @Autowired
    IInsBasicInfoService basicInfoService;
    @Autowired
    IInsDictService dictService;
    @Autowired
    IInsCustomerInfoService customerInfoService;
    @Autowired
    IInsTagInfoService tagInfoService;
    @Autowired
    IInsChannelInfoService channelInfoService;

    @Override
    public void initLoading() {
        log.info("系统初始化加载中...");

//        basicInfoService.findAll();
//        basicInfoService.findAllProvinceAreaInfo();
//        log.info("加载省市数据完成");
        basicInfoService.findAllEnergyInfo();
        log.info("加载能源信息数据完成");
        basicInfoService.findCarType();
        log.info("加载车辆类型完成");
        dictService.findDictInfoByCode(InsightsConstants.ENABLE_CODE);
        log.info("加载字典类型：停用/启用完成");
        dictService.findDictInfoByCode(InsightsConstants.RULE_TYPE);
        log.info("加载字典类型:规则类型完成");
        dictService.findDictInfoByCode(InsightsConstants.ACCOUNT_TYPE);
        log.info("加载字典类型:账号类型完成");
        /*customerInfoService.findAllCustomerInfo();
        log.info("加载获取所有客户信息完成");*/
        dictService.findDictInfoByCode(InsightsConstants.TAG_APP);
        log.info("加载应用标签完成");
        dictService.findDictInfoByCode(InsightsConstants.TAG_TYPE);
        log.info("加载标签分类完成");
        dictService.findDictInfoByCode(InsightsConstants.BUSINESS_ADD_TYPE);
        log.info("加载业务标签新增类型完成");
        dictService.findDictInfoByCode(InsightsConstants.QUALITY_ADD_TYPE);
        log.info("加载质量标签新增类型完成");
        dictService.findDictInfoByCode(InsightsConstants.TAG_ADD_TYPE);
        log.info("加载标签新增类型完成");
        /*tagInfoService.findTageInfoByType(InsightsConstants.QUALITY_TAG_TYPE);
        log.info("加载标签类型: 质量标签完成");*/
        /*tagInfoService.findTageInfoByType(InsightsConstants.BUSINESS_TAG_TYPE);
        log.info("加载标签类型: 业务标签类型完成");*/
        /*channelInfoService.findAllChannelInfo();
        log.info("加载渠道信息完成");*/
        /*dictService.findDictInfoByCode(InsightsConstants.SERIOUSNESS);
        log.info("加载字典类型：严重性完成");*/
        dictService.findDictInfoByCode(InsightsConstants.SOURCE);
        log.info("加载字典类型：来源完成");
        dictService.findDictInfoByCode(InsightsConstants.MODEL_STATUS);
        log.info("加载字典类型：模型状态完成");
        dictService.findDictInfoByCode(InsightsConstants.DATA_TYPE);
        log.info("加载字典类型：数据格式完成");
        dictService.findDictInfoByCode(InsightsConstants.MODEL_TYPE);
        log.info("加载字典类型：模型类型完成");
        dictService.findDictInfoByCode(InsightsConstants.VEHICLE_STAGE);
        log.info("加载字典类型：用车阶段完成");


        log.info("系统初始化加载完成!");
    }


}
