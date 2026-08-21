package com.voc.service.components.sms.service;

import cn.hutool.core.util.RandomUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.components.redis.service.RedisService;
import com.voc.service.components.sms.config.SMSPropertiesALiYun;
import com.voc.service.components.sms.config.SendSmsCallBack;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Configuration
public class SmsUtilALiYun {
/*

    private static final Logger log = LoggerFactory.getLogger(SmsUtilALiYun.class);
    private final SMSPropertiesALiYun smsPropertiesALiYun;
    @Autowired
    private RedisService redisCache;

    public SmsUtilALiYun(SMSPropertiesALiYun properties) {
        this.smsPropertiesALiYun = properties;
    }

    */
/**
     * 随机生成 num位数字字符串
     *
     * @param num
     * @return
     *//*

    public static String generateRandomStr(int num) {
        return RandomUtil.randomString(num);
    }

    */
/**
     * 调用示例
     * SmsUtil.sendSmsThread("17777666777, new SendSmsCallBack() {
     *
     * @Override public void callBack(String result) {
     * SendSmsResponse response = new Gson().fromJson(result, SendSmsResponse.class);
     * }
     * });
     *//*


    */
/**
     * 发送验证码
     *
     * @param phoneNumber 手机号
     * @param callBack    回调函数，返回短信发送结果
     *//*

    public void sendCaptchaSms(String phoneNumber, SendSmsCallBack callBack) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            //定义Runnable子类对象
            MyThread my = new MyThread(phoneNumber, "", null, null, null, callBack);
            //开启线程
            new Thread(my, "sendVerifyCode").start();
        }
    }

    */
/**
     * 发送license
     *
     * @param phoneNumber 手机号
     * @param license     license
     * @param callBack    回调函数，返回短信发送结果
     *//*

    public void sendLicenseSms(String phoneNumber, String license, String tenantCode, String validate, SendSmsCallBack callBack) {
        if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(license)) {
            //定义Runnable子类对象
            MyThread my = new MyThread(phoneNumber, "", license, tenantCode, validate, callBack);
            //开启线程
            new Thread(my, "sendTenantLicense").start();
        }
    }

    */
/**
     * 发送验证码
     *
     * @param phoneNumber 手机号码
     * @return 发送结果
     *//*

    private SendSmsResponse sendVerifyCodeSms(String phoneNumber) throws ClientException {
        String code = generateRandomStr(6);
        SendSmsResponse response = sendSms(smsPropertiesALiYun.getVerifyCodeSigName(), phoneNumber, smsPropertiesALiYun.getVerifyCodeTemplateCode(), code);
        log.info(String.format("【%s】给手机号码【%s】发送的验证码的结果为：%s", LocalDateTime.now(), phoneNumber, JsonMapper.getInstances().toJson(response)));
        return response;
    }

    */
/**
     * 发送租户license密钥
     *
     * @param phoneNumber 手机号码
     * @param license     租户密钥信息
     * @return 发送结果
     *//*

    private SendSmsResponse sendTenantLicenseSms(String phoneNumber, String license) throws ClientException {
        SendSmsResponse response = sendSms(smsPropertiesALiYun.getLicenseSigName(), phoneNumber, smsPropertiesALiYun.getLicenseTemplateCode(), license);
        log.info(String.format("【%s】给手机号码【%s】发送的license密钥的结果为：%s", LocalDateTime.now(), phoneNumber, JsonMapper.getInstances().toJson(response)));
        return response;
    }

    private SendSmsResponse sendSms(String signName, String phoneNumber, String templateCode, String param) throws ClientException {
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsPropertiesALiYun.getAccessKeyId(), smsPropertiesALiYun.getAccessKeySecret());
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", smsPropertiesALiYun.getProduct(), smsPropertiesALiYun.getDomain());
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phoneNumber);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"name\":\"Tom\", \"code\":\"123\"}");
        String templateParam = "{\"value\":" + param + "}";
        request.setTemplateParam(templateParam);
        redisCache.set("sms_" + phoneNumber, param, 5L, TimeUnit.MINUTES);

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            log.info(String.format("【%s】给手机号码【%s】发送短信发生异常:ErrMsg=%s", LocalDateTime.now(), phoneNumber, e.getErrMsg()));
        }

        return sendSmsResponse;
    }

    class MyThread implements Runnable { //实现Runnable接口

        SendSmsCallBack callBack;//发送SMS后的回调
        String phoneNumbers; // 需要发送验证码的手机号码
        String bizId;
        String license;

        // 通过构造方法配置phoneNumbers属性
        public MyThread(String phoneNumbers, String bizId, String license, String tenantCode, String validate, SendSmsCallBack callBack) {
            this.phoneNumbers = phoneNumbers;
            this.bizId = bizId;
            this.license = license;
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                String threadName = Thread.currentThread().getName();
                if ("sendVerifyCode".equals(threadName)) {
                    SendSmsResponse response = sendVerifyCodeSms(phoneNumbers);
                    String jsonStr = JsonMapper.getInstances().toJson(response);
                    if (null != callBack) {
                        callBack.callBack(jsonStr);
                    }
                } else if ("sendTenantLicense".equals(threadName)) {
                    SendSmsResponse response = sendTenantLicenseSms(phoneNumbers, "");
                    String jsonStr = JsonMapper.getInstances().toJson(response);
                    if (null != callBack) {
                        callBack.callBack(jsonStr);
                    }
                } else {
                    if (null != callBack) {
                        callBack.callBack("Exception");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
*/

}
