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

/**
 * @author leiww
 * @version 1.0.0
 * @ClassName
 * @createTime 2024/2/21 14:59
 */
@SuppressWarnings("unchecked")
@SpringBootApplication
public class VocInsModelImplApplication {
    private static final Logger log = LoggerFactory.getLogger(VocInsModelImplApplication.class);

    public static void main(String[] args) throws IOException {
        SpringApplication startupSpringApplication
                = new SpringApplication(VocInsModelImplApplication.class);
        ConfigurableApplicationContext application = startupSpringApplication.run(args);
        Environment env = application.getEnvironment();
        String appname = env.getProperty("spring.application.name");
        String vhost = env.getProperty("server.vhost");
        String vport = env.getProperty("server.vport");
        log.info("--->> biz:{} added", VocInsModelImplApplication.class.getSimpleName());
        log.info("\n----------------------------------------------------------\n\t" +
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
