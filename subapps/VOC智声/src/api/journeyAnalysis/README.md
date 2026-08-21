# 旅程分析模块 API 接口

本模块包含了旅程分析相关的通用报表调用API接口，基于Swagger文档自动生成。

## 文件结构

```
src/api/journeyAnalysis/
├── index.ts        # API接口实现
├── types.d.ts      # TypeScript类型定义
└── README.md       # 使用说明
```

## 主要接口

### 通用报表调用

- **接口**: `callCommonReport`
- **路径**: `POST /api/report/report-analysis/invoke/call`
- **说明**: 通用报表调用接口，支持三种查询方式：
  - requestType=1: 简单查询（基于表名和字段）
  - requestType=2: XML查询（通过指定XML语句ID）
  - requestType=3: 解析类查询（通过指定解析类）

---

### 旅程分析专用接口

以下接口是从API文档中读取的旅程分析模块专用接口：

#### 用户分析类

- **发声用户TOP5**: `getVoiceUserTop` - `POST /report/journey-analysis/getVoiceUserTop`
- **用户类型占比**: `getUserTypeDistribution` - `POST /report/journey-analysis/getUserTypeDistribution`
- **用户性别占比**: `getGenderDistribution` - `POST /report/journey-analysis/getGenderDistribution`
- **各年龄段占比**: `getAgeDistribution` - `POST /report/journey-analysis/getAgeDistribution`

#### 场景分析类

- **用户关注场景TOP10**: `getUserFocusSceneTop` - `POST /report/journey-analysis/getUserFocusSceneTop`
- **飙升场景TOP5**: `getSurgingSceneTop` - `POST /report/journey-analysis/getSurgingSceneTop`
- **高频场景TOP5**: `getHighFreqSceneTop` - `POST /report/journey-analysis/getHighFreqSceneTop`
- **关注场景TOP**: `getFocusSceneTop` - `POST /report/journey-analysis/getFocusSceneTop_3`

#### 观点分析类

- **意图观点TOP**: `getUserIntentionOpinionTop` - `POST /report/journey-analysis/getUserIntentionOpinionTop`

#### 地域分析类

- **所在区域占比**: `getRegionDistribution` - `POST /report/journey-analysis/getRegionDistribution`

#### 渠道分析类

- **渠道数据排行**: `getDataSourceAnalysis` - `POST /report/journey-analysis/getDataSourceAnalysis_3`
- **渠道负面率趋势变化**: `getChannelNegativeTrend` - `POST /report/journey-analysis/getChannelNegativeTrend_3`
- **渠道提及量占比**: `getChannelMentionShare` - `POST /report/journey-analysis/getChannelMentionShare_3`

#### 综合分析类

- **综合分析简报**: `getProductBrief` - `POST /report/journey-analysis/getProductBrief_3`
- **旅程细化分析**: `getJourneyDetailAnalysis` - `POST /report/journey-analysis/getJourneyDetailAnalysis`
- **数据趋势变化**: `getDataTrendChange` - `POST /report/journey-analysis/getDataTrendChange_3`

## 使用示例

### 通用报表调用示例

```typescript
import { callCommonReport } from '@/api/journeyAnalysis'
import type { CommonReportInvokeModel, CommonFilterModel } from '@/api/journeyAnalysis/types'

// 1. 简单查询示例（requestType=1）
const getSimpleData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 1,
    sourceTable: 'dw_table',
    displayFields: ['col1', 'col2'],
    filterData: {
      startDate: '2025-01-01',
      endDate: '2025-08-01',
      sortField: 'publish_time',
      sortOrder: 'desc',
      dataType: 'negativeRate'
    }
  }

  try {
    const response = await callCommonReport(params)
    if (response.success) {
      console.log('简单查询结果:', response.result)
    }
  } catch (error) {
    console.error('简单查询失败:', error)
  }
}

// 2. XML查询示例（requestType=2）
const getXmlData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 2,
    sqlId: 'simpleSelect',
    filterData: {
      startDate: '2025-01-01',
      endDate: '2025-08-01',
      sortField: 'publish_time',
      sortOrder: 'desc'
    }
  }

  try {
    const response = await callCommonReport(params)
    if (response.success) {
      console.log('XML查询结果:', response.result)
    }
  } catch (error) {
    console.error('XML查询失败:', error)
  }
}

// 3. 解析类查询示例（requestType=3）
const getParserData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 3,
    sqlId: 'simpleSelect',
    parserClass: 'com.xx.ParserImpl',
    filterData: {
      startDate: '2025-01-01',
      endDate: '2025-08-01'
    }
  }

  try {
    const response = await callCommonReport(params)
    if (response.success) {
      console.log('解析类查询结果:', response.result)
    }
  } catch (error) {
    console.error('解析类查询失败:', error)
  }
}
```

