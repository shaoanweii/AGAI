package com.voc.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.voc.VocInsDataApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.insights.engine.model.data.InsDataSourceDescModel;
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

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 数据集详情(InsDataDesc)单元web测试
 *
 * @author leiww
 * @since 2024-02-29 10:07:01
 */
@SpringBootTest(
        classes = {VocInsDataApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsDataSourceDescControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsDataSourceDescModel model = new InsDataSourceDescModel();
        model.setId(insert);
        model.setContent("测试内容");
        model.setProvince("广东");
        model.setModelName("测试模型");
        model.setDataId("1");
        model.setChannelId("123456");
        model.setUserName("admin11");
        model.setPublishTime(LocalDateTime.now());
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsDataSourceDescModel model) throws JsonProcessingException {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataDesc/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsDataSourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataDesc/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsDataSourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataDesc/update", HttpMethod.PATCH, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsDataSourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataDesc/".concat(model.getId().toString()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsDataSourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataDesc/delete", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}

