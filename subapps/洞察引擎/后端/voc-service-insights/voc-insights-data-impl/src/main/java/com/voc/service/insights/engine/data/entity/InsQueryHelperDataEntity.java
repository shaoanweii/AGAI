package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/23 10:17
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("report_query_helper_data")
public class InsQueryHelperDataEntity {
    /**
     * 主键
     */
    private String id;
    /**
     * 数据id
     */
    private String dataId;
    /**
     * 批次id
     */
    private String batchId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
