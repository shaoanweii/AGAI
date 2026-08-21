package com.voc.service.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Title: UploadModel
 * @Package: com.voc.service.common.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/21 18:01
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadModel implements Serializable {

    String key;
    String name;
    String url;


}
