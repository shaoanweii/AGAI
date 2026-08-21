# 旅程分析页面实现

## 📋 功能概述

本次实现完成了旅程分析页面的核心功能，包括：

1. **综合分析模块** - 调用3个接口，传递数据给ComprehensiveAnalysis公共组件
2. **数据来源分析模块** - 调用3个接口，传递数据给DataSourceAnalysis公共组件
3. **渠道数据趋势切换** - 实现SwitchButton时间切换功能

## 🔧 实现详情

### 接口调用

#### 综合分析相关接口

- `getProductBrief` - 数据简报
- `getDataTrendChange` - 数据趋势变化
- `getFocusSceneTop` - 关注场景TOP

#### 数据来源分析相关接口

- `getDataSourceAnalysis` - 渠道数据排行
- `getChannelNegativeTrend` - 渠道数据趋势
- `getChannelMentionShare` - 渠道提及量占比

### 核心功能

#### 1. 数据获取与传递

```typescript
// 综合分析数据传递
<ComprehensiveAnalysis
  :product-brief-data="productBriefData"
  :focus-scene-top-data="focusSceneTopData"
  :data-trend-change-data="dataTrendChangeData"
  @scene-top-sort="handleSceneTopSort"
/>

// 数据来源分析数据传递
<DataSourceAnalysis
  :channel-mention-share-data="channelMentionShareData"
  :channel-negative-trend-data="channelNegativeTrendData"
  :data-source-analysis-data="dataSourceAnalysisData"
  @data-type-change="handleChannelTrendDataTypeChange"
/>
```

#### 2. 事件处理

- **关注场景TOP排序** - `handleSceneTopSort` 处理排序变化
- **渠道数据趋势切换** - `handleChannelTrendDataTypeChange` 处理负面率/提及量切换

#### 3. 参数传递方式

直接使用 `VocQueryParams` 类型作为接口参数：

```typescript
// 基础参数调用
const response = await getProductBrief(queryStore.defaultQueryParams)

// 带额外参数的调用
const params = {
  ...queryStore.defaultQueryParams,
  dataType: 'negativeRate'
}
const response = await getChannelNegativeTrend(params)
```

## 📁 文件结构

```
src/components/Business/Scene/JourneyAnalysis/
├── index.vue              # 主页面组件
├── index.test.ts          # 单元测试
└── README.md              # 说明文档
```

## 🧪 测试覆盖

- ✅ 组件正确渲染
- ✅ 组件挂载时调用所有必要的API
- ✅ 数据传递给子组件
- ✅ 事件处理器正确绑定

## 🔄 数据流

1. **组件挂载** → 调用6个API接口获取数据
2. **数据获取** → 更新响应式数据状态
3. **数据传递** → 将数据传递给公共组件
4. **事件处理** → 响应用户交互，重新获取数据

## 📝 注意事项

1. **API参数类型** - 除第一个 `callCommonReport` 接口外，其余接口都使用 `VocQueryParams` 作为入参
2. **类型兼容性** - 使用类型断言 `as any` 解决旅程分析类型与公共组件类型的兼容性问题
3. **错误处理** - 所有API调用都包含错误处理逻辑
4. **参数传递** - 直接使用 `queryStore.defaultQueryParams` 或扩展参数对象
5. **事件绑定** - 正确绑定排序和切换事件处理器

## 🚀 使用方式

组件会在挂载时自动获取所有必要的数据，用户可以：

- 查看综合分析数据（简报、趋势、场景TOP）
- 查看数据来源分析（渠道占比、趋势、排行）
- 通过SwitchButton切换负面率/提及量视图
- 通过表格排序功能调整场景TOP显示

## 🔗 相关文件

- API接口：`src/api/journeyAnalysis/index.ts`
- 类型定义：`src/api/journeyAnalysis/types.d.ts`
- 公共组件：
  - `src/components/Business/Scene/Common/ComprehensiveAnalysis/index.vue`
  - `src/components/Business/Scene/Common/DataSourceAnalysis/index.vue`
