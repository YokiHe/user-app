#!/usr/bin/env bash
# ============================================================
# 把本地 userdb 数据导入 Aiven 免费 MySQL
# 用法：
#   1. 先在 Aiven 控制台拿到 Host / Port / 密码
#   2. 改下面 3 个变量（Host、Port、PASSWORD）
#   3. 在 Git Bash 里执行：bash db/import-to-aiven.sh
# ============================================================
set -e

HOST="mysql-xxxxxxxxxxxx.aivencloud.com"   # Aiven 控制台 Connection information 里的 Host
PORT="00000"                               # 端口，通常是 5 位数
PASSWORD="在这里粘贴 avnadmin 的密码"        # 密码

MYSQL="/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
SQL_FILE="$(dirname "$0")/userdb.sql"

echo ">>> 正在导入表结构与数据到 defaultdb ..."
"$MYSQL" -h "$HOST" -P "$PORT" -u avnadmin -p"$PASSWORD" --ssl-mode=REQUIRED defaultdb < "$SQL_FILE"

echo ">>> 导入完成，检查数据："
"$MYSQL" -h "$HOST" -P "$PORT" -u avnadmin -p"$PASSWORD" --ssl-mode=REQUIRED defaultdb \
  -e "SELECT id, username, nickname FROM tb_user;"
