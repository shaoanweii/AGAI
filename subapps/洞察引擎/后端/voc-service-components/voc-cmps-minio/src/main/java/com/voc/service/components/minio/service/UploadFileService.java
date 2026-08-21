package com.voc.service.components.minio.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TtlWrappers;
import com.voc.service.common.api.IUploadFileService;
import com.voc.service.components.minio.config.MinioConfig;
import com.voc.service.components.minio.service.moltipartFile.MoltipartFileService;
import io.minio.*;
import io.minio.http.Method;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MinIO常用操作
 */

@Service
public class UploadFileService implements IUploadFileService {
    /**
     * 临时桶保留七天
     */
    private static final int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;
    private static final Logger log = LoggerFactory.getLogger(UploadFileService.class);
    @Autowired
    private MinioClient minioClient;
    @Value("${image.ozrf:1024}")
    private Integer imgOzrf;
    @Value("${image.prop:0.6}")
    private Double imgProp;
    @Value("${image.size:1500}")
    private Integer imgSize;
    @Autowired
    MinioConfig config;

    /**
     * 通过文件上传到对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param fileName   File name
     * @return
     */
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, String fileName) {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .filename(fileName)
                        .build());
        return true;

    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param file
     * @param filename   文件名称
     */
    @SneakyThrows
    public boolean putObject(String bucketName, MultipartFile file, String filename) {
        Assert.isTrue(StrUtil.isNotBlank(bucketName), "bucketName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(filename), "filename cannot be empty");
        Assert.isTrue(ObjectUtil.isNotNull(file), "file cannot be empty");

        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", bucketName);
            return false;
        }

        @Cleanup
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(filename).contentType(file.getContentType())
                        .stream(inputStream, inputStream.available(), -1).build());

        return true;
    }

    @SneakyThrows
    public boolean putObject(MultipartFile file, String filename) {
        return this.putObject(config.getBucketName(), file, filename);
    }

    /**
     * 通过InputStream上传对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param stream     要上传的流
     * @return
     */
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream stream) {
        Assert.isTrue(StrUtil.isNotBlank(bucketName), "bucketName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(objectName), "objectName cannot be empty");
        Assert.isTrue(ObjectUtil.isNotNull(stream), "stream cannot be empty");

        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", bucketName);
            return false;
        }

        ObjectWriteResponse res = minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                stream, stream.available(), -1)
                        .build());
        log.info(" {} 上传成功！" , res.object());
        return true;
    }

    @Override
    @SneakyThrows
    public boolean putObject(String objectName, InputStream stream) {
        return this.putObject(config.getBucketName(), objectName, stream);
    }

    /**
     * 删除一个对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        Assert.isTrue(StrUtil.isNotBlank(bucketName), "bucketName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(objectName), "objectName cannot be empty");

        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", bucketName);
            return false;
        }

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
//                        .versionId("my-versionid")
                        .build());
        return true;
    }

    @Override
    @SneakyThrows
    public boolean removeObject(String objectName) {
        return this.removeObject(config.getBucketName(), objectName);
    }

    /**
     * 生成一个给HTTP GET请求用的presigned URL。
     * 浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天。
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName, Integer expires) {
        Assert.isTrue(StrUtil.isNotBlank(bucketName), "bucketName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(objectName), "objectName cannot be empty");
        Assert.isTrue(ObjectUtil.isNotNull(expires), "expires cannot be empty");

        try {
//            minioClient.listBuckets().stream().forEach(bucket -> log.info("{}", bucket.name()));
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", bucketName);
            log.error("minio异常:",e);
            return null;
        }

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires)
                        .build());
    }

    @SneakyThrows
    public String getObjectUrl(String objectName, Integer expires) {

        return this.getObjectUrl(config.getBucketName(), objectName, expires);
    }

    @Override
    @SneakyThrows
    public String getObjectUrl(String objectName) {

        return this.getObjectUrl(objectName, 60 * 60 * 24 * 7);
    }

    @Override
    public Map<String,String> getObjectUrls(Set<String> objectNames){
        Assert.isTrue(CollUtil.isNotEmpty(objectNames), "objectNames cannot be empty");

        List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
        Map<String,String> channelVo = MapUtil.newConcurrentHashMap();

        List<List<String>> sub = CollUtil.split(objectNames, 5);
        for (List<String> strings : sub) {
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                for (String string : strings) {
                    String url = this.getObjectUrl(string);

                    channelVo.put(string, url);
                }
                return null;
            })));
        }

        try {
            CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return  channelVo;
    }

    @Override
    public InputStream getObjectInputStream(String objectName) {
        Assert.hasLength(objectName, "objectName cannot be empty");
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(config.getBucketName()).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", config.getBucketName());
            return null;
        }
        try {
            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder().bucket(config.getBucketName()).object(objectName).build());
            return object;
        } catch (Exception e) {
            log.warn("{} does not exist ", objectName);
            return null;
        }
    }
    @Override
    public InputStream getObjectInputStream(String bucketName, String objectName) {
        Assert.hasLength(objectName, "objectName cannot be empty");
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("{} does not exist ", config.getBucketName());
            return null;
        }
        try {
            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder().bucket(config.getBucketName()).object(objectName).build());
            return object;
        } catch (Exception e) {
            log.warn("{} does not exist ", objectName);
            return null;
        }
    }

    public boolean putObject(String bucket,String filename, String object, String contentType ) throws Exception{
//    public boolean putObject(PutObjectArgs args) throws Exception {
        UploadObjectArgs args = UploadObjectArgs.builder()
                .bucket(bucket)
                .filename(filename)
                .object(object)
                .contentType(contentType)
                .build();
        ObjectWriteResponse res = minioClient.uploadObject(args);
        log.info(" {} 上传成功！" , res.object());
        return true;
    }

    @Override
    public String getUrl(String fileName) {
        Assert.isTrue(StrUtil.isNotBlank(fileName), "carSeriesFileName cannot be empty");
        return config.getWebEndpoint().concat("/voc-cloud").concat(fileName);
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param file
     * @param filename   文件名称
     */
   /* @SneakyThrows
    public void putObjectImg(String bucketName, MultipartFile file, String filename) {
        //处理后生成新的files
        @Cleanup
        InputStream is = file.getInputStream();
        byte[] files = imgCompress(is, filename);
        @Cleanup
        InputStream inputStream = new ByteArrayInputStream(files);
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(filename).contentType(file.getContentType())
                        .stream(inputStream, inputStream.available(), -1).build());
    }*/

    /**
     * 图片压缩处理
     *
     * @param file
     * @return
     *//*
    public byte[] imgCompress(InputStream file, String fileName) throws IOException {
        long beginTime = System.currentTimeMillis();
        log.info("图片压缩开始：", beginTime);
        //压缩图片到指定多少K以内 IMG_SIZE

//        byte[] imageBytes = file.getBytes();

        byte[] imageBytes = IoUtil.readBytes(file);
        if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < imgSize * imgOzrf) {
            return imageBytes;
        }

        long srcSize = imageBytes.length;
//        double accuracy = getAccuracy(srcSize / ONE_ZERO_TWO_FOUR);
        double accuracy = imgProp;
        try {
            @Cleanup
            ByteArrayInputStream inputStream = null;
            @Cleanup
            ByteArrayOutputStream outputStream = null;

            while (imageBytes.length > imgSize * imgOzrf) {
                inputStream = new ByteArrayInputStream(imageBytes);
                outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        //按
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            log.info("图片原大小={}kb | 压缩后大小={}kb",
                    srcSize / imgOzrf, imageBytes.length / imgOzrf);

        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        //byte转file "application/octet-stream"

//        InputStream inputStreams = new ByteArrayInputStream(imageBytes);
//        MultipartFile multipartFile = isCompress(inputStreams);
//        MultipartFile multipartFile = new MoltipartFileService("application/octet-stream", inputStreams);
        log.info("byte转file成功");
        log.info("图片压缩结束:{},{}", System.currentTimeMillis() - beginTime, fileName);
        return imageBytes;
    }*/

    //inputstream转file
    public MultipartFile isCompress(InputStream inputStream) throws IOException {
        long beginTime = System.currentTimeMillis();
        log.info("流转file开始：{}",beginTime);
        //inputStream转file "application/octet-stream"
        MoltipartFileService moltipartFileService = new MoltipartFileService("application/octet-stream", inputStream);

        log.info("流转file结束：{}",System.currentTimeMillis() - beginTime);
        return moltipartFileService;
    }
}
