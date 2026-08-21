package com.voc.web;


import cn.hutool.json.JSONUtil;
import com.voc.VocInsModelImplApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.model.model.InsModelInfoModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * 模型配置数据(InsModelInfo)单元web测试
 *
 * @author leiww
 * @since 2024-02-21 16:10:11
 */
@SpringBootTest(
        classes = {VocInsModelImplApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsModelInfoControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsModelInfoModel model = new InsModelInfoModel();
        model.setId(insert);
        model.setModelName("测试模型");
        model.setProjectId("1");
        model.setModelType("LabelModel");
        model.setClientId("1");
        model.setFormat("TXT");
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsModelInfoModel model) {
        String jsonStr = JSONUtil.toJsonStr(model);
        HttpEntity requestEntity = new HttpEntity<>(jsonStr, requestHeaders);
        Result rs = restTemplate.postForEntity("/insModelInfo/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsModelInfoModel model) {
        String jsonStr = JSONUtil.toJsonStr(model);
        HttpEntity requestEntity = new HttpEntity<>(jsonStr,requestHeaders);
        Result rs = restTemplate.exchange("/insModelInfo/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsModelInfoModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
        Result rs = restTemplate.exchange("/insModelInfo/update", HttpMethod.PATCH, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsModelInfoModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
        Result rs = restTemplate.exchange("/insModelInfo/".concat(model.getId().toString()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsModelInfoModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), requestHeaders);
        Result rs = restTemplate.postForEntity("/insModelInfo/delete", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}

