package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCarModel implements Serializable {

    private String brandName;

    private Set<String> carName;
}
