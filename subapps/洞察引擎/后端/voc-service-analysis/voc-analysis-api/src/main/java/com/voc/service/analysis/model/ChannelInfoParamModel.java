package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:34
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoParamModel implements Serializable {

    private String clientId;
}
