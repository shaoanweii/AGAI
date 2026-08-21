package com.voc.service.config;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import org.jasypt.encryption.StringEncryptor;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AbstractStringEncryptor
 * @createTime 2024年02月02日 15:39
 * @Copyright futong
 */
@Data
public abstract class AbstractStringEncryptor implements StringEncryptor {
    static StringEncryptor encryptor;

    public abstract String getPrefix();

    public abstract String getSuffix();

    @Override
    public String encrypt(String message) {
        return encryptor.encrypt(message);
    }

    @Override
    public String decrypt(String encryptedMessage) {
//        return encryptedMessage;
        return encryptor.decrypt(encryptedMessage);
    }

    public static StringEncryptor getInstance() {
        if (ObjUtil.isNull(encryptor)) {
            new PBEStringEncryptor();
        }
        return encryptor;
    }
}
