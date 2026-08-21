package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsTagLibEntity;
import com.voc.service.insights.engine.model.InsTagLibModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 下午2:05
 * @描述:
 **/
@Mapper
@Repository
public interface InsTagLibMapper extends BaseMapper<InsTagLibEntity> {

    /**
     * @param tagLibName
     * @param tagParentId
     * @return java.lang.Integer
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/21 下午2:49
     * @描述 校验标签名称是否存在
     **/
    InsTagLibEntity checkTagLibName(@Param("tagLibName") String tagLibName, @Param("tagParentId") String tagParentId);

    /**
     * @param tagLibModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsTagLibEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/21 下午3:50
     * @描述 获取标签列表
     **/
    List<InsTagLibEntity> findTagLibList(@Param("tagLibModel") InsTagLibModel tagLibModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/17 上午10:04
     * @描述   根据id更新标签
     * @param tagLibEntity
     * @return java.util.List<com.voc.service.insights.engine.entity.InsTagLibEntity>
     **/
    int updateTagLibById(@Param("tagLibEntity") InsTagLibEntity tagLibEntity);

    /**
     * 向上查找标签列表的层级结构
     *
     * @param tagLibId
     * @return
     */
    String findTagLibNameHierarchical(@Param("tagLibId") String tagLibId);

    /**
     * 根据父级id查找子级标签
     *
     * @param tagLibId
     * @return
     */
    List<InsTagLibEntity> findTagLibListByParentId(@Param("tagLibId") String tagLibId);

    /**
     * @param tagLibId
     * @return com.voc.service.insights.engine.entity.InsTagLibEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/23 下午3:49
     * @描述 根据id获取标签信息
     **/
    InsTagLibEntity findTagLibById(@Param("tagLibId") String tagLibId);

    /**
     * @param tagParentIds
     * @return java.util.List<com.voc.service.insights.engine.entity.InsTagLibEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/5/24 下午4:30
     * @描述 向上查找标签列表的层级结构
     **/
    List<InsTagLibEntity> UpwardFindTagLibHierarchical(@Param("tagParentIds") List<String> tagParentIds);

    List<InsTagLibEntity> findTagLibByIds(@Param("ids") List<String> ids);

    List<InsTagLibEntity> findTagLibHierarchical(@Param("ids") List<String> tagLibIds);

    InsTagLibEntity findTagLibByCode(String code);
}
