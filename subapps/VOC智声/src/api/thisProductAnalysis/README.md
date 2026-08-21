# 本品分析模块 API

本模块提供本品分析相关的API接口，用于获取本品（自有产品）的各种分析数据。

## 接口列表

### 1. 用户旅程分析

- **接口名称**: `getUserJourneyAnalysis`
- **请求路径**: `/api/report/product-self-analysis/user-journey-analysis`
- **功能描述**: 根据时间范围等条件统计旅程维度指标
- **返回数据**: 包含旅程名称、提及量、负面率、客户满意/不满Top5等数据

### 2. 综合分析简报

- **接口名称**: `getProductSelfBrief`
- **请求路径**: `/api/report/product-self-analysis/getProductBrief`
- **功能描述**: 获取本品综合分析简报数据
- **返回数据**: 包含负面率、正面率、提及量、用户数及其环比同比数据

### 3. 关注场景TOP

- **接口名称**: `getFocusSceneTop`
- **请求路径**: `/api/report/product-self-analysis/getFocusSceneTop`
- **功能描述**: 获取关注场景TOP排行数据
- **返回数据**: 包含场景名称、提及量、负面率及环比数据组

### 4. 数据趋势变化

- **接口名称**: `getDataTrendChange`
- **请求路径**: `/api/report/product-self-analysis/getDataTrendChange`
- **功能描述**: 获取数据趋势变化分析
- **返回数据**: 包含负面率均值和趋势数据点

### 5. 渠道数据排行

- **接口名称**: `getDataSourceAnalysis`
- **请求路径**: `/api/report/product-self-analysis/getDataSourceAnalysis`
- **功能描述**: 获取渠道数据排行分析
- **返回数据**: 包含渠道名称、提及量、负面率及环比同比数据

### 6. 渠道负面率趋势变化

- **接口名称**: `getChannelNegativeTrend`
- **请求路径**: `/api/report/product-self-analysis/getChannelNegativeTrend`
- **功能描述**: 获取渠道负面率趋势变化数据
- **返回数据**: 包含日期和渠道数据的趋势变化

### 7. 渠道提及量占比

- **接口名称**: `getChannelMentionShare`
- **请求路径**: `/api/report/product-self-analysis/getChannelMentionShare`
- **功能描述**: 获取渠道提及量占比数据
- **返回数据**: 包含渠道名称、提及量和占比

### 8. 服务标签分析

- **接口名称**: `getServiceTagAnalysis`
- **请求路径**: `/api/report/product-self-analysis/get-service-tag-analysis`
- **功能描述**: 获取服务标签分析数据
- **返回数据**: 包含品牌、标签和分析值数据

### 9. 产品标签分析

- **接口名称**: `getProductTagAnalysis`
- **请求路径**: `/api/report/product-self-analysis/get-product-tag-analysis`
- **功能描述**: 获取产品标签分析数据
- **返回数据**: 包含品牌、标签和分析值数据

## 使用示例

```typescript
import {
  getUserJourneyAnalysis,
  getProductSelfBrief,
  getFocusSceneTop,
  getDataTrendChange,
  getDataSourceAnalysis,
  getChannelNegativeTrend,
  getChannelMentionShare,
  getServiceTagAnalysis,
  getProductTagAnalysis
} from '@/api/thisProductAnalysis'

// 获取用户旅程分析数据
const journeyData = await getUserJourneyAnalysis({
  startDate: '2025-08-04',
  endDate: '2025-09-03',
  sortField: 'publish_time',
  sortOrder: 'desc'
})

// 获取综合分析简报
const briefData = await getProductSelfBrief({
  startDate: '2025-08-04',
  endDate: '2025-09-03'
})

// 获取关注场景TOP
const sceneTopData = await getFocusSceneTop({
  startDate: '2025-08-04',
  endDate: '2025-09-03'
})
```

## 请求参数

所有接口都使用全局的 `VocQueryParams` 类型作为请求参数，包含以下字段：

- `startDate`: 时间范围-开始时间 (必填)
- `endDate`: 时间范围-结束时间 (必填)
- `sortField`: 排序字段 (可选)
- `sortOrder`: 排序类型：asc/desc (可选)
- `dataType`: 数据类型 (可选)
- `tag1Code`: 标签一级编码 (可选)
- `tag2Code`: 标签二级编码 (可选)
- `tag3Code`: 标签三级编码 (可选)
- `tag4Code`: 标签四级编码 (可选)
- `provinceCodeSet`: 省份编码，可多选 (可选)
- `sourceChannelClass`: 来源渠道分类 (可选)

## 注意事项

1. 所有接口都需要在请求头中携带 Authorization token
2. 所有百分比数据都保留两位小数
3. 环比同比数据的计算基于时间范围设置
4. 接口返回的数据结构遵循项目统一的 `BaseResponse<T>` 格式

## 文件结构

```
src/api/thisProductAnalysis/
├── index.ts          # API接口定义
├── types.d.ts        # TypeScript类型定义
├── README.md         # 文档说明
└── example.vue       # 使用示例组件
```

## 数据来源

本模块的接口定义基于 Swagger 文档：

- **文档来源**: VOC智声本地兼容接口中的本品分析契约
- **创建时间**: 2025-09-05
- **数据获取方式**: 使用 Playwright 自动化工具读取 Swagger 文档

## 技术特点

1. **完整的类型支持**: 所有接口都有详细的 TypeScript 类型定义
2. **统一的响应格式**: 使用项目全局的 `BaseResponse<T>` 类型
3. **标准化参数**: 使用全局的 `VocQueryParams` 类型作为请求参数
4. **详细的文档**: 包含接口说明、使用示例和注意事项
5. **代码质量**: 通过 ESLint 和 TypeScript 检查，无语法错误
