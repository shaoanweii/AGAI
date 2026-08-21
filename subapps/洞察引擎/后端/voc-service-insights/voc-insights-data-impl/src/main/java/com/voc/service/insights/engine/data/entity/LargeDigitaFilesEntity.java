package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: LargeDigitaFilesModel
 * @Package: com.voc.service.insights.engine.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/15 18:46
 * @Version:1.0
 */
@Builder
@Data
@TableName("sta_attachment_download_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class LargeDigitaFilesEntity implements Serializable {

    String id;
    String userId;
    String userName;
    String taskId;
    String taskName;
    String type;
    String status;
    String fileKey;
    String parameters;
    LocalDateTime createTime;
    String appId;
}
