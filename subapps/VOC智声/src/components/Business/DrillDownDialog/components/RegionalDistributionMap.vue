<script setup lang="ts">
import { computed } from 'vue'
import type { PropType } from 'vue'
import FMapChart from '@/components/Charts/FMapChart/index.vue'

/**
 * 省份热力图（钻取弹窗专用）
 * @description 复用 FMapChart 渲染全国省份热力分布，颜色区间为 0% - 100%
 */

defineOptions({
  name: 'RegionalDistributionMap'
})

const props = defineProps({
  /** 数据源：接口返回字段
   *  provinceName: 省份名称（如：河南省）
   *  provinceCode: 省份编码
   *  negativeRate: 负面率（0-100）
   *  negativeRateMoM: 环比
   *  negativeRateYoY: 同比
   *  mentions: 提及量
   */
  data: {
    type: Array as PropType<any[]>,
    default: () => []
  },
  /** 数据类型：negativeRate-负面率 | mention-提及量 */
  dataType: {
    type: String as PropType<MentionNegativeRateType>,
    default: 'negativeRate'
  },
  /** 宽度 */
  width: { type: String, default: '100%' },
  /** 高度 */
  height: { type: String, default: '360px' },
  /** 是否显示视觉映射 */
  showVisualMap: { type: Boolean, default: true }
})

// 定义事件
const emit = defineEmits(['provinceClick'])

// 省份简称映射（与 china.json 名称一致）
const getSimplifiedProvinceName = (fullName: string): string => {
  const mapping: Record<string, string> = {
    北京市: '北京',
    天津市: '天津',
    河北省: '河北',
    山西省: '山西',
    内蒙古自治区: '内蒙古',
    辽宁省: '辽宁',
    吉林省: '吉林',
    黑龙江省: '黑龙江',
    上海市: '上海',
    江苏省: '江苏',
    浙江省: '浙江',
    安徽省: '安徽',
    福建省: '福建',
    江西省: '江西',
    山东省: '山东',
    河南省: '河南',
    湖北省: '湖北',
    湖南省: '湖南',
    广东省: '广东',
    广西壮族自治区: '广西',
    海南省: '海南',
    重庆市: '重庆',
    四川省: '四川',
    贵州省: '贵州',
    云南省: '云南',
    西藏自治区: '西藏',
    陕西省: '陕西',
    甘肃省: '甘肃',
    青海省: '青海',
    宁夏回族自治区: '宁夏',
    新疆维吾尔自治区: '新疆',
    台湾省: '台湾',
    香港特别行政区: '香港',
    澳门特别行政区: '澳门'
  }
  return mapping[fullName] || fullName
}

// 预处理数据：补充 name/value，确保与 FMapChart 默认 fieldMapping 兼容
const processedData = computed(() => {
  if (!Array.isArray(props.data)) return []
  return props.data.map((item: any) => ({
    ...item,
    name: getSimplifiedProvinceName(item?.provinceName || ''),
    value: props.dataType === 'mention' ? item?.mentions : item?.negativeRate
  }))
})

// 处理省份点击事件
const handleProvinceClick = (params: any) => {
  if (params?.data) {
    const provinceName = params.data.provinceName || params.name
    emit('provinceClick', provinceName, params.data)
  }
}

// 根据数据类型计算 Visual Map 配置
const visualMapConfig = computed(() => {
  if (props.dataType === 'mention') {
    // 提及量：计算最大值
    const maxMentions = Math.max(...props.data.map((item: any) => item?.mentions || 0), 1)
    return {
      min: 0,
      max: maxMentions,
      text: [String(maxMentions), '0'],
      formatter: (value: number) => String(Math.round(value))
    }
  } else {
    // 负面率：百分比
    return {
      min: 0,
      max: 100,
      text: ['100%', '0%'],
      formatter: (value: number) => value?.toFixed(2) + '%'
    }
  }
})

const { width, height } = props
</script>

<template>
  <FMapChart
    :data="processedData"
    :width="width"
    :height="height"
    :show-visual-map="showVisualMap"
    :visual-map-config="visualMapConfig"
    @chart-click="handleProvinceClick"
  />
</template>

<style scoped lang="scss">
/* 钻取弹窗内默认让地图充满容器 */
:deep(.echarts) {
  width: 100%;
  height: 100%;
}
</style>
