# VOC Cloud MySQL数据库设计文档

## 1. 数据库架构概述

VOC Cloud采用双数据库架构设计，将基础数据和客户数据分离存储，确保数据安全性和系统可扩展性。

### 1.1 数据库分布

```
┌─────────────────────────────────────────────────────────────┐
│                    VOC Cloud 数据库架构                      │
├─────────────────────────────────────────────────────────────┤
│  vdp_ms_be (基础库)                    vdp_ms_td (客户库)    │
│  ├── 用户管理                          ├── 项目管理         │
│  ├── 客户信息                          ├── 权限管理         │
│  ├── 品牌车型                          ├── 数据源管理       │
│  ├── 渠道配置                          ├── 标签管理         │
│  ├── 系统配置                          ├── 统计分析         │
│  └── 基础字典                          └── 业务数据         │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 数据流向

```
基础库 (vdp_ms_be) → 数据同步 → 客户库 (vdp_ms_td) → 数据集成 → StarRocks
     ↓                    ↓              ↓              ↓
  用户数据             权限数据        业务数据      分析数据
  品牌数据             配置数据        标签数据      报表数据
  渠道数据             字典数据        项目数据      洞察数据
```

## 2. 基础库设计 (vdp_ms_be)

### 2.1 数据库概述

**数据库名称**: vdp_ms_be  
**数据库类型**: MySQL 8.0  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci  
**用途**: 存储系统基础数据，包括用户、客户、品牌、渠道等核心基础信息

### 2.2 核心表结构

#### 2.2.1 用户管理表组

##### sys_users (系统用户表)
**表描述**: 存储系统所有用户的基础信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(60) | NO | PRI | - | 用户ID，主键 |
| username | varchar(100) | YES | - | MUL | 用户名，唯一索引 |
| phone | varchar(20) | YES | - | - | 手机号 |
| firstname | varchar(100) | YES | - | - | 名 |
| lastname | varchar(100) | YES | - | - | 姓 |
| email | varchar(50) | YES | - | - | 邮箱 |
| operator | varchar(100) | NO | - | - | 操作员 |
| labelstud_token | varchar(100) | YES | - | - | 标签学习令牌 |
| non_locked | int | NO | - | - | 是否锁定(1:未锁定,0:锁定) |
| enabled | int | NO | - | - | 是否启用(1:启用,0:禁用) |
| expire_date | datetime(3) | NO | - | - | 过期时间 |
| start_expire_date | datetime | YES | - | - | 开始过期时间 |
| client_id | varchar(50) | YES | - | MUL | 客户端ID |
| employee_id | varchar(50) | YES | - | - | 员工ID |
| position | varchar(500) | YES | - | - | 职位 |
| office_phone | varchar(60) | YES | - | - | 办公电话 |
| home_phone | varchar(60) | YES | - | - | 家庭电话 |
| remark | text | YES | - | - | 备注 |
| create_time | datetime | YES | - | - | 创建时间 |
| update_time | datetime | YES | - | - | 更新时间 |

**数据量**: 41条记录

##### sys_credentials (用户凭证表)
**表描述**: 存储用户登录凭证信息

##### sys_login_histroy (登录历史表)
**表描述**: 记录用户登录历史

#### 2.2.2 客户管理表组

##### ins_customer_info (客户信息表)
**表描述**: 存储客户基础信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(60) | NO | PRI | - | 客户ID，主键 |
| code | varchar(20) | NO | - | - | 客户编码 |
| full_name | varchar(50) | NO | - | MUL | 客户全称 |
| abbreviation | varchar(20) | NO | - | - | 客户简称 |
| province | varchar(30) | NO | - | MUL | 省份 |
| city | varchar(30) | NO | - | - | 城市 |
| contacts | varchar(30) | YES | - | - | 联系人 |
| phone | varchar(12) | YES | - | - | 联系电话 |
| email | varchar(50) | YES | - | - | 邮箱 |
| address | varchar(500) | YES | - | - | 地址 |
| status | int | NO | - | - | 状态(1:启用,0:禁用) |
| del_flag | int | NO | - | - | 删除标志(0:未删除,1:已删除) |
| sort | int | YES | - | - | 排序 |
| remark | text | YES | - | - | 备注 |
| create_user | varchar(20) | YES | - | - | 创建人 |
| update_user | varchar(20) | YES | - | - | 更新人 |
| create_time | datetime | NO | - | - | 创建时间 |
| update_time | datetime | YES | - | - | 更新时间 |

**数据量**: 0条记录（待初始化）

#### 2.2.3 品牌车型表组

##### ins_brand_info (品牌信息表)
**表描述**: 存储汽车品牌信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(32) | NO | PRI | - | 品牌ID，主键 |
| code | varchar(50) | NO | PRI | - | 品牌编码，联合主键 |
| name | varchar(100) | NO | - | - | 品牌名称 |
| name_en | varchar(100) | YES | - | - | 品牌英文名 |
| alias | text | YES | - | - | 品牌别名 |
| exclusion_words | text | YES | - | - | 排除词汇 |
| order_by | int | YES | - | - | 排序 |
| img | text | YES | - | - | 品牌图片 |
| app_id | varchar(20) | YES | - | - | 应用ID |
| operator | varchar(32) | YES | - | - | 操作员 |
| del_flag | tinyint(1) | YES | - | - | 删除标志 |
| create_time | datetime | NO | - | - | 创建时间 |
| update_time | datetime | NO | - | - | 更新时间 |

**数据量**: 5条记录

##### ins_car_series_info (车系信息表)
**表描述**: 存储车系信息

##### ins_model_info (车型信息表)
**表描述**: 存储车型信息

#### 2.2.4 渠道配置表组

##### ins_channel (渠道表)
**表描述**: 存储数据采集渠道信息

##### ins_channel_distribution (渠道分布表)
**表描述**: 存储渠道数据分布配置

#### 2.2.5 系统配置表组

##### ins_dict (字典表)
**表描述**: 存储系统字典数据

##### ins_dict_item (字典项表)
**表描述**: 存储字典项数据

##### ins_data_resource (数据资源表)
**表描述**: 存储数据资源信息

##### ins_data_source (数据源表)
**表描述**: 存储数据源配置

### 2.3 表关系图

```
sys_users (用户表)
    ↓ (1:N)
