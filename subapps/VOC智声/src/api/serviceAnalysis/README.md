# 服务分析模块 API 接口文档

> 基于 Swagger 文档自动生成的服务分析模块接口定义

## 📋 接口列表

### 1. 用户意图观点TOP
- **接口路径**: `/api/report/service-analysis/getUserIntentionOpinionTop`
- **请求方法**: `POST`
- **功能描述**: 获取用户意图观点TOP数据
- **返回类型**: `ServiceIntentionOpinionTopVo[]`

### 2. 省份排行
- **接口路径**: `/api/report/service-analysis/getProvinceRank`
- **请求方法**: `POST`
- **功能描述**: 获取省份排行数据
- **返回类型**: `ServiceProvinceRankVo[]`

### 3. 综合分析简报
- **接口路径**: `/api/report/service-analysis/getProductBrief`
- **请求方法**: `POST`
- **功能描述**: 获取综合分析简报数据
- **返回类型**: `ServiceProductBriefVo`

### 4. 关注场景TOP
- **接口路径**: `/api/report/service-analysis/getFocusSceneTop`
- **请求方法**: `POST`
- **功能描述**: 获取关注场景TOP数据
- **返回类型**: `ServiceSceneTopVo[]`

### 5. 关注场景分析
- **接口路径**: `/api/report/service-analysis/getFocusSceneAnalysis`
- **请求方法**: `POST`
- **功能描述**: 获取关注场景分析数据
- **返回类型**: `ServiceSceneAnalysisVo[]`

### 6. 经销商评价排行TOP
- **接口路径**: `/api/report/service-analysis/getDealerRankTop`
- **请求方法**: `POST`
- **功能描述**: 获取经销商评价排行TOP数据
- **返回类型**: `ServiceDealerRankTopVo[]`

### 7. 数据趋势变化
- **接口路径**: `/api/report/service-analysis/getDataTrendChange`
- **请求方法**: `POST`
- **功能描述**: 获取数据趋势变化数据
- **返回类型**: `ServiceTrendVo`

### 8. 数据来源分析
- **接口路径**: `/api/report/service-analysis/getDataSourceAnalysis`
- **请求方法**: `POST`
- **功能描述**: 获取数据来源分析数据
- **返回类型**: `ServiceDataSourceAnalysisVo[]`

### 9. 渠道负面率趋势变化
- **接口路径**: `/api/report/service-analysis/getChannelNegativeTrend`
- **请求方法**: `POST`
- **功能描述**: 获取渠道负面率趋势变化数据
- **返回类型**: `ServiceChannelNegativeTrendVo[]`

### 10. 渠道提及量占比
- **接口路径**: `/api/report/service-analysis/getChannelMentionShare`
- **请求方法**: `POST`
- **功能描述**: 获取渠道提及量占比数据
- **返回类型**: `ServiceChannelMentionShareVo[]`

## 📝 请求参数

所有接口都使用统一的 `VocQueryParams` 类型作为请求参数，该类型包含以下主要字段：

- `startDate`: 开始日期 (string)
- `endDate`: 结束日期 (string)
- `userId`: 用户ID (string)
- `clientId`: 客户端ID (string)
- `channelIds`: 渠道ID集合 (string[])
- `areaIds`: 区域ID集合 (string[])
- `carSeriesList`: 车系集合 (string[])
- `brandCodeList`: 品牌编码集合 (string[])
- `labelTypeLevelFirstList`: 一级标签集合 (string[])
- `labelTypeLevelSecondList`: 二级标签集合 (string[])
- `sentimentList`: 情感集合 (string[])
- 以及其他扩展查询条件...

## 🔧 使用示例

```typescript
import { getUserIntentionOpinionTop, getProvinceRank } from '@/api/serviceAnalysis'

// 获取用户意图观点TOP
const getOpinionData = async () => {
  try {
    const params: VocQueryParams = {
      startDate: '2025-01-01',
      endDate: '2025-08-01',
      // ... 其他参数
    }
    const response = await getUserIntentionOpinionTop(params)
    console.log('用户意图观点TOP数据:', response.result)
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

// 获取省份排行
const getProvinceData = async () => {
  try {
    const params: VocQueryParams = {
      startDate: '2025-01-01',
      endDate: '2025-08-01',
      // ... 其他参数
    }
    const response = await getProvinceRank(params)
    console.log('省份排行数据:', response.result)
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}
```

## 📊 数据结构

详细的数据结构定义请参考 `types.d.ts` 文件，包含了所有接口的请求和响应类型定义。

## 🔗 相关文件

- `index.ts` - 接口函数定义
- `types.d.ts` - TypeScript 类型定义
- `README.md` - 本文档

---

**注意**: 本模块按 VOC智声本地兼容接口中的服务分析契约维护。
