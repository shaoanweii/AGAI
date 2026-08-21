<script setup lang="ts">
/**
 * 服务声誉分析组件
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
import { ref, computed, onMounted } from 'vue'

defineOptions({
  name: 'ServiceReputationAnalysis'
})

// ==================== 数据接口定义 ====================

/**
 * 处理后的品牌数据接口
 * 用于表格展示的数据结构
 */
interface BrandData {
  brandName: string // 品牌名称
  brandCode: string // 品牌编码
  brandIcon?: string // 品牌图标（可选）
  data: {
    // 动态指标名称映射，每个维度包含两个值
    [metric: string]: Record<string, { value1: number; value2: number }>
  }
}

/**
 * 原始数据项接口
 * 从API获取的每一条数据记录
 */
interface RawDataItem {
  brandName: string // 品牌名称
  brandCode: string // 品牌编码
  brandImageUrl: string | null // 品牌图片URL
  tag1Name: string // 一级指标类型（如：服务、营销）
  tag1Code: string // 一级指标编码
  tag2Name: string // 二级维度名称（如：销售服务、售后服务）
  tag2Code: string // 二级维度编码
  value1: number // 第一个指标值（如负面率）
  value2: number // 第二个指标值（如提及量）
}

/**
 *
 *  {
    brandName: '阿维塔',
    brandCode: 'AVT',
    brandIcon: 'V',
    tag1Name: '负面率',
    tag1Code: 'ADB003',
    tag2Name: '销售服务',
    tag2Code: 'ADB003002',
    value: 43.91,
    count: 1552
  },
 */

// 实际API数据
const rawData = ref<RawDataItem[]>([
  {
    brandName: '智行',
    brandCode: 'CA',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001001',
    value1: 33.26,
    value2: 5193
  },
  {
    brandName: '智行',
    brandCode: 'CA',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001002',
    value1: 32.86,
    value2: 5110
  },
  {
    brandName: '智行',
    brandCode: 'CA',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '活动',
    tag2Code: 'ADB003001',
    value1: 33.64,
    value2: 5241
  },
  {
    brandName: '智行',
    brandCode: 'CA',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '渠道',
    tag2Code: 'ADB003002',
    value1: 33.48,
    value2: 5191
  },
  {
    brandName: '欧尚',
    brandCode: 'OS',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001001',
    value1: 33.37,
    value2: 5221
  },
  {
    brandName: '欧尚',
    brandCode: 'OS',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001002',
    value1: 34.59,
    value2: 5180
  },
  {
    brandName: '欧尚',
    brandCode: 'OS',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '活动',
    tag2Code: 'ADB003001',
    value1: 33.97,
    value2: 5223
  },
  {
    brandName: '欧尚',
    brandCode: 'OS',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '渠道',
    tag2Code: 'ADB003002',
    value1: 33.84,
    value2: 5116
  },
  {
    brandName: '凯程',
    brandCode: 'KC',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001001',
    value1: 34.78,
    value2: 5236
  },
  {
    brandName: '凯程',
    brandCode: 'KC',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001002',
    value1: 32.76,
    value2: 5171
  },
  {
    brandName: '凯程',
    brandCode: 'KC',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '渠道',
    tag2Code: 'ADB003002',
    value1: 33.16,
    value2: 5331
  },
  {
    brandName: '凯程',
    brandCode: 'KC',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '活动',
    tag2Code: 'ADB003001',
    value1: 32.88,
    value2: 5179
  },
  {
    brandName: '深蓝',
    brandCode: 'SL',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001002',
    value1: 33.63,
    value2: 5230
  },
  {
    brandName: '深蓝',
    brandCode: 'SL',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001001',
    value1: 32.57,
    value2: 5219
  },
  {
    brandName: '深蓝',
    brandCode: 'SL',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '活动',
    tag2Code: 'ADB003001',
    value1: 32.53,
    value2: 5214
  },
  {
    brandName: '深蓝',
    brandCode: 'SL',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '渠道',
    tag2Code: 'ADB003002',
    value1: 32.74,
    value2: 5132
  },
  {
    brandName: '阿维塔',
    brandCode: 'AVT',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001002',
    value1: 32.81,
    value2: 5245
  },
  {
    brandName: '阿维塔',
    brandCode: 'AVT',
    brandImageUrl: null,
    tag1Name: '服务',
    tag1Code: 'ADB001',
    tag2Name: '售后',
    tag2Code: 'ADB001001',
    value1: 32.86,
    value2: 5121
  },
  {
    brandName: '阿维塔',
    brandCode: 'AVT',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '活动',
    tag2Code: 'ADB003001',
    value1: 34.46,
    value2: 5145
  },
  {
    brandName: '阿维塔',
    brandCode: 'AVT',
    brandImageUrl: null,
    tag1Name: '营销',
    tag1Code: 'ADB003',
    tag2Name: '渠道',
    tag2Code: 'ADB003002',
    value1: 32.92,
    value2: 5119
  }
])

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
 * 动态获取所有维度（二级分类）
 * 使用 tag2Code 来区分相同名称的维度，确保唯一性
 * 注意：此计算属性目前未使用，保留用于后续扩展
 */
