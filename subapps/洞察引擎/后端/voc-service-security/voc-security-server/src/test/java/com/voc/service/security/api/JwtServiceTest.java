package com.voc.service.security.api;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.config.JwtService;
import com.voc.service.security.model.AppModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ComponentScan("com.voc")
public class JwtServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    JwtService jwtService;

    @Test
    public void test_generateToken() {
        String token = jwtService.generateToken(UserModel.builder()
                        .appId("report-nissan-dndc")
                        .type("base")
                        .username("report_dndc_api")
                        .userId("report_dndc_api")
                        .build(),
               365 * 75 * 24 * 10
        );
        System.out.println(token);

    }

}