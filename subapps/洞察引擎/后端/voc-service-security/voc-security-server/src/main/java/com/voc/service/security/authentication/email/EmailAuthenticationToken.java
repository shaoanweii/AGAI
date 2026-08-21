package com.voc.service.security.authentication.email;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationToken
 * @createTime 2024年01月02日 17:21
 * @Copyright futong
 */

public class EmailAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public EmailAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
