# 数据查询API

## 基本信息
- **接口描述**：基于物化视图查询客户反馈分析数据，支持多维度筛选和统计
- **请求方式**：POST
- **接口地址**：/api/v1/report/query
- **权限要求**：报表查询权限

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
| dimensions | array | 否 | 维度字段列表 |
| metrics | array | 否 | 指标字段列表 |
| filters | object | 否 | 筛选条件 |
| &nbsp;&nbsp;dateRange | object | 否 | 日期范围 |
| &nbsp;&nbsp;&nbsp;&nbsp;startDate | string | 否 | 开始日期 |
| &nbsp;&nbsp;&nbsp;&nbsp;endDate | string | 否 | 结束日期 |
| &nbsp;&nbsp;channel | string | 否 | 渠道筛选 |
| &nbsp;&nbsp;brand | string | 否 | 品牌筛选 |
| &nbsp;&nbsp;product | string | 否 | 产品筛选 |
| &nbsp;&nbsp;sentiment | string | 否 | 情感筛选 |
| &nbsp;&nbsp;intention | string | 否 | 意图筛选 |
| &nbsp;&nbsp;topic | string | 否 | 主题筛选 |
| &nbsp;&nbsp;province | string | 否 | 省份筛选 |
| groupBy | array | 否 | 分组字段 |
| orderBy | array | 否 | 排序字段 |
| &nbsp;&nbsp;field | string | 否 | 排序字段名 |
| &nbsp;&nbsp;direction | string | 否 | 排序方向(asc/desc) |
| limit | integer | 否 | 返回记录数限制 |
| offset | integer | 否 | 偏移量 |

## 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 查询结果 |
| &nbsp;&nbsp;records | array | 查询记录列表 |
| &nbsp;&nbsp;&nbsp;&nbsp;dataCreateTime | string | 数据创建时间 |
| &nbsp;&nbsp;&nbsp;&nbsp;industryCode | string | 行业代码 |
| &nbsp;&nbsp;&nbsp;&nbsp;channelCategory | string | 渠道分类 |
| &nbsp;&nbsp;&nbsp;&nbsp;channelName | string | 渠道名称 |
| &nbsp;&nbsp;&nbsp;&nbsp;brandName | string | 品牌名称 |
| &nbsp;&nbsp;&nbsp;&nbsp;productName | string | 产品名称 |
| &nbsp;&nbsp;&nbsp;&nbsp;sentiment | string | 情感分析结果 |
| &nbsp;&nbsp;&nbsp;&nbsp;sentimentScore | number | 情感分析置信度分数 |
| &nbsp;&nbsp;&nbsp;&nbsp;intention | string | 意图识别结果 |
| &nbsp;&nbsp;&nbsp;&nbsp;intentionScore | number | 意图识别置信度分数 |
| &nbsp;&nbsp;&nbsp;&nbsp;topic | string | 主题分类 |
| &nbsp;&nbsp;&nbsp;&nbsp;topicScore | number | 主题分类置信度分数 |
| &nbsp;&nbsp;&nbsp;&nbsp;custProvince | string | 客户省份 |
| &nbsp;&nbsp;&nbsp;&nbsp;custCity | string | 客户城市 |
| &nbsp;&nbsp;&nbsp;&nbsp;dealerName | string | 经销商/门店名称 |
| &nbsp;&nbsp;&nbsp;&nbsp;urgencyLevel | string | 紧急程度 |
| &nbsp;&nbsp;&nbsp;&nbsp;emotionType | string | 情绪类型 |
| &nbsp;&nbsp;&nbsp;&nbsp;satisfactionScore | number | 满意度评分 |
| &nbsp;&nbsp;&nbsp;&nbsp;npsScore | integer | NPS净推荐值 |
| &nbsp;&nbsp;&nbsp;&nbsp;qualityIssueFlag | boolean | 质量问题标记 |
| &nbsp;&nbsp;&nbsp;&nbsp;serviceIssueFlag | boolean | 服务问题标记 |
| &nbsp;&nbsp;&nbsp;&nbsp;priceIssueFlag | boolean | 价格问题标记 |
| &nbsp;&nbsp;&nbsp;&nbsp;deliveryIssueFlag | boolean | 交付问题标记 |
| &nbsp;&nbsp;&nbsp;&nbsp;promotionIssueFlag | boolean | 推广问题标记 |
| &nbsp;&nbsp;&nbsp;&nbsp;contentSummary | string | 内容摘要 |
| &nbsp;&nbsp;&nbsp;&nbsp;viewCount | string | 浏览数 |
| &nbsp;&nbsp;&nbsp;&nbsp;commentCount | string | 评论数 |
| &nbsp;&nbsp;&nbsp;&nbsp;likeCount | string | 点赞数 |
| &nbsp;&nbsp;&nbsp;&nbsp;workOrderStatus | string | 工单状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;customerJourneyStage | string | 客户生命旅程阶段 |
| &nbsp;&nbsp;&nbsp;&nbsp;customerSegment | string | 客户细分 |
| &nbsp;&nbsp;&nbsp;&nbsp;isVip | boolean | 是否VIP客户 |
| &nbsp;&nbsp;&nbsp;&nbsp;isNewCustomer | boolean | 是否新客户 |
| &nbsp;&nbsp;&nbsp;&nbsp;totalSpent | number | 累计消费金额 |
| &nbsp;&nbsp;&nbsp;&nbsp;dealerType | string | 经销商/门店类型 |
| &nbsp;&nbsp;&nbsp;&nbsp;dealerLevel | string | 经销商/门店等级 |
| &nbsp;&nbsp;&nbsp;&nbsp;socialPlatform | string | 社交平台 |
| &nbsp;&nbsp;totalCount | integer | 总记录数 |
| &nbsp;&nbsp;pageSize | integer | 每页记录数 |
| &nbsp;&nbsp;currentPage | integer | 当前页码 |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

