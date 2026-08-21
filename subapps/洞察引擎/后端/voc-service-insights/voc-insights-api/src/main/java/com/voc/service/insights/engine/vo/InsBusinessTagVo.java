package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.model.InsBusinessTagModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: ins_business_tag
 * @Date: 2021-03-30
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Tag(name = "ins_business_tag对象", description = "ins_business_tag")
public class InsBusinessTagVo extends InsBusinessTagModel  implements Serializable {
    List<InsBusinessTagVo> childes;
}
