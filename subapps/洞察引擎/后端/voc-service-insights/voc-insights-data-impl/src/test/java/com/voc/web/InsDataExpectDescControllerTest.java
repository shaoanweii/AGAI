package com.voc.web;


import com.voc.VocInsDataApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.insights.engine.model.data.InsDataExpectDescModel;
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
 * 语料库数据详情(InsDataExpectDesc)单元web测试
 *
 * @author leiww
 * @since 2024-03-05 16:46:10
 */
@SpringBootTest(
        classes = {VocInsDataApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsDataExpectDescControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsDataExpectDescModel model = new InsDataExpectDescModel();
        model.setId(insert);
        model.setExpectId(insert);
        model.setContent("测试内容");
        model.setEmotion("test");
        model.setIntention("test");
        model.setBusiness("test");
        model.setScene("test");
        model.setViewpoint("test");
        model.setQuality("test");
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsDataExpectDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataExpectDesc/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsDataExpectDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataExpectDesc/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsDataExpectDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.exchange("/insDataExpectDesc/update", HttpMethod.PATCH, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsDataExpectDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.exchange("/insDataExpectDesc/".concat(model.getId().toString()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsDataExpectDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataExpectDesc/delete", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}

