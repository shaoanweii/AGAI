package com.voc.service.components.minio.config;

import com.voc.service.components.minio.service.UploadFileService;
import io.minio.MinioClient;
import lombok.Data;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO配置
 *
 * @author 柏伦
 */
@Configuration
@Data
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);
    /**
     * endPoint是一个URL，域名，IPv4或者IPv6地址
     */
    @Value("${minio.oss.endpoint}")
    private String endpoint;
    @Value("${minio.oss.endpointPort}")
    private Integer endpointPort;

    /**
     * accessKey类似于用户ID，用于唯一标识你的账户
     */
    @Value("${minio.oss.accessKeyId}")
    private String accessKey;

    /**
     * secretKey是你账户的密码
     */
    @Value("${minio.oss.accessKeySecret}")
    private String secretKey;
    @Value("${minio.oss.web_endpoint}")
    private String webEndpoint;
    @Value("${minio.oss.bucketName}")
    private String bucketName;


    @Bean
    public MinioClient getMinioClient() throws Exception{
        // 创建信任所有证书的 OkHttpClient
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(createTrustAllSslSocketFactory(), createTrustAllManager())
                .hostnameVerifier((hostname, session) -> true)
                .build();
        log.info("正在初始化MinIO...");
        MinioClient minioClient = MinioClient.builder()
//                .endpoint(endpoint)
                .endpoint(endpoint,endpointPort,false)
                .credentials(accessKey, secretKey)
                .httpClient(httpClient)
                .build();
        log.info("初始化MinIO成功!");
        return minioClient;
    }

    @Bean
    @Primary
    @ConditionalOnBean(MinioClient.class)
    public UploadFileService minioService() {

        return new UploadFileService();
    }



    private X509TrustManager createTrustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
        };
    }

    private SSLSocketFactory createTrustAllSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
//        SSLContext sslContext = SSLContext.getInstance("SSL");
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{createTrustAllManager()}, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }
}
