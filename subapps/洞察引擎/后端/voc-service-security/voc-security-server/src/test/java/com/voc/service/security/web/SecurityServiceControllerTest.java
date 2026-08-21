package com.voc.service.security.web;

import cn.hutool.json.JSONUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ISecurityServiceTest
 * @createTime 2024年01月15日 13:43
 * @Copyright futong
 */


public class SecurityServiceControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("admin11", "Passw0rd@!");
    }

    /**
     * POST 请求样例 ， 不携带token
     */
    @Test(priority = 1)
    public void userinfo() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("token", token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, requestHeaders);

        Result rs = restTemplate.postForEntity("/user/userinfo"
                , requestEntity, Result.class).getBody();

        System.out.println(rs);
        Assert.assertEquals("200", rs.getCode());
        UserModel model = JSONUtil.toBean(JSONUtil.toJsonStr(rs.getResult()), UserModel.class);
        Assert.assertNotNull(model, "userModel is null");
    }

    /**
     * 系统中需认证API访问样例， POST：header + json
     */
    @Test(priority = 2)
    public void enable() {
        requestHeaders.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        System.out.println("userId:".concat(userId));
        HttpEntity requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(params), requestHeaders);

        Result rs = restTemplate.postForEntity("/user/enable"
                , requestEntity, Result.class).getBody();
        System.out.println(rs);
        Assert.assertEquals("200", rs.getCode());

    }


}
