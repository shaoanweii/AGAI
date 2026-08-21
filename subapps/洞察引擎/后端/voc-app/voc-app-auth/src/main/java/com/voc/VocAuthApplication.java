package com.voc;


import cn.hutool.core.util.StrUtil;
import com.voc.service.config.JasyptConfiguration;
import com.voc.service.security.impl.PassService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;

@SpringBootApplication
@SuppressWarnings("unchecked")
//@EnableJpaRepositories(basePackages = "com.voc.security")
@EntityScan(basePackages = "com.voc.security")
@Import(JasyptConfiguration.class)
@EnableConfigurationProperties(PassService.class)
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class VocAuthApplication extends SpringBootServletInitializer {
    private static final Logger logger = LoggerFactory.getLogger(VocAuthApplication.class);
    public static void main(String[] args) throws IOException {
        SpringApplication startupSpringApplication
                = new SpringApplication(VocAuthApplication.class);
        ConfigurableApplicationContext applicaiton = startupSpringApplication.run(args);
        logger.info("rpc_tr_port={}", System.getProperty("rpc_tr_port"));
        Environment env = applicaiton.getEnvironment();
        String appname = env.getProperty("spring.application.name");
        String vhost = env.getProperty("server.vhost");
        String vport = env.getProperty("server.vport");
        String port = StrUtil.isBlank(env.getProperty("server.port")) ? "8080" : env.getProperty("server.port");
        logger.info("--->> biz:{} added", VocAuthApplication.class.getSimpleName());
        logger.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "Doc: \thttp://{}:{}/doc.html\n" +
                        "----------------------------------------------------------",
                appname,
                port,
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? port : vport,
                StrUtil.isBlank(vhost) ? InetAddress.getLocalHost().getHostAddress() : vhost,
                StrUtil.isBlank(vport) ? port : vport
        );

    }
}
