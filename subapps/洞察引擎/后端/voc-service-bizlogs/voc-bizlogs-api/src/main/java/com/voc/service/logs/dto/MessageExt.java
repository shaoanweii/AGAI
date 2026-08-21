package com.voc.service.logs.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0
 * @date 2021/6/16 17:46
 */
@Data
@Builder
@EqualsAndHashCode(of = "key")
public class MessageExt implements Serializable {
    String key;

    Object value;
}
