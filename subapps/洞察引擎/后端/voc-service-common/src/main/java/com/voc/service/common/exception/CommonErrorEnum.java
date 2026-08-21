package com.voc.service.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum CommonErrorEnum implements ErrorCode {
    UNKNOW_EXECPTION(500, "系统内部错误请联系管理员处理"),
    FALLBACK_EXECPTION(502, "调用服务已降级"),
    REQUEST_EXECPTION(400, "请求参数错误"),
    ENUM_NAME_NOT_EXISTS(400, "枚举名称不存在"),
    NOT_AUTHORITY_EXECPTION(401, "没有访问权限"),
    NOT_AUTHTOKEN_EXECPTION(401, "访问令牌不合法"),
    NOTNULL_AUTH_TOKN(401, "没有携带Token信息"),
//    EXPIRED_JWT_EXECPTION(100000, "JWT登陆信息无效"),
    EXPIRED_JWT_EXECPTION(100000, "当前登陆会话已过期或失效"),
    SYSTEM_DATA_ERROR(100004, "数据异常，请联系管理员"),
    LOGIN_ACCOUNR_EXECPTION(100005, "账号不存在"),
    //    LOGIN_ACCOUNR_EXECPTION(100005, "账户不存在或已停用"),
    VERIFICATION_EXECPTION(100013, "验证码有误，请重新输入"),
    LOGIN_PASSWORD_EXECPTION(100006, "账号/密码输入有误，请重新输入"),
    ACCOUNT_DISABLE(100007, "账号已停用"),
    NOT_LOGIN_EXECPTION(100008, "用户未登录"),
    LOGIN_EXPERD_EXECPTION(100009, "登录信息过期"),
    TREE_PARENT_NULL_EXECPTION(100010, "树形结构根节点为空"),
    LOGIN_ENCRYPT_EXECPTION(100011, "登录密码解析错误"),
    LOGIN_USERNAME_ENCRYPT_EXECPTION(100011, "用户名解析错误"),
    CONSTRAINTVIOLATION_EXECPTION(100011, "{0}"),
    MISSINGSERVLETREQUESTPARANETER_EXECPTION(100012, "缺少参数"),
    HTTPMESSAGENOTREADABLE_EXECPTION(100013, "参数解析失败"),
    BIND_EXECPTION(100014, "参数绑定失败"),
    VALIDATION_EXECPTION(100015, "参数验证失败{0}"),
    HTTPREQUESTMETHODNOTSUPPORTED_EXECPTION(100016, "不支持当前请求方法"),
    HTTPMEDIATYPENOTSUPPORTED_EXECPTION(100017, "不支持当前媒体类型"),
    SERVICENOTFOUND_EXECPTION(100018, "请求方法缺失"),
    ACCOUNT_EXP(100019, "账号过期"),
    ACCOUNT_CLIENTID_NOTFOUND(100020, "校验时未获取到ClientId"),
    PWD_CONTAIN_SPECIAL(100021, "密码包含的特殊字符位数不符合要求"),
    PWD_CONTAIN_CAPITAL_ENGLISH(100022, "密码包含的大写英文位数不符合要求"),
    PWD_CONTAIN_LOWERCASE_ENGLISH(100023, "密码包含的小写英文位数不符合要求"),
    PWD_CONTAIN_NUMBER(100024, "密码包含的数字个数不符合要求"),
    PWD_EQUAL_USER_CHECK(100025, "密码和账号不能相同"),
    PWD_PASSWORD_VALIDITY(100026, "密码过期"),
    PWD_PASSWORD_LENGTH(100027, "密码长度不符合要求"),
    PWD_LOCK_THRESHOLD(100028, "密码输入次数过多被锁定"),
    PROHIBIT_DUPLICATE_SUBMISSION(100029, "请勿重复提交"),
    DECODE_ERROR(100030, "解密失败"),
    LOGIN_MUID_EXECPTION(100005, "管理者用户不存在{0}"),
    LOGIN_BMUID_EXECPTION(100005, "被管理者用户不存在{0}"),
    LOGIN_PASSWORD_NULL(100031, "用户未注册"),
    ADD_USER_ERROR(100032, "新增用户失败，请稍后重试"),
    USER_ACCOUNT_ERROR(100033, "用户名或密码错误，请重试"),
    USER_STATUS(100034, "用户已停用，请联系管理员"),
    TOKEN_ERROR(100035, "未识别token"),
    APP_ID_DISABLE(100036, "系统标识不存在"),
    LOGIN_TYPE_DISABLE(100037, "登陆模式标识不存在"),
    SMS_VERIFICATION_EXECPTION(100038, "短信认证失败"),
    SSO_LOGIN_EXECPTION(100039, "单点登陆失败"),
    ACCOUNT_EXISTS(100040, "用户授权信息已存在!"),
    USERINFO_EXECPTION(100041, "用户信息获取失败!"),
    CLIENT_DISABLED(100042, "用户所属客户已被禁用，请联系管理员!"),
    USER_NOT_EXIST(100043, "账号不存在"),
    ACCOUNT_LOCK(100044, "账号已锁定"),
    URL_ILLEGAL(100045, "URL非法"),
    NOT_BOUND_ROLE(100046, "当前登录用户未设置角色，请联系管理员"),
    NOT_BOUND_ACCESS_ROLE(100047, "当前登录用户没有系统访问权限，请联系管理员"),
    RATE_LIMIT_EXCEEDED(100048, "访问频率过高，请稍后重试"),
    ;


    private static List<CommonErrorEnum> list;

    static {
        list = new ArrayList<CommonErrorEnum>(Arrays.asList(values()));
    }

    /**
     * 业务异常编码
     */
    private Integer code;
    /**
     * 业务异常描述
     */
    private String message;

    public static List<CommonErrorEnum> getList() {
        return list;
    }

    public static CommonErrorEnum get(int code) {
        CommonErrorEnum r = null;
        List<CommonErrorEnum> list = getList();
        for (CommonErrorEnum l : list) {
            if (l.getCode().equals(code)) {
                r = l;
                break;
            }
        }
        return r;
    }
}
