package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.AysRegulationInfoVo;
import com.voc.service.insights.engine.vo.InsTableInfoVo;
import com.voc.service.insights.engine.vo.InsValidateInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/8 15:53
 * @描述:
 **/
@FeignClient(name = "service.ins.v1", url = "${service.ins.v1}/regulation")
//@FeignClient(name = "service.ins.v1", url = "localhost:8060/regulation")
public interface IInsRegulationServiceClient {

    @PostMapping("/findAllRegulationInfo")
    Result<List<AysRegulationInfoVo>> findAllRegulationInfo(@RequestBody UserModel userModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/14 16:09
     * @描述   获取表结构及数据
     * @param tableInfoModel
     * @return com.voc.service.common.response.Result<java.util.List<com.voc.service.insights.engine.vo.InsTableInfoVo>>
     **/
    @PostMapping("/findTableInfoList")
    Result<List<InsTableInfoVo>> findTableInfoList(@RequestBody InsTableInfoModel tableInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/14 16:09
     * @描述   获取规则列表
     * @param ruleInfoModel
     * @return com.voc.service.common.response.Result<java.util.List<com.voc.service.insights.engine.vo.InsRuleInfoVo>>
     **/
    @PostMapping("/findRegulationList")
    Result<List<AysRegulationInfoVo>> findRulesList(@RequestBody InsRegulationInfoModel ruleInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/29 15:37
     * @描述   接收规则验证状态
     * @param validateRuleInfoModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/pushValidateRegulationStatus")
    Result<?> pushValidateRuleStatus(@RequestBody InsValidateRuleInfoModel validateRuleInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/23 17:06
     * @描述   获取最新校验信息
     * @return com.voc.service.common.response.Result<java.util.List<com.voc.service.insights.engine.vo.InsValidateInfoVo>>
     **/
    @GetMapping("/findNewestValidateRuleInfo")
    Result<List<InsValidateInfoVo>> findNewestValidateRuleInfo();
}
