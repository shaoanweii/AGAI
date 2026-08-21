DROP  FUNCTION aes256_decrypt(string)  ;

SHOW  FUNCTIONS;

-- 加密示例
SELECT aes256_encrypt('Hello StarRocks');

-- 解密示例
SELECT aes256_decrypt('vz+EbSolpqzpJcpoIENGlQ==');

CREATE  FUNCTION aes256_encrypt(string)
    RETURNS string
    PROPERTIES (
    "symbol" = "com.starrocks.udf.sample.EncryptAesUDF",
    "type" = "StarrocksJar",
     "file" = "http://172.16.60.253/cmp2020/voc/api_test-1.0-SNAPSHOT-jar-with-dependencies.jar"
);


CREATE  FUNCTION aes256_decrypt(string)
    RETURNS string
    PROPERTIES (
    "symbol" = "com.starrocks.udf.sample.DecryptAesUDF",
    "type" = "StarrocksJar",
     "file" = "http://172.16.60.253/cmp2020/voc/api_test-1.0-SNAPSHOT-jar-with-dependencies.jar"
);