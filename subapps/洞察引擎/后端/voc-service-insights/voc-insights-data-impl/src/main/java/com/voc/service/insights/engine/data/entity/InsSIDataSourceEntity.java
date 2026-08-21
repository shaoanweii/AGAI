package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/28 下午1:26
 * @描述:
 **/
@Data
@TableName("ins_si_data_source")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsSIDataSourceEntity implements Serializable {
    /**
     * 主键
     */
    private String id;
    /**
     * 数据源id
     */
    private String dataSourceId;
    /**
     * 数据名称
     */
    private String dataName;
    /**
     * 数据总量
     */
    private Integer totalCount;
    /**
     * 数据校验成功的数量
     */
    private Integer verificationSuccessCount;
    /**
     * 数据处理执行成功的数量
     */
    private Integer executeSuccessCount;
    /**
     * 数据处理执行失败的数量
     */
    private Integer executeFailCount;
    /**
     * 数据最终状态 1:处理中 2:处理完成
     */
    private String status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
