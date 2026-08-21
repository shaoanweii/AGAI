package com.voc.service.security.crypto;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class PasswordUtil {

    /**
     * JAVA6支持以下任意一种算法 PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
     * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
     * */

    /**
     * 定义使用的算法为:PBEWITHMD5andDES算法
     */
    public static final String ALGORITHM = "PBEWithMD5AndDES";//加密算法
    public static final String Salt = "63293188";//密钥

    private static final String  ALG_AES_ECB_PKCS5 = "AES/ECB/PKCS7Padding";
    public static final String aes_key = "Futongdongfang!@";
    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);

    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定义迭代次数为1000次
     */
    private static final int ITERATIONCOUNT = 1000;

    /**
     * 获取加密算法中使用的盐值,解密中使用的盐值必须与加密中使用的相同才能完成操作. 盐长度必须为8字节
     *
     * @return byte[] 盐值
     */
    public static byte[] getSalt() throws Exception {
        // 实例化安全随机数
        SecureRandom random = new SecureRandom();
        // 产出盐
        return random.generateSeed(8);
    }

    public static byte[] getStaticSalt() {
        // 产出盐
        return Salt.getBytes();
    }

    /**
     * 根据PBE密码生成一把密钥
     *
     * @param password 生成密钥时所使用的密码
     * @return Key PBE算法密钥
     */
    private static Key getPBEKey(String password) {
        // 实例化使用的算法
        SecretKeyFactory keyFactory;
        SecretKey secretKey = null;
        try {
            keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

            // 设置PBE密钥参数
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            // 生成密钥
            secretKey = keyFactory.generateSecret(keySpec);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return secretKey;
    }

    /**
     * 加密明文字符串
     *
     * @param plaintext 待加密的明文字符串
     * @param password  生成密钥时所使用的密码
     * @param salt      盐值
     * @return 加密后的密文字符串
     * @throws Exception
     */
    public static String encrypt(String plaintext, String password, String salt) {

        Key key = getPBEKey(password);
        byte[] encipheredData = null;
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            //update-begin for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
            encipheredData = cipher.doFinal(plaintext.getBytes("utf-8"));
            //update-end for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
        } catch (Exception e) {
        }
        return bytesToHexString(encipheredData);
    }


    /**
     * 解密密文字符串
     *
     * @param ciphertext 待解密的密文字符串
     * @param password   生成密钥时所使用的密码(如需解密,该参数需要与加密时使用的一致)
     * @param salt       盐值(如需解密,该参数需要与加密时使用的一致)
     * @return 解密后的明文字符串
     * @throws Exception
     */
    public static String decrypt(String ciphertext, String password, String salt) {

        Key key = getPBEKey(password);
        byte[] passDec = null;
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            passDec = cipher.doFinal(hexStringToBytes(ciphertext));
        } catch (Exception e) {
            // TODO: handle exception
        }
        return new String(passDec);
    }




    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param src 字节数组
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    public static boolean isComplexPassword(String password, String username) {
        // 检查密码长度是否符合要求
        if (password.length() < 8) {
            return false;
        }

        // 检查密码是否包含大写字母、小写字母、数字和符号
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }

        // 检查密码是否包含三种或更多不同类型的字符
        int characterTypes = 0;
        if (hasUppercase) {
            characterTypes++;
        }
        if (hasLowercase) {
            characterTypes++;
        }
        if (hasDigit) {
            characterTypes++;
        }
        if (hasSymbol) {
            characterTypes++;
        }

        if (characterTypes < 3) {
            return false;
        }

        // 检查密码是否包含用户名
        if (username != null && password.contains(username)) {
            return false;
        }

        return true;
    }



    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/1/12 11:28
     * @描述   AES对称加密
     * @param value  需加密的数据
     * @param key  秘钥
     * @return byte[]
     **/
    public static String encrypt(String value, String key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG_AES_ECB_PKCS5);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aes_key.getBytes(), "AES"));
        // 对加密报文进行base64解码
        byte [] encryptText = cipher.doFinal(value.getBytes());
        byte[] encode = Base64.getEncoder().encode(encryptText);
        return new String(encode);
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/1/12 12:34
     * @描述  AES对称解密
     * @param value 需解密的数据
     * @return java.lang.String
     **/
    public static  String decrypt(String value) {
        try {
            if(!cn.hutool.core.codec.Base64.isBase64(value)){
                log.error("解密失败,请检查数据是否正确 {}",value);
                throw new Exception("解密失败,请检查数据是否正确");
            }
            Cipher cipher = Cipher.getInstance(ALG_AES_ECB_PKCS5);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE,  new SecretKeySpec(aes_key.getBytes(), "AES"));
            // 对加密报文进行base64解码
            byte[] encrypted1 = Base64.getDecoder().decode(value);
            // 解密后的报文数组
            byte[] original = cipher.doFinal(encrypted1);
            // 输出utf8编码的字符串，输出字符串需要指定编码格式
            return new String(original);
        }catch (Exception e){
            log.error("解密失败",e);
            return "";
        }
    }
}
