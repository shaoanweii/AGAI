# VOC数据库ER图设计文档

## 1. 概述

### 1.1 设计目标
本文档描述了VOC数据库的实体关系图(ER图)设计，通过图形化的方式展示数据库中各实体之间的关系，为数据库设计和业务理解提供直观的参考。

### 1.2 设计原则
- **清晰性**: 实体关系清晰明确，便于理解
- **完整性**: 覆盖所有主要实体和关系
- **规范性**: 遵循数据库设计规范
- **实用性**: 符合实际业务需求

## 2. 实体定义

### 2.1 核心实体

#### 2.1.1 声音数据实体 (Voice_Data)
**实体描述**: VOC平台的核心业务实体，代表一条客户声音数据
**主要属性**:
- id: 声音ID (主键)
- data_id: 数据唯一标识
- channel_code: 渠道编码
- brand_code: 品牌编码
- vehicle_series_code: 车系编码
- sentiment: 情感倾向
- data_create_time: 数据产生时间

#### 2.1.2 客户实体 (Customer)
**实体描述**: 客户基本信息实体
**主要属性**:
- oneid: 客户唯一标识 (主键)
- cust_nm: 客户姓名
- gender: 性别
- age: 年龄
- cust_type: 客户类型
- is_car_owner_flg: 是否车主

#### 2.1.3 经销商实体 (Dealer)
**实体描述**: 经销商信息实体
**主要属性**:
- dealer_id: 经销商ID (主键)
- dealer_code: 经销商编码
- dealer_name: 经销商名称
- dealer_type: 经销商类型
- province_code: 省份编码
- city_code: 城市编码

#### 2.1.4 车辆实体 (Vehicle)
**实体描述**: 车辆信息实体
**主要属性**:
- vehicle_id: 车辆ID (主键)
- material_code: 物料编码
- brand_code: 品牌编码
- series_code: 车系编码
- model_code: 车型编码
- vin: 车架号

#### 2.1.5 产品实体 (Product)
**实体描述**: 产品信息实体
**主要属性**:
- product_id: 产品ID (主键)
- product_code: 产品编码
- product_name: 产品名称
- product_type: 产品类型
- brand_code: 品牌编码

### 2.2 视图层实体

#### 2.2.1 业务视图实体

**计算结果汇总视图 (Computed_Result_View)**
**实体描述**: 计算结果汇总视图实体
**主要属性**:
- id: 计算结果ID (主键)
- data_id: 数据唯一标识
- channel_code: 渠道编码
- brand_code: 品牌编码
- vehicle_series_code: 车系编码
- sentiment: 情感倾向
- data_create_time: 数据产生时间
- computed_time: 计算时间
- result_type: 结果类型
- result_value: 结果值

**客户信息视图 (Customer_Info_View)**
**实体描述**: 客户信息视图实体
**主要属性**:
- one_id: 客户唯一标识 (主键)
- cust_name: 客户姓名
- cust_mobile: 客户手机号
- cust_age: 客户年龄
- cust_gender: 客户性别
- cust_province: 客户常驻省份
- cust_city: 客户常驻市
- cust_type: 客户类型
- is_vehicle_owner: 是否车主
- update_time: 更新时间

**经销商信息视图 (Dealer_Info_View)**
**实体描述**: 经销商信息视图实体
**主要属性**:
- dealer_id: 经销商ID (主键)
- dealer_code: 经销商编码
- dealer_name: 经销商全称
- dealer_province: 经销商所属省份
- dealer_city: 经销商所在市
- dealer_regional: 经销商所在大区
- dealer_type: 经销商类型
- dealer_level: 经销商等级
- update_time: 更新时间

**车辆信息视图 (Vehicle_Info_View)**
**实体描述**: 车辆信息视图实体
**主要属性**:
- vehicle_id: 车辆ID (主键)
- vehicle_vin: 车辆车架号
- brand_code: 品牌编码
- brand: 品牌名称
- vehicle_series_code: 车系编码
- vehicle_series: 车系名称
- vehicle_model_code: 车型编码
- vehicle_model: 车型名称
- vehicle_purchase_date: 车辆购买日期
- update_time: 更新时间

