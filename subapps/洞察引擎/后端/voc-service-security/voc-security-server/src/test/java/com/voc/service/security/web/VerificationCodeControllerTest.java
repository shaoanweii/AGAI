package com.voc.service.security.web;

import com.voc.service.common.response.Result;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName VerificationCodeControllerTest
 * @createTime 2024年01月15日 15:02
 * @Copyright futong
 */

public class VerificationCodeControllerTest  extends BaseloginTest {

    /**
     * GET请求样例
     */
    @Test
    void randomImage() {

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("token", token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, requestHeaders);

        Result rs = restTemplate.postForEntity("/push"
                , requestEntity, Result.class).getBody();
        Assert.assertEquals("200", rs.getCode());
        System.out.println(rs);
    }
}
