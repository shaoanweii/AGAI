package com.voc.service.insights.engine.api.model;

import com.voc.service.insights.engine.api.constants.EnableStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Title: TaskPartModel
 * @Package: com.voc.service.insights.engine.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/15 18:53
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPartModel implements Serializable {
    String taskId;
    String parentTaskId;
    String parentFileKey;
    @Builder.Default
    String status = EnableStatusEnum.DISABLED.getCode();
    String fileKey;

    Object paramModel;
    int pageNumber;
    int pageSize;
    int sort;
}
