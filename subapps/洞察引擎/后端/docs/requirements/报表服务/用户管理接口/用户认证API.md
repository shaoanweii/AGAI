# 用户认证API

## 基本信息
- **接口描述**：提供用户登录、登出、Token刷新等认证相关功能
- **请求方式**：POST/DELETE
- **接口地址**：/api/v1/auth
- **权限要求**：无（部分接口除外）

## 功能列表

### 1. 用户登录
- **请求方式**：POST
- **接口地址**：/api/v1/auth/login
- **功能说明**：用户登录系统，获取访问Token

### 2. 用户登出
- **请求方式**：DELETE
- **接口地址**：/api/v1/auth/logout
- **功能说明**：用户登出系统，使Token失效

### 3. 刷新Token
- **请求方式**：POST
- **接口地址**：/api/v1/auth/refresh
- **功能说明**：刷新访问Token

### 4. 获取用户信息
- **请求方式**：GET
- **接口地址**：/api/v1/auth/user
- **功能说明**：获取当前登录用户信息

## 请求参数

### Header
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 否 | Bearer Token（除登录外都需要） |
| Content-Type | string | 是 | application/json |

### 1. 用户登录Body参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| captcha | string | 否 | 验证码 |
| rememberMe | boolean | 否 | 记住我 |

### 2. 刷新TokenBody参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | string | 是 | 刷新Token |

## 响应参数

### 1. 用户登录响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 登录结果 |
| &nbsp;&nbsp;accessToken | string | 访问Token |
| &nbsp;&nbsp;refreshToken | string | 刷新Token |
| &nbsp;&nbsp;expiresIn | integer | Token过期时间（秒） |
| &nbsp;&nbsp;tokenType | string | Token类型 |
| &nbsp;&nbsp;user | object | 用户信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;userId | string | 用户ID |
| &nbsp;&nbsp;&nbsp;&nbsp;username | string | 用户名 |
| &nbsp;&nbsp;&nbsp;&nbsp;nickname | string | 用户昵称 |
| &nbsp;&nbsp;&nbsp;&nbsp;email | string | 邮箱 |
| &nbsp;&nbsp;&nbsp;&nbsp;avatar | string | 头像URL |
| &nbsp;&nbsp;&nbsp;&nbsp;roles | array | 角色列表 |
| &nbsp;&nbsp;&nbsp;&nbsp;permissions | array | 权限列表 |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

### 2. 刷新Token响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | Token信息 |
| &nbsp;&nbsp;accessToken | string | 新的访问Token |
| &nbsp;&nbsp;expiresIn | integer | Token过期时间（秒） |
| &nbsp;&nbsp;tokenType | string | Token类型 |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

### 3. 获取用户信息响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | integer | 响应码 |
| message | string | 响应消息 |
| data | object | 用户信息 |
| &nbsp;&nbsp;userId | string | 用户ID |
| &nbsp;&nbsp;username | string | 用户名 |
| &nbsp;&nbsp;nickname | string | 用户昵称 |
| &nbsp;&nbsp;email | string | 邮箱 |
| &nbsp;&nbsp;avatar | string | 头像URL |
| &nbsp;&nbsp;roles | array | 角色列表 |
| &nbsp;&nbsp;permissions | array | 权限列表 |
| &nbsp;&nbsp;lastLoginTime | string | 最后登录时间 |
| &nbsp;&nbsp;lastLoginIp | string | 最后登录IP |
| timestamp | string | 响应时间戳 |
| requestId | string | 请求ID |

## 示例

### 1. 用户登录请求示例
```http
POST /api/v1/auth/login HTTP/1.1
Content-Type: application/json

{
  "username": "admin",
  "password": "encrypted_password",
  "rememberMe": true
}
```

### 1. 用户登录响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "userId": "USER001",
      "username": "admin",
      "nickname": "系统管理员",
      "email": "admin@company.com",
      "avatar": "/avatars/admin.jpg",
      "roles": ["ADMIN", "ANALYST"],
      "permissions": ["user:manage", "report:view", "config:edit"]
    }
  },
  "timestamp": "2024-01-01T12:00:05Z",
  "requestId": "req-auth-2024010112000001"
}
```

### 2. 刷新Token请求示例
```http
POST /api/v1/auth/refresh HTTP/1.1
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

### 2. 刷新Token响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "new_access_token...",
    "expiresIn": 7200,
    "tokenType": "Bearer"
  },
  "timestamp": "2024-01-01T14:00:05Z",
  "requestId": "req-auth-2024010114000001"
}
```

### 3. 获取用户信息请求示例
```http
GET /api/v1/auth/user HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. 获取用户信息响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "USER001",
    "username": "admin",
    "nickname": "系统管理员",
    "email": "admin@company.com",
    "avatar": "/avatars/admin.jpg",
    "roles": ["ADMIN", "ANALYST"],
    "permissions": ["user:manage", "report:view", "config:edit"],
    "lastLoginTime": "2024-01-01T12:00:05Z",
    "lastLoginIp": "192.168.1.100"
  },
  "timestamp": "2024-01-01T14:05:05Z",
  "requestId": "req-auth-2024010114050001"
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 用户名或密码错误 |
| 403 | 账户已被禁用 |
| 429 | 登录尝试过于频繁 |
| 500 | 服务器内部错误 |
| 10101 | 认证失败 |
| 10103 | Token已过期 |
| 10104 | Token无效 |