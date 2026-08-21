# 账号管理API接口文档

## 1. 接口概述

账号管理服务提供完整的RESTful API接口，支持账号的增删改查、角色管理、部门管理等功能。所有接口均需要Bearer Token认证。

### 1.1 基础信息
- **服务路径**: `/accountInfo`
- **认证方式**: Bearer Token
- **响应格式**: JSON
- **字符编码**: UTF-8

### 1.2 通用响应格式
```json
{
  "code": "200",
  "message": "操作成功",
  "result": {},
  "timestamp": 1640995200000
}
```

## 2. 账号管理接口

### 2.1 新增账号信息

**接口地址**: `POST /accountInfo/saveAccountInfo`

**接口描述**: 创建新的用户账号

**请求头**:
```
Authorization: Bearer [token]
Content-Type: application/json
```

**请求参数**:
```json
{
  "accountName": "zhangsan",
  "accountPwd": "password123",
  "userName": "张三",
  "employeeId": "EMP001",
  "deptId": ["dept001", "dept002"],
  "roleId": "role001",
  "contact": "13800138000",
  "position": "工程师",
  "email": "zhangsan@changan.com.cn",
  "remark": "备注信息",
  "status": "1",
  "clientId": "1",
  "loginType": "base"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountName | String | 是 | 账号名称 |
| accountPwd | String | 是 | 账号密码 |
| userName | String | 是 | 用户名 |
| employeeId | String | 否 | 员工编号 |
| deptId | List<String> | 否 | 部门ID列表 |
| roleId | String | 否 | 角色ID |
| contact | String | 否 | 联系方式 |
| position | String | 否 | 职位 |
| email | String | 否 | 邮箱 |
| remark | String | 否 | 备注 |
| status | String | 否 | 状态(0:停用 1:启用) |
| clientId | String | 是 | 客户ID |
| loginType | String | 否 | 登录类型(base:表单 email:邮箱) |

**响应示例**:
```json
{
  "code": "200",
  "message": "新增成功",
  "result": null,
  "timestamp": 1640995200000
}
```

### 2.2 更新账号信息

**接口地址**: `POST /accountInfo/updateAccountInfo`

**接口描述**: 更新用户账号信息

**请求参数**: 同新增接口，需要包含userId字段

**响应示例**:
```json
{
  "code": "200",
  "message": "更新成功",
  "result": null,
  "timestamp": 1640995200000
}
```

### 2.3 查询账号信息

**接口地址**: `POST /accountInfo/findAccountInfo`

**接口描述**: 根据用户ID查询账号详细信息

**请求参数**:
```json
{
  "userId": "user001"
}
```

**响应示例**:
```json
{
  "code": "200",
  "message": "查询成功",
  "result": {
    "userId": "user001",
    "accountName": "zhangsan",
    "userName": "张三",
    "employeeId": "EMP001",
    "deptName": "技术部",
    "deptId": "dept001",
    "roleName": "工程师",
    "roleId": "role001",
    "status": "1",
    "loginCounts": 15,
    "lastLoginTime": "2024-01-01 10:30:00",
    "contact": "13800138000",
    "position": "高级工程师",
    "email": "zhangsan@changan.com.cn",
    "remark": "备注信息"
  },
  "timestamp": 1640995200000
}
```

### 2.4 分页查询账号列表

**接口地址**: `POST /accountInfo/findAccountInfoList`

**接口描述**: 分页查询账号信息列表

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userName": "张",
  "status": "1",
  "deptId": ["dept001"],
  "roleId": "role001"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码(默认1) |
| pageSize | Integer | 否 | 每页大小(默认10) |
| userName | String | 否 | 用户名(模糊查询) |
| status | String | 否 | 状态筛选 |
| deptId | List<String> | 否 | 部门ID筛选 |
| roleId | String | 否 | 角色ID筛选 |

**响应示例**:
```json
{
  "code": "200",
  "message": "查询成功",
  "result": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 25,
    "pages": 3,
    "list": [
      {
        "userId": "user001",
        "accountName": "zhangsan",
        "userName": "张三",
        "employeeId": "EMP001",
        "deptName": "技术部",
        "roleName": "工程师",
        "status": "1",
        "loginCounts": 15,
        "lastLoginTime": "2024-01-01 10:30:00"
      }
    ]
  },
  "timestamp": 1640995200000
}
```

### 2.5 删除账号信息

**接口地址**: `POST /accountInfo/deleteAccountInfo`

**接口描述**: 根据用户ID删除账号(逻辑删除)

**请求参数**:
```json
{
  "userId": "user001"
}
```

**响应示例**:
```json
{
  "code": "200",
  "message": "删除成功",
  "result": null,
  "timestamp": 1640995200000
}
```

## 3. 角色管理接口

### 3.1 获取角色列表

**接口地址**: `POST /accountInfo/queryRoleALlList`

**接口描述**: 获取系统中所有可用角色

**请求参数**:
```json
{
  "enabled": "1",
  "roleName": "工程师",
  "clientId": "1"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| enabled | String | 否 | 角色状态 |
| roleName | String | 否 | 角色名称(模糊查询) |
| clientId | String | 否 | 客户ID |

**响应示例**:
```json
{
  "code": "200",
  "message": "查询成功",
  "result": [
    {
      "roleId": "role001",
      "roleName": "系统管理员",
      "enabled": "1",
      "description": "系统管理员角色"
    },
    {
      "roleId": "role002",
      "roleName": "普通用户",
      "enabled": "1",
      "description": "普通用户角色"
    }
  ],
  "timestamp": 1640995200000
}
```

## 4. 部门管理接口

### 4.1 获取部门列表

**接口地址**: `POST /accountInfo/findDepartList`

**接口描述**: 获取组织架构中的部门信息

**请求参数**: 无

**响应示例**:
```json
{
  "code": "200",
  "message": "查询成功",
  "result": [
    {
      "name": "技术部",
      "value": "dept001"
    },
    {
      "name": "市场部",
      "value": "dept002"
    },
    {
      "name": "人事部",
      "value": "dept003"
    }
  ],
  "timestamp": 1640995200000
}
```

## 5. 查询条件接口

### 5.1 获取查询条件

**接口地址**: `GET /accountInfo/conditions`

**接口描述**: 获取账号管理相关的查询条件选项

**请求参数**: 无

**响应示例**:
```json
{
  "code": "200",
  "message": "查询成功",
  "result": {
    "status": [
      {"label": "启用", "value": "1"},
      {"label": "停用", "value": "0"}
    ],
    "stopOrEnable": [
      {"label": "启用", "value": "1"},
      {"label": "停用", "value": "0"}
    ]
  },
  "timestamp": 1640995200000
}
```

## 6. 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权访问 | 检查Token是否有效 |
| 403 | 权限不足 | 联系管理员分配权限 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 100040 | 用户已存在 | 使用不同的账号名称 |

## 7. 接口调用示例

### 7.1 cURL示例
```bash
# 新增账号
curl -X POST "http://localhost:8080/accountInfo/saveAccountInfo" \
  -H "Authorization: Bearer your-token-here" \
  -H "Content-Type: application/json" \
  -d '{
    "accountName": "testuser",
    "accountPwd": "password123",
    "userName": "测试用户",
    "status": "1",
    "clientId": "1"
  }'
```

### 7.2 JavaScript示例
```javascript
// 查询账号列表
const response = await fetch('/accountInfo/findAccountInfoList', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    pageNum: 1,
    pageSize: 10
  })
});

const result = await response.json();
console.log(result);
```

## 8. 注意事项

1. **认证要求**: 所有接口都需要在请求头中携带有效的Bearer Token
2. **参数验证**: 必填参数不能为空，字符串长度需符合要求
3. **密码安全**: 密码会在服务端进行加密处理，前端无需加密
4. **并发控制**: 同一用户的并发操作可能会导致数据不一致
5. **日志记录**: 所有操作都会记录操作日志，便于审计追踪
