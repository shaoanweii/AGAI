package com.voc.service.components.kafka.security.plain;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.voc.service.common.util.ServiceContextHolder;
import org.apache.kafka.common.security.plain.internals.PlainSaslServerProvider;
import org.jasypt.encryption.StringEncryptor;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

/**
 * @Title: CustomPlainLoginModule
 * @Package: com.voc.service.components.kafka.security.plain
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/23 17:35
 * @Version:1.0
 */
public class CustomPlainLoginModule  implements LoginModule {
    private final EncryptablePropertyDetector detector;
    private final StringEncryptor encryptor;

    private static final String USERNAME_CONFIG = "username";
    private static final String PASSWORD_CONFIG = "password";

    public CustomPlainLoginModule() {
        detector = ServiceContextHolder.getApplicationContext().getBean(EncryptablePropertyDetector.class);
        encryptor = ServiceContextHolder.getApplicationContext().getBean(StringEncryptor.class);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        String username = (String)options.get("username");
        if (username != null) {
            subject.getPublicCredentials().add(username);
        }

        String password = (String)options.get("password");
        if (password != null) {
            String unwrappedProperty = detector.unwrapEncryptedValue(password.trim());
            subject.getPrivateCredentials().add( encryptor.decrypt(unwrappedProperty));
        }

    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public boolean logout() {
        return true;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public boolean abort() {
        return false;
    }

    static {
        PlainSaslServerProvider.initialize();
    }
}
