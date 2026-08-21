package com.voc.service.components.sms.service;

import cn.hutool.core.util.RandomUtil;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.components.redis.service.RedisService;
import com.voc.service.components.sms.config.SMSPropertiesKuanYun;
import com.voc.service.components.sms.tool.KYSmsSendResponse;
import com.voc.service.components.sms.tool.SmsTemplate;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SmsUtilKuanYun implements SmsSendApi {
    private static final Logger log = LoggerFactory.getLogger(SmsUtilKuanYun.class);
    private final SMSPropertiesKuanYun smsProperties;
    @Autowired
    private RedisService redisCache;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public SmsUtilKuanYun(SMSPropertiesKuanYun properties) {
        this.smsProperties = properties;
    }

    /**
     * 随机生成 num位数字字符串
     *
     * @param num
     * @return
     */
    public static String generateRandomStr(int num) {
        return RandomUtil.randomString(num);
    }

    public static String MD5(String s) {
        if (s == null && "".equals(s)) {
            return null;
        }

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发送验证码
     *
     * @param phoneNumber
     * @return
     */
    @Override
    public Boolean sendCaptchaSms(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            String code = generateRandomStr(6);
            log.info("发生的验证码为:{}", code);
            KYSmsSendResponse response = sendSms(phoneNumber, SmsTemplate.VERITIFY_MESSAGE, code);
            log.info(String.format("【%s】给手机号码【%s】发送的验证码的结果为：%s", LocalDateTime.now(), phoneNumber, JsonMapper.getInstances().toJson(response)));
            if (response.getCode() == 0) {
                stringRedisTemplate.opsForValue().set("sms_" + phoneNumber, code, 5, TimeUnit.MINUTES);
            }
            return "0".equals(response.getCode());
        } else {
            return false;
        }
    }

    /**
     * 发送license
     *
     * @param phoneNumber
     * @param license
     * @return
     */
    @Override
    public Boolean sendLicenseSms(String phoneNumber, String license, String tenantCode, String validate) {
        if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(license)) {
            KYSmsSendResponse response = sendSms(phoneNumber, SmsTemplate.LICENSE_MESSAGE, license, tenantCode, validate);
            log.info(String.format("【%s】给手机号码【%s】发送的license密钥的结果为：%s", LocalDateTime.now(), phoneNumber, JsonMapper.getInstances().toJson(response)));
            return "0".equals(response.getCode());
        } else {
            return false;
        }
    }

    /**
     * 发送短信
     *
     * @param phone       接收的手机号
     * @param smsTemplate 短信模板实例
     * @param obj         短信模板实参可变数组
     * @return 运营商返回的发送结果
     */
    private KYSmsSendResponse sendSms(String phone, SmsTemplate smsTemplate, Object... obj) {
        String url = this.smsProperties.getBaseUrl() + this.smsProperties.getSmsSendUrl();
        String ts = String.valueOf(System.currentTimeMillis());
        String md5Content = this.smsProperties.getUserId() + ts + this.smsProperties.getAppKey();
        String md5 = MD5(md5Content);
//        String md5 = DigestUtils.md5DigestAsHex(md5Content.getBytes());
        Map<String, String> params = new ConcurrentHashMap();
        params.put("userid", this.smsProperties.getUserId());
        params.put("ts", ts);
        params.put("sign", md5.toLowerCase());
        params.put("mobile", phone);
        params.put("msgcontent", this.smsTemplateContent(smsTemplate, obj));
        OkHttpClient okClient = new OkHttpClient();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        params.forEach(formBodyBuilder::add);
        FormBody formBody = formBodyBuilder.build();
        Request request = new Request.Builder().post(formBody).url(url).build();
        try {
            Response response = okClient.newCall(request).execute();
            if (response.isSuccessful()) {
                KYSmsSendResponse smsSendResponse = JsonMapper.getInstances().fromJson(response.body().string(), KYSmsSendResponse.class);
                log.info(smsSendResponse.toString());
                return smsSendResponse;
            } else {
                //return KYSmsSendResponse.failResponse(ErrorCodeEnum.SMS_SEND_FAILED);
                return null;
            }
        } catch (IOException e) {
            log.error("宽云平台发送短信IO异常", e);
            return KYSmsSendResponse.fallbackResponse();
        }
    }

    private String smsTemplateContent(SmsTemplate smsTemplate, Object... obj) {
        String template = smsTemplate.getContent();
        return MessageFormat.format(template, obj);
    }

}

