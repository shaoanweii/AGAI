package com.voc.service.config;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName JasyptEncryptablePropertyConfigTest
 * @createTime 2023年12月29日 11:02
 * @Copyright futong
 */

public class JasyptEncryptablePropertyConfigTest {

    @org.testng.annotations.Test
    public void encrypt_test() {
        //解密后的文本
//        System.out.println(jasyptStringEncryptor.encrypt("voc2024."));
//        System.out.println(jasyptStringEncryptor.encrypt("ft2024"));
//        System.out.println(jasyptStringEncryptor.decrypt("jScI2dO1XloXEDVX8qmU50tKiHLQf1dXNG9R+mLWbqwYeWBUOa8aQMvXFJtNlwkr"));
        System.out.println(PBEStringEncryptor.getInstance().encrypt("voc2024."));
        System.out.println(PBEStringEncryptor.getInstance().encrypt("ft2024"));
    }

    @org.testng.annotations.Test
    public void decrypt_test() {
        //解密后的文本
//        System.out.println(jasyptStringEncryptor.encrypt("voc2024."));
//        System.out.println(jasyptStringEncryptor.encrypt("ft2024"));
//        System.out.println(jasyptStringEncryptor.decrypt("jScI2dO1XloXEDVX8qmU50tKiHLQf1dXNG9R+mLWbqwYeWBUOa8aQMvXFJtNlwkr"));
        System.out.println(PBEStringEncryptor.getInstance().decrypt("f2Xlh4sZS0MZu0JcuTpz4PdE/186LDjN9MAFSl2oByr/iF1ZvBFCdWwTD8ybwiqH"));
    }

    /*@Test
    void test3() {
        String prefix = "ECC@";

        System.out.println(StrUtil.startWith("ECC@(UsernameENCStr)", prefix, false, false));
    }

    @Test
    void test4() {


        System.out.println(PBEStringEncryptor.getInstance().encrypt("123"));
    }*/
}
