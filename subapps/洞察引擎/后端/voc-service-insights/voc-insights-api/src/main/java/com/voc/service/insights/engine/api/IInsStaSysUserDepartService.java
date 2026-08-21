package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.model.InsStaSysUserDepartModel;
import com.voc.service.insights.engine.vo.InsStaSysUserDepartVo;

import java.util.List;

public interface IInsStaSysUserDepartService {


    Boolean saveOrUpdateDepart(InsStaSysUserDepartModel staSysUserDepartModel, String clientId);

    List<InsStaSysUserDepartVo> findStaSysUserDepartList(InsStaSysUserDepartModel staSysUserDepartModel, String clientId);

    List<String> getDepIdByUserIdList(List<String> depIdList, String clientId);

    List<String> findUserIdByDepId(String deptId, String clientId);

    List<InsStaSysUserDepartVo> findUserDepartMapping(String clientId);
}