const dimensions = computed(() => {
  const dimensionSet = new Set<string>()
  rawData.value.forEach(item => {
    // 使用 tag2Code 作为唯一标识，tag2Name 作为显示名称
    dimensionSet.add(`${item.tag2Name}-${item.tag2Code}`)
  })
  return Array.from(dimensionSet)
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
 * 2. 返回维度数组的长度
 * 3. 这个数量用于设置表头合并列数（colspan）
 *
 * @param metric 指标类型名称，如：'服务'、'营销'
 * @returns 该指标类型下的维度数量，如：服务有2个维度，返回2
 */
const getMetricDimensionCount = (metric: string) => {
  return getMetricDimensions(metric).length
}

/**
 * 处理后的品牌数据
 * 存储按品牌聚合后的数据结构，用于表格渲染
 */
const processedBrands = ref<BrandData[]>([])

/**
 * 生成品牌图标
 * 为每个品牌生成对应的图标字符
 *
 * 逻辑说明：
 * 1. 预定义品牌名称与图标的映射关系
 * 2. 如果品牌在映射表中，返回对应图标
 * 3. 如果品牌不在映射表中，返回品牌名称的第一个字符
 *
 * @param brandName 品牌名称
 * @returns 品牌图标字符
 */
const generateBrandIcon = (brandName: string) => {
  const iconMap: Record<string, string> = {
    智行: 'VOC',
    欧尚: 'OS',
    凯程: 'KC',
    深蓝: 'SL',
    阿维塔: 'AV'
  }
  return iconMap[brandName] || brandName.charAt(0)
}

/**
 * 数据处理：按品牌聚合原始数据
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
const processData = () => {
  const brandMap = new Map<string, BrandData>()

  rawData.value.forEach(item => {
    const {
      brandName,
      brandCode,
      tag1Name: metric,
      tag2Name: dimension,
      tag2Code,
      value1,
      value2
    } = item

    // 如果品牌不存在，创建新的品牌数据结构
    if (!brandMap.has(brandName)) {
      brandMap.set(brandName, {
        brandName,
        brandCode,
        brandIcon: generateBrandIcon(brandName),
        data: {}
      })
    }

    const brandData = brandMap.get(brandName)!

    // 初始化指标数据结构
    if (!brandData.data[metric]) {
      brandData.data[metric] = {}
    }

    // 使用 dimension-tag2Code 作为唯一键来存储数据
    const dimensionKey = `${dimension}-${tag2Code}`
    brandData.data[metric][dimensionKey] = {
      value1,
      value2
    }
  })

  processedBrands.value = Array.from(brandMap.values())
}

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
  // 遍历所有指标类型，找到包含该维度的数据
  for (const metric of Object.keys(brand.data)) {
    const dimensionData = brand.data[metric]?.[dimensionKey]
    if (dimensionData) {
      const value = dimensionData[valueType]
      if (value !== undefined && value !== null) {
        // 如果是 value1，通常显示百分比格式
        if (valueType === 'value1') {
          return `${value}%`
        }
        // 如果是 value2，通常显示数字格式
        return value.toLocaleString()
      }
    }
  }
  return '-'
}

/**
 * 判断是否需要高亮显示（value1 较高的单元格）
 * 当负面率超过阈值时，对单元格进行高亮显示
 *
 * 判断逻辑：
 * 1. 只对 value1（负面率）类型的数据进行高亮判断
 * 2. 遍历品牌的所有指标类型，查找指定维度的数据
 * 3. 如果负面率超过35%，返回 true 进行高亮显示
 * 4. 其他情况返回 false，不高亮
 *
 * @param brand 品牌数据对象
 * @param dimensionKey 维度键值，格式：'维度名称-维度编码'
 * @param valueType 值类型，只有 'value1' 才会进行高亮判断
 * @returns 是否需要高亮显示
 */
const shouldHighlight = (brand: BrandData, dimensionKey: string, valueType: string) => {
  if (valueType !== 'value1') return false

  // 遍历所有指标类型，找到包含该维度的数据
  for (const metric of Object.keys(brand.data)) {
    const dimensionData = brand.data[metric]?.[dimensionKey]
    if (dimensionData) {
      const value1 = dimensionData.value1
      return value1 !== undefined && value1 > 35 // value1 超过35%高亮显示
    }
  }
  return false
}

onMounted(() => {
  processData()
})
</script>

<template>
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
  <div>
    <div>
      <table>
        <!-- 表头部分：两级表头结构 -->
        <thead>
          <!-- 第一级表头：品牌、指标、指标类型分组 -->
          <tr>
            <!-- 品牌列：跨越两行表头 -->
            <th rowspan="2">品牌</th>
            <!-- 空白间隔列1：品牌列右侧的16px间隔 -->
            <th rowspan="2" class="spacer-column"></th>
            <!-- 指标列：跨越两行表头 -->
            <th rowspan="2">指标</th>
            <!-- 空白间隔列2：指标列右侧的16px间隔 -->
            <th rowspan="2" class="spacer-column"></th>
            <!-- 动态指标类型列：根据数据生成，使用 colspan 合并下属维度 -->
            <template v-for="metric in metrics" :key="metric">
              <th :colspan="getMetricDimensionCount(metric) + 1">
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
                <th>
                  {{ getDimensionDisplayName(dimension) }}
                </th>
                <th
                  v-if="
                    dimension !==
                    getMetricDimensions(metric)[getMetricDimensions(metric).length - 1]
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
          <template v-for="brand in processedBrands" :key="brand.brandName">
            <!-- 每个品牌的两行：第一行显示负面率，第二行显示提及量 -->
            <tr
              v-for="(valueType, valueIndex) in ['value1', 'value2']"
              :key="`${brand.brandName}-${valueType}`"
            >
              <!-- 品牌单元格：只在第一行显示，使用 rowspan=2 跨越两行 -->
              <td v-if="valueIndex === 0" :rowspan="2">
                <!-- 品牌图标 -->
                <div v-if="brand.brandIcon">{{ brand.brandIcon }}</div>
                <!-- 品牌名称 -->
                <span>{{ brand.brandName }}</span>
              </td>

              <!-- 空白间隔列1：只在第一行显示，使用 rowspan=2 跨越两行 -->
              <td v-if="valueIndex === 0" :rowspan="2" class="spacer-column"></td>

              <!-- 指标类型单元格：显示当前行的数据类型 -->
              <td>
                {{ valueType === 'value1' ? '负面率(%)' : '提及量' }}
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
                    :class="{
                      highlight:
                        shouldHighlight(brand, dimension, valueType) && valueType === 'value1'
                    }"
                  >
                    {{ getDimensionValue(brand, dimension, valueType as 'value1' | 'value2') }}
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
  </div>
</template>

<style lang="scss" scoped>
table {
  border-collapse: separate;
  // border-spacing: 8px; // 统一间距8px
  border-spacing: 0 8px; // 统一间距8px

  th,
  td {
    width: 125px;
    height: 52px;
    background: #f2f4f7;
    border-radius: 8px;
    text-align: center;
    vertical-align: middle;
    padding: 0;
  }

  // 空白间隔列样式
  .spacer-column {
    width: 16px !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
  }

  .spacer-8 {
    width: 8px !important;
    background: transparent !important;
    border: none !important;
    border-radius: 0 !important;
  }

  // 高亮样式
  .highlight {
    background: #fff2e8;
    border: 1px solid #ff7d00;
  }
}
</style>
