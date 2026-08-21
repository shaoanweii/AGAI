package com.voc.service.components.minio.service.bucketPolicy;

import io.minio.MinioClient;
import org.springframework.stereotype.Service;

@Service
public interface BucketPolicyService {

    /**
     * @throws
     * @Title: createBucketPolicy
     * @Description: 设置桶策略
     * @param: @param client
     * @param: @param bucket
     * @param: @return
     * @return: boolean
     */
    boolean createBucketPolicy(MinioClient client, String bucket);
}
