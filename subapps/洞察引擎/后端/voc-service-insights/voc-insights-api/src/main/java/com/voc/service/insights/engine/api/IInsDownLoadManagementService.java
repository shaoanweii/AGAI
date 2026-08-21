package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.vo.DownLoadFileVo;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;


public interface IInsDownLoadManagementService {


    IPage<DownLoadFileVo> findReportDownLoadFileList(LargeDigitaFilesModel model);


    void downloadAgain(LargeDigitaFilesModel model, HttpServletResponse response);


    List<InsDownAccountInfoAuthVo> findVisibleUserList(Boolean isAllVisible);

    void downloadFile(LargeDigitaFilesModel model, HttpServletResponse response);
}
