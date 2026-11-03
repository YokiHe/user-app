# ============================================
# 阶段一：构建后端 WAR（Maven + JDK 8）
# ============================================
FROM maven:3.8-openjdk-8 AS backend-build
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -B dependency:go-offline
COPY backend/src ./src
RUN mvn -B package -DskipTests

# ============================================
# 阶段二：构建前端静态文件（Node 22 + Vite）
# ============================================
FROM node:22-alpine AS frontend-build
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend .
RUN npm run build

# ============================================
# 阶段三：Tomcat 9 运行（前端为 ROOT，后端为 /userapi）
# ============================================
FROM tomcat:9-jdk8-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=backend-build /build/target/*.war /usr/local/tomcat/webapps/userapi.war
COPY --from=frontend-build /fe/dist/ /usr/local/tomcat/webapps/ROOT/

# Render 会注入 PORT 环境变量（默认 10000），启动前把 Tomcat 监听端口改为 $PORT
ENV PORT=10000
EXPOSE 10000
CMD ["sh", "-c", "sed -i s/port=\\\"8080\\\"/port=\\\"${PORT}\\\"/ /usr/local/tomcat/conf/server.xml && catalina.sh run"]
