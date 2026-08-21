package com.voc.api;


import com.voc.VocInsModelImplApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.model.IInsModelDescService;
import com.voc.service.insights.engine.model.model.InsModelDescModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 模型训练详情(InsModelDesc)单元web测试
 *
 * @author leiww
 * @since 2024-02-22 16:51:04
 */
@SpringBootTest(
        classes = {VocInsModelImplApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsModelDescServiceTest extends AbstractTestNGSpringContextTests {

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
        model.setCreateBy("test");
        model.setUpdateBy("test");
        model.setTestAcc("1");
        return new Object[]{model};
    }


    @Autowired
    IInsModelDescService insModelDescService;

    @Test(dataProvider = "mockModel", priority = 1)
    public void test_insert(InsModelDescModel model) {
        Assert.assertEquals(true, insModelDescService.insert(model));
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void test_queryBySelect(InsModelDescModel model) {
        Result<?> result = insModelDescService.queryBySelect(new InsModelDescModel());
        Assert.assertEquals("200", result.getCode());
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void test_update(InsModelDescModel model) {
        Assert.assertEquals(true, insModelDescService.update(model));
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void test_queryById(InsModelDescModel model) {
        Assert.assertNotEquals(null, insModelDescService.queryById(model.getId()));
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void test_deleteByIds(InsModelDescModel model) {
        List<Serializable> longs = Collections.singletonList(model.getId());
        Assert.assertEquals(true, insModelDescService.deleteByIds(longs));
    }
}

