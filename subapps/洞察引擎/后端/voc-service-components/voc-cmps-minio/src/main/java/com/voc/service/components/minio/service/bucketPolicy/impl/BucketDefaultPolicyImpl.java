package com.voc.service.components.minio.service.bucketPolicy.impl;

import com.voc.service.components.minio.service.bucketPolicy.BucketPolicyService;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BucketDefaultPolicyImpl implements BucketPolicyService {

    private static final Logger log = LoggerFactory.getLogger(BucketDefaultPolicyImpl.class);

    @Override
    public boolean createBucketPolicy(MinioClient client, String bucket) {
        log.info("默认没有权限:{}", bucket);
        return false;
    }
}
