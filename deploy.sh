#!/usr/bin/env bash
# 在服务器上执行：一键装 Docker 并启动全部服务
set -e

if ! command -v docker >/dev/null 2>&1; then
  echo ">>> 未检测到 Docker，开始安装（国内服务器可用下方阿里云镜像加速）"
  # 官方脚本（如超时，注释掉下面这行，改用阿里云镜像源）
  curl -fsSL https://get.docker.com | sh
  # curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun
fi

echo ">>> 启动容器（首次会构建镜像，约 3~5 分钟）"
docker compose up -d --build

echo ">>> 当前容器状态："
docker compose ps

echo ">>> 部署完成，浏览器访问：http://<服务器公网IP>:8080"
