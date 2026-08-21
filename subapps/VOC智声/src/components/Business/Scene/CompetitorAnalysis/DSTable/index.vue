<script setup lang="ts">
import { computed } from 'vue'
import type { SourceCompareVo } from '@/api/competitorAnalysis/types'
import { fmtHoverData } from '@/utils'
import HoverPopover from '@components/Business/Scene/Common/HoverPopover.vue'
import { MarketAverage } from '../constants'

defineOptions({
  name: 'CA_DSTable'
})

interface Props {
  data?: SourceCompareVo[] | null
}

const props = withDefaults(defineProps<Props>(), {
  data: null
})

const emit = defineEmits<{
  (e: 'cell-click', data: { brand: BrandData; dimension: string; rowData: any }): void
}>()

interface BrandData {
  brandName: string
  brandCode: string
  brandImageUrl?: string
  data: {
    [channelKey: string]: {
      negativeRate: number
      mentions: number
      rateColor?: string
      rateBackgroundColor?: string
      mentionsMoM?: number
      mentionsYoY?: number
      negativeRateMoM?: number
      negativeRateYoY?: number
      channelCode?: string
    }
  }
}

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

const getDimensionDisplayName = (dimensionKey: string) => {
  if (!props.data || props.data.length === 0) {
    return dimensionKey
  }

  const matchedItem = props.data.find(item => {
    const channelKey = `${item.channelName}-${item.channelCode}`
    return channelKey === dimensionKey
  })

  return matchedItem ? matchedItem.channelName : dimensionKey
}

const processedBrands = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }

  const brandMap = new Map<string, BrandData>()

  props.data.forEach(item => {
    const { name, code, imgUrl, channelName, channelCode, negativeRate, mentions } = item

    if (!brandMap.has(name || '')) {
      brandMap.set(name || '', {
        brandName: name || '',
        brandCode: code || '',
        brandImageUrl: imgUrl,
        data: {}
      })
    }

    const brandData = brandMap.get(name || '')!
    const channelKey = `${channelName || ''}-${channelCode || ''}`
    brandData.data[channelKey] = {
      ...item,
      negativeRate: negativeRate ?? 0,
      mentions: mentions ?? 0,
      rateColor: item.rateColor,
      rateBackgroundColor: item.rateBackgroundColor
    }
  })

  return Array.from(brandMap.values())
})

const getNegativeRate = (brand: BrandData, channelKey: string): string => {
  const channelData = brand.data[channelKey]
  if (channelData && channelData.negativeRate !== undefined && channelData.negativeRate !== null) {
    return `${channelData.negativeRate}%`
  }
  return '-'
}

const getMentions = (brand: BrandData, channelKey: string): string => {
  const channelData = brand.data[channelKey]
  if (channelData && channelData.mentions !== undefined && channelData.mentions !== null) {
    return channelData.mentions.toLocaleString()
  }
  return '-'
}

const getRateBackgroundColor = (brand: BrandData, channelKey: string): string | undefined => {
  const channelData = brand.data[channelKey]
  if (channelData) {
    return channelData.rateBackgroundColor
  }
  return undefined
}

const getTextColor = (brand: BrandData, channelKey: string): string | undefined => {
  const channelData = brand.data[channelKey]
  if (channelData) {
    return channelData.rateColor
  }
  return undefined
}

const handleCellClick = (brand: BrandData, dimension: string) => {
  const rowData = brand.data[dimension]
  emit('cell-click', { brand, dimension, rowData })
}
</script>

<template>
  <FEmpty v-if="!processedBrands.length" />

  <div v-else class="table-wrapper">
    <table>
      <thead>
        <tr>
          <th class="table-header first-column-header">品牌</th>
          <th class="spacer-column"></th>
          <template v-for="(dimension, index) in dimensions" :key="dimension">
            <th class="table-header">
              {{ getDimensionDisplayName(dimension) }}
            </th>
            <th v-if="index !== dimensions.length - 1" class="spacer-8"></th>
          </template>
        </tr>
      </thead>
      <tbody>
        <tr v-for="brand in processedBrands" :key="brand.brandName">
          <td
            class="first-column"
            :style="{
              backgroundColor: brand.brandName === MarketAverage ? '#E5FAFE' : undefined
            }"
          >
            <img
              v-if="brand.brandImageUrl"
              :src="brand.brandImageUrl"
              :alt="brand.brandName"
              class="brand-icon"
            />
            <div>{{ brand.brandName }}</div>
          </td>

          <td class="spacer-column"></td>

          <template v-for="(dimension, index) in dimensions" :key="dimension">
            <td
              :style="{
                width: `calc(100% / ${dimensions.length || 1})`,
                cursor: brand.brandName === MarketAverage ? 'default' : 'pointer',
                backgroundColor: getRateBackgroundColor(brand, dimension) || undefined,
                color: getTextColor(brand, dimension) || undefined
              }"
              @click="handleCellClick(brand, dimension)"
            >
              <HoverPopover
                :disabled="!brand.data[dimension]"
                :table-config="{
                  title: `${brand.brandName} - ${getDimensionDisplayName(dimension)}`,
                  data: fmtHoverData(brand.data[dimension]),
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
.table-wrapper {
  width: 100%;
  overflow-x: auto;
  margin-top: 16px;
}

table {
  border-collapse: separate;
  border-spacing: 0 8px;
  width: auto;

  th,
  td {
    width: 152px;
    min-width: 152px;
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

  th {
    height: 80px;
  }

  td {
    // height: 122px;
    height: 205px;
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
    width: 152px;
    background: #eaf3ff;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    line-height: 24px;
  }

  .first-column {
    width: 120px !important;
    min-width: 120px !important;
    max-width: 120px !important;
    background: #eaf3ff;
    font-weight: 600;
    font-size: 16px;
    color: #1d252f;
    line-height: 28px;

    .brand-icon {
      width: 24px;
      height: 24px;
      margin: 0 auto 8px;
      object-fit: contain;
    }
  }

  .first-column-header {
    width: 120px !important;
    min-width: 120px !important;
    max-width: 120px !important;
  }
}
</style>
