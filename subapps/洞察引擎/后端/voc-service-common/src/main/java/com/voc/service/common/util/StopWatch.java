package com.voc.service.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: StopWatch
 * @Package: com.voc.service.common.util
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/26 10:26
 * @Version:1.0
 */
public class StopWatch {
    private static final Logger logger = LoggerFactory.getLogger(StopWatch.class);
    private ThreadLocal<HutoolStopWatch> stopWatch;

    public StopWatch() {
        this.stopWatch = new ThreadLocal<HutoolStopWatch>() {
            @Override
            protected HutoolStopWatch initialValue() {
                return new HutoolStopWatch();
            }
        };
    }


    public StopWatch(final String name) {
        this.stopWatch = new ThreadLocal<HutoolStopWatch>() {
            @Override
            protected HutoolStopWatch initialValue() {
                return new HutoolStopWatch(name);
            }
        };
    }

    public void start(String taskName) {
        try {
            if (!this.stopWatch.get().isRunning()) {
                this.stopWatch.get().start(taskName);
            } else {
                this.stopWatch.get().stop();
            }
        } catch (Exception e) {
            logger.info("{}",String.format("开始监控任务%s失败,原因:%s", taskName, e.getCause()));
        }
    }

    public void stop() {
        try {
            if (this.stopWatch.get().isRunning()) {
                this.stopWatch.get().stop();
            }
        } catch (Exception e) {
            logger.info("{}",String.format("停止监控任务失败,原因:%s", e.getCause()));
        }
    }

    public String prettyPrint() {
        try {
            this.stopWatch.get().prettyPrint();
        } catch (Exception e) {
            logger.info("{}",String.format("打印监控日志失败,原因:%s", e.getCause()));
        }
        return "";
    }
}
