package com.voc.service.logs.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0
 * @date 2021/6/16 17:45
 */
@Data
@Builder

public class MessageDTO implements Serializable {
    String appId;

    String username;

    String action;

    String type;

    Object data;

    String tid;

    String token;

    @Builder.Default
    Set<MessageExt> ext = new HashSet<>();
}
