#!/usr/bin/env bash
set -euo pipefail

BASE=${1:-http://localhost:8080}
START_DATE=${2:-2025-01-01}
END_DATE=${3:-2025-08-01}

LOGIN_JSON=$(cat <<JSON
{
  "username": "FIrr1sFJXuddntYxB0zv+w==",
  "password": "0QIkzmttXTV12GmFAnq5kg==",
  "checkKey": "1755856508177",
  "captcha": "2587"
}
JSON
)

TOKEN=$(curl -s -X POST "$BASE/base/login" -H 'Content-Type: application/json' -d "$LOGIN_JSON" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
  echo "[ERROR] 登录失败，未获取到 token"
  curl -s -X POST "$BASE/base/login" -H 'Content-Type: application/json' -d "$LOGIN_JSON" -i | sed -n '1,200p'
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
  echo -e "\n== ${name} (${path}) =="
  curl -s -X POST "$BASE$path" \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d "$DATA" -i | sed -n '1,120p'
}

call "综合分析简报" "/product-analysis/getProductBrief"
call "数据趋势变化" "/product-analysis/getDataTrendChange"
call "关注场景TOP" "/product-analysis/getFocusSceneTop"
call "关注场景分析" "/product-analysis/getFocusSceneAnalysis"
call "用户意图观点TOP" "/product-analysis/getUserIntentionOpinionTop"
call "渠道提及量占比" "/product-analysis/getChannelMentionShare"
call "渠道负面率趋势变化" "/product-analysis/getChannelNegativeTrend"
call "数据来源分析" "/product-analysis/getDataSourceAnalysis"

