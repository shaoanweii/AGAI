package com.voc.service.security.crypto;

import cn.hutool.crypto.digest.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName MD5PasswordEncoder
 * @Description ckcui
 * @createTime 2023年11月29日 17:36
 * @Copyright futong
 */

public final class MD5PasswordEncoder implements PasswordEncoder {
    private static final PasswordEncoder INSTANCE = new MD5PasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(MD5PasswordEncoder.class);

    public MD5PasswordEncoder() {
        log.info("--->> init MD5PasswordEncoder {}");
    }

    public static PasswordEncoder getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {

        System.out.println(MD5PasswordEncoder.INSTANCE.encode("admin"));
        System.out.println(MD5PasswordEncoder.INSTANCE.matches("admin", "21232f297a57a5a743894a0e4a801fc3"));
    }

    /**
     * 1、非对称解密密
     * 2、重新加密-md5
     *
     * @return
     */
    @Override
    public String encode(CharSequence rawPassword) {
        //非对称解密密
        log.info("需实现非对称解密密");
//        return DigestUtil.md5Hex(rawPassword.toString());
        return MD5.create().digestHex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
//        log.info("[{}]",this.encode(rawPassword).toString());
//        log.info("[{}]",encodedPassword.toString());
        return this.encode(rawPassword.toString()).equals(encodedPassword.toString());
    }
}