核心服务<BR>
[voc-app-auth](voc-app-auth) 核心安全服务（认证+用户信息获取等）
<BR>
[voc-app-insights](voc-app-insights) 洞察引擎服务
<BR>
[voc-app-template](voc-app-template) 样板间服务
<BR>
[voc-app-logs](voc-app-logs) 日志组件服务
<BR>
[voc-app-msg](voc-app-msg) 消息通知服务
<BR>
[voc-app-model](voc-app-model) 模型计算服务
<BR>

<HR>
local环境 -消息通知服务举例 <BR>
<B>#vm options </B><BR>
-javaagent:agent/skywalking-agent.jar  <BR>
-Dskywalking.collector.backend_service=172.16.80.16:30122  <BR>
-Dskywalking.agent.service_name=app-msg <BR>
