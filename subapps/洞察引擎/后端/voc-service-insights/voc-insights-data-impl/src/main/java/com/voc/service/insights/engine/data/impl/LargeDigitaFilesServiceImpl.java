package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.ttl.TtlWrappers;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.minio.config.MinioConfig;
import com.voc.service.components.minio.service.UploadFileService;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.ILargeDigitaFilesService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.data.entity.LargeDigitaFilesEntity;
import com.voc.service.insights.engine.data.mapper.LargeDigitaFileMapper;
import com.voc.service.insights.engine.vo.DownLoadFileVo;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @Title: 大文件处理服务
 * @Package: com.voc.service.insights.engine.data.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/15 18:13
 * @Version:1.0
 */
@Service
public class LargeDigitaFilesServiceImpl
        extends ServiceImpl<LargeDigitaFileMapper, LargeDigitaFilesEntity>
        implements ILargeDigitaFilesService {
    private static final Logger log = LoggerFactory.getLogger(LargeDigitaFilesServiceImpl.class);
    @Value("${files.readBatchSize:10000}")
    int batchSize = 10000;
    @Value("${files.readTimeOut:600}")
    long allOfTimeoutSeconds;
    @Value("${files.maxRows:200000}")
    long maxRows;
    @Autowired
    UploadFileService uploadFileService;
    @Autowired
    MinioConfig minioConfig;


    @Override
    public void start(final String fileName, final String taskId, final long total, Class<?> clazz, Function<Page, List<?>> data) {
        try {
            Assert.isTrue(StrUtil.isNotBlank(fileName), "fileName cannot be empty");
            Page page_ = PageHelper.startPage(1, 20000);
            page_.setTotal(total);
            int pages = page_.getPages();
            PageHelper.clearPage();
            log.info("开始拆解任务，共{}页，每页{}条数据", pages, 20000);

//            final String taskId = IdWorker.getId();
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            final String tempFileName = taskId.concat(".xlsx");
            FileItem fileItem = factory.createItem("textField", "text/plain", true, taskId.concat(tempFileName));
            log.info("创建临时文件 {}", tempFileName);
            @Cleanup
            OutputStream outputStream = fileItem.getOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(outputStream).head(clazz).build();

            if (total <= 0) {
                log.info("空数据集 {}", taskId);
                excelWriter.write(ArrayList::new, EasyExcel.writerSheet("Sheet1").build());

            } else {

                List<CompletableFuture> futureList = new ArrayList<>();
                if (pages>5){
                    pages = 5;
                }
                for (int i = 1; i <= pages; i++) {
                    final int finalI = i;

                    futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                        final List<?> rows = data.apply(new Page(finalI, 20000));
                        if (CollUtil.isEmpty(rows)) {
                            log.info("查询到数据未空");
                            return null;
                        }
                        synchronized (excelWriter) {
                            excelWriter.write(rows, EasyExcel.writerSheet("Sheet1").build());
                        }
                        return null;
                    })));
                }

                try {
                    CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get(allOfTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
                    log.info("临时文件写入完成");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            try {
                excelWriter.finish();
                log.info("文件写入开始");

                final String fileKey = "files/".concat(this.cleanFileName(fileName).concat(".xlsx"));
                @Cleanup
                InputStream is = fileItem.getInputStream();
                uploadFileService.putObject("resource", fileKey, is);
                this.baseMapper.updateAttachmentDownloadRecord(LargeDigitaFilesModel.builder().id(taskId).fileKey(fileKey).status("1").build());
//                this.update();
                fileItem.delete();
                log.info("文件上传完成");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                this.baseMapper.updateAttachmentDownloadRecord(LargeDigitaFilesModel.builder().id(taskId).status("0").build());
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFileUrl(LargeDigitaFilesModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "getUserId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(model.getType()), "getType cannot be empty");
        QueryWrapper<LargeDigitaFilesEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(LargeDigitaFilesEntity::getUserId, model.getUserId());
        wrapper.lambda().eq(LargeDigitaFilesEntity::getType, model.getType());

        LargeDigitaFilesEntity entity = this.baseMapper.selectOne(wrapper);
        if (ObjUtil.isNull(entity)) {
            return null;
        }
        final String url = uploadFileService.getObjectUrl("resource", entity.getFileKey(), 30);
        log.info("getFileUrl {}", url);

        return url;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public IPage<DownLoadFileVo> getFileList(LargeDigitaFilesModel model) {
        final String appId = ServiceContextHolder.getSystemId();
        IPage<LargeDigitaFilesEntity> pages = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(model.getPageNum(), model.getPageSize());
        model.setAppId(appId);
        IPage<LargeDigitaFilesEntity> fileList = this.baseMapper.getFileList(pages, model);
        IPage<DownLoadFileVo> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        if (ObjectUtils.isEmpty(fileList.getRecords())) {
            log.info("暂无下载列表信息");
            return page;
        }
        page.setSize(fileList.getSize());
        page.setCurrent(fileList.getCurrent());
        page.setTotal(fileList.getTotal());
        List<LargeDigitaFilesEntity> records = fileList.getRecords();
        List<DownLoadFileVo> resource = records.stream().map(e -> {
            DownLoadFileVo build = DownLoadFileVo.builder()
                    .id(e.getId())
                    .fileName(e.getTaskName())
                    .downloadTime(e.getCreateTime())
                    .operator(e.getUserName())
                    .status(ObjectUtils.isNotEmpty(e.getStatus()) ? e.getStatus() : null)
                    .filePath(ObjectUtils.isNotEmpty(e.getFileKey()) ? e.getTaskName() : null)
                    .build();
//            if (ObjectUtils.isNotEmpty(e.getFileKey())) {
//                final String url = getFileURL(e.getFileKey());
//                build.setFilePath(url);
//            }
            return build;
        }).collect(Collectors.toList());
        page.setRecords(resource);
        return page;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public LargeDigitaFilesModel getFile(LargeDigitaFilesModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getId()), "id cannot be empty");
        QueryWrapper<LargeDigitaFilesEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(LargeDigitaFilesEntity::getId, model.getId());
        LargeDigitaFilesEntity entity = this.baseMapper.selectOne(wrapper);
        LargeDigitaFilesModel largeDigitaFilesModel = new LargeDigitaFilesModel();
        BeanUtils.copyProperties(entity, largeDigitaFilesModel);
        return largeDigitaFilesModel;
    }

    @Override
    public void insert(LargeDigitaFilesModel model) {
        LargeDigitaFilesEntity entity = new LargeDigitaFilesEntity();
        BeanUtils.copyProperties(model, entity);
        this.baseMapper.insert(entity);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public void update(LargeDigitaFilesModel model) {
        LargeDigitaFilesEntity entity = new LargeDigitaFilesEntity();
        BeanUtils.copyProperties(model, entity);
        this.baseMapper.updateById(entity);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public List<String> findUserIds(LargeDigitaFilesModel model) {
        return this.baseMapper.findUserIds();
    }

    @Override
    @SwitchClientDS(datasource ="starrock_dndc")
    public List<InsDownAccountInfoAuthVo> findVisibleUserList(List<String> userIds) {
        return this.baseMapper.findVisibleUserList(userIds);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.appClient")
    public void downloadFile(LargeDigitaFilesModel model, HttpServletResponse response) {
        LargeDigitaFilesEntity largeDigitaFilesEntity = this.baseMapper.selectById(model.getId());
        if (ObjectUtils.isEmpty(largeDigitaFilesEntity)) {
            throw new BussinessException("文件下载记录不存在");
        }
        if (ObjectUtils.isEmpty(largeDigitaFilesEntity.getFileKey())) {
            throw new BussinessException("文件暂未生成");
        }
        String fileName = this.buildDownloadFileName(largeDigitaFilesEntity);
        InputStream inputStream = uploadFileService.getObjectInputStream("resource", largeDigitaFilesEntity.getFileKey());
        if (ObjectUtils.isEmpty(inputStream)) {
            throw new BussinessException("文件不存在或已失效");
        }
        try (InputStream in = inputStream) {
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (BussinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载文件失败, id:{}, fileKey:{}", model.getId(), largeDigitaFilesEntity.getFileKey(), e);
            throw new BussinessException("下载文件失败");
        }
    }

    private String buildDownloadFileName(LargeDigitaFilesEntity entity) {
        String taskName = entity.getTaskName();
        if (StrUtil.isBlank(taskName)) {
            taskName = entity.getId();
        }
        String suffix = "";
        if (StrUtil.isNotBlank(entity.getFileKey()) && entity.getFileKey().contains(".")) {
            suffix = entity.getFileKey().substring(entity.getFileKey().lastIndexOf("."));
        }
        if (StrUtil.isBlank(suffix) || taskName.endsWith(suffix)) {
            return taskName;
        }
        return taskName + suffix;
    }

    // 定义非法字符的正则表达式
    private static final String ILLEGAL_CHARACTERS_REGEX = "[\\\\/:*?\"<>|]";

    // 定义保留名称
    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    public String cleanFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "default";
        }

        // 替换非法字符
        String cleanedFileName = fileName.replaceAll(ILLEGAL_CHARACTERS_REGEX, "_");

        // 检查是否为保留名称
        for (String reservedName : RESERVED_NAMES) {
            if (cleanedFileName.equalsIgnoreCase(reservedName)) {
                cleanedFileName = cleanedFileName + "_";
                break;
            }
        }

        return cleanedFileName;
    }


    public String getFileURL(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        String objectUrl = uploadFileService.getObjectUrl(key);
        if (ObjectUtils.isNotEmpty(objectUrl) && objectUrl.contains(minioConfig.getEndpoint() + ":" + minioConfig.getEndpointPort())) {
            objectUrl = objectUrl.replace(minioConfig.getEndpoint() + ":" + minioConfig.getEndpointPort(), minioConfig.getWebEndpoint());
        }
        objectUrl = objectUrl.substring(objectUrl.indexOf("files"));

//        objectUrl = removeQueryParamsIfDev(objectUrl);
        return objectUrl;
    }
}
