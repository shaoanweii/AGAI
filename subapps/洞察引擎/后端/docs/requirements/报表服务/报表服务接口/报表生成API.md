# 报表生成API

## 基本信息
- **接口描述**：根据配置生成客户反馈分析报表，支持多种报表类型和格式
- **请求方式**：POST
- **接口地址**：/api/v1/report/generate
- **权限要求**：报表生成权限

## 请求参数

### Header
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer Token |
| Content-Type | string | 是 | application/json |

### Body
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| industryCode | string | 是 | 行业代码 |
| reportType | string | 是 | 报表类型(SUMMARY/DETAIL/TREND/CUSTOM) |
| reportName | string | 否 | 报表名称 |
| templateId | string | 否 | 报表模板ID |
| config | object | 否 | 报表配置 |
| &nbsp;&nbsp;dimensions | array | 否 | 维度字段 |
| &nbsp;&nbsp;metrics | array | 否 | 指标字段 |
| &nbsp;&nbsp;filters | object | 否 | 筛选条件 |
| &nbsp;&nbsp;&nbsp;&nbsp;dateRange | object | 否 | 日期范围 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;startDate | string | 否 | 开始日期 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;endDate | string | 否 | 结束日期 |
| &nbsp;&nbsp;&nbsp;&nbsp;otherFilters | object | 否 | 其他筛选条件 |
| &nbsp;&nbsp;groupBy | array | 否 | 分组字段 |
| &nbsp;&nbsp;orderBy | array | 否 | 排序字段 |
| &nbsp;&nbsp;chartTypes | array | 否 | 图表类型 |
| outputFormat | string | 否 | 输出格式(PDF/Excel/HTML/Word) |
| schedule | object | 否 | 定时配置 |
| &nbsp;&nbsp;enabled | boolean | 否 | 是否启用定时 |
| &nbsp;&nbsp;cronExpression | string | 否 | Cron表达式 |
| &nbsp;&nbsp;recipients | array | 否 | 接收人邮箱列表 |
| callbackUrl | string | 否 | 回调URL |

## 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 报表生成结果 |
| &nbsp;&nbsp;reportId | string | 报表ID |
| &nbsp;&nbsp;reportName | string | 报表名称 |
| &nbsp;&nbsp;status | string | 报表状态(PENDING/PROCESSING/COMPLETED/FAILED) |
| &nbsp;&nbsp;reportUrl | string | 报表访问地址 |
| &nbsp;&nbsp;downloadUrl | string | 报表下载地址 |
| &nbsp;&nbsp;estimatedCompletionTime | string | 预计完成时间 |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

## 示例

### 请求示例
```json
{
  "industryCode": "automotive",
  "reportType": "SUMMARY",
  "reportName": "2024年1月汽车客户反馈分析报告",
  "config": {
    "dimensions": ["brand_name", "product_name"],
    "metrics": ["sentiment_score", "satisfaction_score", "feedback_count"],
    "filters": {
      "dateRange": {
        "startDate": "2024-01-01",
        "endDate": "2024-01-31"
      }
    },
    "groupBy": ["brand_name"],
    "orderBy": [
      {
        "field": "satisfaction_score",
        "direction": "desc"
      }
    ],
    "chartTypes": ["bar", "line", "pie"]
  },
  "outputFormat": "PDF",
  "schedule": {
    "enabled": true,
    "cronExpression": "0 0 9 1 * ?",
    "recipients": ["manager@company.com", "analyst@company.com"]
  }
}
```

### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reportId": "REP202401010001",
    "reportName": "2024年1月汽车客户反馈分析报告",
    "status": "PROCESSING",
    "reportUrl": "/api/v1/reports/REP202401010001/view",
    "downloadUrl": "/api/v1/reports/REP202401010001/download",
    "estimatedCompletionTime": "2024-01-01T12:05:00Z"
  },
  "timestamp": "2024-01-01T12:00:05Z",
  "requestId": "req-rpt-2024010112000001"
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 报表生成任务已提交 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 报表模板不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务暂时不可用 |
| 50102 | 报表生成失败 |