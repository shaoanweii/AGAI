package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsTagClientBatchModel;
import com.voc.service.insights.engine.model.InsTagClientModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.ConditionDetailsVo;
import com.voc.service.insights.engine.vo.InsTagClientVo;

import java.io.Serializable;
import java.util.List;

public interface IInsTagClientService {
    PageInfo queryInsClientInfo(InsTagInfoQueryModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    Boolean insert(InsTagClientModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    Boolean update(InsTagClientModel model);

    /**
     * 删除实体数据
     *
     * @param ids 删除实体ids
     * @return 是否成功
     */
    Boolean deleteByIds(List<Serializable> ids);

    /**
     * 查询实体数据
     *
     * @param id 查询实体id
     * @return 查询数据
     */
    InsTagClientVo queryVoById(Serializable id);

    /**
     * 批量新增标签
     * @param model
     * @return
     */
    Boolean insertBatch(InsTagClientBatchModel model);

    List<ConditionDetailsVo> queryTagClientTree(InsTagInfoQueryModel model);
}
