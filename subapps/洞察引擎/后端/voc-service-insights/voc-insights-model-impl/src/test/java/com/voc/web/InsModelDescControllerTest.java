package com.voc.web;


import cn.hutool.json.JSONUtil;
import com.voc.VocInsModelImplApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.insights.engine.model.model.InsModelDescModel;
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
 * 模型训练详情(InsModelDesc)单元web测试
 *
 * @author leiww
 * @since 2024-02-22 16:51:03
 */
@SpringBootTest(
        classes = {VocInsModelImplApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsModelDescControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsModelDescModel model = new InsModelDescModel();
        model.setId(insert);
        model.setModelDesc("test");
        model.setModelId("1");
        model.setVersion("1.0.0.00");
        model.setModelLabel("多标签分类模型");
        model.setModelPath("test.os");
        model.setStatus("1");
        model.setVersionDesc("这是一段描述");
        model.setTestAcc("1");
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsModelDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
        Result rs = restTemplate.postForEntity("/insModelDesc/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsModelDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insModelDesc/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsModelDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
        Result rs = restTemplate.exchange("/insModelDesc/update", HttpMethod.PATCH, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsModelDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
        Result rs = restTemplate.exchange("/insModelDesc/".concat(model.getId().toString()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsModelDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), requestHeaders);
        Result rs = restTemplate.postForEntity("/insModelDesc/delete", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}

