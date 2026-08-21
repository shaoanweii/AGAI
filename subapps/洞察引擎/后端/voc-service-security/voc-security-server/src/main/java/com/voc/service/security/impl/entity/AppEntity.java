package com.voc.service.security.impl.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_apps")
public class AppEntity implements Serializable {
    @NonNull
    private String id;
    @NonNull
    private String appId;

    private String note;
    private String urls;
}
