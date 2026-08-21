<script setup lang="ts">
// ==================== 常量定义 ====================
import { fmtPer, fmtFix, fmtNum } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'

/** 集团均值文案 */
const GROUP_AVERAGE_TEXT = '集团均值'

/**
 * 产品分析组件
 *
 * 功能说明：
 * 1. 展示各品牌在不同服务维度下的负面率和提及量数据
 * 2. 支持两级表头结构：一级表头为指标类型（服务、营销等），二级表头为具体维度
 * 3. 支持数据高亮显示（负面率超过阈值时标红）
 * 4. 支持品牌图标显示和数据动态加载
 *
 * 数据结构：
 * - 原始数据按 tag1（指标类型）和 tag2（具体维度）进行分组
 * - 每个品牌在每个维度下有两个值：value1（负面率）和 value2（提及量）
 *
 * 表格结构：
 * - 第一列：品牌名称和图标
 * - 第二列：指标类型（负面率/提及量）
 * - 其余列：按指标类型分组的维度数据
 */
import { ref, computed } from 'vue'
import type { ProductSelfTagAnalysisRowVo } from '@/api/thisProductAnalysis/types'

defineOptions({
  name: 'FWFX'
})

// ==================== Props 定义 ====================

interface Props {
  /** 标签分析数据 */
  data?: ProductSelfTagAnalysisRowVo[]
  /** 指标列标题数组，第一个值对应 value1 字段，第二个值对应 value2 字段 */
  metricTitles?: string[]
  /** 高亮阈值，当 value1 超过此值时进行高亮显示 */
  highlightThreshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  metricTitles: () => ['负面率', '提及量'],
  highlightThreshold: 35
})

// ==================== 数据接口定义 ====================

/**
 * 处理后的品牌数据接口
 * 用于表格展示的数据结构
 */
interface BrandData {
  name: string
  code: string
  imageUrl?: string
  data: {
    [metric: string]: Record<string, ProductSelfTagAnalysisRowVo>
  }
}

// 获取实际使用的数据
const rawData = computed(() => props.data || [])

// ==================== 数据处理逻辑 ====================

/**
 * 动态获取所有指标类型（一级分类）
 * 从原始数据中提取所有的 tag1Name（如：服务、营销等）
 *
 * 逻辑说明：
 * 1. 使用 Set 数据结构自动去重
 * 2. 遍历所有原始数据，提取 tag1Name 字段
 * 3. 转换为数组返回，用于构建表头的一级分类
 *
 * @returns {string[]} 去重后的指标类型数组，如：['服务', '营销']
 */
const metrics = computed(() => {
  const metricSet = new Set<string>()
  rawData.value.forEach(item => {
    metricSet.add(item.tag1Name)
  })
  return Array.from(metricSet)
})

/**
 * 获取维度显示名称
 * 从维度键值中提取显示名称（去掉编码部分）
 * @param dimensionKey 维度键值，格式：'维度名称-编码'
 * @returns 维度显示名称
 */
const getDimensionDisplayName = (dimensionKey: string) => {
  return dimensionKey.split('-')[0]
}

/**
 * 获取指定指标类型下的所有维度
 * 用于构建二级表头和数据列
 *
 * 逻辑说明：
 * 1. 遍历原始数据，筛选出指定指标类型的数据
 * 2. 为每个维度生成唯一键值：'维度名称-维度编码'
 * 3. 去重处理，确保每个维度只出现一次
 * 4. 返回该指标类型下的所有维度键值数组
 *
 * @param metric 指标类型名称，如：'服务'、'营销'
 * @returns 该指标类型下的所有维度键值数组，如：['售后-ADB001001', '售后-ADB001002']
 */
const getMetricDimensions = (metric: string) => {
  const metricDimensions: string[] = []
  rawData.value.forEach(item => {
    if (item.tag1Name === metric) {
      const dimensionKey = `${item.tag2Name}-${item.tag2Code}`
      if (!metricDimensions.includes(dimensionKey)) {
        metricDimensions.push(dimensionKey)
      }
    }
  })
  return metricDimensions
}

