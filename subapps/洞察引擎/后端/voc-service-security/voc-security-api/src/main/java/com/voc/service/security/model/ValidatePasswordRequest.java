package com.voc.service.security.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidatePasswordRequest implements Serializable {
    private String type;
    private String appId;

    private String userId;
    private String currentPassword;
}
