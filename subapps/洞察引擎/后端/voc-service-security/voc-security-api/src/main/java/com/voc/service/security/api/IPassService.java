package com.voc.service.security.api;

import java.util.List;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IPassService
 * @createTime 2024年01月04日 9:48
 * @Copyright futong
 */
public interface IPassService {

    List<String> passwordValidator(final String pass);

    void reset(Set<String> rules);
}
