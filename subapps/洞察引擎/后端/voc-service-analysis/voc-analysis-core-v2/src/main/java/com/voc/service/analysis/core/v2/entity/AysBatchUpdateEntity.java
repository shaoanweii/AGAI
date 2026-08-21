package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @Title: OrderTypeModel
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 15:47
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_flow_batch_update_record")
public class AysBatchUpdateEntity implements Serializable {
    private String id ;
    private String requestId;
    private String ids;
    private Object attrs;
    private Object filters;
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime;
    @Builder.Default
    private Integer type = 1;
    @Builder.Default
    private Integer status = 0;
}
