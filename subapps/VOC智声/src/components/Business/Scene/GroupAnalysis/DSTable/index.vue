<script setup lang="ts">
/**
 * 数据来源组件
 *
 * 功能说明：
 * 1. 展示各品牌在不同服务维度下的负面率数据
 * 2. 支持单级表头结构：表头为具体维度
 * 3. 支持数据高亮显示（负面率超过阈值时标红）
 * 4. 支持品牌图标显示和数据动态加载
 *
 * 数据结构：
 * - 原始数据按 tag2（具体维度）进行分组
 * - 每个品牌在每个维度下显示 value1（负面率）
 *
 * 表格结构：
 * - 第一列：品牌名称和图标
 * - 其余列：各个维度的负面率数据
 */
import { computed } from 'vue'
import type { GroupDataSourceAnalysisVo } from '@/api/groupAnalysis/types'
import { fmtFix, fmtHoverData } from '@/utils'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'GA_DSTable'
})

// ==================== Props 定义 ====================

interface Props {
  /** 数据来源分析数据 */
  data?: GroupDataSourceAnalysisVo[] | null
  /** 高亮阈值，当 negativeRate 超过此值时进行高亮显示 */
  highlightThreshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  data: null,
  highlightThreshold: 35
})

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'cell-click', data: { brand: BrandData; dimension: string; rowData: any }): void
}>()

// ==================== 事件处理方法 ====================

/**
 * 处理单元格点击事件
 */
const handleCellClick = (brand: BrandData, dimension: string) => {
  const rowData = brand.data[dimension]
  emit('cell-click', { brand, dimension, rowData })
}

// ==================== 数据接口定义 ====================

/**
 * 处理后的品牌数据接口
 * 用于表格展示的数据结构
 */
interface BrandData {
  brandName: string // 品牌名称
  brandCode: string // 品牌编码
  brandImageUrl?: string // 品牌图片URL（可选）
  data: {
    // 渠道映射，每个渠道保留接口原始字段，便于悬浮表按业务规则展示环比/同比。
    [channelKey: string]: GroupDataSourceAnalysisVo
  }
}

/**
 * 动态获取所有渠道维度
 * 从原始数据中提取所有的渠道信息
 */
const dimensions = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }

  const channelSet = new Set<string>()
  props.data.forEach(item => {
    const channelKey = `${item.channelName}-${item.channelCode}`
    channelSet.add(channelKey)
  })
  return Array.from(channelSet)
})

/**
 * 获取渠道显示名称
 * 直接从原始数据中查找对应的 channelName
 */
const getDimensionDisplayName = (dimensionKey: string) => {
  if (!props.data || props.data.length === 0) {
    return dimensionKey
  }

  // 从原始数据中查找匹配的渠道名称
  const matchedItem = props.data.find(item => {
    const channelKey = `${item.channelName}-${item.channelCode}`
    return channelKey === dimensionKey
  })

  return matchedItem ? matchedItem.channelName : dimensionKey
}

/**
 * 处理后的品牌数据
 * 将数据来源分析数据按品牌聚合，渠道作为维度
 */
const processedBrands = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }

  const brandMap = new Map<string, BrandData>()

  props.data.forEach(item => {
    const {
      brandName,
      brandCode,
      brandImageUrl,
      channelName,
      channelCode,
      negativeRate,
      mentions
    } = item

    // 如果品牌不存在，创建新的品牌数据结构
    if (!brandMap.has(brandName)) {
      brandMap.set(brandName, {
        brandName,
        brandCode,
        brandImageUrl,
        data: {}
      })
    }

    const brandData = brandMap.get(brandName)!

    // 使用 channelName-channelCode 作为唯一键来存储数据
    const channelKey = `${channelName}-${channelCode}`
    brandData.data[channelKey] = {
      ...item,
      negativeRate, // 负面率
      mentions, // 提及量
      rateColor: item.rateColor, // 文字色
      rateBackgroundColor: item.rateBackgroundColor // 背景色
    }
  })

  return Array.from(brandMap.values())
})

/**
 * 判断提及量与提及量环比是否同时为 0。
 * 同时为 0 表示当前渠道没有有效波动基数，表格统一展示占位符。
 */
const isEmptyMentionMoM = (item?: GroupDataSourceAnalysisVo) => {
  return Number(item?.mentions) === 0 && Number(item?.mentionsMoM) === 0
}

