package com.voc.api;

import com.voc.VocLogsApplication;
import com.voc.service.common.util.IdWorker;
import com.voc.service.logs.api.IBizLogService;
import com.voc.service.logs.model.OpsLogModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

@SpringBootTest(
        classes = {VocLogsApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BizLogsServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    IBizLogService bizLogService;

    @DataProvider(name = "mockModel")
    public Object[] data() {
        OpsLogModel model = OpsLogModel.builder()
                .appId("voc")
                .userid("test_user")
                .username("test_user_name")
                .costTime(Long.valueOf(30))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .logType(1)
                .logContent("ffff")
                .build();

        return new Object[]{model};
    }

    @Test(dataProvider = "mockModel", priority = 2)
    void test_insert(OpsLogModel model) {
        bizLogService.pushBizLogsMsg(model);
    }


}
