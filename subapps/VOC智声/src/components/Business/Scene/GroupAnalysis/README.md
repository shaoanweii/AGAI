# 集团分析模块使用指南

## 概述

集团分析模块提供了完整的集团层面数据分析功能，包括综合分析简报、品牌趋势变化、车系排行等核心功能。

## 组件结构

```
GroupAnalysis/
├── index.vue                    # 主页面，负责数据获取和状态管理
├── ComprehensiveAnalysis/       # 综合分析模块
│   ├── index.vue               # 综合分析容器组件
│   ├── BrandTrend.vue          # 品牌趋势变化组件
│   └── CarSeriesRank.vue       # 品牌车系排行组件
├── ServiceReputationAnalysis/   # 服务口碑分析
├── CPAnalysis/                  # 产品分析
└── DSTable/                     # 数据来源表格
```

## 使用方式

### 1. 基本使用

```vue
<template>
  <GroupAnalysis />
</template>

<script setup lang="ts">
import GroupAnalysis from '@/components/Business/Scene/GroupAnalysis/index.vue'
</script>
```

### 2. 单独使用综合分析组件

```vue
<template>
  <ComprehensiveAnalysis
    :product-brief-data="briefData"
    :brand-trend-data="trendData"
    :brand-series-rank-data="rankData"
    @brand-trend-switch="handleTrendSwitch"
    @brand-series-rank-switch="handleRankSwitch"
  />
</template>

<script setup lang="ts">
import ComprehensiveAnalysis from '@/components/Business/Scene/GroupAnalysis/ComprehensiveAnalysis/index.vue'
import type {
  ProductBriefVo,
  BrandTrendChangeVo,
  SeriesRankItemVo
} from '@/api/groupAnalysis/types'

const briefData = ref<ProductBriefVo | null>(null)
const trendData = ref<BrandTrendChangeVo | null>(null)
const rankData = ref<SeriesRankItemVo[]>([])

const handleTrendSwitch = (dataType: 'negativeRate' | 'mention') => {
  // 处理趋势切换
}

const handleRankSwitch = (dataType: 'brand' | 'series') => {
  // 处理排行切换
}
</script>
```

## API接口

### 1. 数据简报接口

```typescript
import { getGroupProductBrief } from '@/api/groupAnalysis'

const fetchBrief = async () => {
  const response = await getGroupProductBrief({
    startDate: '2024-01-01',
    endDate: '2024-12-31'
  })
  return response.result
}
```

### 2. 品牌趋势接口

```typescript
import { getBrandTrendChange } from '@/api/groupAnalysis'

const fetchTrend = async (dataType: 'negativeRate' | 'mention') => {
  const response = await getBrandTrendChange({
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    dataType
  })
  return response.result
}
```

### 3. 车系排行接口

```typescript
import { getBrandSeriesRank } from '@/api/groupAnalysis'

const fetchRank = async (dataType: 'brand' | 'series') => {
  const response = await getBrandSeriesRank({
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    dataType
  })
  return response.result
}
```

## 数据类型

### ProductBriefVo - 数据简报

```typescript
interface ProductBriefVo {
  negativeRate: number // 负面率
  negativeRateMoM: number // 负面率环比
  negativeRateYoY: number // 负面率同比 (新增)
  positiveRate: number // 正面率
  positiveRateMoM: number // 正面率环比
  positiveRateYoY: number // 正面率同比 (新增)
  mentions: number // 提及量
  mentionsMoM: number // 提及量环比
  mentionsYoY: number // 提及量同比 (新增)
  users: number // 用户数
  usersMoM: number // 用户数环比
  usersYoY: number // 用户数同比 (新增)
}
```

### BrandTrendVo - 品牌趋势 (完全重构)

```typescript
interface BrandTrendVo {
  date: string // 时间点
  brandSeries: BrandTrendSeriesVo[] // 该时间点的品牌数据集合
}

interface BrandTrendSeriesVo {
  date: string // 时间轴
  brandName: string // 品牌名称
  brandCode: string // 品牌编码
  value1: number // 提及量
  value2: number // 负面率
}
```