#### 2.2.2 权限视图实体

**用户标签权限视图 (User_Label_Permission_View)**
**实体描述**: 用户标签权限视图实体
**主要属性**:
- user_id: 用户ID (主键)
- label_code: 标签编码
- label_name: 标签名称
- permission_type: 权限类型
- scope_level: 权限范围级别
- is_active: 是否激活
- update_time: 更新时间

**车系权限视图 (Car_Series_Permission_View)**
**实体描述**: 车系权限视图实体
**主要属性**:
- user_id: 用户ID (主键)
- car_series_code: 车系编码
- car_series_name: 车系名称
- permission_type: 权限类型
- is_active: 是否激活
- update_time: 更新时间

#### 2.2.3 配置视图实体

**车系配置视图 (Car_Series_Config_View)**
**实体描述**: 车系配置视图实体
**主要属性**:
- car_series_code: 车系编码 (主键)
- car_series_name: 车系名称
- brand_code: 品牌编码
- brand_name: 品牌名称
- is_active: 是否激活
- sort_order: 排序顺序
- update_time: 更新时间

**渠道配置视图 (Channel_Config_View)**
**实体描述**: 渠道配置视图实体
**主要属性**:
- channel_code: 渠道编码 (主键)
- channel_name: 渠道名称
- channel_type: 渠道类型
- is_active: 是否激活
- sort_order: 排序顺序
- update_time: 更新时间

**标签配置视图 (Tag_Config_View)**
**实体描述**: 标签配置视图实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_category: 标签分类
- tag_level: 标签层级
- parent_code: 父级标签编码
- is_active: 是否激活
- sort_order: 排序顺序
- update_time: 更新时间

#### 2.2.4 元数据视图实体

**原始数据元数据视图 (Raw_Meta_Data_View)**
**实体描述**: 原始数据元数据视图实体
**主要属性**:
- data_source: 数据源 (主键)
- table_name: 表名
- min_date: 最小日期
- max_date: 最大日期
- record_count: 记录数
- update_time: 更新时间

**统计字典视图 (Statistics_Dict_View)**
**实体描述**: 统计字典视图实体
**主要属性**:
- dict_code: 字典编码 (主键)
- dict_name: 字典名称
- dict_type: 字典类型
- dict_value: 字典值
- is_active: 是否激活
- update_time: 更新时间

### 2.3 标签实体

#### 2.3.1 VTR标签实体 (VTR_Tag)
**实体描述**: 车辆技术相关标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.2 COM标签实体 (COM_Tag)
**实体描述**: 商品化属性标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.3 ADB标签实体 (ADB_Tag)
**实体描述**: 全领域业务标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.4 WOM标签实体 (WOM_Tag)
**实体描述**: 口碑评价指标标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.5 CX标签实体 (CX_Tag)
**实体描述**: 客户体验指标标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.6 CJ标签实体 (CJ_Tag)
**实体描述**: 全旅程客户标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.7 SL标签实体 (SL_Tag)
**实体描述**: 销售线索标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

#### 2.3.8 OM标签实体 (OM_Tag)
**实体描述**: 全媒体指标标签实体
**主要属性**:
- tag_code: 标签编码 (主键)
- tag_name: 标签名称
- tag_level: 标签层级
- parent_code: 父级标签编码

## 3. 关系定义

### 3.1 核心关系

#### 3.1.1 声音数据与客户关系
- **关系类型**: 多对一 (N:1)
- **关系描述**: 一条声音数据属于一个客户
- **外键**: Voice_Data.one_id → Customer.oneid
- **业务含义**: 客户产生的声音数据

#### 3.1.2 声音数据与经销商关系
- **关系类型**: 多对一 (N:1)
- **关系描述**: 一条声音数据关联一个经销商
- **外键**: Voice_Data.dealer_id → Dealer.dealer_id
- **业务含义**: 声音数据涉及的经销商

#### 3.1.3 声音数据与车辆关系
- **关系类型**: 多对一 (N:1)
- **关系描述**: 一条声音数据关联一辆车
- **外键**: Voice_Data.vehicle_vin → Vehicle.vin
- **业务含义**: 声音数据涉及的车辆