## 示例

### 请求示例
```json
{
  "industryCode": "automotive",
  "dimensions": ["brand_name", "product_name"],
  "metrics": ["sentiment_score", "satisfaction_score"],
  "filters": {
    "dateRange": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "sentiment": "负面"
  },
  "groupBy": ["brand_name", "product_name"],
  "orderBy": [
    {
      "field": "sentiment_score",
      "direction": "asc"
    }
  ],
  "limit": 100,
  "offset": 0
}
```

### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "dataCreateTime": "2024-01-15",
        "industryCode": "automotive",
        "channelCategory": "社交媒体",
        "channelName": "微博",
        "brandName": "大众",
        "productName": "朗逸",
        "sentiment": "负面",
        "sentimentScore": 0.25,
        "intention": "投诉",
        "intentionScore": 0.92,
        "topic": "售后服务",
        "topicScore": 0.88,
        "custProvince": "广东",
        "custCity": "深圳",
        "dealerName": "深圳大众4S店",
        "urgencyLevel": "紧急",
        "emotionType": "愤怒",
        "satisfactionScore": 0.3,
        "npsScore": 2,
        "qualityIssueFlag": false,
        "serviceIssueFlag": true,
        "priceIssueFlag": false,
        "deliveryIssueFlag": false,
        "promotionIssueFlag": false,
        "contentSummary": "售后服务态度差，维修时间长",
        "viewCount": "125",
        "commentCount": "36",
        "likeCount": "5",
        "workOrderStatus": "处理中",
        "customerJourneyStage": "售后服务阶段",
        "customerSegment": "忠诚客户",
        "isVip": true,
        "isNewCustomer": false,
        "totalSpent": 185000.00,
        "dealerType": "4S店",
        "dealerLevel": "金牌",
        "socialPlatform": "微博"
      }
    ],
    "totalCount": 1,
    "pageSize": 100,
    "currentPage": 1
  },
  "timestamp": "2024-01-01T12:00:05Z",
  "requestId": "req-rpt-2024010112000001"
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 查询成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务暂时不可用 |