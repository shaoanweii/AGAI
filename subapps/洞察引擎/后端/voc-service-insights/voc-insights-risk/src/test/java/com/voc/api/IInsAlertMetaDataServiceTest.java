package com.voc.api;

import com.voc.VocAnalysisRiskApplication;
import com.voc.service.insights.engine.api.alert.IInsAlertMetaDataService;
import com.voc.service.insights.engine.api.constants.AlertTaskEnum;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

/**
 * @author cuick
 * @version 1.1.0
 * @ClassName
 * @Description
 * @createTime 2024/3/20 15:59
 */
@SpringBootTest(
        classes = {VocAnalysisRiskApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IInsAlertMetaDataServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IInsAlertMetaDataService metaDataService;



    @DataProvider(name = "mockModel")
    public Object[] data() {
        AlertTaskModel model = AlertTaskModel.builder()
                .dataType(AlertTaskEnum.META_DATA.getCode())
                .id("1")
                .createTime(LocalDateTime.now())
                .channelId("1372001238165569242")
                .clientId("1")
                .name(AlertTaskEnum.META_DATA.getName().concat("400"))
                .build();
        return new Object[]{model};
    }

    @Test(dataProvider = "mockModel", priority = 1)
    public void test_meta_insert(AlertTaskModel model) {
        metaDataService.execute(model);
    }
}
