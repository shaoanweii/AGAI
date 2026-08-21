# 原声详情原文高亮逻辑说明

## 概述

本文说明 `#/selfService/originalSoundQuery` 页面中，「结果数据」Tab 的原声详情原文高亮逻辑。

当前高亮逻辑位于右侧详情组件 `src/components/Business/VoiceListPanel/TheDetails.vue`，核心工具函数位于 `src/utils/voiceTopicHighlight.ts`。

## 页面与接口链路

### 页面入口

`src/views/selfService/originalSoundQuery/index.vue`

「结果数据」Tab 复用 `VoiceListPanel`：

```vue
<VoiceList
  ref="resultDataVoiceListRef"
  key="ResultDataList"
  class="el-card"
  :queryParams="resultDataSearchParams"
  v-bind="resultDataVoiceListProps"
/>
```

`VoiceListPanel` 默认使用领导版接口：

```ts
listApiUrl: '/report/vocLeadership/getVocListSounds'
detailApiUrl: '/report/vocLeadership/getSoundsDetails'
```

### 详情接口

原声详情接口：

```http
POST /report/vocLeadership/getSoundsDetails
```

详情请求位置：`src/components/Business/VoiceListPanel/TheDetails.vue`

请求参数来自当前选中的列表项：

```ts
const queryParams = getRealAttr({
  newId: props.curItem.id,
  originalId: props.curItem.originalId
})
```

## 高亮字段来源

### 完整原文

详情正文展示与高亮的完整原文来自：

```ts
result.originalTextScene
```

该字段传入 `buildVoiceTopicHighlightHtml` 作为 `originalText`：

```ts
const highlightedOriginalTextHtml = computed(() =>
  buildVoiceTopicHighlightHtml({
    originalText: curDetail.value.originalTextScene,
    activeTopic: activeTopic.value
  })
)
```

最终通过 `v-html` 渲染：

```vue
<div class="detail-text-content ml-8 flex-1" v-html="highlightedOriginalTextHtml"></div>
```

### 观点与匹配片段

详情中的「识别观点」列表来源优先级如下：

```ts
curDetail.value?.soundslist
props.curItem?.soundslist
curDetail.value?.topics
props.curItem?.topics
```

其中高亮匹配真正使用的不是 `topic` 观点名称，而是当前观点对象里的原声片段字段：

```ts
topic.originalTexTScene || topic.originalTextScene
```

字段提取逻辑：

```ts
export const getVoiceTopicSceneText = (topic?: VoiceTopicHighlightItem | null) => {
  return normalizeLineBreak(
    String(topic?.originalTexTScene || topic?.originalTextScene || '')
  ).trim()
}
```

## 当前高亮流程

1. 详情接口返回 `originalTextScene`、`soundslist`、`topics` 等字段。
2. 前端优先使用 `soundslist` 作为可点击的「识别观点」列表。
3. 用户点击某个观点标签后，组件将该观点设置为 `activeTopic`。
4. `buildVoiceTopicHighlightHtml` 从 `activeTopic` 中取 `originalTexTScene` 或 `originalTextScene` 作为匹配片段。
5. 工具函数把详情正文 `originalTextScene` 解析成纯文本，用匹配片段查找命中区间。
6. 命中后，在原始 HTML 结构中对应文本节点外包裹：

```html
<span class="voice-topic-highlight">命中文本</span>
```

7. 高亮颜色根据当前观点的 `sentiment` 取值，未识别时使用默认蓝色。

### 默认选中规则

当前逻辑只有在观点数量为 1 时才会默认选中并高亮：

```ts
export const getDefaultVoiceTopicIndex = (topics?: VoiceTopicHighlightItem[] | null) => {
  return Array.isArray(topics) && topics.length === 1 ? 0 : -1
}
```

如果一条详情存在多个观点，进入详情后默认不高亮，需要用户点击某个观点标签后才会高亮。

## 当前匹配缺陷

当前实现存在一个重要匹配缺陷：匹配两侧的文本处理方式不一致。

### 正文侧处理

`originalTextScene` 会被解析为 DOM，再抽取纯文本参与匹配：

```ts
const plainText = extractPlainText(sourceHtml)
const ranges = findAllRanges(plainText, topicSceneText)
```

因此正文侧用于匹配的内容不包含 HTML 标签，例如 `<div>`、`</div>`、`<span>` 会被剥离。

### 片段侧处理

`soundslist[*].originalTexTScene` 当前只做换行和空格规范化，没有先剥离 HTML 标签：

```ts
String(topic?.originalTexTScene || topic?.originalTextScene || '')
```

如果该字段本身包含 `</div><div>` 等 HTML 标签，匹配片段会带着标签去匹配已经转成纯文本的正文，导致无法命中。

### 缺陷影响

因此当前逻辑下：

- 片段是纯文本时，可以正常高亮。
- 片段带 HTML 标签时，大概率无法高亮。
- 观点名称 `topic` 不参与匹配，所以即使观点名称能在正文中找到，也不会因此高亮。

## 样例数据分析

以已确认的详情接口样例为例，该条数据有 8 个 `soundslist` 观点，因此默认不会自动高亮，需要点击观点后触发。

### 预计可以高亮

以下观点的 `originalTexTScene` 是纯文本，能在 `originalTextScene` 的纯文本中命中：

| 观点 | 匹配片段 |
| --- | --- |
| 风噪小 | 且车内隔音做得不错，高速上基本听不到风噪，听歌体验感拉满 |
| 车辆隔音效果好/噪音小 | 且车内隔音做得不错，高速上基本听不到风噪，听歌体验感拉满 |
| 座椅包裹性好 | 座椅的包裹性也很好，长时间开车腰也不会酸 |

### 预计无法高亮或不稳定

以下观点的 `originalTexTScene` 包含 HTML 标签片段，例如 `</div><div>`。由于正文侧已经转成纯文本，匹配片段仍保留 HTML 标签，所以大概率无法命中：

| 观点 | 失败原因 |
| --- | --- |
| 纯电续航里程长 | 匹配片段包含多个 `<div>` 标签 |
| 车辆顿挫 | 匹配片段以 `</div><div>` 开头 |
| 后排腿部空间设计合理 | 匹配片段以 `</div><div>` 开头 |
| 后备箱开口小 | 匹配片段以 `</div><div>` 开头 |
| 车机系统卡滞/不流畅 | 匹配片段以 `</div><div>` 开头 |

## 后续修复建议

如果需要提高高亮命中率，建议将 `soundslist[*].originalTexTScene` 与 `soundslist[*].originalTextScene` 在匹配前也转换为纯文本，与正文侧保持一致。

修复方向：

```ts
export const getVoiceTopicSceneText = (topic?: VoiceTopicHighlightItem | null) => {
  const rawSceneText = String(topic?.originalTexTScene || topic?.originalTextScene || '')
  return extractPlainText(rawSceneText).trim()
}
```

需要注意：

1. `extractPlainText` 当前不是导出函数，如果在同文件内修改可直接复用。
2. 修复后应确认带 HTML 标签的片段可以命中。
3. 如果后端能保证 `soundslist[*].originalTexTScene` 返回纯文本，也可以从接口契约侧解决。
4. 如果片段内容跨越异常 HTML 结构，例如后端返回的 `<span>` 标签未闭合，前端仍可能受浏览器 DOM 修正影响，需要结合真实数据验证。