#### 3.1.4 声音数据与产品关系
- **关系类型**: 多对一 (N:1)
- **关系描述**: 一条声音数据关联一个产品
- **外键**: Voice_Data.brand_code → Product.brand_code
- **业务含义**: 声音数据涉及的产品

### 3.2 标签关系

#### 3.2.1 声音数据与VTR标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个VTR标签，一个VTR标签可以关联多条声音数据
- **中间表**: Voice_Data_VTR_Tag
- **业务含义**: 声音数据的技术特征标签

#### 3.2.2 声音数据与COM标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个COM标签，一个COM标签可以关联多条声音数据
- **中间表**: Voice_Data_COM_Tag
- **业务含义**: 声音数据的商品化属性标签

#### 3.2.3 声音数据与ADB标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个ADB标签，一个ADB标签可以关联多条声音数据
- **中间表**: Voice_Data_ADB_Tag
- **业务含义**: 声音数据的业务领域标签

#### 3.2.4 声音数据与WOM标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个WOM标签，一个WOM标签可以关联多条声音数据
- **中间表**: Voice_Data_WOM_Tag
- **业务含义**: 声音数据的口碑评价标签

#### 3.2.5 声音数据与CX标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个CX标签，一个CX标签可以关联多条声音数据
- **中间表**: Voice_Data_CX_Tag
- **业务含义**: 声音数据的客户体验标签

#### 3.2.6 声音数据与CJ标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个CJ标签，一个CJ标签可以关联多条声音数据
- **中间表**: Voice_Data_CJ_Tag
- **业务含义**: 声音数据的客户旅程标签

#### 3.2.7 声音数据与SL标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个SL标签，一个SL标签可以关联多条声音数据
- **中间表**: Voice_Data_SL_Tag
- **业务含义**: 声音数据的销售线索标签

#### 3.2.8 声音数据与OM标签关系
- **关系类型**: 多对多 (M:N)
- **关系描述**: 一条声音数据可以有多个OM标签，一个OM标签可以关联多条声音数据
- **中间表**: Voice_Data_OM_Tag
- **业务含义**: 声音数据的媒体指标标签

### 3.3 标签层级关系

#### 3.3.1 VTR标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级VTR标签可以有多个子级VTR标签
- **外键**: VTR_Tag.parent_code → VTR_Tag.tag_code
- **业务含义**: VTR标签的层级结构

#### 3.3.2 COM标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级COM标签可以有多个子级COM标签
- **外键**: COM_Tag.parent_code → COM_Tag.tag_code
- **业务含义**: COM标签的层级结构

#### 3.3.3 ADB标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级ADB标签可以有多个子级ADB标签
- **外键**: ADB_Tag.parent_code → ADB_Tag.tag_code
- **业务含义**: ADB标签的层级结构

#### 3.3.4 WOM标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级WOM标签可以有多个子级WOM标签
- **外键**: WOM_Tag.parent_code → WOM_Tag.tag_code
- **业务含义**: WOM标签的层级结构

#### 3.3.5 CX标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级CX标签可以有多个子级CX标签
- **外键**: CX_Tag.parent_code → CX_Tag.tag_code
- **业务含义**: CX标签的层级结构

#### 3.3.6 CJ标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级CJ标签可以有多个子级CJ标签
- **外键**: CJ_Tag.parent_code → CJ_Tag.tag_code
- **业务含义**: CJ标签的层级结构

#### 3.3.7 SL标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级SL标签可以有多个子级SL标签
- **外键**: SL_Tag.parent_code → SL_Tag.tag_code
- **业务含义**: SL标签的层级结构

#### 3.3.8 OM标签层级关系
- **关系类型**: 一对多 (1:N)
- **关系描述**: 一个父级OM标签可以有多个子级OM标签
- **外键**: OM_Tag.parent_code → OM_Tag.tag_code
- **业务含义**: OM标签的层级结构

## 4. ER图表示