/**
 * 获取指定指标类型下的维度数量
 * 用于设置一级表头的 colspan 属性
 *
 * 逻辑说明：
 * 1. 调用 getMetricDimensions 获取指定指标的所有维度
 * 2. 计算 colspan = 维度数量 + 维度间空白列数量
 * 3. 维度间空白列数量 = 维度数量 - 1（每两个维度之间有一个 spacer-8）
 * 4. 所以 colspan = 维度数量 + (维度数量 - 1) = 2 * 维度数量 - 1
 * 5. 注意：指标类型间的 spacer-column 不计入 colspan，它们在一级表头中单独处理
 *
 * @param metric 指标类型名称，如：'服务'、'营销'
 * @returns 该指标类型下的 colspan 值
 */
const getMetricDimensionCount = (metric: string) => {
  const dimensionCount = getMetricDimensions(metric).length
  // 如果只有一个维度，colspan = 1
  // 如果有多个维度，colspan = 维度数量 + 维度间空白列数量
  if (dimensionCount <= 1) {
    return dimensionCount
  }
  return dimensionCount + (dimensionCount - 1)
}

//总共分了几列
const colCount = computed(() => {
  return metrics.value.reduce((acc, metric) => {
    return acc + getMetricDimensions(metric).length
  }, 0)
})

/**
 * 处理后的品牌数据
 * 将扁平化的原始数据转换为按品牌分组的嵌套结构
 *
 * 处理逻辑：
 * 1. 使用 Map 数据结构按品牌名称分组
 * 2. 为每个品牌创建数据结构：品牌信息 + 指标数据
 * 3. 指标数据按 指标类型 -> 维度 -> 值 的三层结构组织
 * 4. 维度使用 '维度名称-维度编码' 作为唯一键，避免重名冲突
 * 5. 每个维度存储两个值：value1（负面率）和 value2（提及量）
 *
 * 数据结构转换示例：
 * 原始数据：[{brandName: '智行', tag1Name: '服务', tag2Name: '售后', value1: 33.26, value2: 5193}]
 * 转换后：{智行: {data: {服务: {'售后-VOC001001': {value1: 33.26, value2: 5193}}}}}
 */
const processedBrands = computed(() => {
  const brandMap = new Map<string, BrandData>()

  rawData.value.forEach(item => {
    if (!brandMap.has(item.name)) {
      brandMap.set(item.name, {
        name: item.name,
        code: item.code,
        imageUrl: item.imageUrl,
        data: {}
      })
    }

    const brandData = brandMap.get(item.name)!

    if (!brandData.data[item.tag1Name]) {
      brandData.data[item.tag1Name] = {}
    }

    const dimensionKey = `${item.tag2Name}-${item.tag2Code}`
    brandData.data[item.tag1Name][dimensionKey] = item
  })

  return Array.from(brandMap.values())
})

/**
 * 获取指定品牌、维度的值（聚合所有指标类型的数据）
 * 在品牌的所有指标类型中查找指定维度的数据
 *
 * 查找逻辑：
 * 1. 遍历品牌的所有指标类型（服务、营销等）
 * 2. 在每个指标类型中查找指定的维度键值
 * 3. 找到数据后根据值类型进行格式化
 * 4. value1（负面率）格式化为百分比，保留2位小数
 * 5. value2（提及量）格式化为千分位数字
 * 6. 如果找不到数据，返回 '-'
 *
 * @param brand 品牌数据对象
 * @param dimensionKey 维度键值，格式：'维度名称-维度编码'
 * @param valueType 值类型，'value1' 为负面率，'value2' 为提及量
 * @returns 格式化后的值字符串
 */
const getDimensionValue = (
  brand: BrandData,
  dimensionKey: string,
  valueType: 'value1' | 'value2'
) => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      const value = item[valueType]
      if (value !== undefined && value !== null && value !== 0) {
        if (valueType === 'value1') {
          return `${value}%`
        }
        return value.toLocaleString()
      }
    }
  }
  return '-'
}

/**
 * 获取指定品牌和维度的 MoM 值
 * 用于显示环比数据
 *
 * @param brand 品牌数据对象
 * @param dimensionKey 维度键值，格式：'维度名称-维度编码'
 * @param valueType 值类型，'value1' 对应 value1MoM，'value2' 对应 value2MoM
 * @returns 格式化后的 MoM 值字符串
 */
