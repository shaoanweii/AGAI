package com.voc.service.config;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName EncryptablePropertyConfig
 * @createTime 2023年12月29日 10:41
 * @Copyright futong
 */

//@ConfigurationProperties(prefix = "jasypt.encryptor")
@Configuration(proxyBeanMethods = false)
@EnableEncryptableProperties
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JasyptConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(JasyptConfiguration.class);
    public JasyptConfiguration() {
        logger.info("--->> {}", this.getClass().getSimpleName());
    }

    @Bean("encryptablePropertyDetector")
    public EncryptablePropertyDetector detector(EnvCopy envCopy, @Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {

        AbstractStringEncryptor encryptor = (AbstractStringEncryptor) stringEncryptor;
        final String prefix = envCopy.get().getProperty("jasypt.encryptor.prefix", encryptor.getPrefix());
        final String suffix = envCopy.get().getProperty("jasypt.encryptor.suffix", encryptor.getSuffix());

        logger.info("prefix: '{}', suffix: '{}'", prefix, suffix);
        return new DefaultPropertyDetector(prefix, suffix);
    }

    @Bean("encryptablePropertyResolver")
    public EncryptablePropertyResolver EncryptablePropertyResolver(
            @Qualifier("encryptablePropertyDetector") EncryptablePropertyDetector detector,
            @Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor
    ) {
        return new CustomEncryptablePropertyResolver(detector, stringEncryptor);
    }

    /*@ConditionalOnProperty(prefix = "jasypt.encryptor", name = "enabled", havingValue = "voc", matchIfMissing = false)
    @Bean("jasyptStringEncryptor")
    public StringEncryptor vocStringEncryptor() {
        logger.info("--->> {}", VocStringEncryptor.class.getSimpleName());
        return new VocStringEncryptor();
    }*/

    //    @ConditionalOnProperty(prefix = "jasypt.encryptor", name = "enabled", havingValue = "pbe", matchIfMissing = false)
    @Bean("jasyptStringEncryptor")
    public StringEncryptor pbeStringEncryptor() {
        logger.info("--->> {}", PBEStringEncryptor.class.getSimpleName());
        return new PBEStringEncryptor();
    }

    /*@Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("P@ssw0rd!@#123..");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }*/

}
