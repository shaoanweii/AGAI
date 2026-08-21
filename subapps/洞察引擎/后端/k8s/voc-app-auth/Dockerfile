FROM gaia.changan.com.cn/changan/yinxiao-docker/snapshot/openjdk:jdk17-sw-ag-9.1.0
WORKDIR /voc-app
COPY voc-app/voc-app-auth/build/libs/voc-app-auth*.jar /voc-app/voc-app-auth.jar
ENV JAVA_OPTS=""
EXPOSE 8080
CMD java ${JAVA_OPTS} -jar voc-app-auth.jar
