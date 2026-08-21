package com.voc.service.security.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistroyModel implements Serializable {

    private String id;
    @NonNull
    private String appId;
    @NonNull
    private String credentialId;
    @NonNull
    private String userId;
    @NonNull
    private String loginType;
    @NonNull
    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime loginTime = LocalDateTime.now();

    private String tid;
}
