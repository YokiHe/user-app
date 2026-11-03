#!/usr/bin/env bash
# ============================================================
# 部署后一键验收：检查页面、登录、列表、退出四步
# 用法：
#   bash scripts/smoke-test.sh                          # 默认测本地
#   bash scripts/smoke-test.sh https://xxx.onrender.com # 测线上
# ============================================================
BASE="${1:-http://localhost:8080}"
CTX="/userapi"
API="$BASE$CTX/api/v1"
COOKIE="$(mktemp)"
PASS=0
FAIL=0

check() { # $1=名称 $2=实际输出 $3=期望关键字
  if echo "$2" | grep -q "$3"; then
    echo "  [通过] $1"
    PASS=$((PASS + 1))
  else
    echo "  [失败] $1  —— 实际输出：$(echo "$2" | head -c 200)"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== 1. 前端页面 ==="
HOME_HTML=$(curl -s -m 20 "$BASE/")
check "首页可访问且返回 Vue 容器" "$HOME_HTML" 'id="app"'

echo "=== 2. 未登录状态 ==="
ME_ANON=$(curl -s -m 20 "$API/auth/me")
check "未登录返回 401" "$ME_ANON" '401'

echo "=== 3. 登录（kylin / Kylin@123）==="
LOGIN=$(curl -s -m 20 -c "$COOKIE" -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"kylin","password":"Kylin@123"}')
check "登录返回 code 200" "$LOGIN" '"code":200'
check "返回昵称" "$LOGIN" 'kylin'

echo "=== 4. 带 Cookie 查用户列表 ==="
LIST=$(curl -s -m 20 -b "$COOKIE" "$API/users?page=1&size=5")
check "列表返回 code 200" "$LIST" '"code":200'
check "列表含 total 字段" "$LIST" '"total"'

echo "=== 5. 退出登录 ==="
LOGOUT=$(curl -s -m 20 -b "$COOKIE" -X POST "$API/auth/logout")
check "退出返回 code 200" "$LOGOUT" '"code":200'

rm -f "$COOKIE"
echo "============================================"
echo "通过 $PASS 项，失败 $FAIL 项"
[ "$FAIL" -eq 0 ] && echo "结论：部署成功 ✅" || echo "结论：存在失败项，请查看上方输出 ❌"
