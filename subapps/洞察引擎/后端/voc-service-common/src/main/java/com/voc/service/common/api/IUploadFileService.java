package com.voc.service.common.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * @Title: IUploadFileService
 * @Package: com.voc.service.common.web
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/21 17:42
 * @Version:1.0
 */
public interface IUploadFileService {

    String getObjectUrl(String objectName);

    boolean putObject(String objectName, InputStream stream);

    boolean removeObject(String objectName);

    Map<String,String> getObjectUrls(Set<String> objectNames);

    InputStream getObjectInputStream(String objectName);


    InputStream getObjectInputStream(String bucketName, String objectName);

    boolean putObject(String bucket,String filename, String object, String contentType ) throws Exception;

    String getUrl(String fileName);
}
