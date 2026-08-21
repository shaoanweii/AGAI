package com.voc.service.common.config;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName OkHttpConfig
 * @createTime 2024年01月05日 9:28
 * @Copyright futong
 */
@Configuration
public class OkHttpConfig {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpConfig.class);
    public OkHttpConfig() {
        logger.info("--->> init {}", this.getClass().getSimpleName());
    }


    /**
     * OkHttp 客户端配置
     *
     * @return OkHttp 客户端配
     */
    @Bean
    public OkHttpClient okHttpClient() {
        TimeUnit timeUnit = TimeUnit.SECONDS;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory(), x509TrustManager())
                .hostnameVerifier(hostnameVerifier())
                .connectTimeout(15L, timeUnit)
                .readTimeout(15L, timeUnit)
                .writeTimeout(15L, timeUnit)
                .proxy(Proxy.NO_PROXY)
                .retryOnConnectionFailure(true);

        /*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                if(logger.isInfoEnabled()) {
                    logger.info(message);
                }
            }
        });

        builder.addInterceptor(interceptor);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);*/

        return builder.build();


    }

    @Bean
    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    /**
     * 信任所有 SSL 证书
     *
     * @return
     */
    @Bean
    public SSLSocketFactory sslSocketFactory() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{x509TrustManager()};
//            SSLContext sslContext = SSLContext.getInstance("SSL");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 信任所有主机名
     *
     * @return 主机名校验
     */
    @Bean
    public HostnameVerifier hostnameVerifier() {
        return (s, sslSession) -> true;
    }

    /**
     * 连接池配置
     *
     * @return 连接池
     */
    @Bean
    public ConnectionPool pool() {
        // 最大连接数、连接存活时间、存活时间单位（分钟）
        return new ConnectionPool(200, 5, TimeUnit.MINUTES);
    }

    @Bean
    public OkHttpClient.Builder okHttpClientBuilder() {
        return new OkHttpClient.Builder().addInterceptor(new FeignOkHttpClientResponseInterceptor());
    }

    /**
     * okHttp响应拦截器
     */
    public static class FeignOkHttpClientResponseInterceptor implements Interceptor {


        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {

            Request originalRequest = chain.request();
            Response response = chain.proceed(originalRequest);

            MediaType mediaType = response.body().contentType();
            String content = response.body().string();

            //解析content，做你想做的事情！！

            //生成新的response返回，网络请求的response如果取出之后，直接返回将会抛出异常
            return response.newBuilder()
                    .body(ResponseBody.create(mediaType, content))
                    .build();
        }
    }
}
