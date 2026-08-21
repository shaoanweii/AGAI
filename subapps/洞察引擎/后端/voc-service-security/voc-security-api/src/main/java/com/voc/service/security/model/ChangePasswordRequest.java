package com.voc.service.security.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest implements Serializable {
    private String type;
    private String appId;
    private String userId;
    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
}
