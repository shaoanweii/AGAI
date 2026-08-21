package com.voc.service.insights.engine.api;


import com.voc.service.common.exception.SystemException;
import com.voc.service.insights.engine.model.InsBusinessTagModel;

import java.util.List;

/**
 * @Date: 2021-03-30
 * @Version: V1.0
 */
public interface IInsBusinessTagService {

    /**
     * 根节点父ID的值
     */
    public static final String ROOT_PID_VALUE = "0";

    /**
     * 树节点有子节点状态值
     */
    public static final String HASCHILD = "1";

    /**
     * 树节点无子节点状态值
     */
    public static final String NOCHILD = "0";

    /**
     * 新增节点
     */
    void addInsBusinessTag(InsBusinessTagModel insBusinessTagModel);

    /**
     * 修改节点
     */
    void updateInsBusinessTag(InsBusinessTagModel insBusinessTagModel) throws SystemException;

    /**
     * 删除节点
     */
    void deleteInsBusinessTag(String id) throws SystemException;

    String getNameByCode(String zbCode);

//    List<InsBusinessTagVo> queryTreeList(QueryWrapper<InsBusinessTagModel> queryWrapper);

//    IPage<InsBusinessTagListVo> queryByPage(Page<InsBusinessTagListVo> page, InsBusinessTagListQueryModel insBusinessTag, HttpServletRequest req);

//    Result<?> batchImport(MultipartFile file);

    String queryIdByTagBrandAndCode(String brand, String parentTagCode);

    boolean addRecord(InsBusinessTagModel record);

    String getAllName(String labelCode);

    List<InsBusinessTagModel> findAll();



}
