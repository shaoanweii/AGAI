package com.voc.service.security.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.voc.service.common.model.UserModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TokenModel implements Serializable {

    //  @Column(unique = true)
    public String token;
    public String tid;

    @Builder.Default
    public TokenType tokenType = TokenType.BEARER;

    public boolean revoked;

    public boolean expired;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime expirationTime;

    //  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "user_id")
    public UserModel user;

}
