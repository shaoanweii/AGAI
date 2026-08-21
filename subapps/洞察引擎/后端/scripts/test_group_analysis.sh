#!/usr/bin/env bash
set -euo pipefail

BASE=${1:-http://localhost:8080}
START_DATE=${2:-2025-01-01}
END_DATE=${3:-2025-08-01}

LOGIN_PAYLOAD='{"username":"FIrr1sFJXuddntYxB0zv+w==","password":"0QIkzmttXTV12GmFAnq5kg==","checkKey":"1755856508177","captcha":"2587"}'

# 登录获取 token
LOGIN_JSON=$(curl -s -X POST "$BASE/base/login" -H 'Content-Type: application/json' -d "$LOGIN_PAYLOAD")
export LOGIN_JSON
TOKEN=$(python3 - <<'PY'
import os, json
try:
    d=json.loads(os.environ['LOGIN_JSON'])
    print(d.get('result',{}).get('access_token',''))
except Exception as e:
    print('')
PY
)

if [ -z "$TOKEN" ]; then
  echo "[ERROR] 登录失败，未获取到 token。响应如下："
  echo "$LOGIN_JSON"
  exit 1
fi

echo "token_len=${#TOKEN}"
DATA=$(cat <<JSON
{"startDate":"${START_DATE}","endDate":"${END_DATE}"}
JSON
)

echo "将以统一入参调用：$DATA"

call(){
  local name="$1" path="$2"
  echo "\n== ${name} (${path}) =="
  curl -s -X POST "$BASE$path" \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d "$DATA" -i | sed -n '1,100p'
}

call "综合分析简报" "/group-analysis/getProductBrief"
call "品牌趋势变化" "/group-analysis/get-brand-trend-change"
call "集团车系排行" "/group-analysis/get-series-rank"
call "服务口碑分析" "/group-analysis/get-service-reputation-analysis"
call "产品分析" "/group-analysis/get-product-tag-analysis"
call "观点评价" "/group-analysis/get-opinion-evaluation"
call "数据来源" "/group-analysis/get-data-source-analysis"

