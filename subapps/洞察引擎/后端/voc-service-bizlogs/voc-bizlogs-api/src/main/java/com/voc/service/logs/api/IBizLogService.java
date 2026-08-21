package com.voc.service.logs.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.model.UserMenuVisitRecordModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IBizLogService {
    /**
     * 向业务日志消息队列中添加日志
     * @param model
     */
    void pushBizLogsMsg(OpsLogModel model);

    /**
     * 查询业务日志消息
     *
     * @param messageDTO
     * @return
     */
    PageInfo findBizLogsMsg(OpsLogModel messageDTO);

    /**
     * 查看全部业务日志
     *
     * @param messageDTO
     * @return
     */
    List<OpsLogModel> findAllBizLogsMsg(OpsLogModel messageDTO);

   void pushMenuVisitRecord(UserMenuVisitRecordModel model);

}
