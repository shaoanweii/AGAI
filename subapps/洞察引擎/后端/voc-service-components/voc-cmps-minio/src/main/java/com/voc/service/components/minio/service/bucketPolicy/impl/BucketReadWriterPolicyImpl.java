package com.voc.service.components.minio.service.bucketPolicy.impl;

import com.voc.service.components.minio.service.bucketPolicy.BucketPolicyService;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BucketReadWriterPolicyImpl implements BucketPolicyService {


    /**
     * 桶占位符
     */
    private static final String BUCKET_PARAM = "${bucket}";

    /**
     * bucket权限-读写
     */
    private static final String READ_WRITE = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";
    private static final Logger log = LoggerFactory.getLogger(BucketReadWriterPolicyImpl.class);

    @Override
    public boolean createBucketPolicy(MinioClient client, String bucket) {
        log.info("读写权限:{}", bucket);
        try {
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(READ_WRITE.replace(BUCKET_PARAM, bucket)).build());
            return true;
        } catch (Exception e) {
            log.error("error: {}", e.getMessage(), e);
        }
        return false;
    }
}
