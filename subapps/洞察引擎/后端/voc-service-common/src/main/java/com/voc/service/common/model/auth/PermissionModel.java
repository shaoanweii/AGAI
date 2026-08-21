package com.voc.service.common.model.auth;

import cn.hutool.core.util.StrUtil;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0.0
 * @ClassName PermissionModel.java
 * @Description
 * @createTime 2023年01月04日 15:05
 * @Copyright futong
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionModel implements Serializable {
    @Getter
    @Setter
    @Builder.Default
    Map<String, Object> values = new ConcurrentHashMap<>();

    public void setValues(@NonNull String key, @NonNull Object val) {
        values.put(key, val);
    }

    public <T> T getValue(@NonNull String key) {
        if(StrUtil.isBlank(key)){
            return null;
        }
        return (T) values.get(key);
    }

    public boolean isEmpty(){
        return values.isEmpty();
    }

    public void setEmpty(boolean v){}
}

