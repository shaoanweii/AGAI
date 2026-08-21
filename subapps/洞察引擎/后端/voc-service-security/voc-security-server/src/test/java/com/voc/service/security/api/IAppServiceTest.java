package com.voc.service.security.api;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.security.model.AppModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ComponentScan("com.voc")
public class IAppServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    IAppService appService;

    @DataProvider(name="mockMoel")
    public Object[] datas(){
        return  new Object[]{AppModel.builder().appId("voc").build()};
    }

    @Test(dataProvider = "mockMoel")
    public void find(AppModel mockMoel) {
        AppModel rs = appService.find(mockMoel);
        assertEquals(rs.getAppId(), mockMoel.getAppId());
    }
    @Test
    public void findAll() {
        List<AppModel> rs = appService.findAll();
        assertTrue(CollUtil.isNotEmpty(rs));
    }

}