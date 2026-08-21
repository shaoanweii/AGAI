# 使用基础镜像
FROM 172.16.80.21:30002/library/busybox:latest

# 维护者信息
LABEL maintainer="ckcui@futongdf.com.cn"

# 设置工作目录
WORKDIR /tmp
# 复制文件到工作目录
COPY ./voc-app-insights-1.0.0-SNAPSHOT.jar /tmp

# 设置环境变量
ENV MY_ENV_VAR="value"

# 暴露容器端口
EXPOSE 8080

# 定义容器启动命令
CMD ["sleep", "120"]
