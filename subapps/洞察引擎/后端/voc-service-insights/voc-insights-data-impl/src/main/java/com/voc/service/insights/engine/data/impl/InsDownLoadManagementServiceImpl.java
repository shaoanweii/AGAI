package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StringUtil;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsDownLoadManagementService;
import com.voc.service.insights.engine.api.ILargeDigitaFilesService;
import com.voc.service.insights.engine.api.data.IInsCqCaDataSourceService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.DownLoadFileVo;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class InsDownLoadManagementServiceImpl implements IInsDownLoadManagementService {
    private static final Logger log = LoggerFactory.getLogger(InsDownLoadManagementServiceImpl.class);

    @Autowired
    ILargeDigitaFilesService downLoadFilesService;
    @Autowired
    private IInsCqCaDataSourceService iInsCqCaDataSourceService;


    @Override
    public IPage<DownLoadFileVo> findReportDownLoadFileList(LargeDigitaFilesModel model) {
        if (ObjectUtils.isNotEmpty(model.getIsAllVisible()) && !model.getIsAllVisible()) {
            String userId = ServiceContextHolder.getUserId();
            model.setUserIds(List.of(userId));
        }
        return downLoadFilesService.getFileList(model);
    }

    @Override
    public void downloadAgain(LargeDigitaFilesModel model, HttpServletResponse response) {
        Assert.hasLength(model.getId(), "id不能为空");
        LargeDigitaFilesModel file = downLoadFilesService.getFile(model);
        if (ObjectUtils.isEmpty(file)) {
            throw new BussinessException("下载记录不存在");
        }
        try {
            final String uncompress = StringUtil.uncompress(file.getParameters());
            InsCqCaDataQueryModel queryModel = JSONObject.toJavaObject(JSONObject.parseObject(uncompress), InsCqCaDataQueryModel.class);
            log.info("下载记录参数：{}", JSONObject.toJSONString(queryModel));
            queryModel.setTaskId(file.getTaskId());
            this.exportAccountData(queryModel, file, response);
        } catch (Exception e) {
            throw new BussinessException("下载失败");
        }
    }

    @Override
    public List<InsDownAccountInfoAuthVo> findVisibleUserList(Boolean isAllVisible) {
        List<String> userIds = new ArrayList<>();
        if (isAllVisible) {
            LargeDigitaFilesModel model = new LargeDigitaFilesModel();
            List<String> userId = downLoadFilesService.findUserIds(model);
            if (ObjectUtils.isEmpty(userId)) {
                return List.of();
            }
            userIds.addAll(userId);
        } else {
            userIds.add(ServiceContextHolder.getUserId());
        }
        // 获取用户信息
        return downLoadFilesService.findVisibleUserList(userIds);
    }

    @Override
    public void downloadFile(LargeDigitaFilesModel model, HttpServletResponse response) {
        Assert.hasLength(model.getId(), "id不允许为空");
        downLoadFilesService.downloadFile(model, response);
    }


    private void exportAccountData(InsCqCaDataQueryModel userModel, LargeDigitaFilesModel model, HttpServletResponse response) throws Exception {
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                if (model.getType().equals("exportRawDataResult")) {
                    iInsCqCaDataSourceService.exportRawDataResult(userModel, response);
                } else if (model.getType().equals("exportRawData")) {
                    iInsCqCaDataSourceService.exportRawData(userModel, response);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