### 旅程分析专用接口使用示例

```typescript
import {
  getVoiceUserTop,
  getUserTypeDistribution,
  getJourneyDetailAnalysis,
  getRegionDistribution,
  getGenderDistribution
} from '@/api/journeyAnalysis'
import type { CommonReportInvokeModel } from '@/api/journeyAnalysis/types'

// 获取发声用户TOP5
const getVoiceUserTopData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 1,
    filterData: {
      startDate: '2025-08-04',
      endDate: '2025-09-03',
      sortField: 'publish_time',
      sortOrder: 'desc',
      dataType: 'mention'
    }
  }

  try {
    const response = await getVoiceUserTop(params)
    if (response.success) {
      console.log('发声用户TOP5:', response.result)
      // response.result 类型为 VoiceUserTopVo[]
      response.result.forEach(user => {
        console.log(`用户: ${user.userName}, 提及量: ${user.value}, 负面率: ${user.negativeRate}%`)
      })
    }
  } catch (error) {
    console.error('获取发声用户TOP5失败:', error)
  }
}

// 获取用户类型占比
const getUserTypeData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 1,
    filterData: {
      startDate: '2025-08-04',
      endDate: '2025-09-03'
    }
  }

  try {
    const response = await getUserTypeDistribution(params)
    if (response.success) {
      console.log('用户类型占比:', response.result)
      // response.result 类型为 UserTypeDistributionVo[]
    }
  } catch (error) {
    console.error('获取用户类型占比失败:', error)
  }
}

// 获取旅程细化分析
const getJourneyDetailData = async () => {
  const params: CommonReportInvokeModel = {
    requestType: 1,
    filterData: {
      startDate: '2025-08-04',
      endDate: '2025-09-03',
      dataType: 'negativeRate'
    }
  }

  try {
    const response = await getJourneyDetailAnalysis(params)
    if (response.success) {
      console.log('旅程细化分析:', response.result)
      // response.result 类型为 JourneyDetailAnalysisVo[]
      response.result.forEach(item => {
        console.log(`标签: ${item.tagName}, 层级: ${item.tagLevel}, 数值: ${item.value}`)
      })
    }
  } catch (error) {
    console.error('获取旅程细化分析失败:', error)
  }
}
```

## 类型说明

### 基础类型

#### CommonFilterModel

通用过滤模型，包含时间范围、排序和数据类型等过滤条件。

**主要字段**：

- `startDate`: 开始时间（必填，格式：yyyy-MM-dd）
- `endDate`: 结束时间（必填，格式：yyyy-MM-dd）
- `sortField`: 排序字段（可选）
- `sortOrder`: 排序类型，'asc' | 'desc'（可选）
- `dataType`: 数据类型（可选）

### CommonReportInvokeModel

通用报表调用入参模型，支持三种请求类型。

**主要字段**：

- `requestType`: 请求类型（1=简单查询, 2=指定XML, 3=指定解析类）
- `sourceTable`: 来源表名（requestType=1时必填）
- `displayFields`: 显示字段（requestType=1时必填）
- `sqlId`: XML语句ID（requestType=2或3时可用）
- `parserClass`: 解析处理类（requestType=3时必填）
- `filterData`: 过滤数据
- `sortField`: 排序字段
- `sortOrder`: 排序方向

### ResultObject

通用报表调用响应结果。

**字段说明**：

- `success`: 成功标志
- `message`: 返回处理消息
- `code`: 返回代码
- `result`: 返回数据对象（any类型，具体结构取决于查询内容）
- `tid`: 请求标识

### 旅程分析响应类型

#### VoiceUserTopVo - 发声用户TOP5

- `userName`: 用户名
- `userId`: 用户ID
- `value`: 提及量
- `negativeRate`: 负面率(%)
- `valueMoM`: 环比(%)
- `valueYoY`: 同比(%)

