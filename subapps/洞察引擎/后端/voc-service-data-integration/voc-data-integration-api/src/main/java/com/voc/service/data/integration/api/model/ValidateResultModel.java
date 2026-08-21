package com.voc.service.data.integration.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: ValidateResultModel
 * @Package: com.voc.service.data.integration.in.mpp.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 10:58
 * @Version:1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ValidateResultModel implements Serializable {
    @Builder.Default
    List<Object> successfulDataset  = new ArrayList<>();

    @Builder.Default
    List<Object> failedDataset = new ArrayList<>();
}
