package com.voc.service.security.authentication.freelogin;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationToken
 * @createTime 2024年01月02日 17:21
 * @Copyright futong
 */
public class FreeAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public FreeAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
