# DrillDownTable 组件使用说明

DrillDownTable 是一个功能强大的下钻表格组件，支持多级表头、自定义样式、趋势图展示等功能。该组件已经过性能优化，支持样式缓存和类型安全。

## 功能特性

- ✅ 支持多级表头配置
- ✅ 自定义背景颜色和内间距
- ✅ 内嵌趋势图展示
- ✅ 排序功能
- ✅ 行点击事件
- ✅ 自定义操作列
- ✅ 虚拟滚动支持
- ✅ 加载状态显示
- ✅ **性能优化**：样式缓存机制
- ✅ **类型安全**：完整的 TypeScript 类型定义
- ✅ **错误处理**：完善的边界情况处理

## 组件导入

```typescript
import { DrillDownTable } from '@/components/Business/DrillDownDialog/components/DrillDownTable'
import type { DrillDownTableProps, TableHeaderGroup } from '@/components/Business/DrillDownDialog/components/DrillDownTable/types'
```

## 基本用法

```vue
<template>
  <DrillDownTable
    :data="tableData"
    :columns="tableColumns"
    :height="'500px'"
    :loading="loading"
    @sort-change="handleSortChange"
    @row-click="handleRowClick"
    @trend-click="handleTrendClick"
  >
    <!-- 自定义操作列 -->
    <template #operations="{ row, index }">
      <el-button @click="handleEdit(row, index)">编辑</el-button>
      <el-button @click="handleDelete(row, index)">删除</el-button>
    </template>
  </DrillDownTable>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TableHeaderGroup } from '@/components/Business/DrillDownDialog/components/DrillDownTable/types'

const tableData = ref([
  {
    id: 1,
    name: '产品A',
    mentions: 1234,
    mentionsTrendData: [10, 20, 15, 25, 30],
    satisfaction: 85
  }
])

const tableColumns = ref<TableHeaderGroup[]>([
  {
    key: 'name',
    label: '产品名称',
    width: 150
  },
  {
    key: 'mentions',
    label: '提及量',
    backgroundColor: '#EAF3FF',
    sortable: true,
    columns: [
      {
        key: 'mentions',
        label: '数量',
        width: 100
      },
      {
        key: 'trend',
        label: '趋势',
        render: 'trend',
        width: 120
      }
    ]
  }
])
</script>
```

## Props 配置

### 必需属性

| 属性名 | 类型 | 说明 |
|--------|------|------|
| data | `any[]` | 表格数据 |
| columns | `TableHeaderGroup[]` | 表头配置 |

### 可选属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| title | `string` | - | 表格标题 |
| height | `string` | `'400px'` | 表格高度 |
| headerHeight | `string` | `'56px'` | 表头高度 |
| rowHeight | `string \| number` | `'56px'` | 行高度 |
| headerBackgroundColor | `string` | `'#EAF3FF'` | 默认表头背景色 |
| rowBackgroundColor | `string` | `'#F2F4F7'` | 默认行背景色 |
| stripe | `boolean` | `false` | 是否显示斑马纹 |
| border | `boolean` | `false` | 是否显示边框 |
| loading | `boolean` | `false` | 加载状态 |

## 表头配置 (TableHeaderGroup)

```typescript
// 渲染函数类型
export type RenderFunction = (row: Record<string, any>, column: TableHeaderColumn) => string

// 渲染类型
export type RenderType = 'trend' | RenderFunction

interface TableHeaderGroup {
  key: string                    // 数据字段名
  label: string                  // 显示标题
  width?: string | number        // 列宽度
  minWidth?: string | number     // 最小宽度
  sortable?: boolean             // 是否可排序
  render?: RenderType            // 渲染类型或函数（类型安全）
  backgroundColor?: string | string[]  // 背景色配置
  columnPadding?: string | string[]    // 内间距配置
  rowHeight?: string | number          // 行高度
  columns?: TableHeaderColumn[]        // 子列配置
}
```

### 背景色配置

```typescript
// 单一颜色 - 表头和内容使用相同颜色
backgroundColor: '#EAF3FF'

// 双色配置 - [表头颜色, 内容颜色]
backgroundColor: ['#EAF3FF', '#F2F4F7']
```

### 内间距配置

```typescript
// CSS 标准格式
columnPadding: '0 0 0 25px'

// 数组格式
columnPadding: ['0', '0', '0', '25px']
```

## 事件处理

### 事件列表

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `sortChange` | `(sortBy: string, sortOrder: string)` | 排序变化 |
| `rowClick` | `(row: any, index: number)` | 行点击 |
| `trendClick` | `(row: any)` | 趋势图点击 |

### 事件示例

```typescript
const handleSortChange = (sortBy: string, sortOrder: string) => {
  console.log('排序字段:', sortBy, '排序方向:', sortOrder)
}

const handleRowClick = (row: any, index: number) => {
  console.log('点击行:', row, '索引:', index)
}

const handleTrendClick = (row: any) => {
  console.log('点击趋势图:', row)
}
```

