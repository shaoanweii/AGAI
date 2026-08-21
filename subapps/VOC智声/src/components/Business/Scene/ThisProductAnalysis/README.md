# 本品分析模块

## 📋 概述

本品分析模块是VOC数据分析系统中的核心功能模块，专门用于分析特定产品的客户反馈数据。该模块集成了综合分析、用户旅程分析、服务分析、产品分析和数据来源分析等多个维度的数据展示。

## 🚀 功能特性

### 综合分析
- **简报数据**: 负面率、正面率、提及量、用户数及其环比数据
- **关注场景TOP**: 各场景的提及量和负面率排行
- **数据趋势变化**: 时间序列数据的趋势分析
- **排序功能**: 支持按不同字段进行排序

### 用户旅程分析
- 用户在不同旅程阶段的反馈分析
- 客户满意度和不满意度TOP5观点

### 服务分析
- 服务相关的客户反馈分析
- 服务质量评估指标

### 产品分析
- 产品功能和特性的客户反馈
- 产品改进建议分析

### 数据来源分析
- 不同渠道的数据分布
- 渠道负面率趋势
- 渠道提及量占比

## 🔧 技术实现

### API接口
- `getProductSelfBrief`: 获取综合分析简报数据
- `getFocusSceneTop`: 获取关注场景TOP数据
- `getDataTrendChange`: 获取数据趋势变化数据

### 组件结构
```
ThisProductAnalysis/
├── index.vue              # 主组件
├── index.test.ts          # 单元测试
├── README.md              # 文档说明
├── CPFX/                  # 产品分析子组件
├── FWFX/                  # 服务分析子组件
└── UserJourneyAnalysis/   # 用户旅程分析子组件
```

### 数据流
1. 组件挂载时自动调用三个核心API接口
2. 获取到的数据传递给ComprehensiveAnalysis公共组件
3. ComprehensiveAnalysis组件负责数据的展示和交互
4. 支持排序事件的处理和数据更新

## 📊 数据类型

### ProductSelfBriefVo
```typescript
interface ProductSelfBriefVo {
  negativeRate: number        // 负面率
  negativeRateMoM: number     // 负面率环比
  negativeRateYoY: number     // 负面率同比
  positiveRate: number        // 正面率
  positiveRateMoM: number     // 正面率环比
  positiveRateYoY: number     // 正面率同比
  mentions: number            // 提及量
  mentionsMoM: number         // 提及量环比
  mentionsYoY: number         // 提及量同比
  users: number               // 用户数
  usersMoM: number            // 用户数环比
  usersYoY: number            // 用户数同比
}
```

### ProductSelfSceneTopVo
```typescript
interface ProductSelfSceneTopVo {
  scenario: string                    // 场景名称
  mentions: number                    // 提及量
  negativeRate: number                // 负面率
  mentionsMoMGroup: string[]          // 提及量环比数据组
  negativeRateMoMGroup: string[]      // 负面率环比数据组
}
```

### ProductSelfTrendVo
```typescript
interface ProductSelfTrendVo {
  negativeRateAvg: number             // 负面率均值
  trend: ProductSelfTrendPointVo[]    // 趋势数据点
}
```

## 🔄 使用方法

### 基本使用
```vue
<template>
  <ThisProductAnalysis />
</template>

<script setup lang="ts">
import ThisProductAnalysis from '@/components/Business/Scene/ThisProductAnalysis/index.vue'
</script>
```

### 数据获取
组件会自动在挂载时获取数据，使用`useQueryStore`中的默认查询参数。

### 排序功能
组件支持关注场景TOP的排序功能，点击表头可以触发排序事件。

## 🧪 测试

运行单元测试：
```bash
npm run test src/components/Business/Scene/ThisProductAnalysis/index.test.ts
```

测试覆盖：
- ✅ 组件正确渲染
- ✅ API接口正确调用
- ✅ 数据正确传递到子组件
- ✅ 排序功能正常工作

## 🔗 相关组件

- `ComprehensiveAnalysis`: 综合分析公共组件
- `MetricSummaryCards`: 指标概览卡片
- `DataTrend`: 数据趋势图表
- `FollowSceneTOP`: 关注场景TOP表格

## 📝 注意事项

1. 该组件依赖`useQueryStore`和`useGeneralScenarioStore`
2. 所有API调用都包含错误处理
3. 组件支持产品分析和本品分析两种数据类型
4. 排序功能会重新调用API获取排序后的数据

## 🔄 更新日志

### v1.0.0 (2023-09-05)
- ✨ 初始版本发布
- ✨ 实现综合分析数据获取和展示
- ✨ 支持关注场景TOP排序功能
- ✨ 完整的单元测试覆盖
- ✨ 兼容产品分析和本品分析数据类型
