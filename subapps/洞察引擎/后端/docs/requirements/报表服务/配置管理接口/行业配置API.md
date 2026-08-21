# 行业配置API

## 基本信息
- **接口描述**：管理系统支持的行业配置信息，包括行业基本信息和特定字段配置
- **请求方式**：GET/POST/PUT/DELETE
- **接口地址**：/api/v1/config/industries
- **权限要求**：配置管理权限

## 功能列表

### 1. 获取行业列表
- **请求方式**：GET
- **接口地址**：/api/v1/config/industries
- **功能说明**：获取所有支持的行业配置列表

### 2. 获取行业详情
- **请求方式**：GET
- **接口地址**：/api/v1/config/industries/{industryCode}
- **功能说明**：获取特定行业的详细配置信息

### 3. 创建行业配置
- **请求方式**：POST
- **接口地址**：/api/v1/config/industries
- **功能说明**：创建新的行业配置

### 4. 更新行业配置
- **请求方式**：PUT
- **接口地址**：/api/v1/config/industries/{industryCode}
- **功能说明**：更新现有行业配置

### 5. 删除行业配置
- **请求方式**：DELETE
- **接口地址**：/api/v1/config/industries/{industryCode}
- **功能说明**：删除行业配置

## 请求参数

### Header
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer Token |
| Content-Type | string | 是 | application/json |

### 查询参数（GET /api/v1/config/industries）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页记录数 |
| keyword | string | 否 | 搜索关键字 |

### 路径参数（GET/PUT/DELETE /api/v1/config/industries/{industryCode}）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| industryCode | string | 是 | 行业代码 |

### Body参数（POST/PUT）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| industryCode | string | 是 | 行业代码 |
| industryName | string | 是 | 行业名称 |
| description | string | 否 | 行业描述 |
| status | string | 否 | 状态(ENABLED/DISABLED) |
| specificFields | array | 否 | 行业特定字段配置 |
| &nbsp;&nbsp;fieldName | string | 是 | 字段名称 |
| &nbsp;&nbsp;fieldType | string | 是 | 字段类型 |
| &nbsp;&nbsp;description | string | 否 | 字段描述 |
| &nbsp;&nbsp;required | boolean | 否 | 是否必填 |
| &nbsp;&nbsp;defaultValue | string | 否 | 默认值 |
| &nbsp;&nbsp;validationRule | string | 否 | 验证规则 |

## 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 行业配置信息 |
| &nbsp;&nbsp;industryCode | string | 行业代码 |
| &nbsp;&nbsp;industryName | string | 行业名称 |
| &nbsp;&nbsp;description | string | 行业描述 |
| &nbsp;&nbsp;status | string | 状态 |
| &nbsp;&nbsp;createTime | string | 创建时间 |
| &nbsp;&nbsp;updateTime | string | 更新时间 |
| &nbsp;&nbsp;specificFields | array | 行业特定字段配置 |
| &nbsp;&nbsp;&nbsp;&nbsp;fieldName | string | 字段名称 |
| &nbsp;&nbsp;&nbsp;&nbsp;fieldType | string | 字段类型 |
| &nbsp;&nbsp;&nbsp;&nbsp;description | string | 字段描述 |
| &nbsp;&nbsp;&nbsp;&nbsp;required | boolean | 是否必填 |
| &nbsp;&nbsp;&nbsp;&nbsp;defaultValue | string | 默认值 |
| &nbsp;&nbsp;&nbsp;&nbsp;validationRule | string | 验证规则 |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

## 示例

### 1. 获取行业列表请求示例
```http
GET /api/v1/config/industries?page=1&size=10 HTTP/1.1
Authorization: Bearer xxx
```

### 1. 获取行业列表响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "industryCode": "automotive",
      "industryName": "汽车行业",
      "description": "汽车行业客户反馈分析",
      "status": "ENABLED",
      "createTime": "2024-01-01T00:00:00Z",
      "updateTime": "2024-01-01T00:00:00Z",
      "specificFields": [
        {
          "fieldName": "vehicle_info",
          "fieldType": "object",
          "description": "车辆信息",
          "required": false,
          "defaultValue": null,
          "validationRule": null
        }
      ]
    },
    {
      "industryCode": "starbucks",
      "industryName": "星巴克",
      "description": "星巴克客户反馈分析",
      "status": "ENABLED",
      "createTime": "2024-01-01T00:00:00Z",
      "updateTime": "2024-01-01T00:00:00Z",
      "specificFields": []
    }
  ],
  "timestamp": "2024-01-01T12:00:05Z",
  "requestId": "req-cfg-2024010112000001"
}
```

### 2. 创建行业配置请求示例
```http
POST /api/v1/config/industries HTTP/1.1
Authorization: Bearer xxx
Content-Type: application/json

{
  "industryCode": "beauty",
  "industryName": "美妆行业",
  "description": "美妆行业客户反馈分析",
  "status": "ENABLED",
  "specificFields": [
    {
      "fieldName": "effect_duration",
      "fieldType": "string",
      "description": "产品效果持续时间",
      "required": false
    }
  ]
}
```

### 2. 创建行业配置响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "industryCode": "beauty",
    "industryName": "美妆行业",
    "description": "美妆行业客户反馈分析",
    "status": "ENABLED",
    "createTime": "2024-01-01T12:00:05Z",
    "updateTime": "2024-01-01T12:00:05Z",
    "specificFields": [
      {
        "fieldName": "effect_duration",
        "fieldType": "string",
        "description": "产品效果持续时间",
        "required": false,
        "defaultValue": null,
        "validationRule": null
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:05Z",
  "requestId": "req-cfg-2024010112000002"
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 行业配置不存在 |
| 409 | 行业代码已存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务暂时不可用 |