const getDimensionMoMValue = (
  brand: BrandData,
  dimensionKey: string,
  valueType: 'value1' | 'value2'
) => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      const momValue = valueType === 'value1' ? item.value1MoM : item.value2MoM
      if (momValue !== undefined && momValue !== null && momValue !== 0) {
        return `${momValue > 0 ? '+' : ''}${momValue}%`
      }
    }
  }
  return '-'
}

/**
 * 获取指定品牌和维度的 rateBackgroundColor 值
 * 用于数据单元格背景色渲染
 *
 * @param brand 品牌数据对象
 * @param dimensionKey 维度键值，格式：'维度名称-维度编码'
 * @returns rateBackgroundColor 的色值，如果找不到则返回 undefined
 */
const getRateBackgroundColor = (brand: BrandData, dimensionKey: string): string | undefined => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      return item.rateBackgroundColor
    }
  }
  return undefined
}

/**
 * 获取指定品牌和维度的 rateColor 值
 * 用于数据单元格文字颜色渲染
 *
 * @param brand 品牌数据对象
 * @param dimensionKey 维度键值，格式：'维度名称-维度编码'
 * @returns rateColor 的色值，如果找不到则返回 undefined
 */
const getTextColor = (brand: BrandData, dimensionKey: string): string | undefined => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      return item.rateColor
    }
  }
  return undefined
}

// ==================== 事件定义 ====================

const emit = defineEmits<{
  (e: 'cell-click', data: ProductSelfTagAnalysisRowVo): void
}>()

/**
 * 处理数据单元格点击事件
 * @param brand 品牌数据
 * @param dimensionKey 维度键值
 */
/**
 * 处理数据单元格点击事件
 * @param brand 品牌数据
 * @param dimensionKey 维度键值
 */
const handleCellClick = (brand: BrandData, dimensionKey: string) => {
  // 查找对应的原始数据
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      emit('cell-click', item)
      return
    }
  }
}

/**
 * 生成悬浮提示表格配置
 * @param brand 品牌数据
 * @param dimensionKey 维度键值
 * @param valueType 值类型
 */
const getHoverTableConfig = (
  brand: BrandData,
  dimensionKey: string,
  valueType: 'value1' | 'value2'
) => {
  // 查找对应的原始数据
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      const isValue1 = valueType === 'value1'
      const metricTitle = isValue1 ? props.metricTitles[0] : props.metricTitles[1]
      const value = isValue1 ? fmtPer(item.value1) : fmtNum(item.value2)
      const momValue = isValue1 ? fmtFix(item.value1MoM) : fmtFix(item.value2MoM)
      const yoyValue = isValue1 ? fmtFix(item.value1YoY) : fmtFix(item.value2YoY)

      return {
        title: `${brand.name} - ${getDimensionDisplayName(dimensionKey)}`,
        data: [
          {
            name: metricTitle,
            value: value,
            momValue: momValue,
            yoyValue: yoyValue
          }
        ],
        columns: [
          { title: '名称', dataIndex: 'name', width: 70 },
          { title: '数值', dataIndex: 'value', width: 90 },
          { title: '环比', dataIndex: 'momValue', className: 'c666' },
          { title: '同比', dataIndex: 'yoyValue', className: 'c666' }
        ]
      }
    }
  }

  // 如果没有找到数据，返回空配置
  return {
    title: '',
    data: [],
    columns: []
  }
}
</script>

