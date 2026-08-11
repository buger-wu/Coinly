# ============================================================
# Coinly 后端 Dockerfile（多阶段构建）
# Stage 1: Maven 编译打包
# Stage 2: JRE 运行时
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY coinly-common/pom.xml coinly-common/
COPY coinly-business/pom.xml coinly-business/
COPY coinly-integration/pom.xml coinly-integration/
COPY coinly-system/pom.xml coinly-system/
RUN mvn dependency:go-offline -B

# 复制源码并编译
COPY . .
RUN mvn clean package -DskipTests -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 复制构建产物
COPY --from=builder /build/coinly-system/target/*.jar app.jar

# 时区设置
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.profiles.active=docker"]
