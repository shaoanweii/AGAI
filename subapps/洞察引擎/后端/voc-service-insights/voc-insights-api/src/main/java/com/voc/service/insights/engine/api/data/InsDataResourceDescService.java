package com.voc.service.insights.engine.api.data;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.model.data.InsDataResourceExcelModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资源详情(InsDataResourceDesc)表接口服务层
 *
 * @author leiww
 * @since 2024-04-02 17:00:19
 */
public interface InsDataResourceDescService {

    /**
     * 通过ID查询单条数据
     *
     * @param model@return 实例对象
     */
    InsDataResourceDescModel queryById(InsDataResourceDescModel model);

    /**
     * 分页查询
     *
     * @param model 筛选条件
     * @return 查询结果
     */
    Result<?> queryBySelect(InsDataResourceDescModel model);

    /**
     * 新增数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean insert(InsDataResourceDescModel model);

    /**
     * 修改数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean update(InsDataResourceDescModel model);

    /**
     * 通过主键删除数据
     *
     * @param model@return 是否成功
     */
    Boolean deleteByIdResourceId(InsDataResourceDescModel model);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    List<ResourceDescDto> queryByParam(InsDataResourceDescModel model);

    List<ResourceDescDto> queryByResourceId(InsDataResourceDescModel model);

    Boolean updateStatus(InsDataResourceDescModel model);
    /**
     * @param resourceId
     * @param clientId
     * @param list
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/8/19 下午5:25
     * @描述 Excel解析
     **/
    void analysisExcel(String resourceId, String clientId, List<InsDataResourceExcelModel> list);

    void dataResourceUpload(MultipartFile file,String clientId,String resourceId);

    List<ResourceDescDto> findAllDataResourceDesc(InsDataResourceDescModel model);

    void changeResourceStatus(InsDataResourceDescModel model);

    Map<String,Integer> countByCategoryIds(Set<String> ids);
}
