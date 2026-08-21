package com.voc.service.insights.engine.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.model.InsCarSceneExcelModel;
import com.voc.service.insights.engine.model.InsCarSceneModel;
import com.voc.service.insights.engine.model.TagLibExcelModel;
import com.voc.service.insights.engine.vo.InsCarSceneOperatorVo;
import com.voc.service.insights.engine.vo.InsCarSceneVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/3/9
 * @描述: 用车场景服务接口
 **/
public interface IInsCarSceneService {

    /**
     * 新增用车场景
     *
     * @param model 用车场景
     */
    void saveCarScene(InsCarSceneModel model);

    /**
     * 修改用车场景
     *
     * @param model 用车场景
     */
    void updateCarScene(InsCarSceneModel model);

    /**
     * 分页查询用车场景
     *
     * @param model 查询参数
     * @return 分页结果
     */
    IPage<InsCarSceneVo> findCarSceneList(InsCarSceneModel model);

    /**
     * 查询操作人列表
     *
     * @param isAllVisible 是否查询全部可见数据
     * @return 操作人列表
     */
    List<InsCarSceneOperatorVo> findCarSceneOperatorList(Boolean isAllVisible);

    /**
     * 批量修改状态
     *
     * @param model 参数
     */
    void batchChangeStatus(InsCarSceneModel model);

    /**
     * 批量移动
     *
     * @param model 参数
     */
    void batchMoveCarScene(InsCarSceneModel model);

    void analyzeExcelData(List<InsCarSceneExcelModel> list);

    void uploadExcel(MultipartFile file);
}
