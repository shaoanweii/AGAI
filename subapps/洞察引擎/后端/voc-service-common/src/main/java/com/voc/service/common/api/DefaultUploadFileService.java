package com.voc.service.common.api;

import org.springframework.stereotype.Service;

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
@Service
public class DefaultUploadFileService implements IUploadFileService {

    @Override
    public String getObjectUrl(String objectName) {
        return null;
    }

    @Override
    public boolean putObject(String objectName, InputStream stream) {
        return false;
    }

    @Override
    public boolean removeObject(String objectName) {
        return false;
    }

    @Override
    public Map<String, String> getObjectUrls(Set<String> objectNames) {
        return null;
    }

    @Override
    public InputStream getObjectInputStream(String objectName) {
        return null;
    }

    @Override
    public InputStream getObjectInputStream(String bucketName, String objectName) {
        return null;
    }

    @Override
    public boolean putObject(String bucket, String filename, String object, String contentType) throws Exception {
        return false;
    }

    @Override
    public String getUrl(String fileName) {
        return "";
    }

}
