package com.voc.service.config;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.exception.DecryptionException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CustomEncryptablePropertyResolver
 * @createTime 2023年12月29日 17:12
 * @Copyright futong
 */
public class CustomEncryptablePropertyResolver implements EncryptablePropertyResolver {
    /**
     * 属性探测器
     */
    private final EncryptablePropertyDetector detector;
    private final StringEncryptor encryptor;

    public CustomEncryptablePropertyResolver(EncryptablePropertyDetector detector, StringEncryptor stringEncryptor) {
        this.detector = detector;
        this.encryptor = stringEncryptor;
    }

    /**
     * 处理真正的解密逻辑
     *
     * @param value 原始值
     * @return 如果值未加密，返回原值，如果加密，返回解密之后的值
     */
    @Override
    public String resolvePropertyValue(String value) {
        return Optional.ofNullable(value)
                .filter(detector::isEncrypted)
                .map(resolvedValue -> {
                    try {
                        // 1.过滤加密规则后的字符串
                        String unwrappedProperty = detector.unwrapEncryptedValue(resolvedValue.trim());

//                        encryptor.decrypt(unwrappedProperty);

                        return encryptor.decrypt(unwrappedProperty);
                    } catch (EncryptionOperationNotPossibleException e) {
                        throw new DecryptionException("Unable to decrypt: " + value + ". Decryption of Properties failed,  make sure encryption/decryption " +
                                "passwords match", e);
                    }
                })
                .orElse(value);
    }
}