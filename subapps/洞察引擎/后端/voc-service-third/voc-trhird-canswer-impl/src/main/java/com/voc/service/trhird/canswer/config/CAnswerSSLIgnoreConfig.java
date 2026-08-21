package com.voc.service.trhird.canswer.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

@Component
public class CAnswerSSLIgnoreConfig {
    /**
     * 创建忽略SSL验证的RestTemplate
     */
    public static RestTemplate createIgnoreSSLRestTemplate() {
        try {
            // 信任所有证书
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial((X509Certificate[] chain, String authType) -> true)
                    .build();

            // 创建SSL socket工厂
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE
            );

            // 创建连接管理器
            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            // 创建HttpClient
            HttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            // 创建RestTemplate
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            return new RestTemplate(requestFactory);

        } catch (Exception e) {
            throw new RuntimeException("创建忽略SSL验证的RestTemplate失败", e);
        }
    }
}