### 4.1 核心实体关系图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Customer      │    │   Voice_Data    │    │   Dealer        │
│                 │    │                 │    │                 │
│ oneid (PK)      │◄───┤ id (PK)         │───►│ dealer_id (PK)  │
│ cust_nm         │    │ data_id         │    │ dealer_code     │
│ gender          │    │ channel_code    │    │ dealer_name     │
│ age             │    │ brand_code      │    │ dealer_type     │
│ cust_type       │    │ sentiment       │    │ province_code   │
│ is_car_owner_flg│    │ data_create_time│    │ city_code       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                │
                                ▼
                       ┌─────────────────┐
                       │   Vehicle       │
                       │                 │
                       │ vehicle_id (PK) │
                       │ material_code   │
                       │ brand_code      │
                       │ series_code     │
                       │ model_code      │
                       │ vin             │
                       └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Product       │    │   Voice_Data    │    │   VTR_Tag       │
│                 │    │                 │    │                 │
│ product_id (PK) │◄───┤ id (PK)         │───►│ tag_code (PK)   │
│ product_code    │    │ data_id         │    │ tag_name        │
│ product_name    │    │ channel_code    │    │ tag_level       │
│ product_type    │    │ brand_code      │    │ parent_code     │
│ brand_code      │    │ sentiment       │    └─────────────────┘
└─────────────────┘    └─────────────────┘
```

### 4.2 视图层关系图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Customer_Info_View│    │Computed_Result_View│  │Dealer_Info_View │
│                 │    │                 │    │                 │
│ one_id (PK)     │◄───┤ id (PK)         │───►│ dealer_id (PK)  │
│ cust_name       │    │ data_id         │    │ dealer_code     │
│ cust_mobile     │    │ channel_code    │    │ dealer_name     │
│ cust_age        │    │ brand_code      │    │ dealer_province │
│ cust_gender     │    │ sentiment       │    │ dealer_city     │
│ cust_type       │    │ data_create_time│    │ dealer_regional │
│ update_time     │    │ computed_time   │    │ update_time     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                │
                                ▼
                       ┌─────────────────┐
                       │Vehicle_Info_View│
                       │                 │
                       │ vehicle_id (PK) │
                       │ vehicle_vin     │
                       │ brand_code      │
                       │ vehicle_series  │
                       │ vehicle_model   │
                       │ update_time     │
                       └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│User_Label_Perm_View│  │Car_Series_Perm_View│ │Car_Series_Config_View│
│                 │    │                 │    │                 │
│ user_id (PK)    │    │ user_id (PK)    │    │ car_series_code(PK)│
│ label_code      │    │ car_series_code │    │ car_series_name │
│ label_name      │    │ car_series_name │    │ brand_code      │
│ permission_type │    │ permission_type │    │ brand_name      │
│ scope_level     │    │ is_active       │    │ is_active       │
│ is_active       │    │ update_time     │    │ sort_order      │
│ update_time     │    └─────────────────┘    │ update_time     │
└─────────────────┘                           └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Channel_Config_View│   │Tag_Config_View │    │Raw_Meta_Data_View│
│                 │    │                 │    │                 │
│ channel_code(PK)│    │ tag_code (PK)   │    │ data_source(PK) │
│ channel_name    │    │ tag_name        │    │ table_name      │
│ channel_type    │    │ tag_category    │    │ min_date        │
│ is_active       │    │ tag_level       │    │ max_date        │
│ sort_order      │    │ parent_code     │    │ record_count    │
│ update_time     │    │ is_active       │    │ update_time     │
└─────────────────┘    │ sort_order      │    └─────────────────┘
                       │ update_time     │
                       └─────────────────┘
```

