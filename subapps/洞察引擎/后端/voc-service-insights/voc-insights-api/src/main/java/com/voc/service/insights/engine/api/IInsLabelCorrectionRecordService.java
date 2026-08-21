package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.api.model.InsCqCaLabelCorrectionRecordModel;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.InsCqCaUpdateLabelRecordModel;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionInfoVo;

import java.util.List;

public interface IInsLabelCorrectionRecordService {

    PageInfo queryLabelCorrectionList(InsCqCaLabelCorrectionRecordQueryModel model);

    Boolean insertLabelCorrection(InsCqCaLabelCorrectionRecordModel model);

    PageInfo queryDataInfo(InsCqCaLabelCorrectionRecordQueryModel model);


    InsCqCaCorrectionInfoVo queryCorrectionInfo(InsCqCaLabelCorrectionRecordQueryModel model);

    List<String> queryCreateUserList(InsCqCaLabelCorrectionRecordQueryModel model);


    Boolean auditLabelCorrection(InsCqCaUpdateLabelRecordModel model);

    Boolean del(InsCqCaLabelCorrectionRecordQueryModel model);

}
