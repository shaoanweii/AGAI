package com.voc;


import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;

@SpringBootApplication
@SuppressWarnings("unchecked")
//@EnableJpaRepositories(basePackages = "com.voc.security")
@EntityScan(basePackages = "com.voc")
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class VocAuthServerApplication extends SpringBootServletInitializer {
    private static final Logger log = LoggerFactory.getLogger(VocAuthServerApplication.class);
    public static void main(String[] args) throws IOException {
        SpringApplication startupSpringApplication
                = new SpringApplication(VocAuthServerApplication.class);
        ConfigurableApplicationContext applicaiton = startupSpringApplication.run(args);
        log.info("rpc_tr_port={}", System.getProperty("rpc_tr_port"));
        Environment env = applicaiton.getEnvironment();
        String appname = env.getProperty("spring.application.name");
        String vhost = env.getProperty("server.vhost");
        String vport = env.getProperty("server.vport");
        String port = StrUtil.isBlank(env.getProperty("server.port")) ? "8080" : env.getProperty("server.port");
        log.info("--->> biz:{} added", VocAuthServerApplication.class.getSimpleName());
        log.info("\n----------------------------------------------------------\n\t" +
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
