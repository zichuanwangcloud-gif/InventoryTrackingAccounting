# 多阶段构建 - 构建阶段
FROM gradle:8.5-jdk17 AS builder

# 设置工作目录
WORKDIR /app

# 复制构建文件
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# 下载依赖（利用Docker缓存层）
RUN gradle dependencies --no-daemon

# 复制源代码
COPY src/ src/

# 构建应用
RUN gradle build --no-daemon -x test

# 运行阶段
FROM openjdk:17-jre-slim

# 安装必要的包
RUN apt-get update && apt-get install -y     curl     && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/build/libs/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3     CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
