<script setup lang="ts">
import { fmtPer, fmtFix, fmtNum } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'
import { computed } from 'vue'
import { MarketAverage } from '../constants'

defineOptions({
  name: 'FWFX'
})

interface Props {
  data?: any[]
  dataType?: 'negativeRateMention' | 'negativeRateMoM' | 'mentionMoM'
  metricTitles?: string[]
  highlightThreshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  dataType: 'negativeRateMention',
  metricTitles: () => ['负面率', '提及量'],
  highlightThreshold: 35
})

interface BrandData {
  name: string
  code: string
  imageUrl?: string
  data: {
    [metric: string]: Record<string, any>
  }
}

const rawData = computed(() => props.data || [])

const metrics = computed(() => {
  const metricSet = new Set<string>()
  rawData.value.forEach(item => {
    metricSet.add(item.tag1Name)
  })
  return Array.from(metricSet)
})

const getDimensionDisplayName = (dimensionKey: string) => {
  return dimensionKey.split('-')[0]
}

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

const getMetricDimensionCount = (metric: string) => {
  const dimensionCount = getMetricDimensions(metric).length
  if (dimensionCount <= 1) {
    return dimensionCount
  }
  return dimensionCount + (dimensionCount - 1)
}

const colCount = computed(() => {
  return metrics.value.reduce((acc, metric) => {
    return acc + getMetricDimensions(metric).length
  }, 0)
})

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

const getRateBackgroundColor = (brand: BrandData, dimensionKey: string): string | undefined => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      return item.rateBackgroundColor
    }
  }
  return undefined
}

const getTextColor = (brand: BrandData, dimensionKey: string): string | undefined => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      return item.rateColor
    }
  }
  return undefined
}

const emit = defineEmits<{
  (e: 'cell-click', data: any): void
}>()

const handleCellClick = (brand: BrandData, dimensionKey: string) => {
  for (const metric of Object.keys(brand.data)) {
    const item = brand.data[metric]?.[dimensionKey]
    if (item) {
      emit('cell-click', item)
      return
    }
  }
}

const getHoverTableConfig = (
  brand: BrandData,
  dimensionKey: string,
  valueType: 'value1' | 'value2'
) => {
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

  return {
    title: '',
    data: [],
    columns: []
  }
}
</script>

<template>
  <FEmpty v-if="!rawData.length" />

  <div v-else class="table-wrapper">
    <table>
      <thead>
        <tr>
          <th rowspan="2" class="table-header">品牌</th>
          <th rowspan="2" class="spacer-column"></th>
          <th rowspan="2" class="table-header">指标</th>
          <th rowspan="2" class="spacer-column"></th>
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
        <tr>
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
      <tbody>
        <template v-for="brand in processedBrands" :key="brand.name">
          <tr
            v-for="(valueType, valueIndex) in ['value1', 'value2']"
            :key="`${brand.name}-${valueType}`"
          >
            <td
              v-if="valueIndex === 0"
              :rowspan="2"
              :class="['first-column', { 'group-average': brand.name === MarketAverage }]"
            >
              <img
                v-if="brand.imageUrl"
                :src="brand.imageUrl"
                :alt="brand.name"
                class="brand-image"
              />
              <span>{{ brand.name }}</span>
            </td>

            <td v-if="valueIndex === 0" :rowspan="2" class="spacer-column"></td>

            <td class="second-column">
              {{ valueType === 'value1' ? `${props.metricTitles[0]}` : props.metricTitles[1] }}
            </td>

            <td class="spacer-column"></td>

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
                        :style="{
                          cursor: brand.name === MarketAverage ? 'default' : 'pointer',
                          width: '100%',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'center',
                          alignItems: 'center'
                        }"
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
.table-wrapper {
  width: 100%;
  height: 741px;
  overflow-x: auto;
  margin-top: 16px;
}

table {
  border-collapse: separate;
  border-spacing: 0 8px;
  width: auto;

  th,
  td {
    width: 125px;
    min-width: 125px;
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
    // height: 145px !important;
    height: 90px !important;
  }

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

  .table-header {
    background: #eaf3ff;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    line-height: 24px;
    height: 62px !important;
  }

  .first-column {
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

    &.group-average {
      background: #e5fafe;
    }
  }

  .second-column {
    font-weight: 400;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;
  }

  tbody td:not(.first-column):not(.second-column):not(.spacer-column):not(.spacer-8) {
    div {
      &:first-child {
        font-weight: 500;
        font-size: 16px;
        line-height: 24px;
      }

      &:last-child {
        font-weight: 400;
        font-size: 14px;
        line-height: 20px;
        margin-top: 8px;
      }
    }
  }
}
</style>
