package com.voc.service.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voc.service.common.util.ServiceContextHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse implements Serializable {
    @JsonProperty("username")
    @Schema(description = "登陆账号" , example = "admin")
    private String username;
    @JsonProperty("userid")
    @Schema(description = "登陆账号标识" , example = "1")
    private String userid;
    @JsonProperty("access_token")
    @Schema(description = "登陆账号TOKEN" , example = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMSIsImlkZW50aXR5X3R5cGUiOiJiYXNlIiwiYXBwX2lkIjoiaW5zaWdodHMiLCJ1c2VybmFtZSI6IklDeTMvdUhmMGJUMDdoUVFYaUFPd1hSUEY2cC9nbEJWd0NTWno2MUlhSC9LNUIvdDRzc29jeEI2dEpoUWhRZEYiLCJzdWIiOiIxIiwiaWF0IjoxNzA5NTQwNjc4LCJleHAiOjE3MTIxMzI2Nzh9.zMidSM2L7wD4NFWqwwjJ5XdjUT5jo7GwqeccSsYAt2c")
    private String accessToken;
    //    @JsonProperty("refresh_token")
    @JsonIgnore
    private String refreshToken;
    @Schema(description = "登陆账号标识" , example = "voc")
    private String appId;
    @Schema(description = "登陆账号标识" , example = "base")
    private String type;
}