sys_credentials (凭证表)
    ↓ (1:N)
sys_login_histroy (登录历史)

ins_customer_info (客户表)
    ↓ (1:N)
ins_customer_permission (客户权限)

ins_brand_info (品牌表)
    ↓ (1:N)
ins_car_series_info (车系表)
    ↓ (1:N)
ins_model_info (车型表)

ins_channel (渠道表)
    ↓ (1:N)
ins_channel_distribution (渠道分布)

ins_dict (字典表)
    ↓ (1:N)
ins_dict_item (字典项表)
```

## 3. 客户库设计 (vdp_ms_td)

### 3.1 数据库概述

**数据库名称**: vdp_ms_td  
**数据库类型**: MySQL 8.0  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci  
**用途**: 存储客户业务数据，包括项目、权限、标签、统计分析等

### 3.2 核心表结构

#### 3.2.1 项目管理表组

##### ins_project_info (项目信息表)
**表描述**: 存储项目基础信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(60) | NO | PRI | - | 项目ID，主键 |
| project_Name | varchar(100) | YES | - | - | 项目名称 |
| project_desc | text | YES | - | - | 项目描述 |
| status | varchar(5) | YES | - | - | 项目状态 |
| create_user | varchar(200) | YES | - | - | 创建人 |
| update_user | varchar(200) | YES | - | - | 更新人 |
| create_time | datetime | YES | - | - | 创建时间 |
| update_time | datetime | YES | - | - | 更新时间 |

**数据量**: 2条记录

##### ins_project_details (项目详情表)
**表描述**: 存储项目详细信息

#### 3.2.2 权限管理表组

##### ins_role (角色表)
**表描述**: 存储角色信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(32) | NO | PRI | - | 角色ID，主键 |
| role_name | varchar(100) | YES | - | - | 角色名称 |
| role_type | int | NO | - | - | 角色类型(1:默认) |
| enabled | int | NO | - | - | 是否启用(1:启用,0:禁用) |
| create_user | varchar(32) | YES | - | - | 创建人 |
| create_time | datetime | YES | - | - | 创建时间 |
| update_time | datetime | YES | - | - | 更新时间 |

**数据量**: 0条记录（待初始化）

##### ins_user_role (用户角色关联表)
**表描述**: 存储用户与角色的关联关系

##### ins_menu_permission (菜单权限表)
**表描述**: 存储菜单权限配置

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(32) | NO | PRI | - | 权限ID，主键 |
| parent_id | varchar(32) | YES | - | - | 父级ID |
| name | varchar(100) | YES | - | - | 权限名称 |
| html_uri | varchar(255) | YES | - | - | 前端路由 |
| api_url | varchar(255) | YES | - | - | 后端接口 |
| sort_no | int | YES | - | - | 排序号 |
| icon | varchar(100) | YES | - | - | 图标 |
| last_level | tinyint(1) | YES | - | - | 是否最后一级 |
| app_id | varchar(20) | YES | - | - | 应用ID |
| permission_key | varchar(100) | YES | - | - | 权限标识 |
| filter_status | int | YES | - | - | 过滤状态 |
| del_flag | varchar(10) | YES | - | - | 删除标志 |
| create_time | datetime | YES | - | - | 创建时间 |

##### ins_role_relation_permission (角色权限关联表)
**表描述**: 存储角色与权限的关联关系

#### 3.2.3 统计分析表组

##### sta_sys_role (统计系统角色表)
**表描述**: 存储报表系统的角色信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(32) | NO | - | - | 角色ID |
| role_name | varchar(200) | YES | - | - | 角色名称 |
| role_code | varchar(100) | YES | - | - | 角色编码 |
| role_type | varchar(100) | YES | - | - | 角色类型 |
| brand_code | varchar(3000) | YES | - | - | 品牌编码 |
| is_export | decimal(1,0) | YES | - | - | 是否允许导出 |
| is_download | decimal(1,0) | YES | - | - | 是否允许下载 |
| is_quality | decimal(1,0) | YES | - | - | 是否允许质量 |
| is_all | decimal(1,0) | YES | - | - | 是否全部权限 |
| role_status | decimal(1,0) | YES | - | - | 角色状态 |
| remark | text | YES | - | - | 备注 |
| create_by | varchar(32) | YES | - | - | 创建人 |
| update_by | varchar(32) | YES | - | - | 更新人 |
| create_time | datetime | YES | - | - | 创建时间 |
| update_time | datetime | YES | - | - | 更新时间 |

##### sta_sys_user_role (统计系统用户角色表)
**表描述**: 存储报表系统用户与角色的关联

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(32) | NO | PRI | - | 关联ID，主键 |
| user_id | varchar(32) | NO | - | MUL | 用户ID |
| role_id | varchar(32) | NO | - | - | 角色ID |
| create_time | datetime | YES | - | - | 创建时间 |

##### sta_sys_depart (组织机构表)
**表描述**: 存储组织机构信息

##### sta_sys_role_area (角色区域表)
**表描述**: 存储角色区域权限

##### sta_sys_role_business_tag (角色业务标签表)
**表描述**: 存储角色业务标签权限

##### sta_sys_role_channel (角色渠道表)
**表描述**: 存储角色渠道权限

##### sta_sys_role_permission (角色权限表)
**表描述**: 存储角色权限配置

##### sta_sys_role_quality_tag (角色质量标签表)
**表描述**: 存储角色质量标签权限

##### sta_sys_role_series (角色车系表)
**表描述**: 存储角色车系权限

#### 3.2.4 数据源管理表组

##### ins_data_source (数据源表)
**表描述**: 存储数据源配置信息

| 字段名 | 数据类型 | 是否为空 | 主键 | 索引 | 说明 |
|--------|----------|----------|------|------|------|
| id | varchar(60) | NO | PRI | - | 数据源ID，主键 |
| data_source_name | varchar(100) | YES | - | - | 数据源名称 |
| data_source_type | varchar(5) | YES | - | - | 数据源类型 |
| data_source_access_way | varchar(20) | YES | - | - | 数据源访问方式 |
| label_type | json | YES | - | - | 标签类型配置 |
| model_type | varchar(10) | YES | - | - | 模型类型 |
| create_user | varchar(100) | YES | - | - | 创建人 |
| create_time | datetime | YES | - | - | 创建时间 |

##### ins_data_resource (数据资源表)
**表描述**: 存储数据资源信息

##### ins_data_resource_desc (数据资源描述表)
**表描述**: 存储数据资源详细描述

##### ins_data_source_desc (数据源描述表)
**表描述**: 存储数据源详细描述

#### 3.2.5 标签管理表组

##### ins_tag_client (标签客户端表)
**表描述**: 存储标签客户端配置

##### ins_label_correction_record (标签校正记录表)
**表描述**: 存储标签校正历史记录

#### 3.2.6 区域管理表组

##### ins_region (区域表)
**表描述**: 存储区域信息

##### ins_region_detail (区域详情表)
**表描述**: 存储区域详细信息

#### 3.2.7 客户分析表组

##### cli_high_frequency_opinions (高频意见表)
**表描述**: 存储高频意见分析结果

##### cli_high_frequency_words (高频词汇表)
**表描述**: 存储高频词汇分析结果

##### cli_risk_keywords (风险关键词表)
**表描述**: 存储风险关键词配置

##### cli_allocation_opinions_record (意见分配记录表)
**表描述**: 存储意见分配历史记录

##### cli_allocation_words_record (词汇分配记录表)
**表描述**: 存储词汇分配历史记录

### 3.3 表关系图

```
ins_project_info (项目表)
    ↓ (1:N)
