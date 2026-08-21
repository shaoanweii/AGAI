项目中使用：
<BR>
jdk17 + springboot3 + gradle8.13 + nacos2 + redis6 + mysql8<HR>


[voc-app](voc-app) 运行服务适配集合<BR>
[voc-service-common](voc-service-common) 通用服务组件<BR>
[voc-service-config](voc-service-config) 通用服务属性配置<BR>
[voc-service-components](voc-service-components)通用组件集合<BR>
[voc-service-security](voc-service-security) 安全服务集合<BR>
[voc-service-msgevent](voc-service-msgevent) 消息通知服务集合<BR>
[voc-service-bizlogs](voc-service-bizlogs) 业务日志服务集合<BR>
[voc-service-third](voc-service-third) 第三方服务集合<BR>
[voc-service-insights](voc-service-insights) 洞察引擎服务集合<BR>
[voc-service-template](voc-service-template) 样板间服务集合<BR>
[voc-service-analysis](voc-service-analysis) 数据清洗服务集合<BR>
<HR>
<B>本地开发说明：</B><BR>
直接运行开发任务任务即可。例如：[voc-stats-impl]或[voc-app-stats]内的application , profiles=local
<BR>

<B>开发环境调试说明：</B><BR>
<B>local:</B> 环境接口swagger访问地址：<BR>
http://127.0.0.1:[port]/doc.html <BR>
<B>dev:</B> 环境接口swagger访问地址：<BR>
http://172.16.80.16:30305/doc.html <BR>
<HR>
研发环境说明：https://futongdf.feishu.cn/wiki/Aa8rwgnJ4iRx4Ik1eW1cZoZfn7e
<BR>
研发境搭建说明：
https://futongdf.feishu.cn/wiki/XtX8waXVTisY04kdqEJcDchUnTf
<BR>
研发工具说明:
https://futongdf.feishu.cn/wiki/LzaJw9fOhihpggkzUPXcrkN4n0d
<HR>
<BR>

<B>接口访问流程</B><BR>
<B>步骤一：认证</B> 访问授权限管理内API接口时需先获取系统用户登录token，并加入到请求Header 中： Authorization: Bearer [access_token]<BR>
例如开发环境[dev]：基于表单email登录方式如下<BR>
http://172.16.80.16:30305/api/auth/login
<BR>
POST请求 application/json
<BR>
``` json
{
    "email":"zhangxiwen@gwm.cn",
    "password":"zhangxiwen@gwm.cn",
    "appId":"modeltraining",
    "type":"email",
    "checkKey":"123",
    "captcha":"2587"
}
```
响应：<BR>
``` json
{
    "success": true,
    "message": "操作成功！",
    "code": "200",
    "result": {
        "appId": "modeltraining",
        "type": "email",
        "username": "zhangxiwen@gwm.cn",
        "userid": "a80e07812fcb19f487c3e0ceef29bcf2",
        "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYTgwZTA3ODEyZmNiMTlmNDg3YzNlMGNlZWYyOWJjZjIiLCJpZGVudGl0eV90eXBlIjoiZW1haWwiLCJhcHBfaWQiOiJtb2RlbHRyYWluaW5nIiwidXNlcm5hbWUiOiJueStHQ25KLzZCdDdsME4vOFlPanJKTkYwc2R2Q25OU0RySTE3N25qcVNETy9JUFRJalorTUJLRW5ieHlsQ1lhd3R5dkhJVm1sNG9GeXJPM3pmc2cyQT09Iiwic3ViIjoiYTgwZTA3ODEyZmNiMTlmNDg3YzNlMGNlZWYyOWJjZjIiLCJpYXQiOjE3MDg0OTcxMzAsImV4cCI6MTcxMTA4OTEzMH0.RKmAhuy_QjWzwS7_oNhPspjf3uOXezMYw6c1Alkcq68"
    },
    "tid": "d09d2ecb9abd4c698f23250c8cbfda75.144.17084971300210587"
}
```
<B>步骤二：访问</B> API接口<BR>
curl http://172.16.80.16:30305/api/auth/user/enable -H "Authorization: Bearer [access_token]"

<HR>
