#!/usr/bin/env bash
# ============================================================
# 把本地 userdb 数据导入 Aiven MySQL
# 用法（推荐）：
#   bash db/import-to-aiven.sh <Host> <Port> <Password>
# 例：
#   bash db/import-to-aiven.sh mysql-xxxx.aivencloud.com 13306 'Abc123456'
#
# 不带参数时使用下方默认值（自行填写后直接 bash db/import-to-aiven.sh）
# ============================================================
set -e

HOST="${1:-mysql-xxxxxxxxxxxx.aivencloud.com}"
PORT="${2:-00000}"
PASSWORD="${3:-在这里粘贴密码}"
USER_NAME="avnadmin"
DB_NAME="defaultdb"

MYSQL="/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
SQL_FILE="$(dirname "$0")/userdb.sql"

echo ">>> 目标：${USER_NAME}@${HOST}:${PORT}/${DB_NAME}"
echo ">>> 正在导入表结构与数据 ..."
"$MYSQL" -h "$HOST" -P "$PORT" -u "$USER_NAME" -p"$PASSWORD" --ssl-mode=REQUIRED "$DB_NAME" < "$SQL_FILE"

echo ">>> 导入完成，校验数据："
"$MYSQL" -h "$HOST" -P "$PORT" -u "$USER_NAME" -p"$PASSWORD" --ssl-mode=REQUIRED "$DB_NAME" \
  -e "SELECT id, username, nickname FROM tb_user;"
