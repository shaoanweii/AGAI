package com.voc.service.config;

import cn.hutool.core.util.ObjUtil;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CustomStringEncryptor
 * @createTime 2023年12月29日 16:55
 * @Copyright futong
 */
public class PBEStringEncryptor extends AbstractStringEncryptor {
//    private final Environment environment;
    private static final Logger logger = LoggerFactory.getLogger(PBEStringEncryptor.class);
    String prefix = "PBE(";
    String suffix = ")";

    static {
        PooledPBEStringEncryptor pooledPBEStringEncryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("P@ssw0rd!@#123..");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        pooledPBEStringEncryptor.setConfig(config);

        encryptor = pooledPBEStringEncryptor;
    }

    public PBEStringEncryptor() {
        logger.info("--->> {}", this.getClass().getSimpleName());
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    public static StringEncryptor getInstance() {
        if (ObjUtil.isNull(encryptor)) {
            new PBEStringEncryptor();
        }
        return encryptor;
    }
}
