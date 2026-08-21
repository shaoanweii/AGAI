package com.voc.web;

import cn.hutool.json.JSONUtil;
import com.voc.VocInsDataApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.model.InsRegulationDetailsModel;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/28 13:38
 * @描述:
 **/
@SpringBootTest(classes = {VocInsDataApplication.class}
        , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class InsRegulationInfoControllerTest extends BaseloginTest {

    static final String insert = IdWorker.getId();
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsRegulationInfoModel model = new InsRegulationInfoModel();
        InsRegulationDetailsModel detailsModel = new InsRegulationDetailsModel();
//        detailsModel.setRegulationCondition("单元测试条件");
        model.setId(insert);
        model.setName("单元测试"+insert.substring(0,6));
        model.setMatchingRule("all");
//        model.setRegulationCondition(Arrays.asList(detailsModel));
        model.setStatus("1");
        return new Object[]{model};
    }

    /**
     * 新增规则信息
     * @param model
     */
    @Test(dataProvider = "mockModel",priority=1)
    public void test_saveRegulationInfo(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("id",model.getId());
        params.put("name",model.getName());
        params.put("matchingRule",model.getMatchingRule());
//        params.put("regulationCondition",model.getRegulationCondition());
        params.put("status", model.getStatus());
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/saveRegulationInfo"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }


    /**
     * 更新规则信息
     * @param model
     */
    @Test(dataProvider = "mockModel",priority=2)
    public void test_updateRegulationInfo(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("id",insert);
        params.put("name",model.getName()+1);
        params.put("matchingRule",model.getMatchingRule());
//        params.put("regulationCondition",model.getRegulationCondition());
        params.put("status",0);
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/updateRegulationInfo"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    /**
     * 分页查询规则列表信息
     */
    @Test(priority=3)
    public void test_findRegulationInfoList(){
        Map<String, Object> params = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/findRegulationInfoList"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    /**
     * 按规则名称条件查询列表信息
     * @param model
     */
    @Test(dataProvider = "mockModel",priority=3)
    public void test_findRegulationInfoList1(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("name",model.getName().substring(0,2));
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/findRegulationInfoList"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    /**
     * 按状态查询规则列表信息
     * @param model
     */
    @Test(dataProvider = "mockModel",priority=3)
    public void test_findRegulationInfoList2(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("status",model.getStatus());
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/findRegulationInfoList"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    /**
     * 按规则名称和状态查询规则列表信息
     * @param model
     */
    @Test(dataProvider = "mockModel",priority=3)
    public void test_findRegulationInfoList3(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("name",model.getName().substring(0,2));
        params.put("status",model.getStatus());
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/findRegulationInfoList"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    /**
     * 根据id查询规则信息
     */
    @Test(priority=4)
    public void test_findRegulationInfo(){
        Map<String, Object> params = new HashMap<>();
        params.put("id",insert);
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/findRegulationInfo"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(priority=5)
    public void test_deleteRegulationInfo(){
        Map<String, Object> params = new HashMap<>();
        params.put("id",insert);
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/deleteRegulationInfo"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel",priority=6)
    public void test_validateRegulationInfoError(InsRegulationInfoModel model){
        Map<String, Object> params = new HashMap<>();
        params.put("id",insert);
//        params.put("name",model.getName());
        params.put("matchingRule",model.getMatchingRule());
//        params.put("regulationCondition",model.getRegulationCondition());
        params.put("status", model.getStatus());
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);
        Result rs = restTemplate.postForEntity("/regulation/saveRegulationInfo"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "500");
    }

}
