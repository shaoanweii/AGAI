package com.voc.service.security.client.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AuthenticationRequest
 * @Description ckcui
 * @createTime 2023年09月25日 18:40
 * @Copyright futong
 */
@Data
@Builder
public class AuthenticationResponse implements Serializable {

    private Object user;
    private String token;
}

