package com.voc.service.insights.engine.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.entity.AysPostprocessDataEntity;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionDataVo;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionGroupDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface AysLabelPostprocessDataMapper extends BaseMapper<AysPostprocessDataEntity> {


    String refreshViewData();

    List<InsCqCaCorrectionDataVo> querySoundsData(List<String> idList);

    List<InsCqCaCorrectionGroupDataVo> queryGroupData(List<String> idList);

    int batchUpdateByDtoList(@Param("dtoList") List<AysPostprocessDataEntity> dtoList);


    @SwitchClientDS(datasource = "starrock_dndc")
    List<String> findResultDataIdsByBrandCode(@Param("cqCaDataQueryModel") InsCqCaDataQueryModel cqCaDataQueryModel);
}