
#!/bin/bash


port=$1
if [ "$port" ]
then
  echo "启动端口："$port
else
  echo "需要指定端口"
  exit
fi

whoami=$(whoami)

sp_jar=$(ls voc-app-insights-1.0.0-SNAPSHOT-plain.jar)
echo "jar file : $sp_jar"
if [ "$sp_jar" ]
then
   rm $whoami-voc-app-insights-1.0.0-SNAPSHOT-plain.jar
fi

mv voc-app-insights-1.0.0-SNAPSHOT-plain.jar $whoami-voc-app-insights-1.0.0-SNAPSHOT-plain.jar

echo "查询是否存在已启动的进程"
sp_pid=$(ps -ef | grep $whoami-voc-app-insights-1.0.0-SNAPSHOT-plain.jar | grep -v grep | awk '{print $2}')
echo "curent pid : $sp_pid"
if [ "$sp_pid" ]
then
 echo "find pid result:$sp_pid"
 kill -9 $sp_pid
else
 echo "[not find ft pid]"
fi

echo "启动程序"

nohup /opt/apps/dragonwell17/bin/java \
-javaagent:/opt/apps/agent/skywalking-agent.jar \
-Dskywalking.collector.backend_service=172.16.80.22:32101 \
-Dskywalking.agent.service_name=voc-insights-service-$whoami \
-Dlogging.config=./logback-spring.xml \
-Dlog.path=./ \
-Dserver.port=$port \
-agentlib:jdwp=transport=dt_socket,address=*:50188,server=y,suspend=n \
-Dspring.profiles.active=local \
-jar $whoami-voc-app-insights-1.0.0-SNAPSHOT-plain.jar > info.log &

echo "show log..."
tail -f ./logs/info.log