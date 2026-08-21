package com.voc.service.data.integration.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Title: OrderTypeModel
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 15:47
 * @Version:1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PublicDomainInfoDataModel implements Serializable {

    String id;
    String data;

    String dateType;
}
