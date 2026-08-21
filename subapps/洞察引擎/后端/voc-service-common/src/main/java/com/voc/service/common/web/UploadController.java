package com.voc.service.common.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.api.IUploadFileService;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Title: UploadController
 * @Package: com.voc.service.common.web
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/21 17:34
 * @Version:1.0
 */

@RestController
@Tag(name = "文件上传")
@RequestMapping("/")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    @Autowired
    IUploadFileService uploadFileService;

    static final String BRAND_PACKAGE_PATH = "static/品牌";
    static final String CAR_PACKAGE_PATH = "static/车系/pc";
    static final String AUTOMARK_PACKAGE_PATH = "static/车企";

    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 获取上传文件对象
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");

        final String suffix = this.getSuffix(file.getOriginalFilename());
        final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
        UploadModel model = new UploadModel();
        model.setKey(this.getFileName(fileName_));

        @Cleanup
        InputStream fileIs = file.getInputStream();
        uploadFileService.putObject(this.getFileName(fileName_), fileIs);

        final String url = uploadFileService.getObjectUrl(this.getFileName(fileName_));
        model.setUrl(url);
        return Result.OK(model);
    }

    @PostMapping(value = "/uploadCarSeries")
    public Result<?> uploadCarSeries(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 获取上传文件对象
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");
            this.assertPngFile(file);

            final String suffix = this.getSuffix(file.getOriginalFilename());
            final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
            UploadModel model = new UploadModel();
            model.setKey(fileName_);

            @Cleanup
            InputStream fileIs = file.getInputStream();
            uploadFileService.putObject(this.getCarSeriesFileName(fileName_), fileIs);
            final String url = uploadFileService.getObjectUrl(this.getCarSeriesFileName(fileName_));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            model.setUrl(imgUrl);
            return Result.OK(model);
        }catch (IllegalArgumentException illegalArgumentException) {
            log.error("文件上传-上传车系图片异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("文件上传-上传车系图片异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("文件上传-上传车系图片异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }

    }

    @PostMapping(value = "/uploadBrand")
    public Result<?> uploadBrand(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 获取上传文件对象
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");
            this.assertPngFile(file);

            final String suffix = this.getSuffix(file.getOriginalFilename());
            final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
            UploadModel model = new UploadModel();
            model.setKey(fileName_);

            @Cleanup
            InputStream fileIs = file.getInputStream();
            uploadFileService.putObject(this.getBrandFileName(fileName_), fileIs);

            final String url = uploadFileService.getObjectUrl(this.getBrandFileName(fileName_));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            model.setUrl(imgUrl);
            return Result.OK(model);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("文件上传-上传品牌图片异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("文件上传-上传品牌图片异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("文件上传-上传品牌图片异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }

    }

    @PostMapping(value = "/uploadAutomark")
    public Result<?> uploadAutomark(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 获取上传文件对象
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("file");
            this.assertPngFile(file);

            final String suffix = this.getSuffix(file.getOriginalFilename());
            final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
            UploadModel model = new UploadModel();
            model.setKey(fileName_);

            @Cleanup
            InputStream fileIs = file.getInputStream();
            uploadFileService.putObject(this.getAutomarkFileName(fileName_), fileIs);

            final String url = uploadFileService.getObjectUrl(this.getAutomarkFileName(fileName_));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            model.setUrl(imgUrl);
            return Result.OK(model);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("文件上传-上传车企图片异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("文件上传-上传车企图片异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("文件上传-上传车企图片异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    /**
     * 获取文件后缀
     *
     * @param fileName
     * @return
     */
    public String getSuffix(String fileName) {
        Assert.isTrue(StrUtil.isNotBlank(fileName), "fileName cannot be empty");
        return FileUtil.getSuffix(fileName);
    }

    /**
     * 校验仅允许上传PNG图片
     */
    private void assertPngFile(MultipartFile file) {
        Assert.notNull(file, "上传文件不能为空");
        Assert.isTrue(!file.isEmpty(), "上传文件不能为空");
        Assert.isTrue("png".equalsIgnoreCase(this.getSuffix(file.getOriginalFilename())), "仅支持PNG图片上传");
        String contentType = file.getContentType();
        if (StrUtil.isNotBlank(contentType)) {
            Assert.isTrue("image/png".equalsIgnoreCase(contentType) || "image/x-png".equalsIgnoreCase(contentType),
                    "仅支持PNG图片上传");
        }
    }

    /**
     * 拼装文件路径  insights/car-series/51b2cc3210d88d7fb2518c8bdd3f42c1.png
     *
     * @param name
     * @return
     */
    private String getFileName(String name) {
        return ServiceContextHolder.getSystemId().concat("/").concat(name);
    }


    /**
     * 车系路径
     */
    private String getCarSeriesFileName(String name) {
        return CAR_PACKAGE_PATH.concat("/").concat(name);
    }

    /**
     * 品牌路径
     */
    private String getBrandFileName(String name) {
        return BRAND_PACKAGE_PATH.concat("/").concat(name);
    }

    /**
     * 车企路径
     */
    private String getAutomarkFileName(String name) {
        return AUTOMARK_PACKAGE_PATH.concat("/").concat(name);
    }
}