/**
 * 判断提及量是否为空。
 * 提及量为空时不展示悬浮明细，避免无有效提及数据仍弹出明细。
 */
const isEmptyMentions = (item?: GroupDataSourceAnalysisVo) => {
  return item?.mentions === undefined || item?.mentions === null || Number(item.mentions) === 0
}

/**
 * 获取指定品牌、渠道的负面率
 * 直接从品牌数据中查找指定渠道的负面率数据
 */
const getNegativeRate = (brand: BrandData, channelKey: string): string => {
  const channelData = brand.data[channelKey]
  // console.log('channelData11',channelData)
  if (isEmptyMentionMoM(channelData)) {
    return '-'
  }
  if (channelData && channelData.negativeRate !== undefined && channelData.negativeRate !== null) {
    return `${channelData.negativeRate}%`
  }
  return '-'
}

/**
 * 获取指定品牌、渠道的提及量
 * 直接从品牌数据中查找指定渠道的提及量数据
 */
const getMentions = (brand: BrandData, channelKey: string): string => {
  const channelData = brand.data[channelKey]
  // console.log('channelData222',channelData)
  if (isEmptyMentionMoM(channelData)) {
    return '-'
  }
  if (channelData && channelData.mentions !== undefined && channelData.mentions !== null) {
    return channelData.mentions.toLocaleString()
  }
  return '-'
}

/**
 * 获取数据来源悬浮表数据。
 * 当提及量与提及量环比同时为 0 时，环比展示为占位符；提及量非 0 时保留接口环比值。
 */
const getDataSourceHoverData = (item?: GroupDataSourceAnalysisVo) => {
  if (!item) return []

  const showPlaceholder = isEmptyMentionMoM(item)

  return fmtHoverData(item).map(row => {
    if (row.label !== '提及量') {
      return row
    }

    return {
      ...row,
      value: showPlaceholder ? '-' : row.value,
      rateMoM: showPlaceholder ? '-' : fmtFix(item.mentionsMoM)
    }
  })
}

/**
 * 获取指定品牌和渠道的 rateBackgroundColor 值
 * 用于数据单元格背景色渲染
 */
const getRateBackgroundColor = (brand: BrandData, channelKey: string): string | undefined => {
  const channelData = brand.data[channelKey]
  if (channelData) {
    return channelData.rateBackgroundColor
  }
  return undefined
}

/**
 * 获取指定品牌和渠道的 rateColor 值
 * 用于数据单元格文字颜色渲染
 */
const getTextColor = (brand: BrandData, channelKey: string): string | undefined => {
  const channelData = brand.data[channelKey]
  if (channelData) {
    return channelData.rateColor
  }
  return undefined
}
</script>

