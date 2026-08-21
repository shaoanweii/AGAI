package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.vo.DownLoadFileVo;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.function.Function;

/**
 * @Title: ILargeDigitaFilesService
 * @Package: com.voc.service.insights.engine.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/15 18:39
 * @Version:1.0
 */
public interface ILargeDigitaFilesService {


    void start(String fileName, String taskId, long total, Class<?> clazz, Function<com.github.pagehelper.Page, List<?>> data);

    String getFileUrl(LargeDigitaFilesModel model);

    IPage<DownLoadFileVo> getFileList(LargeDigitaFilesModel model);

    LargeDigitaFilesModel getFile(LargeDigitaFilesModel model);

    void insert(LargeDigitaFilesModel model);

    void update(LargeDigitaFilesModel model);

    List<String> findUserIds(LargeDigitaFilesModel model);

    List<InsDownAccountInfoAuthVo> findVisibleUserList(List<String> userIds);

    void downloadFile(LargeDigitaFilesModel model, HttpServletResponse response);
}
