package com.voc.service.components.redis.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Title: PrefixRedisSerializer
 * @Package: com.voc.service.components.redis.config
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/6 14:51
 * @Version:1.0
 */
@Order
@Configuration
public class PrefixRedisSerializer extends StringRedisSerializer {
    /**
     * 演示这里就写死了 可以抽取到配置文件中
     */
    public static final String DEFUAL_PREFIX = "VDP_::";

    /**
     * 序列化
     *
     * @param s key
     * @return 结果
     */
    @Override
    public byte[] serialize(String s) {
        if (s == null) {
            return new byte[0];
        }
        // 这里加上你需要加上的key前缀
        String realKey = DEFUAL_PREFIX + s;
        return super.serialize(realKey);
    }

    /**
     * 反序列化
     *
     * @param bytes 数据
     * @return 结果
     */
    @Override
    public String deserialize(byte[] bytes) {
        String s = bytes == null ? null : new String(bytes);
        if (StrUtil.isBlank(s)) {
            return s;
        }
        int index = s.indexOf(DEFUAL_PREFIX);
        if (index != -1) {
            return s.substring(DEFUAL_PREFIX.length());
        }
        return s;
    }
}





