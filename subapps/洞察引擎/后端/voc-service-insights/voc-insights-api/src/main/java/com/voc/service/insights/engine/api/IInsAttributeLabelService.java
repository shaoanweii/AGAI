package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.model.InsAttributeLabelModel;
import com.voc.service.insights.engine.model.InsAutomarkExcelModel;
import com.voc.service.insights.engine.vo.InsAttributeLabelVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/4/9 13:40
 * @描述:
 **/
public interface IInsAttributeLabelService {

    /**
     * 新增属性标签
     *
     * @param model 属性标签
     */
    void saveAttributeLabel(InsAttributeLabelModel model);

    /**
     * 编辑属性标签
     *
     * @param model 属性标签
     */
    void updateAttributeLabel(InsAttributeLabelModel model);

    /**
     * 批量修改属性标签状态
     *
     * @param model 属性标签
     */
    void batchChangeStatus(InsAttributeLabelModel model);

    /**
     * 分页获取属性标签列表
     *
     * @param model 查询条件
     * @return 属性标签分页数据
     */
    IPage<InsAttributeLabelVo> findAttributeLabelList(InsAttributeLabelModel model);

    /**
     * 获取标签属性列表
     *
     * @param model 查询条件
     * @return 标签属性列表
     */
    List<InsAttributeLabelVo> findAllAttributeLabelList(InsAttributeLabelModel model);

    void uploadExcel(MultipartFile file);

    void analyzeExcelData(List<InsAttributeLabelModel> list);
}
