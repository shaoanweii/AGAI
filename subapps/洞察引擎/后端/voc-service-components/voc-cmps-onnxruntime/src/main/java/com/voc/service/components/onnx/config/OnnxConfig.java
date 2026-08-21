package com.voc.service.components.onnx.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.components.minio.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.Cleanup;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/7 下午1:36
 * @描述:
 **/
@Configuration
public class OnnxConfig {

    private static final Logger log = LoggerFactory.getLogger(OnnxConfig.class);
    @Autowired
    private MinioClient minioClient;
    @Autowired
    MinioConfig config;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * @return ai.onnxruntime.OrtSession
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/8/9 下午1:28
     * @描述 启动是加载onnxRuntime模型会话
     **/
    @Bean
    public OrtSession getOnnxSession() {
        log.info("正在初始化建立onnx模型会话连接..");
        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        OrtSession session = null;
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        if ("local".equals(activeProfile)){
            try {
                URL modelUrl = ResourceUtil.getResources("model.onnx").get(0);
                String path = modelUrl.getPath();
                //兼容windows系统
                if(path.contains(":")) {
                    path =  StrUtil.replaceFirst(path, "/","");
                    path = StrUtil.replace(path,"/", "\\\\");
                }
                session = env.createSession(path, sessionOptions);
            } catch (OrtException e) {
                log.error("初始化建立onnx模型会话连接异常:{}", e);
                throw new RuntimeException(e);
            }
        }else {
            try {
                @Cleanup
                GetObjectResponse object = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(config.getBucketName())
                                .object("ai-workflow/model.onnx")
                                .build());
                byte[] bytes = object.readAllBytes();
                if (ObjectUtils.isEmpty(bytes)) {
                    log.error("从minio中获取onnx模型文件的字节数组为空");
                    throw new RuntimeException("从minio中获取onnx模型文件的字节数组为空");
                }
                //创建全局唯一onnxRuntime会话
                session = env.createSession(bytes, sessionOptions);
            } catch (OrtException e) {
                log.error("初始化建立onnx模型会话连接异常:{}", e);
                throw new RuntimeException("初始化建立onnx模型会话连接异常");
            } catch (ServerException e) {
                throw new RuntimeException(e);
            } catch (InsufficientDataException e) {
                throw new RuntimeException(e);
            } catch (ErrorResponseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (InvalidResponseException e) {
                throw new RuntimeException(e);
            } catch (XmlParserException e) {
                throw new RuntimeException(e);
            } catch (InternalException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("初始化建立onnx模型会话连接成功!");
        return session;
    }
}