ins_project_details (项目详情)

ins_role (角色表)
    ↓ (1:N)
ins_user_role (用户角色关联)
    ↓ (1:N)
ins_role_relation_permission (角色权限关联)
    ↓ (1:N)
ins_menu_permission (菜单权限)

sta_sys_role (统计角色表)
    ↓ (1:N)
sta_sys_user_role (统计用户角色)
    ↓ (1:N)
sta_sys_role_area (角色区域)
sta_sys_role_business_tag (角色业务标签)
sta_sys_role_channel (角色渠道)
sta_sys_role_permission (角色权限)
sta_sys_role_quality_tag (角色质量标签)
sta_sys_role_series (角色车系)

ins_data_source (数据源表)
    ↓ (1:N)
ins_data_source_desc (数据源描述)
    ↓ (1:N)
ins_data_resource (数据资源)
    ↓ (1:N)
ins_data_resource_desc (数据资源描述)

ins_region (区域表)
    ↓ (1:N)
ins_region_detail (区域详情)

cli_risk_keywords (风险关键词)
    ↓ (1:N)
cli_high_frequency_opinions (高频意见)
cli_high_frequency_words (高频词汇)
cli_allocation_opinions_record (意见分配记录)
cli_allocation_words_record (词汇分配记录)
```

## 4. 数据库设计原则

### 4.1 分离原则
- **基础库**: 存储系统基础数据，相对稳定，变更频率低
- **客户库**: 存储业务数据，变更频繁，需要高可用性

### 4.2 权限分离
- **基础库权限**: 系统管理员级别，严格控制访问
- **客户库权限**: 业务用户级别，按角色分配权限

### 4.3 数据同步
- 基础数据从基础库同步到客户库
- 保持数据一致性和实时性
- 支持增量同步和全量同步

### 4.4 扩展性设计
- 支持多租户架构
- 支持水平扩展
- 支持数据分片

## 5. 性能优化策略

### 5.1 索引策略
- 主键索引：所有表都有主键索引
- 唯一索引：用户名、客户编码等唯一字段
- 普通索引：查询频繁的字段
- 复合索引：多字段查询场景

### 5.2 分区策略
- 按时间分区：日志表、历史表
- 按范围分区：大表数据
- 按哈希分区：均匀分布数据

### 5.3 缓存策略
- 字典数据缓存
- 权限数据缓存
- 配置数据缓存

## 6. 数据安全策略

### 6.1 访问控制
- 基于角色的访问控制(RBAC)
- 数据行级权限控制
- 数据列级权限控制

### 6.2 数据加密
- 敏感字段加密存储
- 传输数据加密
- 备份数据加密

### 6.3 审计日志
- 数据变更审计
- 访问日志记录
- 操作轨迹追踪

## 7. 维护管理

### 7.1 备份策略
- 每日全量备份
- 每小时增量备份
- 异地备份存储

### 7.2 监控策略
- 数据库性能监控
- 数据质量监控
- 异常告警机制

### 7.3 维护计划
- 定期索引重建
- 定期统计信息更新
- 定期数据清理

## 8. 版本管理

### 8.1 版本号规则
- 主版本号.次版本号.修订号
- 例如：1.0.0

### 8.2 变更记录
- 记录每次数据库变更
- 记录变更原因和影响
- 记录回滚方案

### 8.3 发布流程
- 开发环境测试
- 测试环境验证
- 生产环境发布 