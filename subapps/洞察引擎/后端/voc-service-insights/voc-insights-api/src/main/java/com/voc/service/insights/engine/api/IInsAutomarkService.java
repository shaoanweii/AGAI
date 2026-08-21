package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.model.InsAutomarkExcelModel;
import com.voc.service.insights.engine.model.InsAutomarkModel;
import com.voc.service.insights.engine.vo.AutomarkVo;
import com.voc.service.insights.engine.vo.CarSeriesTemplateVo;
import com.voc.service.insights.engine.vo.InsAutomarkInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 15:30
 * @描述:
 **/
public interface IInsAutomarkService {
    /**
     * 保存车企信息
     * @param model
     */
    void saveAutomark(InsAutomarkModel model);
    /**
     * 修改车企信息
     * @param model
     */
    void updateAutomark(InsAutomarkModel model);

    /**
     * 根据id查询车企信息
     * @param model
     * @return
     */
    InsAutomarkInfoVo findAutomarkInfo(InsAutomarkModel model);

    /**
     * 分页查询车企列表
     * @param model
     * @return
     */
    IPage<InsAutomarkInfoVo> findAutomarkList(InsAutomarkModel model);

    /**
     * 查询车企列表
     * @return
     */
    List<AutomarkVo> findAutomarkInfoList(InsAutomarkModel model);

    /**
     * 批量修改状态
     * @param model
     */
    void batchChangeStatus(InsAutomarkModel model);


    void uploadExcel(MultipartFile file);


    void analyzeExcelData(List<InsAutomarkExcelModel> list);
}
