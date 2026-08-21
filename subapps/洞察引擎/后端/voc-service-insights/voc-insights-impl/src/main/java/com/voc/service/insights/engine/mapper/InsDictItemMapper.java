package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsDictItemEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface InsDictItemMapper extends BaseMapper<InsDictItemEntity> {
    @Select("SELECT * FROM sys_dict_item WHERE DICT_ID = #{mainId} order by sort_order asc, item_value asc")
    public List<InsDictItemEntity> selectItemsByMainId(String mainId);
}
