# UniversaFilter 通用筛选组件

## 功能特性

- ✅ 配置化字段定义，统一在 helper.ts 中管理
- ✅ 通过路由 name 自动获取对应配置
- ✅ 支持 el-col 的 span 配置
- ✅ 支持占位符（placeholder）实现空白占位
- ✅ 品牌、车系独立组件，可复用
- ✅ 完整的查询和重置功能
- ✅ 支持默认值配置
- ✅ 支持分割线配置

## 基础使用

```vue
<script setup lang="ts">
import { useRoute } from 'vue-router'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'

const route = useRoute()

// 查询事件
function handleSearch(data: Record<string, any>) {
  console.log('查询数据：', data)
  // 执行查询逻辑
}

// 重置事件
function handleReset() {
  console.log('重置筛选')
}
</script>

<template>
  <UniversaFilter
    :route-name="route.name as string"
    date-range-text="近7天"
    @search="handleSearch"
    @reset="handleReset"
  />
</template>
```

## 配置管理

所有页面的筛选配置统一在 `helper.ts` 中管理：

```typescript
// src/components/Business/UniversaFilter/helper.ts
import type { FilterFieldConfig } from './types'

const filterConfigMap: Record<string, FilterFieldConfig[]> = {
  // 竞品分析页面配置
  CompetitorAnalysis: [
    {
      type: 'daterange',
      prop: 'dateRange',
      label: '日期范围',
      span: 24
    },
    {
      type: 'brand',
      prop: 'brand',
      label: '品牌',
      span: 24,
      options: [
        { label: '智行', value: 'zhixing' },
        { label: '远途', value: 'yuantu' }
      ],
      defaultValue: 'yinli',
      showSplitLine: true
    },
    // ... 更多配置
  ],
  
  // 产品分析页面配置
  ProductAnalysis: [
    // ... 配置
  ]
}

export function getFilterConfig(routeName: string): FilterFieldConfig[] {
  return filterConfigMap[routeName] || []
}
```

## 字段类型

| 类型 | 说明 | 配置项 |
|------|------|--------|
| `daterange` | 日期范围选择器 | span, label, prop, placeholder, defaultValue |
| `brand` | 品牌选择器 | span, label, prop, options, defaultValue |
| `series` | 车系选择器（支持竞品展开） | span, label, prop, options, defaultValue |
| `select` | 下拉选择框 | span, label, prop, options, clearable, multiple, placeholder, defaultValue |
| `input` | 输入框 | span, label, prop, placeholder, defaultValue |
| `placeholder` | 占位符（空白列） | span |

## 完整配置示例

```typescript
{
  // 路由名称作为 key
  CompetitorAnalysis: [
    // 日期范围 - 占满一行
    {
      type: 'daterange',
      prop: 'dateRange',
      label: '日期范围',
      span: 24,
      defaultValue: null
    },
    
    // 品牌 - 占满一行，显示分割线
    {
      type: 'brand',
      prop: 'brand',
      label: '品牌',
      span: 24,
      options: [
        { label: '智行', value: 'zhixing' },
        { label: '远途', value: 'yuantu' }
      ],
      defaultValue: 'yinli',
      showSplitLine: true
    },
    
    // 车系 - 占满一行，显示分割线
    {
      type: 'series',
      prop: 'series',
      label: '车系',
      span: 24,
      options: [
        { label: 'CS75 PLUS', value: 'cs75plus', isCompetitor: false },
        { label: 'CS55PLUS', value: 'cs55plus', isCompetitor: false },
        { label: '哈弗H6', value: 'h6', isCompetitor: true },
        { label: '博越', value: 'boyue', isCompetitor: true }
      ],
      defaultValue: 'cs75plus',
      showSplitLine: true
    },
    
    // 第一行：两个下拉框 + 占位符
    {
      type: 'select',
      prop: 'dataSource',
      label: '数据源',
      span: 6,
      clearable: true,
      options: [{ label: '数据源1', value: 1 }]
    },
    {
      type: 'select',
      prop: 'dataType',
      label: '数据类型',
      span: 6,
      clearable: true,
      options: [{ label: '类型1', value: 1 }]
    },
    {
      type: 'placeholder',
      prop: '',
      span: 12 // 占位12列
    },
    
    // 更多字段...
  ]
}
```

## Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| routeName | 路由名称，用于获取对应配置 | `string` | - |
| dateRangeText | 日期范围显示文本 | `string` | `'近7天'` |

## Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| search | 点击查询按钮 | `(value: Record<string, any>)` |
| reset | 点击重置按钮 | `()` |

## 注意事项

1. **路由名称**：确保传入的 routeName 在 helper.ts 中有对应的配置
2. **占位符使用**：使用 `type: 'placeholder'` 可以实现空白占位，配合 span 实现灵活布局
3. **分割线**：设置 `showSplitLine: true` 会在该字段后显示分割线
4. **默认值**：通过 `defaultValue` 设置字段默认值，重置时会恢复到默认值
5. **车系配置**：车系选项中 `isCompetitor: true` 的会显示在"更多"展开区域
