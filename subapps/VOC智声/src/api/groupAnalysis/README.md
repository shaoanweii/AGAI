# 集团分析模块API接口

> 集团分析模块提供集团层面的数据分析功能，包括综合分析简报、服务口碑分析、产品分析、观点评价、数据来源分析、品牌趋势变化和车系排行等功能。

## 📋 接口列表

### 1. 综合分析简报

- **接口名称**: `getGroupProductBrief`
- **请求路径**: `POST /api/report/group-analysis/getProductBrief`
- **功能描述**: 获取集团综合分析简报数据，包括负面率、正面率、提及量、用户数等关键指标及其环比、同比数据
- **返回类型**: `ResultProductBriefVo`

### 2. 服务口碑分析

- **接口名称**: `getServiceReputationAnalysis`
- **请求路径**: `POST /api/report/group-analysis/get-service-reputation-analysis`
- **功能描述**: 获取服务口碑分析数据，按品牌和标签维度分析服务相关的口碑表现
- **返回类型**: `ResultListTagAnalysisRowVo`

### 3. 产品分析

- **接口名称**: `getProductTagAnalysis`
- **请求路径**: `POST /api/report/group-analysis/get-product-tag-analysis`
- **功能描述**: 获取产品分析数据，按品牌和标签维度分析产品相关的表现
- **返回类型**: `ResultListTagAnalysisRowVo`

### 4. 观点评价

- **接口名称**: `getOpinionEvaluation`
- **请求路径**: `POST /api/report/group-analysis/get-opinion-evaluation`
- **功能描述**: 获取观点评价数据，分析各观点的提及量和负面率表现
- **返回类型**: `ResultListOpinionEvaluationVo`

### 5. 数据来源分析

- **接口名称**: `getGroupDataSourceAnalysis`
- **请求路径**: `POST /api/report/group-analysis/get-data-source-analysis`
- **功能描述**: 获取集团数据来源分析，按渠道维度分析数据来源分布和表现
- **返回类型**: `ResultListGroupDataSourceAnalysisVo`

### 6. 品牌趋势变化

- **接口名称**: `getBrandTrendChange`
- **请求路径**: `POST /api/report/group-analysis/get-brand-trend-change`
- **功能描述**: 获取品牌趋势变化数据，展示时间序列的趋势变化情况
- **返回类型**: `ResultBrandTrendChangeVo`

### 7. 集团车系排行

- **接口名称**: `getBrandSeriesRank`
- **请求路径**: `POST /api/report/group-analysis/get-brand-series-rank`
- **功能描述**: 获取集团车系排行数据，按车系维度进行排行分析
- **返回类型**: `ResultListSeriesRankItemVo`

## 🔧 使用示例

```typescript
import {
  getGroupProductBrief,
  getServiceReputationAnalysis,
  getProductTagAnalysis,
  getOpinionEvaluation,
  getGroupDataSourceAnalysis,
  getBrandTrendChange,
  getBrandSeriesRank
} from '@/api/groupAnalysis'

// 获取集团综合分析简报
const briefData = await getGroupProductBrief({
  startDate: '2025-01-01',
  endDate: '2025-08-01'
  // 其他查询参数...
})

// 获取服务口碑分析
const serviceData = await getServiceReputationAnalysis({
  startDate: '2025-01-01',
  endDate: '2025-08-01',
  dataType: 'negativeRate'
  // 其他查询参数...
})
```

## 📊 数据类型说明

### 查询参数

所有接口都使用全局的 `VocQueryParams` 类型，包含以下主要字段：

- `startDate`: 开始时间
- `endDate`: 结束时间
- `dataType`: 数据类型（negativeRate/mention等）
- `sortField`: 排序字段
- `sortOrder`: 排序方式（asc/desc）
- `items`: 自定义过滤条件
- `tag1Code`~`tag4Code`: 标签编码
- `provinceCodeSet`: 省份编码集合
- `sourceChannelClass`: 来源渠道分类

### 响应数据

所有接口返回数据都遵循统一的 `BaseResponse<T>` 格式：

```typescript
{
  success: boolean // 成功标志
  message: string // 返回消息
  code: string // 返回代码
  result: T // 具体数据
  tid: string // 请求标识
}
```

## 🚀 特性

- ✅ 完整的 TypeScript 类型支持
- ✅ 统一的错误处理机制
- ✅ 支持复杂的查询条件
- ✅ 支持环比、同比数据分析
- ✅ 多维度数据分析支持
- ✅ 与项目整体架构保持一致

## 📝 注意事项

1. 所有接口都需要在请求头中携带 `Authorization: Bearer [token]`
2. 请求参数中的时间格式为 `YYYY-MM-DD`
3. 百分比数据保留两位小数
4. 接口路径前缀为 `/api/report/group-analysis/`
5. 所有接口都使用 POST 方法，参数通过请求体传递
