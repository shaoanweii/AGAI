package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityCodeModel  implements Serializable {

    private String dealershipCode;

    private String cityCode;

    private String cityName;

    private String provinceCode;

    private String provinceName;

    private String bigAreaCode;

    private String bigAreaSale;

    private String smallAreaCode;

    private String samllAreaSale;


}
