# 健康检查API

## 基本信息
- **接口描述**：提供系统健康状态检查功能，用于监控系统运行状态
- **请求方式**：GET
- **接口地址**：/api/v1/health
- **权限要求**：系统管理员权限或无需权限（根据配置）

## 功能列表

### 1. 系统健康检查
- **请求方式**：GET
- **接口地址**：/api/v1/health
- **功能说明**：检查系统整体健康状态

### 2. 详细健康检查
- **请求方式**：GET
- **接口地址**：/api/v1/health/detail
- **功能说明**：获取系统各组件详细健康状态

### 3. 就绪检查
- **请求方式**：GET
- **接口地址**：/api/v1/health/ready
- **功能说明**：检查系统是否就绪，可以处理请求

### 4. 存活检查
- **请求方式**：GET
- **接口地址**：/api/v1/health/live
- **功能说明**：检查应用进程是否存活

## 请求参数

### Header
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 否 | Bearer Token（根据配置可能需要） |

### Query参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| detail | boolean | 否 | 是否返回详细信息 |

## 响应参数

### 1. 基本健康检查响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 健康状态 |
| &nbsp;&nbsp;status | string | 系统状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;timestamp | string | 检查时间 |
| requestId | string | 请求ID |

### 2. 详细健康检查响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 详细健康状态 |
| &nbsp;&nbsp;status | string | 系统总体状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;timestamp | string | 检查时间 |
| &nbsp;&nbsp;components | object | 各组件状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;database | object | 数据库状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status | string | 状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;details | object | 详细信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;cache | object | 缓存状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status | string | 状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;details | object | 详细信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;diskSpace | object | 磁盘空间状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status | string | 状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;details | object | 详细信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;aiModel | object | AI模型状态 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status | string | 状态(UP/DOWN/UNKNOWN) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;details | object | 详细信息 |
| requestId | string | 请求ID |

## 示例

### 1. 基本健康检查请求示例
```http
GET /api/v1/health HTTP/1.1
```

### 1. 基本健康检查响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:05Z"
  },
  "requestId": "req-health-2024010112000001"
}
```

### 2. 详细健康检查请求示例
```http
GET /api/v1/health/detail HTTP/1.1
```

### 2. 详细健康检查响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:05Z",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "database": "PostgreSQL",
          "version": "14.5",
          "connections": {
            "active": 15,
            "idle": 5,
            "max": 50
          }
        }
      },
      "cache": {
        "status": "UP",
        "details": {
          "type": "Redis",
          "version": "6.2.7",
          "mode": "standalone",
          "uptime": "7 days"
        }
      },
      "diskSpace": {
        "status": "UP",
        "details": {
          "total": "100GB",
          "free": "60GB",
          "threshold": "10GB"
        }
      },
      "aiModel": {
        "status": "UP",
        "details": {
          "modelName": "voc-analysis-model",
          "version": "2.1.0",
          "lastUpdated": "2024-01-01T00:00:00Z"
        }
      }
    }
  },
  "requestId": "req-health-2024010112000002"
}
```

### 3. 就绪检查请求示例
```http
GET /api/v1/health/ready HTTP/1.1
```

### 3. 就绪检查响应示例
```json
{
  "code": 200,
  "message": "系统已就绪",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:05Z"
  },
  "requestId": "req-health-2024010112000003"
}
```

### 4. 存活检查请求示例
```http
GET /api/v1/health/live HTTP/1.1
```

### 4. 存活检查响应示例
```json
{
  "code": 200,
  "message": "应用进程存活",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:05Z"
  },
  "requestId": "req-health-2024010112000004"
}
```

## 状态说明

### 系统状态
| 状态 | 说明 |
|------|------|
| UP | 系统正常运行 |
| DOWN | 系统存在故障 |
| UNKNOWN | 系统状态未知 |

### 组件状态
| 状态 | 说明 |
|------|------|
| UP | 组件正常运行 |
| DOWN | 组件存在故障 |
| UNKNOWN | 组件状态未知 |
| OUT_OF_SERVICE | 组件停止服务 |

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 系统正常 |
| 503 | 系统异常 |
| 500 | 服务器内部错误 |