## 插槽使用

### operations 插槽

用于自定义操作列内容：

```vue
<template #operations="{ row, index }">
  <el-button size="small" @click="handleView(row)">查看</el-button>
  <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
</template>
```

## 特殊渲染类型

### 趋势图渲染

设置 `render: 'trend'` 可显示趋势图：

```typescript
{
  key: 'trend',
  label: '趋势',
  render: 'trend',  // 特殊渲染类型
  width: 120
}
```

数据格式要求：
```typescript
{
  mentionsTrendData: [10, 20, 15, 25, 30]  // 趋势数据数组
}
```

### 自定义渲染函数（类型安全）

```typescript
{
  key: 'status',
  label: '状态',
  render: (row: Record<string, any>, column: TableHeaderColumn): string => {
    return `<span class="status-${row.status}">${row.statusText}</span>`
  }
}
```

## 实例方法

通过 ref 获取组件实例，调用以下方法：

```typescript
const tableRef = ref<DrillDownTableInstance>()

// 刷新表格
tableRef.value?.refresh()

// 清空排序
tableRef.value?.clearSort()

// 滚动到顶部
tableRef.value?.scrollToTop()

// 获取表格数据
const data = tableRef.value?.getTableData()
```

## 完整示例

```vue
<template>
  <div class="table-demo">
    <DrillDownTable
      ref="tableRef"
      :data="tableData"
      :columns="columns"
      :height="'600px'"
      :loading="loading"
      :stripe="true"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      @trend-click="handleTrendClick"
    >
      <template #operations="{ row, index }">
        <el-button size="small" @click="handleDetail(row)">详情</el-button>
      </template>
    </DrillDownTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { TableHeaderGroup, DrillDownTableInstance } from './types'

const tableRef = ref<DrillDownTableInstance>()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    brand: '品牌A',
    mentions: 1234,
    mentionsTrendData: [100, 120, 110, 130, 125],
    satisfaction: 85.5,
    growth: 12.3
  }
])

const columns = ref<TableHeaderGroup[]>([
  {
    key: 'brand',
    label: '品牌名称',
    width: 150,
    backgroundColor: '#F0F9FF'
  },
  {
    key: 'mentions',
    label: '提及分析',
    backgroundColor: ['#EAF3FF', '#F2F4F7'],
    columnPadding: '0 0 0 25px',
    columns: [
      {
        key: 'mentions',
        label: '提及量',
        width: 100,
        sortable: true
      },
      {
        key: 'trend',
        label: '趋势',
        render: 'trend',
        width: 120
      },
      {
        key: 'growth',
        label: '增长率(%)',
        width: 100,
        render: (row: any) => `${row.growth > 0 ? '+' : ''}${row.growth}%`
      }
    ]
  },
  {
    key: 'Operations',
    label: '操作',
    width: 120
  }
])

const handleSortChange = (sortBy: string, sortOrder: string) => {
  console.log('排序变化:', { sortBy, sortOrder })
}

const handleRowClick = (row: any, index: number) => {
  console.log('行点击:', { row, index })
}

const handleTrendClick = (row: any) => {
  console.log('趋势图点击:', row)
}

const handleDetail = (row: any) => {
  console.log('查看详情:', row)
}

onMounted(() => {
  // 组件挂载后可以调用实例方法
  console.log('表格数据:', tableRef.value?.getTableData())
})
</script>
```

## 性能优化特性

### 样式缓存机制

组件采用智能缓存策略，避免重复计算相同的样式配置：

```typescript
// 自动缓存样式计算结果
const dynamicStyles = computed(() => {
  // 生成缓存键，只有配置变化时才重新计算
  const cacheKey = generateStyleCacheKey(props.columns)
  
  // 检查缓存，命中则直接返回
  if (styleCache.value.has(cacheKey)) {
    return styleCache.value.get(cacheKey)!
  }
  
  // 计算新样式并缓存
  // ...
})
```

### 错误边界处理

组件内置完善的错误处理机制：

- 数据验证和边界检查
- 渲染函数异常捕获
- 颜色值格式验证
- 索引越界保护

## 注意事项

1. **数据格式**: 确保传入的 data 数组中每个对象都包含 columns 配置中定义的字段
2. **趋势图数据**: 使用趋势图渲染时，数据对象需包含 mentionsTrendData 字段
3. **背景色**: 支持十六进制颜色值，组件会自动验证和生成对应的 CSS 类名
4. **性能优化**: 组件已内置样式缓存，大数据量场景下表现良好
5. **类型安全**: 使用 TypeScript 时，请确保数据类型符合接口定义
6. **样式覆盖**: 组件使用动态样式注入，避免与全局样式冲突
7. **错误处理**: 组件会在控制台输出警告信息，便于调试

## 类型定义

详细的类型定义请参考 `types.d.ts` 文件，包含完整的接口定义和类型约束。