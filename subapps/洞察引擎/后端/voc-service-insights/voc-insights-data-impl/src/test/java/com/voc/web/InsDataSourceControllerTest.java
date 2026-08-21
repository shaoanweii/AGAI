package com.voc.web;


import cn.hutool.json.JSONUtil;
import com.voc.VocInsDataApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
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
 * 数据源集(InsDataSource)单元web测试
 *
 * @author leiww
 * @since 2024-02-29 09:53:37
 */
@SpringBootTest(
        classes = {VocInsDataApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsDataSourceControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsDataSourceModel model = new InsDataSourceModel();
        model.setId(insert);
        model.setDataSourceName("testInsert");
        model.setDataSourceType("TXT");
        model.setDataSourceType("1");
//        model.setClientId("富通东方");
        model.setClientId("1");
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsDataSourceModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), this.requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataSource/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsDataSourceModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataSource/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsDataSourceModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), this.requestHeaders);
        Result rs = restTemplate.exchange("/insDataSource/update", HttpMethod.PATCH, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsDataSourceModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), this.requestHeaders);
        Result rs = restTemplate.exchange("/insDataSource/".concat(model.getId().toString()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsDataSourceModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), this.requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataSource/delete", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}

