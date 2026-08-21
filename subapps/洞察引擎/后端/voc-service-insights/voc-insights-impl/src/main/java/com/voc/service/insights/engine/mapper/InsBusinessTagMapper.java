package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.voc.service.insights.engine.entity.InsBusinessTagEntity;
import com.voc.service.insights.engine.model.InsBusinessTagListQueryModel;
import com.voc.service.insights.engine.vo.InsBusinessTagListVo;
import com.voc.service.insights.engine.vo.InternationalVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Date: 2021-03-30
 * @Version: V1.0
 */

public interface InsBusinessTagMapper extends BaseMapper<InsBusinessTagEntity> {

    /**
     * 编辑节点状态
     *
     * @param id
     * @param status
     */
    void updateTreeNodeStatus(@Param("id") String id, @Param("status") String status);

    @Select("select name from ins_business_tag ${ew.customSqlSegment}")
    String getNameByCode(@Param(Constants.WRAPPER) QueryWrapper<InsBusinessTagEntity> wrapper);

    IPage<InsBusinessTagListVo> queryByPage(IPage<InsBusinessTagListVo> page, @Param("model") InsBusinessTagListQueryModel model);

    @Select("select id from ins_business_tag where brand=#{brand} and tag_code=#{parentTagCode} ")
    String queryIdByTagBrandAndCode(@Param("brand") String brand, @Param("parentTagCode") String parentTagCode);

    @Insert("insert  into ins_business_tag(id,name,NAME_EN,enable,tag_code,tag_type,industry_id,has_child,pid,brand,other) " +
            "values (#{tag.id},#{tag.name},#{tag.nameEn},#{tag.enable},#{tag.tagCode},#{tag.tagType},#{tag.industryId},#{tag.hasChild},#{tag.pid},#{tag.brand},#{tag.other})  ")
    int addRecord(@Param("tag") InsBusinessTagEntity insBusinessTag);


    @Select("SELECT TAG_CODE AS code ,NAME AS value FROM VOC_BUSINESS_TAG")
    List<InternationalVo> internationalCnTags_();
}
