package com.voc.api;


import com.voc.VocInsModelImplApplication;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.model.IInsModelInfoService;
import com.voc.service.insights.engine.model.model.InsModelInfoModel;
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
 * 模型配置数据(InsModelInfo)单元web测试
 *
 * @author leiww
 * @since 2024-02-21 16:24:44
 */
@SpringBootTest(
        classes = {VocInsModelImplApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InsModelInfoServiceTest extends AbstractTestNGSpringContextTests {

    static final String insert = IdWorker.getId();
    static final String update = IdWorker.getId();

    @DataProvider(name = "mockModel")
    public Object[] data() {
        InsModelInfoModel model = new InsModelInfoModel();
        model.setId(insert);
        model.setModelName("测试模型");
        model.setProjectId("1");
        model.setProjectName("测试模型项目");
        model.setModelType("1");
        model.setClientId("1");
        model.setFormat("1");
        return new Object[]{model};
    }


    @Autowired
    private IInsModelInfoService insModelInfoService;

    @Test(dataProvider = "mockModel", priority = 1)
    public void test_insert(InsModelInfoModel model) {
        Assert.assertEquals(true, insModelInfoService.insert(model));
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void test_queryBySelect(InsModelInfoModel model) {
        Result<?> result = insModelInfoService.queryBySelect(new InsModelInfoModel());
        Assert.assertEquals("200", result.getCode());
    }

    @Test(dataProvider = "mockModel", priority = 3)
    public void test_update(InsModelInfoModel model) {
        Assert.assertEquals(true, insModelInfoService.update(model));
    }

    @Test(dataProvider = "mockModel", priority = 4)
    public void test_queryById(InsModelInfoModel model) {
        Assert.assertNotEquals(null, insModelInfoService.queryById(model.getId()));
    }

    @Test(dataProvider = "mockModel", priority = 5)
    public void test_deleteByIds(InsModelInfoModel model) {
        List<Serializable> longs = Collections.singletonList(model.getId());
        Assert.assertEquals(true, insModelInfoService.deleteByIds(longs));
    }
}

