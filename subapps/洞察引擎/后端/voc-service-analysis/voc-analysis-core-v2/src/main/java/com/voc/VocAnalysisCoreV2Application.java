package com.voc;


import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;

@SuppressWarnings("unchecked")
@SpringBootApplication
public class VocAnalysisCoreV2Application {
    private static final Logger logger = LoggerFactory.getLogger(VocAnalysisCoreV2Application.class);
    public static void main(String[] args) throws IOException {
        SpringApplication startupSpringApplication
                = new SpringApplication(VocAnalysisCoreV2Application.class);
        ConfigurableApplicationContext applicaiton = startupSpringApplication.run(args);

        Environment env = applicaiton.getEnvironment();
        String appname = env.getProperty("spring.application.name");
        String vhost = env.getProperty("server.vhost");
        String vport = env.getProperty("server.vport");
        logger.info("--->> biz:{} added", VocAnalysisCoreV2Application.class.getSimpleName());
        logger.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "Doc: \thttp://{}:{}/doc.html\n" +
                        "----------------------------------------------------------",
                appname,
                env.getProperty("server.port"),
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? env.getProperty("server.port") : vport,
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? env.getProperty("server.port") : vport
        );

    }
}