<template>
  <!-- 空状态展示 -->
  <FEmpty v-if="!rawData.length" />

  <!--
    服务声誉分析表格组件 - HTML结构详细说明

    ==================== 表格整体结构 ====================
    表格采用标准的 HTML table 结构，包含 thead（表头）和 tbody（表体）两部分

    ==================== 表头结构（thead）====================
    采用两级表头设计：

    第一级表头（第一个 tr）：
    ┌─────────┬─────────┬─────────┬─────────┬──────────────────┬──────────────────┐
    │  品牌   │ 空白列  │  指标   │ 空白列  │       服务       │       营销       │
    │(rowspan)│(rowspan)│(rowspan)│(rowspan)│   (colspan=2)    │   (colspan=2)    │
    └─────────┴─────────┴─────────┴─────────┼──────────────────┼──────────────────┤
                                            │                  │                  │
    第二级表头（第二个 tr）：                │                  │                  │
                                            ├─────────┬────────┼─────────┬────────┤
                                            │  售后   │  售后  │  活动   │  渠道  │
                                            └─────────┴────────┴─────────┴────────┘

    列结构说明：
    1. 品牌列：rowspan=2，跨越两行表头
    2. 空白间隔列1：16px宽度，透明背景
    3. 指标列：rowspan=2，跨越两行表头
    4. 空白间隔列2：16px宽度，透明背景
    5. 动态指标列：根据数据动态生成，每个指标类型使用 colspan 合并其下属维度数量

    ==================== 表体结构（tbody）====================
    每个品牌占用两行数据：

    品牌行结构：
    ┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
    │  智行   │ 空白列  │负面率(%)│ 空白列  │ 33.26%  │ 32.86%  │ 33.64%  │ 33.48%  │
    │(rowspan)│(rowspan)├─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
    │   CA    │         │ 提及量  │ 空白列  │  5,193  │  5,110  │  5,241  │  5,191  │
    └─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

    行结构说明：
    1. 品牌单元格：rowspan=2，包含品牌图标和名称
    2. 空白间隔列1：rowspan=2，跨越两行数据
    3. 指标类型单元格：分别显示"负面率(%)"和"提及量"
    4. 空白间隔列2：每行都有一个
    5. 数据单元格：按指标类型和维度动态生成

    ==================== 样式类说明 ====================
    - .spacer-column: 空白间隔列样式，16px宽度，透明背景
    - .highlight: 高亮样式，当负面率超过35%时应用
  -->
  <div v-else class="table-wrapper">
    <table>
      <!-- 表头部分：两级表头结构 -->
      <thead>
        <!-- 第一级表头：品牌、指标、指标类型分组 -->
        <tr>
          <!-- 品牌列：跨越两行表头 -->
          <th rowspan="2" class="table-header">品牌</th>
          <!-- 空白间隔列1：品牌列右侧的16px间隔 -->
          <th rowspan="2" class="spacer-column"></th>
          <!-- 指标列：跨越两行表头 -->
          <th rowspan="2" class="table-header">指标</th>
          <!-- 空白间隔列2：指标列右侧的16px间隔 -->
          <th rowspan="2" class="spacer-column"></th>
          <!-- 动态指标类型列：根据数据生成，使用 colspan 合并下属维度 -->
          <template v-for="metric in metrics" :key="metric">
            <th :colspan="getMetricDimensionCount(metric)" class="table-header">
              {{ metric }}
            </th>
            <th
              v-if="metric !== metrics[metrics.length - 1]"
              rowspan="2"
              class="spacer-column"
            ></th>
          </template>
        </tr>
        <!-- 第二级表头：具体维度名称 -->
        <tr>
          <!-- 遍历每个指标类型，生成其下属的维度列 -->
          <template v-for="metric in metrics" :key="metric">
            <template
              v-for="dimension in getMetricDimensions(metric)"
              :key="`${metric}-${dimension}`"
            >
              <th class="table-header">
                {{ getDimensionDisplayName(dimension) }}
              </th>
              <th
                v-if="
                  dimension !== getMetricDimensions(metric)[getMetricDimensions(metric).length - 1]
                "
                class="spacer-8"
              ></th>
            </template>
          </template>
        </tr>
      </thead>
      <!-- 表体部分：品牌数据展示 -->
      <tbody>
        <!-- 遍历每个品牌，每个品牌生成两行数据 -->
        <template v-for="brand in processedBrands" :key="brand.name">
          <!-- 每个品牌的两行：第一行显示负面率，第二行显示提及量 -->
          <tr
            v-for="(valueType, valueIndex) in ['value1', 'value2']"
            :key="`${brand.name}-${valueType}`"
          >
            <!-- 品牌单元格：只在第一行显示，使用 rowspan=2 跨越两行 -->
            <td
              v-if="valueIndex === 0"
              :rowspan="2"
              :class="['first-column', { 'group-average': brand.name === GROUP_AVERAGE_TEXT }]"
            >
              <!-- 品牌图片 -->
              <img
                v-if="brand.imageUrl"
                :src="brand.imageUrl"
                :alt="brand.name"
                class="brand-image"
              />
              <!-- 品牌名称 -->
              <span>{{ brand.name }}</span>
            </td>

            <!-- 空白间隔列1：只在第一行显示，使用 rowspan=2 跨越两行 -->
            <td v-if="valueIndex === 0" :rowspan="2" class="spacer-column"></td>

            <!-- 指标类型单元格：显示当前行的数据类型 -->
            <td class="second-column">
              {{ valueType === 'value1' ? `${props.metricTitles[0]}` : props.metricTitles[1] }}
            </td>

            <!-- 空白间隔列2：每行都有一个 -->
            <td class="spacer-column"></td>

            <!-- 数据单元格：遍历所有指标类型和维度，显示对应数据 -->
            <template v-for="metric in metrics" :key="metric">
              <template
                v-for="dimension in getMetricDimensions(metric)"
                :key="`${metric}-${dimension}`"
              >
                <td
                  :style="{
                    width: `calc(100% / ${colCount || 1})`,
                    backgroundColor:
                      valueType === 'value1' && getRateBackgroundColor(brand, dimension)
                        ? getRateBackgroundColor(brand, dimension)
                        : undefined,
                    color:
                      valueType === 'value1' && getTextColor(brand, dimension)
                        ? getTextColor(brand, dimension)
                        : undefined
                  }"
                >
                  <HoverPopover
                    placement="top"
                    :show-after="200"
                    :width="410"
                    trigger="hover"
                    :table-config="
                      getHoverTableConfig(brand, dimension, valueType as 'value1' | 'value2')
                    "
                  >
                    <template #reference>
                      <div
                        style="
                          cursor: pointer;
                          width: 100%;
                          height: 100%;
                          display: flex;
                          flex-direction: column;
                          justify-content: center;
                          align-items: center;
                        "
                        @click="handleCellClick(brand, dimension)"
                      >
                        <div>
                          {{
                            getDimensionValue(brand, dimension, valueType as 'value1' | 'value2')
                          }}
                        </div>
                        <div>
                          {{
                            getDimensionMoMValue(brand, dimension, valueType as 'value1' | 'value2')
                          }}
                        </div>
                      </div>
                    </template>
                  </HoverPopover>
                </td>
                <td
                  v-if="
                    dimension !==
                    getMetricDimensions(metric)[getMetricDimensions(metric).length - 1]
                  "
                  class="spacer-8"
                ></td>
              </template>
              <td v-if="metric !== metrics[metrics.length - 1]" class="spacer-column"></td>
            </template>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<style lang="scss" scoped>
