# 产品分析模块 API 接口

本模块包含了产品分析相关的所有API接口，基于Swagger文档自动生成。

## 文件结构

```
src/api/productAnalysis/
├── index.ts        # API接口实现
├── types.d.ts      # TypeScript类型定义
└── README.md       # 使用说明
```

## 主要接口

### 1. 综合分析简报

- **接口**: `getProductBrief`
- **路径**: `POST /api/report/product-analysis/getProductBrief`
- **说明**: 获取产品综合分析简报数据

### 2. 用户意图观点TOP

- **接口**: `getUserIntentionOpinionTop`
- **路径**: `POST /api/report/product-analysis/getUserIntentionOpinionTop`
- **说明**: 获取用户意图观点TOP数据
- **更新**: 根据最新API文档，返回类型为数组而非分类对象

### 3. 关注场景TOP

- **接口**: `getFocusSceneTop`
- **路径**: `POST /api/report/product-analysis/getFocusSceneTop`
- **说明**: 获取关注场景TOP数据

### 4. 关注场景分析

- **接口**: `getFocusSceneAnalysis`
- **路径**: `POST /api/report/product-analysis/getFocusSceneAnalysis`
- **说明**: 获取关注场景分析数据，包含品牌、车系、标签等维度信息

### 5. 数据趋势变化

- **接口**: `getDataTrendChange`
- **路径**: `POST /api/report/product-analysis/getDataTrendChange`
- **说明**: 获取产品数据趋势变化，包含负面率均值和趋势数据

### 6. 数据来源分析

- **接口**: `getDataSourceAnalysis`
- **路径**: `POST /api/report/product-analysis/getDataSourceAnalysis`
- **说明**: 获取数据来源分析，按渠道统计提及量和负面率

### 7. 渠道负面率趋势变化

- **接口**: `getChannelNegativeTrend`
- **路径**: `POST /api/report/product-analysis/getChannelNegativeTrend`
- **说明**: 获取各渠道负面率趋势变化数据

### 8. 渠道提及量占比

- **接口**: `getChannelMentionShare`
- **路径**: `POST /api/report/product-analysis/getChannelMentionShare`
- **说明**: 获取各渠道提及量占比数据

## 使用示例

```typescript
import {
  getProductBrief,
  getUserIntentionOpinionTop,
  getFocusSceneTop
} from '@/api/productAnalysis'
// VocQueryParams 是全局类型，无需导入

// 查询参数示例
const queryParams: VocQueryParams = {
  dateUnit: 1, // 月度
  startDate: '2023-08-01',
  endDate: '2023-08-31',
  brandCodeSet: ['BRAND001'],
  sentimentSet: ['positive', 'negative'],
  pageNum: 1,
  pageSize: 10
}

// 获取综合分析简报
const getBriefData = async () => {
  try {
    const response = await getProductBrief(queryParams)
    if (response.success) {
      console.log('简报数据:', response.result)
      // 处理数据...
    }
  } catch (error) {
    console.error('获取简报数据失败:', error)
  }
}

// 获取用户意图观点TOP
const getOpinionTop = async () => {
  try {
    const response = await getUserIntentionOpinionTop(queryParams)
    if (response.success) {
      // 新的API返回格式：数组形式
      const opinionList = response.result
      console.log('观点TOP数据:', opinionList)
      opinionList.forEach(item => {
        console.log(`观点: ${item.opinion}, 提及量: ${item.mentions}, 环比: ${item.mentionsMoM}%`)
      })
      // 处理数据...
    }
  } catch (error) {
    console.error('获取观点数据失败:', error)
  }
}

// 获取关注场景TOP
const getSceneTop = async () => {
  try {
    const response = await getFocusSceneTop(queryParams)
    if (response.success) {
      console.log('场景TOP数据:', response.result)
      // 处理数据...
    }
  } catch (error) {
    console.error('获取场景数据失败:', error)
  }
}
```

## 类型说明

### VocQueryParams

产品分析查询参数，包含时间维度、日期范围、品牌、情感、意图等多种筛选条件。基于最新的 ExtendComQueryModel 内容更新，包含了用户意图观点TOP接口的所有参数，统一使用 VocQueryParams 命名。

**主要更新字段**：

- `channelIds`: 渠道ID集合
- `sentimentList`: 情感集合（正面、负面、中性）
- `carSeriesList`: 车系集合
- `labelTypeLevelFirstList`: 一级标签集合
- `intention`: 意图字段（抱怨、投诉、建议、咨询、表扬、陈述）
- `yoy`: 是否比较同比日期范围
- `dateUnit`: 时间维度（1天 2周 3月 4季度 5年）
- 以及更多详细的查询参数...

### ProductBriefVo

综合分析简报数据结构，包含负面率、正面率、提及量、用户数及其环比数据。

### IntentionOpinionTopVo

**重要更新**: 用户意图观点TOP数据结构已更新。根据最新API文档，返回的是观点项数组，而不是分类对象。

**新结构**：

```typescript
interface IntentionOpinionTopVo {
  opinion: string // 观点名
  mentions: number // 提及量
  mentionsMoM: number // 提及量环比，% 两位小数
}
```

**兼容性**: 保留了原有的分类结构 `IntentionOpinionTopGroupVo` 以确保向后兼容。

### SceneTopVo

场景TOP数据结构，包含场景名称、提及量、负面率及其环比数据。

### SceneAnalysisVo

关注场景分析数据结构，包含品牌、车系、标签、提及量、负面率等信息。

### ProductTrendVo

数据趋势变化数据结构，包含负面率均值和趋势数据点。

### DataSourceAnalysisVo

数据来源分析数据结构，包含渠道名称、编码、提及量、负面率及环比数据。

### ChannelNegativeTrendVo

渠道负面率趋势变化数据结构，包含渠道信息、负面率时间序列、时间轴和提及量时间序列。

### ChannelMentionShareVo

渠道提及量占比数据结构，包含渠道信息、提及量和占比。

## 注意事项

1. 所有接口都需要在请求头中携带Authorization token
2. 请求参数中的数组字段（如brandCodeSet）可以为空数组
3. 时间维度参数：-1(日)、0(周)、1(月)、2(季)、3(年)
4. 所有接口返回的数据都遵循BaseResponse<T>格式
5. 环比数据以百分比形式返回，保留两位小数
