package com.voc.service.analysis.risk.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * SM4解密 + Base64校验/解码 工具类
 */
public class SM4DecryptUtil {
    // 国密SM4算法标识（ECB模式）
    private static final String SM4_ALGORITHM = "SM4/ECB/PKCS7Padding";
    // 固定密钥（与SQL中一致）
    private static final String SM4_KEY = "changanvoc2025xx";
    // Base64合法字符集正则（标准Base64：A-Za-z0-9+/=）
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]+$");

    static {
        // 注册BouncyCastle加密提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    // ======================== 【新增】SM4 加密方法 ========================
    /**
     * 加密手机号：明文 → SM4加密 → Base64编码
     * @param plainMobile 明文手机号
     * @return Base64格式的加密串
     */
    public static String encryptMobile(String plainMobile) {
        if (plainMobile == null || plainMobile.isEmpty()) {
            return null;
        }
        try {
            byte[] encryptedBytes = sm4Encrypt(plainMobile.getBytes(StandardCharsets.UTF_8), SM4_KEY.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println("加密失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * SM4 ECB加密（PKCS7填充）
     */
    private static byte[] sm4Encrypt(byte[] plainBytes, byte[] keyBytes) throws Exception {
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "SM4");
        Cipher cipher = Cipher.getInstance(SM4_ALGORITHM, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(plainBytes);
    }

    /**
     * 核心方法：模拟SQL逻辑解密mobile
     * @param base64Mobile 待解密的Base64格式mobile字符串
     * @return 解密后的明文mobile，解密失败返回null
     */
    public static String decryptMobile(String base64Mobile) {
        // 步骤1：校验Base64合法性
        if (!isValidBase64(base64Mobile)) {
            System.out.println("输入的mobile不是合法的Base64字符串");
            return base64Mobile;
        }

        try {
            // 步骤2：Base64解码（对应SQL的FROM_BASE64）
            byte[] encryptedBytes = Base64.getDecoder().decode(base64Mobile);

            // 步骤3：SM4解密（对应SQL的SM4_DECRYPT）
            return sm4Decrypt(encryptedBytes, SM4_KEY.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("解密失败：" + e.getMessage());
            return base64Mobile;
        }
    }

    /**
     * 校验字符串是否为合法Base64格式
     * @param str 待校验字符串
     * @return 合法返回true，否则false
     */
    public static boolean isValidBase64(String str) {
        // 空值/非字符串直接返回false
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 长度必须是4的整数倍
        if (str.length() % 4 != 0) {
            return false;
        }
        // 字符集校验
        if (!BASE64_PATTERN.matcher(str).matches()) {
            return false;
        }
        // 最终兜底：尝试解码（避免格式合法但内容无效的情况）
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * SM4-ECB模式解密（PKCS7填充）
     * @param encryptedBytes 加密后的字节数组
     * @param keyBytes 密钥字节数组（必须16字节）
     * @return 解密后的明文
     * @throws Exception 解密异常
     */
    private static String sm4Decrypt(byte[] encryptedBytes, byte[] keyBytes) throws Exception {
        // 校验密钥长度（SM4-128要求16字节）
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节");
        }

        // 创建SM4密钥规范
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "SM4");

        // 初始化解密器
        Cipher cipher = Cipher.getInstance(SM4_ALGORITHM, "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        // 执行解密并去除填充
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // 测试示例
    public static void main(String[] args) {
        String s = encryptMobile("男");
        System.out.println(s);
    }
}