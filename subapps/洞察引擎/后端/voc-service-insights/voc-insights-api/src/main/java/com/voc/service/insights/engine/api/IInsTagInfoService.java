package com.voc.service.insights.engine.api;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsTagInfoBatchModel;
import com.voc.service.insights.engine.model.InsTagInfoModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.InsTagInfoListVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:08
 * @描述:
 **/
public interface IInsTagInfoService {

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 14:10
     * @描述   根据类型获取标签信息
     * @param type
     * @return java.util.List<com.voc.service.insights.engine.model.InsTagInfoModel>
     **/
    List<InsTagInfoModel> findTageInfoByType(String type);

    Result<?> queryInsTagInfo(InsTagInfoQueryModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    public void insert(InsTagInfoModel model);

    void insertBatch(InsTagInfoBatchModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    public void update(InsTagInfoModel model);

    /**
     * 删除实体数据
     *
     * @param ids 删除实体ids
     * @return 是否成功
     */
    public void deleteByIds(List<Serializable> ids);

    /**
     * 查询实体数据
     *
     * @param id 查询实体id
     * @return 查询数据
     */
    public InsTagInfoModel queryById(Serializable id);

    InsTagInfoListVo queryVoById(Serializable id);


    List<InsTagInfoModel> queryByIdList(List<String> idList);

//    void tagConvert(MultipartFile file, HttpServletResponse response, String tagType);

//    void tagGetCode(List<TagConvertTemplate> list, String response, Map<String, List<TagConvertTemplate>> map);
}
