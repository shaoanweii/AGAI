package com.voc.service.common.config;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 解决idworker工具类在集群及分布式部署时，避免出现重复值问题
 *
 * @Author qiu kq
 * @Date 2019/8/17
 */
//@Configuration
public class IdWorkConfig {

//    @Bean
    public IdWorker idWorker() {
        /*try {
            //优先读取配置数据
            String c_id = System.getProperty("idworker.id");
            String d_id = System.getProperty("data.id");
            log.info("[IdWorkConfig] id Custom property idworker.id:{},datacenter.Id：{}", c_id, d_id);
            if (ObjectUtils.isNotEmpty(c_id) && ObjectUtils.isNotEmpty(d_id)) {
                try {
                    log.info("[IdWorkConfig] using Custom property idworker.id:{},datacenter.Id：{}", c_id, d_id);
                    return new IdWorker(null);
                } catch (Exception e) {
                    log.warn("[IdWorkConfig] 未配置参数(idworker.id,data.id),将使用随机数生成配置");
                    log.error(e.getMessage(), e);
                }
            }
            int c1 = this.getRandom();
            int c2 = this.getRandom();

            log.info("[IdWorkConfig] Random -> idworker.id:{},datacenter.Id：{}", c1, c2);
            return new IdWorker(null);
        } catch (Exception e) {
            log.warn("[IdWorkConfig] 未配置参数,将使用随机数生成配置");
            log.error(e.getMessage(), e);
        }*/

        return new IdWorker(null);
    }



    public class IdWorker {

        /**
         * Start time cut (2020-05-03)
         */
        private final long twepoch = 1588435200000L;

        /**
         * The number of bits occupied by workerId
         */
        private final int workerIdBits = 10;

        /**
         * The number of bits occupied by timestamp
         */
        private final int timestampBits = 41;

        /**
         * The number of bits occupied by sequence
         */
        private final int sequenceBits = 12;

        /**
         * Maximum supported machine id, the result is 1023
         */
        private final int maxWorkerId = ~(-1 << workerIdBits);

        /**
         * business meaning: machine ID (0 ~ 1023)
         * actual layout in memory:
         * highest 1 bit: 0
         * middle 10 bit: workerId
         * lowest 53 bit: all 0
         */
        private long workerId;

        /**
         * timestamp and sequence mix in one Long
         * highest 11 bit: not used
         * middle  41 bit: timestamp
         * lowest  12 bit: sequence
         */
        private AtomicLong timestampAndSequence;

        /**
         * mask that help to extract timestamp and sequence from a long
         */
        private final long timestampAndSequenceMask = ~(-1L << (timestampBits + sequenceBits));

        /**
         * instantiate an IdWorker using given workerId
         * @param workerId if null, then will auto assign one
         */
        public IdWorker(Long workerId) {
            initTimestampAndSequence();
            initWorkerId(workerId);
        }

        /**
         * init first timestamp and sequence immediately
         */
        private void initTimestampAndSequence() {
            long timestamp = getNewestTimestamp();
            long timestampWithSequence = timestamp << sequenceBits;
            this.timestampAndSequence = new AtomicLong(timestampWithSequence);
        }

        /**
         * init workerId
         * @param workerId if null, then auto generate one
         */
        private void initWorkerId(Long workerId) {
            if (workerId == null) {
                workerId = generateWorkerId();
            }
            if (workerId > maxWorkerId || workerId < 0) {
                String message = String.format("worker Id can't be greater than %d or less than 0", maxWorkerId);
                throw new IllegalArgumentException(message);
            }
            this.workerId = workerId << (timestampBits + sequenceBits);
        }




        /**
         * get newest timestamp relative to twepoch
         */
        private long getNewestTimestamp() {
            return System.currentTimeMillis() - twepoch;
        }

        /**
         * auto generate workerId, try using mac first, if failed, then randomly generate one
         * @return workerId
         */
        private long generateWorkerId() {
            try {
                return generateWorkerIdBaseOnMac();
            } catch (Exception e) {
                return generateRandomWorkerId();
            }
        }

        /**
         * use lowest 10 bit of available MAC as workerId
         * @return workerId
         * @throws Exception when there is no available mac found
         */
        private long generateWorkerIdBaseOnMac() throws Exception {
            Enumeration<NetworkInterface> all = NetworkInterface.getNetworkInterfaces();
            while (all.hasMoreElements()) {
                NetworkInterface networkInterface = all.nextElement();
                boolean isLoopback = networkInterface.isLoopback();
                boolean isVirtual = networkInterface.isVirtual();
                if (isLoopback || isVirtual) {
                    continue;
                }
                byte[] mac = networkInterface.getHardwareAddress();
                return ((mac[4] & 0B11) << 8) | (mac[5] & 0xFF);
            }
            throw new RuntimeException("no available mac found");
        }

        /**
         * randomly generate one as workerId
         * @return workerId
         */
        private long generateRandomWorkerId() {
            return new SecureRandom().nextInt(maxWorkerId + 1);
        }
    }

}
