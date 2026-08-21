package com.voc.web;

import com.voc.VocInsDataApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
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
 * 资源详情(InsDataResourceDesc)单元web测试
 *
 * @author leiww
 * @since 2024-04-08 11:18:21
 */
@SpringBootTest(
        classes = {VocInsDataApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsDataResourceDescControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    static final String insert = IdWorker.getId();

    static final String update = IdWorker.getId();

    static final Integer random = Math.abs(IdWorker.getId().hashCode());

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsDataResourceDescModel model = new InsDataResourceDescModel();
        model.setId(insert);
        model.setName(update);
        model.setStatus("Disabled");
        model.setResourceId(update);
        // 处理其他数据类型
        // 处理其他数据类型
        model.setUpdateBy(update);
        model.setCreateBy(update);
        return new Object[]{model};
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test(dataProvider = "mockModel", priority = 1)
    public void testInsert(InsDataResourceDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.postForEntity("/insDataResourceDesc/insert", requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void testQuery(InsDataResourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(JsonMapper.getInstances().toJson(model), requestHeaders);
        Result rs = restTemplate.exchange("/insDataResourceDesc/list", HttpMethod.POST, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void testUpdate(InsDataResourceDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.exchange("/insDataResourceDesc/update", HttpMethod.PUT, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void testSelectOne(InsDataResourceDescModel model) {
        String json = JsonMapper.getInstances().toJson(model);
        HttpEntity requestEntity = new HttpEntity<>(json, requestHeaders);
        Result rs = restTemplate.exchange("/insDataResourceDesc/".concat(model.getId()), HttpMethod.GET, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void testDelete(InsDataResourceDescModel model) {
        HttpEntity requestEntity = new HttpEntity<>(Collections.singletonList(model.getId()), requestHeaders);
        Result rs = restTemplate.exchange("/insDataResourceDesc/delete/".concat(model.getId()).concat("/").concat(model.getResourceId()), HttpMethod.DELETE, requestEntity, Result.class).getBody();
        Assert.assertEquals(rs.getCode(), "200");

    }
}


