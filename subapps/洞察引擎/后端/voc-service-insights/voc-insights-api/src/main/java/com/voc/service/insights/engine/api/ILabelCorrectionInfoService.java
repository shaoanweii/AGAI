package com.voc.service.insights.engine.api;

import java.util.List;

public interface ILabelCorrectionInfoService {

    Boolean batchInsert(String id,List<String> newIdList);

    Boolean del(String correctionRecordId);
}
