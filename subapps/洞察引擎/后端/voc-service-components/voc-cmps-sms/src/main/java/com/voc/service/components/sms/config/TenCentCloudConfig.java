package com.voc.service.components.sms.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Description: 百度文本校验基础配置
 * @Author houxy
 * @Date 2022/10/24
 * @Version 1.0
 */
@Data
@Component
public class TenCentCloudConfig {

    private String appId = "AKIDGKBqMeiYBzZw4yf7QUM4XAj5MRK3au0v";

    private String secretKey = "oPo00nQzNYWN1SghW7VgeNuGQM4f3EE8";

    private String apiKey = "1400733458";

    private String signName = "我AI健康";


    private String templateId = "1539427";

    @Bean
    public SmsClient getSmsClient() {
        /* 必要步骤：
         * 实例化一个认证对象，入参需要传入腾讯云账户密钥对secretId，secretKey。
         * 这里采用的是从环境变量读取的方式，需要在环境变量中先设置这两个值。
         * 你也可以直接在代码中写死密钥对，但是小心不要将代码复制、上传或者分享给他人，
         * 以免泄露密钥对危及你的财产安全。
         * SecretId、SecretKey 查询: https://console.cloud.tencent.com/cam/capi */
        Credential cred = new Credential(appId, secretKey);

        // 实例化一个http选项，可选，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        // 设置代理（无需要直接忽略）
        // httpProfile.setProxyHost("真实代理ip");
        // httpProfile.setProxyPort(真实代理端口);
        /* SDK默认使用POST方法。
         * 如果你一定要使用GET方法，可以在这里设置。GET方法无法处理一些较大的请求 */
        httpProfile.setReqMethod("POST");
        /* SDK有默认的超时时间，非必要请不要进行调整
         * 如有需要请在代码中查阅以获取最新的默认值 */
        httpProfile.setConnTimeout(60);
        /* 指定接入地域域名，默认就近地域接入域名为 sms.tencentcloudapi.com ，也支持指定地域域名访问，例如广州地域的域名为 sms.ap-guangzhou.tencentcloudapi.com */
        httpProfile.setEndpoint("sms.tencentcloudapi.com");
        /* 非必要步骤:
         * 实例化一个客户端配置对象，可以指定超时时间等配置 */
        ClientProfile clientProfile = new ClientProfile();
        /* SDK默认用TC3-HMAC-SHA256进行签名
         * 非必要请不要修改这个字段 */
        clientProfile.setSignMethod("HmacSHA256");
        clientProfile.setHttpProfile(httpProfile);
        /* 实例化要请求产品(以sms为例)的client对象
         * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，支持的地域列表参考 https://cloud.tencent.com/document/api/382/52071#.E5.9C.B0.E5.9F.9F.E5.88.97.E8.A1.A8 */
        return new SmsClient(cred, "ap-guangzhou", clientProfile);
    }
}
