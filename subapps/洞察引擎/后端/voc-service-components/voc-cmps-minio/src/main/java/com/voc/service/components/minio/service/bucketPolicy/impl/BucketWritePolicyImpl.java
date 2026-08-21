package com.voc.service.components.minio.service.bucketPolicy.impl;

import com.voc.service.components.minio.service.bucketPolicy.BucketPolicyService;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class BucketWritePolicyImpl implements BucketPolicyService {


    /**
     * 桶占位符
     */
    private static final String BUCKET_PARAM = "${bucket}";

    private static final String WRITE_ONLY1 = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::base-temp\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\"],\"Resource\":[\"arn:aws:s3:::base-temp/*\"]}]}";

    /**
     * bucket权限-只写
     */
    private static final String WRITE_ONLY = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:AbortMultipartUpload\",\"s3:DeleteObject\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";
    private static final Logger log = LoggerFactory.getLogger(BucketWritePolicyImpl.class);

    @Override
    public boolean createBucketPolicy(MinioClient client, String bucket) {
        log.info("只写权限:{}", bucket);
        try {
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(WRITE_ONLY.replace(BUCKET_PARAM, bucket)).build());
            return true;
        } catch (Exception e) {
            log.error("error: {}", e.getMessage(), e);
        }
        return false;
    }
}
