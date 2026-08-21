package com.voc.service.components.minio.service;

import com.voc.service.components.minio.enums.BucketPolicyContent;
import com.voc.service.components.minio.service.bucketPolicy.BucketPolicyService;
import com.voc.service.components.minio.service.bucketPolicy.impl.BucketDefaultPolicyImpl;
import com.voc.service.components.minio.service.bucketPolicy.impl.BucketReadPolicyImpl;
import com.voc.service.components.minio.service.bucketPolicy.impl.BucketReadWriterPolicyImpl;
import com.voc.service.components.minio.service.bucketPolicy.impl.BucketWritePolicyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BucketPolicyFactoryService {

    private static final Logger log = LoggerFactory.getLogger(BucketPolicyFactoryService.class);
    static Map<String, BucketPolicyService> operationMap = new HashMap<>();

    static {
        // 只读
        operationMap.put(BucketPolicyContent.READ, new BucketReadPolicyImpl());
        // 只写
        operationMap.put(BucketPolicyContent.WRITE, new BucketWritePolicyImpl());
        // 读写
        operationMap.put(BucketPolicyContent.READ_WRITE, new BucketReadWriterPolicyImpl());
    }

    public static BucketPolicyService getBucketPolicyInterface(String policy) {
        BucketPolicyService object = operationMap.get(policy);
        if (object == null) {
            object = new BucketDefaultPolicyImpl();
        }
        log.info("获取桶策略:{}", object);
        return object;
    }

}
