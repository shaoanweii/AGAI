package com.voc.service.security.impl;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.security.api.IPassService;
import lombok.Setter;
import org.passay.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName PassService
 * @createTime 2024年01月04日 9:41
 * @Copyright futong
 */
@Service
@ConfigurationProperties(prefix = "pass.policy")
public class PassService implements IPassService {

    @Setter
    Map<String, String> messages;

    private PasswordValidator passwordValidator;

    /**
     * 密码长度应在 8 到 16 个字符之间。
     * 密码不应包含任何空格。
     * 密码必须至少包含 1 个大写字符。
     * 密码必须至少包含 1 个小写字符。
     * 密码必须至少包含 1 位数字字符。
     * 密码必须至少包含 1 个符号（特殊字符）。
     * 拒绝包含按字母顺序排列的 >= 5 个字符序列的密码（例如 abcdef）。
     * 拒绝包含 >= 5 个字符的数字序列（例如 12345）的密码
     *
     * @return
     */
    @Override
    public List<String> passwordValidator(final String pass) {
        RuleResult ruleResult = passwordValidator.validate(new PasswordData(pass));
        List<String> messages = passwordValidator.getMessages(ruleResult);
//        String messageTemplate = String.join(",", messages);

        return messages;
    }

    @PostConstruct
    public void reset() {
        this.reset(null);
    }

    @Override
    public void reset(Set<String> rules) {
        if(CollUtil.isEmpty(messages)){
            throw new SecurityException("PropertiesMessage cannot be empty");
        }
        Properties props = new Properties();
        props.putAll(messages);
        MessageResolver resolver = new PropertiesMessageResolver(props);


        if (CollUtil.isNotEmpty(rules)) {
            //passwordValidator =
            //TODO ckcui 密码策略数据加载策略
            return;
        }

        passwordValidator = new PasswordValidator(resolver, Arrays.asList(
                // length between 8 and 16 characters
                new LengthRule(8, 38),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),
                // no whitespace
                new WhitespaceRule(),
                // rejects passwords that contain a sequence of >= 5 characters alphabetical  (e.g. abcdef)
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
                // rejects passwords that contain a sequence of >= 5 characters numerical   (e.g. 12345)
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false)
        ));
    }
}