### SeriesRankItemVo - 车系排行

```typescript
interface SeriesRankItemVo {
  name: string // 名称
  code: string // 编码 (新增)
  imageUrl: string // 图片URL
  negativeRate: number // 负面率
  negativeRateMoM: number // 负面率环比
  negativeRateYoY: number // 负面率同比 (新增)
  mentions: number // 提及量
  mentionsMoM: number // 提及量环比
  mentionsYoY: number // 提及量同比 (新增)
  mentionsTrend: string[] // 提及量趋势
  negativeMentionsTrend: string[] // 负面趋势
}
```

## 功能特性

### 1. 数据简报

- 显示负面率、正面率、提及量、用户数
- 支持环比数据展示
- 自动格式化数值显示

### 2. 品牌趋势变化

- 支持负面率/提及量切换
- 面积图展示趋势变化
- 详细的tooltip信息
- 响应式图表配置

### 3. 品牌车系排行

- 支持品牌/车系切换
- 动态表格标题
- 集成趋势图表
- 支持图片展示

## 注意事项

1. **数据格式**：确保API返回的数据格式与类型定义一致
2. **错误处理**：组件内置了错误处理，会显示用户友好的错误信息
3. **加载状态**：支持loading状态显示
4. **响应式**：组件支持响应式布局
5. **类型安全**：使用TypeScript确保类型安全

## 扩展开发

如需添加新的分析功能：

1. 在对应目录下创建新组件
2. 在主页面中引入并使用
3. 添加相应的API接口
4. 更新类型定义
5. 添加事件处理逻辑

## 更新日志

### 2024-12-XX - API接口更新

#### 🔄 重大变更

- **品牌趋势变化接口** 数据结构完全重构
  - 旧结构：`BrandTrendChangeVo` 包含单一趋势数组
  - 新结构：`BrandTrendVo[]` 多品牌时间序列数据
  - 图表现在支持多品牌对比显示

#### ➕ 新增字段

- **数据简报接口** 新增同比数据：
  - `negativeRateYoY` - 负面率同比
  - `positiveRateYoY` - 正面率同比
  - `mentionsYoY` - 提及量同比
  - `usersYoY` - 用户数同比

- **车系排行接口** 新增字段：
  - `code` - 编码字段
  - `negativeRateYoY` - 负面率同比
  - `mentionsYoY` - 提及量同比

#### 🛠️ 组件更新

- **BrandTrend组件** 完全重构以支持多品牌显示
  - 新增图例显示
  - 优化tooltip显示多品牌信息
  - 支持面积图和多色彩显示
- **数据安全性** 增强所有组件的空数据处理
- **类型安全** 更新所有TypeScript类型定义

#### 📋 接口路径

所有接口路径保持不变：

- `/api/report/group-analysis/getProductBrief`
- `/api/report/group-analysis/get-brand-trend-change`
- `/api/report/group-analysis/get-brand-series-rank`

### 2024-12-XX - 排序功能实现

#### 🔧 新增功能

- **车系排行表格排序** 支持按字段排序
  - 支持提及量排序 (`mentions`)
  - 支持负面率排序 (`negativeRate`)
  - 支持升序/降序切换

#### 🛠️ 实现细节

- **CarSeriesRank组件** 添加排序事件emit
  - 监听FTable的 `@sort-change` 事件
  - 转换排序参数格式并向上传递
- **ComprehensiveAnalysis组件** 转发排序事件
  - 接收子组件排序事件并转发到主页面
- **主页面** 处理排序逻辑
  - 接收排序参数并调用API重新获取数据
  - 支持sortField和sortOrder参数

#### 📊 事件流程

```
FTable @sort-change → CarSeriesRank handleSort →
ComprehensiveAnalysis handleBrandSeriesRankSort →
GroupAnalysis handleBrandSeriesRankSort →
fetchBrandSeriesRank(dataType, sortField, sortOrder)
```
