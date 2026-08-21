package com.voc.service.components.minio.enums;

import com.google.common.collect.Lists;

import java.util.List;

public class BucketPolicyContent {

    public static final String READ = "read";

    public static final String WRITE = "write";

    public static final String READ_WRITE = "read-write";

    public static final List<String> AUTH_OPTION = Lists.newArrayList("GetObject", "ListAllMyBuckets", "ListBucket", "PutObject", "DeleteObject", "GetBucketLocation");
}
