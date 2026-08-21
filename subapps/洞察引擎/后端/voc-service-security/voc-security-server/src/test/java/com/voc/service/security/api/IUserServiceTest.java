package com.voc.service.security.api;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.model.UserModel;
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
public class IUserServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    ISecurityService service;

    @DataProvider(name="mockMoel")
    public Object[] datas(){
        return  new Object[]{AppModel.builder().appId("voc").build()};
    }

    @Test
    public void findAll() {
        List<UserModel> rs = service.findAll(UserModel.builder()
                .appId("modeltraining")
                .build());
        System.out.println(rs);
        assertTrue(CollUtil.isNotEmpty(rs));
    }

    @Test
    public void findByUserId() {
        List<UserModel> rs = service.findByUserId(UserModel.builder()
                .appId("modeltraining")
                .userId("93e1a6f5798c1d8339c784083affc044").build());
        System.out.println(rs);
        assertTrue(CollUtil.isNotEmpty(rs));
    }
}