package com.voc.service.components.xxljob.config;

import cn.hutool.core.util.StrUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @Title: ToolsServiceImpl
 * @Package: com.voc.service.data.integration.services
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/21 18:28
 * @Version:1.0
 */
@Service
public class ToolsServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(ToolsServiceImpl.class);

    @XxlJob("ping_")
    public void ping_() {
        final String param = XxlJobHelper.getJobParam();

        if (StrUtil.isBlank(param)) {
            log.error("退出");
            return;
        }
        log.info("开始执行：{}", param);
        {
            final String urlString = param;

            for (int i = 0; i < 1000; i++) {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(urlString))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    int responseCode = response.statusCode();

                    if (responseCode == 200) {
                        log.info("Response Content: {}",response);
                    } else {
                        log.error("{} Request failed. {} {}", urlString,responseCode,response);
                    }
                    Thread.sleep(300);
                } catch (Exception e) {
                    log.error(e.getCause().getMessage(), e);
                }
            }


        }
    }

}
