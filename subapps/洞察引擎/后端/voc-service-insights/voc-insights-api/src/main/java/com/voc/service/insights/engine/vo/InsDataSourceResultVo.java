package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/24 下午3:32
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDataSourceResultVo implements Serializable {
    /**
     * 数据总量
     */
    private Integer totalCount;
    /**
     * 数据校验成功的数量
     */
    private Integer verificationSuccessCount;
    /**
     * 执行成功数量
     */
    private Integer executeSuccessCount;
    /**
     * 日期 格式：YYYY-MM-DD
     */
    private String date;

    private String dataSourceId;
    private String clientId;

    @Schema(description = "成功数量")
    @Builder.Default
    private String finishCount="0";

    @Schema(description = "失败数量")
    @Builder.Default
    private String failCount="0";

    private String createTime;
    private String data;


}
