package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.vo.DictVo;

import java.util.List;

/**
 * @version 1.0.0
 * @ClassName ProvinceAreaService.java
 * @Description
 * @createTime 2022年10月21日 16:52
 * @Copyright futong
 */
public interface IProvinceAreaService {
    List<DictVo> queryProvinceByAreaCode(String value);

    String queryAreaByProvince(String province);
}
