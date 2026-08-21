package com.voc.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.VocAnalysisCoreV2Application;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(
        classes = {VocAnalysisCoreV2Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnalysisControllerTest extends AbstractTestNGSpringContextTests {
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    AnalysisConfig config;

    @DataProvider(name = "mockModel")
    public Object[] data() {

        String c = StrUtil.removeAll("350km\n" +
                "行驶里程\n" +
                "13.0kWh\n" +
                "春秋电耗\n" +
                "410公里\n" +
                "春秋续航\n" +
                "13.18万\n" +
                "裸车购买价\n" +
                "2024-04\n" +
                "购买时间\n" +
                "宿迁\n" +
                "购买地点\n" +
                "最满意\n" +
                "满意的当属德系的底盘调教，德系的做工工艺。驾驶感受也是当仁不让。\n" +
                "最不满意\n" +
                "内饰简漏，车机不够智能，还没有备胎没有安全感！\n" +
                "空间\n" +
                "4\n" +
                "别看车身小，空间缺不小！全车轴距2765足以证明了\n" +
                "驾驶感受\n" +
                "5\n" +
                "18寸的轮毂！四轮独立，再加上后驱，就这驾驶感觉能差喽？\n" +
                "续航\n" +
                "4\n" +
                "按照现在春季充满电，从城区跑到乡下，正常410公里这样\n" +
                "外观\n" +
                "4\n" +
                "外观流线行的外观设计，张扬的大灯设计在呆萌的前脸上又突现出精神\n" +
                "内饰\n" +
                "3\n" +
                "内饰简漏不是大众和所有合资车的特点吗？不过做工精细也是德系独有的个性\n" +
                "性价比\n" +
                "4\n" +
                "360全景，L2智能驾驶，电动调节座椅，真皮方向盘，还有加热，性价比很高\n" +
                "智能化\n" +
                "3\n" +
                "智能化就不谈了动不动没网，不过听听歌，用用导航，还是可以的", "\n");

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final String data = "{\n" +
                    "        \"clientId\": \"1\",\n" +
                    "        \"channelId\": \"1372001238165561345\",\n" +
                    "        \"contentType\": \"text\",\n" +
                    "        \"brand\": \"品牌\",\n" +
                    "        \"car_series\": \"车系\",\n" +
                    "        \"channel_code\": \"渠道code\",\n" +
                    "        \"collections_count\": \"收藏数\",\n" +
                    "        \"comments_count\": \"评论数\",\n" +
                    "        \"content\": \"" + c + "\",\n" +
                    "        \"content_type\": \"内容类型(PGC/UGC)\",\n" +
                    "        \"favor_count\": \"点赞数\",\n" +
                    "        \"focus_count\": \"关注数\",\n" +
                    "        \"id\": \"" + IdWorker.getId() + "\",\n" +
                    "        \"one_id\": \"20012381655\",\n" +
                    "        \"province\": \"省份\",\n" +
                    "        \"publish_time\": \"2024-06-13 10:09:00\",\n" +
                    "        \"reading_count\": \"阅读数\",\n" +
                    "        \"redirection_count\": \"转发数\",\n" +
                    "        \"source\": \"来源\",\n" +
                    "        \"title\": \"标题\",\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"user_id\": \"用户id\",\n" +
                    "        \"user_name\": \"昵称\"\n" +
                    "    }";
            list.add(data);
        }
        return new Object[]{list};
    }

    @Test(dataProvider = "mockModel", priority = 2)
    public void test_process(List<Object> model) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Bearer ".concat(config.getDefaultToken()));
        requestHeaders.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));

        Stream.generate(Math::random).limit(200).forEach(i -> {
            HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(model), requestHeaders);
//            Result rs = restTemplate.postForEntity("http://172.16.80.16:30320/push/e11ab369ea4d56a7a64ab0a3c491a2cc"
            Result rs = restTemplate.postForEntity("http://127.0.0.1:8080/push/0"
                    , requestEntity, Result.class).getBody();
            System.out.println(rs);
            Assert.assertEquals(rs.getCode(), "200");
        });

    }


    @Test(priority = 3)
    public void test_pre_process() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Bearer ".concat(config.getDefaultToken()));
        requestHeaders.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));

        Map<String, String> params = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);

        Result rs = restTemplate.postForEntity("/pre_process"
                , requestEntity, Result.class).getBody();
        System.out.println(rs);
        Assert.assertEquals(rs.getCode(), "200");
    }
}
