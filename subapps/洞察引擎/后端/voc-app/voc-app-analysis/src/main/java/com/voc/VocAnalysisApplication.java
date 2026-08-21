package com.voc;


import cn.hutool.core.util.StrUtil;
import com.voc.service.config.JasyptConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;

@SuppressWarnings("unchecked")
//@ServletComponentScan
@SpringBootApplication
@Import(JasyptConfiguration.class)
public class VocAnalysisApplication {
    private static final Logger logger = LoggerFactory.getLogger(VocAnalysisApplication.class);
    public static void main(String[] args) throws IOException {

        SpringApplication startupSpringApplication
                = new SpringApplication(VocAnalysisApplication.class);
        ConfigurableApplicationContext applicaiton = startupSpringApplication.run(args);
        logger.info("rpc_tr_port={}", System.getProperty("rpc_tr_port"));
        Environment env = applicaiton.getEnvironment();
        String appname = env.getProperty("spring.application.name");
        String prefix = env.getProperty("server.servlet.context-path");
        String vhost = env.getProperty("server.vhost");
        String vport = env.getProperty("server.vport");
        String port = StrUtil.isBlank(env.getProperty("server.port")) ? "8080" : env.getProperty("server.port");
        logger.info("--->> biz:{} added", VocAnalysisApplication.class.getSimpleName());
        logger.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "Doc: \thttp://{}:{}/doc.html\n" +
                        "----------------------------------------------------------",
                appname,
                port,
                StrUtil.isBlank(prefix) ? "" : prefix,
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? port : vport,
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? port : vport
        );

    }
}
