package com.voc.service.security.authentication.phone;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationToken
 * @createTime 2024年01月02日 17:21
 * @Copyright futong
 */
public class PhoneAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public PhoneAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
