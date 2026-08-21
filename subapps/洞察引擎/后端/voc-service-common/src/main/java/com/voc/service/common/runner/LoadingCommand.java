package com.voc.service.common.runner;

import com.voc.service.common.api.ISystemInitLoadingService;
import com.voc.service.common.util.ServiceContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Title: LoadingCommand
 * @Package: com.voc.service.common.runner
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/22 12:16
 * @Version:1.0
 */
@Component
public class LoadingCommand implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(LoadingCommand.class);
    @Override
    public void run(String... args) throws Exception {
        Map<String, ISystemInitLoadingService> services = ServiceContextHolder.getApplicationContext().getBeansOfType(ISystemInitLoadingService.class);
        services.values().stream().forEach( e-> {
            e.initLoading();
            logger.info("{} 加载系统初始化数据完成", e.getClass().getSimpleName());
        });
    }
}