<template>
  <!-- 空状态展示 -->
  <FEmpty v-if="!processedBrands.length" />

  <!--
    数据来源分析表格组件 - HTML结构详细说明

    ==================== 表格整体结构 ====================
    表格采用标准的 HTML table 结构，包含 thead（表头）和 tbody（表体）两部分

    ==================== 表头结构（thead）====================
    采用单级表头设计：

    表头结构：
    ┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
    │  品牌   │ 空白列  │  微博   │ 空白列  │  微信   │ 空白列  │  抖音   │ 空白列  │
    └─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

    列结构说明：
    1. 品牌列：显示品牌名称和图片
    2. 空白间隔列：16px宽度，透明背景
    3. 渠道列：根据数据动态生成，显示各个渠道名称

    ==================== 表体结构（tbody）====================
    每个品牌占用一行数据：

    品牌行结构：
    ┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
    │  智行   │ 空白列  │ 33.26%  │ 空白列  │ 32.86%  │ 空白列  │ 34.12%  │ 空白列  │
    │  [图片] │         │ 5,193   │         │ 5,110   │         │ 5,324   │         │
    └─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

    行结构说明：
    1. 品牌单元格：包含品牌图片和名称
    2. 空白间隔列：16px宽度，透明背景
    3. 数据单元格：显示各渠道的负面率和提及量数据

    ==================== 样式类说明 ====================
    - .spacer-column: 空白间隔列样式，16px宽度，透明背景
    - .spacer-8: 小间隔列样式，8px宽度，透明背景
    - .highlight: 高亮样式，当负面率超过阈值时应用
  -->
  <div v-else class="table-wrapper">
    <table>
      <!-- 表头部分：单级表头结构 -->
      <thead>
        <tr>
          <!-- 品牌列 -->
          <th class="table-header first-column-header">品牌</th>
          <!-- 空白间隔列：品牌列右侧的16px间隔 -->
          <th class="spacer-column"></th>
          <!-- 动态渠道列：根据数据生成 -->
          <template v-for="(dimension, index) in dimensions" :key="dimension">
            <th class="table-header">
              {{ getDimensionDisplayName(dimension) }}
            </th>
            <th v-if="index !== dimensions.length - 1" class="spacer-8"></th>
          </template>
        </tr>
      </thead>
      <!-- 表体部分：品牌数据展示 -->
      <tbody>
        <!-- 遍历每个品牌，每个品牌生成一行数据 -->
        <tr v-for="brand in processedBrands" :key="brand.brandName">
          <!-- 品牌单元格 -->
          <td class="first-column">
            <!-- 品牌图标 -->
            <img
              v-if="brand.brandImageUrl"
              :src="brand.brandImageUrl"
              :alt="brand.brandName"
              class="brand-icon"
            />
            <!-- 品牌名称 -->
            <div>{{ brand.brandName }}</div>
          </td>

          <!-- 空白间隔列 -->
          <td class="spacer-column"></td>

          <!-- 数据单元格：遍历所有渠道，显示负面率和提及量数据 -->
          <template v-for="(dimension, index) in dimensions" :key="dimension">
            <td
              :style="{
                width: `calc(100% / ${dimensions.length || 1})`,
                cursor: 'pointer',
                backgroundColor: getRateBackgroundColor(brand, dimension) || undefined,
                color: getTextColor(brand, dimension) || undefined
              }"
              @click="handleCellClick(brand, dimension)"
            >
              <HoverPopover
                :disabled="!brand.data[dimension] || isEmptyMentions(brand.data[dimension])"
                :table-config="{
                  title: '',
                  data: getDataSourceHoverData(brand.data[dimension]),
                  columns: [
                    { title: '名称', dataIndex: 'label', width: 70 },
                    { title: '数值', dataIndex: 'value', width: 85 },
                    { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
                    { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
                  ]
                }"
              >
                <template #reference>
                  <div class="cell">
                    {{ getNegativeRate(brand, dimension) }} <br />
                    {{ getMentions(brand, dimension) }}
                  </div>
                </template>
              </HoverPopover>
            </td>
            <td v-if="index !== dimensions.length - 1" class="spacer-8"></td>
          </template>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style lang="scss" scoped>
/* 表格外层容器 */
.table-wrapper {
  width: 100%;
  overflow-x: auto;
  margin-top: 16px;
}

table {
  border-collapse: separate;
  // border-spacing: 8px; // 统一间距8px
  border-spacing: 0 8px; // 统一间距8px
  /* 表格宽度自适应内容，不受外部容器限制 */
  width: auto;

  th,
  td {
    width: 152px;
    min-width: 152px; /* 确保列宽不被压缩 */
    background: #f2f4f7;
    border-radius: 8px;
    text-align: center;
    vertical-align: middle;
    padding: 0;

    font-weight: 500;
    font-size: 16px;
    color: #666666;
    line-height: 24px;
  }

  // 表头高度
  th {
    height: 80px;
  }

  // 表体高度
  td {
    height: 122px;
  }

  // 空白间隔列样式
  .spacer-column {
    width: 16px !important;
    min-width: 16px !important;
    max-width: 16px !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
  }

  .spacer-8 {
    width: 8px !important;
    min-width: 8px !important;
    max-width: 8px !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
  }

  // 表头样式
  .table-header {
    width: 152px;
    // 可在此处添加更多表头自定义样式
    background: #eaf3ff;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    line-height: 24px;
  }

  // 第一列（品牌列）样式
  .first-column {
    // 可在此处添加更多第一列自定义样式
    width: 120px !important;
    min-width: 120px !important;
    max-width: 120px !important;
    background: #eaf3ff;
    font-weight: 600;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;

    // 品牌图标样式
    .brand-icon {
      width: 24px;
      height: 24px;
      margin: 0 auto 8px;
      object-fit: contain;
    }
  }

  // 第一列表头样式
  .first-column-header {
    width: 120px !important;
    min-width: 120px !important;
    max-width: 120px !important;
  }

  // 高亮样式
  .highlight {
    background: #ffd1c9;
    font-weight: 600;
    font-size: 16px;
    color: #333333;
    line-height: 24px;
  }
}
</style>
