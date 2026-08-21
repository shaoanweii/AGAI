<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getBrandRankingQueryParams } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import { getBrandRanking } from '@/api/overview/leader'
import RankTable from './RankTable.vue'
import { debounce } from 'lodash-es'

defineOptions({
  name: 'BrandRank'
})

// 事件定义
const emit = defineEmits<{
  tableClick: [data: { row: any; column?: any; sceneData?: any; queryType: string }]
}>()

// 处理表格点击事件
const handleTableClick = (data: { row: any; column?: any; sceneData?: any; queryType: string }) => {
  emit('tableClick', data)
}

// 基础设置
const curSort = ref<string>('')
const curOrder = ref<string | null>(null)
const loading = ref<boolean>(true)
const theData = ref<any[]>([])

// 下拉框
const selectType = ref<any>('mention')
const selectOpts = ref([
  { itemValue: 'negativeRate', itemText: '负面率排名' },
  { itemValue: 'mention', itemText: '提及量排名' }
])

// 调接口
const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams

/**
 * 当前市场横评默认卡片就是集团卡。
 * 当 store 里还未同步 brandCode 时，也按集团模式展示，避免右侧切换与卡片选中态不一致。
 */
const isGroupSelected = computed(() => !storePms.brandCode || storePms.brandCode === 'groupCode')

/**
 * 根据当前卡片选中态返回该模式下的默认维度。
 * - 集团：默认展示车企
 * - 具体品牌：默认展示品牌
 */
const getDefaultSwitchType = () => (isGroupSelected.value ? 'seriesFactory' : 'brand')

// Switch
const switchType = ref<any>(getDefaultSwitchType())

const rankTableRef = ref<InstanceType<typeof RankTable> | null>(null)

/**
 * 根据当前卡片选中态动态切换右上角维度：
 * - 集团：车企/品牌
 * - 具体品牌：品牌/车系
 */
const switchOpts = computed(() => {
  if (isGroupSelected.value) {
    return [
      { value: 'seriesFactory', label: '车企' },
      { value: 'brand', label: '品牌' }
    ]
  }

  return [
    { value: 'brand', label: '品牌' },
    { value: 'series', label: '车系' }
  ]
})

/**
 * 归一化当前 switch 值，保证切换品牌卡片后仍然命中合法维度。
 * 例如：
 * - 集团模式下不允许保留 series
 * - 具体品牌模式下不允许保留 seriesFactory
 */
const normalizeSwitchType = () => {
  const validSwitchValues = switchOpts.value.map(item => item.value)
  if (validSwitchValues.includes(switchType.value)) {
    return false
  }

  switchType.value = getDefaultSwitchType()
  return true
}

/**
 * 按当前模式恢复默认 switch。
 * 仅用于页面初始化、时间重置或品牌模式切换这类“系统重置”场景，
 * 不影响用户在同一模式下主动切换后的选择。
 */
const resetSwitchToDefault = () => {
  const defaultSwitchType = getDefaultSwitchType()
  if (switchType.value === defaultSwitchType) {
    return false
  }

  switchType.value = defaultSwitchType
  return true
}

const fetchData = async () => {
  let errMsg = '获取集团简报数据失败'

  try {
    loading.value = true
    theData.value = []

    const queryParams = getBrandRankingQueryParams(storePms, {
      queryType: switchType.value,
      dataType: selectType.value,
      sortField: curSort.value,
      sortOrder: curOrder.value
    })

    const response = await getBrandRanking(queryParams)
    if (response.success && response.result) {
      theData.value = response.result.slice(0, 6)
    } else {
      ElMessage.error(response.message || errMsg)
    }
  } catch (error) {
    console.error(`${errMsg}:, ${error}`)
    ElMessage.error(`${errMsg}，请稍后重试`)
  } finally {
    loading.value = false
  }
}

const fetchDataDelay = debounce(fetchData, 300)

const handleSwitch = () => {
  curSort.value = ''
  curOrder.value = null
  rankTableRef.value?.clearSort()
  fetchDataDelay()
}

// 排序
const handleSort = (prop: string, order: string | null) => {
  // console.log('sort', prop, order)
  curSort.value = prop
  curOrder.value = order
  fetchDataDelay()
}

onMounted(() => {
  resetSwitchToDefault()
  fetchDataDelay()
})

watch(
  () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate
  }),
  () => {
    if (isGroupSelected.value && resetSwitchToDefault()) {
      handleSwitch()
      return
    }

    fetchDataDelay()
  }
)

watch(
  () => storePms.brandCode,
  () => {
    fetchDataDelay()
  }
)

watch(
  isGroupSelected,
  (newValue, oldValue) => {
    if (oldValue === undefined) {
      return
    }

    if (!newValue) {
      if (normalizeSwitchType()) {
        handleSwitch()
      }
      return
    }

    if (oldValue === false && resetSwitchToDefault()) {
      handleSwitch()
    }
  }
)
</script>

<template>
  <FCard title="品牌排行" style="box-shadow: none !important">
    <!-- 下拉框` 负面率/提及量` -->
    <template #leftExtra>
      <FSelect
        v-model="selectType"
        :options="selectOpts"
        :clearable="false"
        :filterable="false"
        style="width: 122px; margin-left: 8px"
        @change="fetchData"
      />
    </template>
    <template #more>
      <!-- switch 品牌/车系 -->
      <SwitchButton
        v-model="switchType"
        :options="switchOpts"
        @change="handleSwitch"
      ></SwitchButton>
    </template>

    <!-- 砖块图 -->
    <div style="width: calc(100% + 10px); margin-top: 14px; position: relative; left: -5px">
      <RankTable
        ref="rankTableRef"
        :loading="loading"
        :ranking-data="theData"
        :data-type="selectType"
        :query-type="switchType"
        :highlight-threshold="30"
        @sort="handleSort"
        @table-click="handleTableClick"
      />
    </div>
  </FCard>
</template>

<style lang="scss" scoped></style>