### 4.3 标签层级关系图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   VTR_Tag       │    │   COM_Tag       │    │   ADB_Tag       │
│                 │    │                 │    │                 │
│ tag_code (PK)   │    │ tag_code (PK)   │    │ tag_code (PK)   │
│ tag_name        │    │ tag_name        │    │ tag_name        │
│ tag_level       │    │ tag_level       │    │ tag_level       │
│ parent_code     │    │ parent_code     │    │ parent_code     │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   WOM_Tag       │    │   Voice_Data    │    │   CX_Tag        │
│                 │    │                 │    │                 │
│ tag_code (PK)   │◄───┤ id (PK)         │───►│ tag_code (PK)   │
│ tag_name        │    │ data_id         │    │ tag_name        │
│ tag_level       │    │ channel_code    │    │ tag_level       │
│ parent_code     │    │ brand_code      │    │ parent_code     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                │
                                ▼
                       ┌─────────────────┐
                       │   CJ_Tag        │
                       │                 │
                       │ tag_code (PK)   │
                       │ tag_name        │
                       │ tag_level       │
                       │ parent_code     │
                       └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   SL_Tag        │    │   Voice_Data    │    │   OM_Tag        │
│                 │    │                 │    │                 │
│ tag_code (PK)   │◄───┤ id (PK)         │───►│ tag_code (PK)   │
│ tag_name        │    │ data_id         │    │ tag_name        │
│ tag_level       │    │ channel_code    │    │ tag_level       │
│ parent_code     │    │ brand_code      │    │ parent_code     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 5. 关系约束

### 5.1 外键约束
- Voice_Data.one_id → Customer.oneid
- Voice_Data.dealer_id → Dealer.dealer_id
- Voice_Data.vehicle_vin → Vehicle.vin
- Voice_Data.brand_code → Product.brand_code

### 5.2 视图层关系约束
- Computed_Result_View.data_id → Voice_Data.data_id
- Customer_Info_View.one_id → Customer.oneid
- Dealer_Info_View.dealer_id → Dealer.dealer_id
- Vehicle_Info_View.vehicle_vin → Vehicle.vin
- Car_Series_Config_View.brand_code → Product.brand_code
- Tag_Config_View.parent_code → Tag_Config_View.tag_code

### 5.3 权限视图约束
- User_Label_Permission_View.user_id → User.user_id
- Car_Series_Permission_View.user_id → User.user_id
- Car_Series_Permission_View.car_series_code → Car_Series_Config_View.car_series_code

### 5.4 标签层级约束
- VTR_Tag.parent_code → VTR_Tag.tag_code
- COM_Tag.parent_code → COM_Tag.tag_code
- ADB_Tag.parent_code → ADB_Tag.tag_code
- WOM_Tag.parent_code → WOM_Tag.tag_code
- CX_Tag.parent_code → CX_Tag.tag_code
- CJ_Tag.parent_code → CJ_Tag.tag_code
- SL_Tag.parent_code → SL_Tag.tag_code
- OM_Tag.parent_code → OM_Tag.tag_code

### 5.5 业务约束
- 声音数据必须关联客户
- 声音数据可以关联多个标签
- 标签必须遵循层级结构
- 时间字段必须有效
- 视图数据必须与基础表数据保持一致
- 权限视图必须与用户权限配置一致

## 6. 索引设计

### 6.1 主键索引
- Customer.oneid
- Voice_Data.id
- Dealer.dealer_id
- Vehicle.vehicle_id
- 各标签表的tag_code
- 各视图的主键字段

### 6.2 外键索引
- Voice_Data.one_id
- Voice_Data.dealer_id
- Voice_Data.vehicle_vin
- Voice_Data.brand_code
- 各标签表的parent_code
- 视图层的外键字段

### 6.3 复合索引
- Voice_Data(channel_code, data_create_time)
- Voice_Data(brand_code, data_create_time)
- Voice_Data(sentiment, data_create_time)
- 视图层的复合查询字段

## 7. 数据完整性

### 7.1 实体完整性
- 所有主键字段不允许为空
- 主键值必须唯一
- 视图层主键必须唯一

### 7.2 参照完整性
- 外键值必须存在于被参照表中
- 删除被参照记录时需要考虑级联关系
- 视图层数据必须与基础表数据保持一致

### 7.3 域完整性
- 字段类型和长度符合定义
- 字段值在有效范围内
- 必填字段不允许为空
- 视图层字段约束与基础表一致

### 7.4 业务完整性
- 时间字段格式正确
- 编码字段符合规范
- 标签层级关系正确
- 权限配置与用户角色一致
- 配置数据与业务规则一致 