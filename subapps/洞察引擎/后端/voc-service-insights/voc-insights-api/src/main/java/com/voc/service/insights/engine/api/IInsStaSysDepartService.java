package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.model.InsAccountInfoModel;
import com.voc.service.insights.engine.model.InsSysDepartModel;
import com.voc.service.insights.engine.vo.InsSysDepartVo;

import java.util.List;

public interface IInsStaSysDepartService {

    List<InsSysDepartModel> getClientDepartList(InsAccountInfoModel accountInfoModel);

    List<InsSysDepartVo> getDepartList(String clientId);

    List<InsSysDepartVo> getDepartSubtree(String clientId, String deptId);

    List<InsSysDepartVo> getDepartAncestorList(String clientId, String deptId);
}
