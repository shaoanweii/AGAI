package com.voc.service.security.web;

import com.voc.service.common.response.Result;
import org.springframework.http.HttpStatusCode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ISecurityServiceTest
 * @createTime 2024年01月15日 13:43
 * @Copyright futong
 */


public class SSOServiceControllerTest extends BaseloginTest {
    @BeforeClass
    public void setUp() {
        login("test_sso_user1", "Passw0rd@!");
    }

    /**
     * POST 请求样例 ， 不携带token
     */
    @Test
    public void test_sso() {

        HttpStatusCode code = restTemplate.getForEntity("/auth/sso"
                        .concat("?redirect=").concat("http://www.baidu.com")
                        .concat("&token=").concat(token)
                , Result.class).getStatusCode();
        System.out.println(code.value());
//        System.out.println(code);
        Assert.assertEquals(302, code.value());
    }
}