/* 表格外层容器 */
.table-wrapper {
  width: 100%;
  height: 741px;
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
    width: 125px;
    min-width: 125px; /* 确保列宽不被压缩 */
    height: 52px;
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

  tbody td {
    height: 145px !important;
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
    // 可在此处添加更多表头自定义样式
    background: #eaf3ff;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    line-height: 24px;
    height: 62px !important;
  }

  // 第一列（品牌列）样式
  .first-column {
    // 可在此处添加更多第一列自定义样式
    background: #eaf3ff;
    font-weight: 600;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;
    text-align: center;
    vertical-align: middle;

    .brand-image {
      width: 24px;
      height: 24px;
      object-fit: contain;
      display: block;
      margin: 0 auto 8px auto;
    }

    // 集团均值特殊样式
    &.group-average {
      background: #e5fafe;
    }
  }

  // 第二列（指标列）样式
  .second-column {
    // 可在此处添加更多第二列自定义样式
    font-weight: 400;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;
  }

  // 数据单元格样式 - 支持两行数据显示
  tbody td:not(.first-column):not(.second-column):not(.spacer-column):not(.spacer-8) {
    div {
      &:first-child {
        font-weight: 500;
        font-size: 16px;
        // color: #333333;
        line-height: 24px;
      }

      &:last-child {
        font-weight: 400;
        font-size: 14px;
        // color: #666666;
        line-height: 20px;
        margin-top: 8px;
      }
    }
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