#### JourneyDetailAnalysisVo - 旅程细化分析

- `tagName`: 标签名称
- `tagCode`: 标签编码
- `tagLevel`: 标签层级(1/2/3/4)
- `value`: 负面率/提及量
- `valueAvg`: 负面率/提及量均值
- `valueMoM`: 负面率/提及量环比(%)
- `valueYoY`: 负面率/提及量同比(%)

#### UserTypeDistributionVo - 用户类型占比

- `userType`: 用户类型
- `value`: 用户数量
- `percent`: 占比(%)
- `valueMoM`: 环比(%)
- `valueYoY`: 同比(%)

#### IntentionOpinionTopVo - 意图观点TOP

- `opinion`: 观点名
- `mentions`: 提及量
- `mentionsMoM`: 提及量环比，% 两位小数
- `mentionsYoY`: 提及量同比，% 两位小数
- `sound`: 最新声音信息 (LatestSoundVo)

#### LatestSoundVo - 最新声音信息

- `id`: 声音ID
- `soundContent`: 原声内容
- `tags`: 标签数组
- `userName`: 用户名称

#### RegionDistributionVo - 所在区域占比

- `regionName`: 区域名称
- `percentage`: 占比(%)
- `count`: 数量

#### GenderDistributionVo - 用户性别占比

- `gender`: 性别
- `percentage`: 占比(%)
- `count`: 数量

#### AgeDistributionVo - 各年龄段占比

- `ageGroup`: 年龄段
- `percentage`: 占比(%)
- `count`: 数量

#### UserFocusSceneTopVo - 用户关注场景TOP

- `sceneName`: 场景名称
- `value`: 用户数量
- `valueMoM`: 环比(%)
- `valueYoY`: 同比(%)

#### SurgingSceneTopVo - 飙升场景TOP

- `sceneName`: 场景名称
- `surgingIndex`: 飙升指数
- `growthRate`: 增长率(%)

#### HighFreqSceneTopVo - 高频场景TOP

- `sceneName`: 场景名称
- `frequency`: 频次
- `percentage`: 占比(%)

#### 其他响应类型

更多响应类型详见 `types.d.ts` 文件中的完整定义。

## 数据类型选项

`dataType` 字段支持以下选项：

- `negativeRate`: 负面率
- `mention`: 提及量
- `brand`: 品牌
- `series`: 车系
- `negativeRateMention`: 负面率+提及量
- `negativeRateMoM`: 负面率+负面率环比
- `mentionMoM`: 提及量+提及量环比

## 注意事项

### 通用注意事项

1. 所有接口都需要在请求头中携带Authorization token
2. `startDate` 和 `endDate` 是必填字段，格式为 yyyy-MM-dd
3. 不同的 `requestType` 需要不同的必填参数：
   - requestType=1: 需要 `sourceTable` 和 `displayFields`
   - requestType=2: 需要 `sqlId`
   - requestType=3: 需要 `sqlId` 和 `parserClass`
4. `sqlId` 可以传入完整的 namespace.id，或仅传入 id（使用默认namespace）
5. `parserClass` 需要实现 ReportResultParser 接口
6. 所有接口返回的数据都遵循 BaseResponse<T> 格式

### 旅程分析接口注意事项

1. **接口来源**: 所有旅程分析专用接口都是从API文档中读取并生成的
2. **参数统一**: 所有旅程分析接口都使用 `CommonReportInvokeModel` 作为请求参数
3. **路径规范**: 所有接口路径都以 `/report/journey-analysis/` 开头（已移除 `/api` 前缀）
4. **类型安全**: 每个接口都有对应的强类型响应定义，确保类型安全
5. **扩展查询**: 所有接口都支持扩展公共查询条件Model (ExtendComQueryModel)
6. **过滤条件**: 支持多种过滤条件：
   - 时间范围过滤 (startDate, endDate)
   - 标签过滤 (tag1Code, tag2Code, tag3Code, tag4Code)
   - 品牌过滤 (brandCode)
   - 省份过滤 (provinceCodeSet)
   - 渠道分类过滤 (sourceChannelClass)
   - 数据类型过滤 (dataType)
7. **排序支持**: 支持按指定字段排序 (sortField, sortOrder)
8. **版本标识**: 部分接口带有版本后缀 (\_2, \_3)，表示接口的不同版本
