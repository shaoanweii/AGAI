<script setup lang="ts">
import JAChart from './JAChart.vue'
import JABarChart from './JABarChart.vue'
import TopFrequentScenarios from './TopFrequentScenarios.vue'
import TopSurgingScenarios from './TopSurgingScenarios.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import type {
  JourneyDetailAnalysisVo,
  SurgingSceneTopVo,
  HighFreqSceneTopVo
} from '@/api/journeyAnalysis/types'
import { computed, ref, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import { ElMessage } from 'element-plus'
import FDdbreadcrumb from '@components/UI/FDdbreadcrumb/index.vue'
import {
  isFirstLevelAll,
  computeTitleList,
  canDrillDown,
  getNextDrillDownLevel,
  mapTagPathToQueryParams,
  type TagPathItem
} from '../journeyAnalysis'

defineOptions({
  name: 'JourneyDetailed'
})

const { journeyDetailAnalysisData, surgingSceneTopData, highFreqSceneTopData, dataType, tagPath } =
  defineProps<{
    journeyDetailAnalysisData: JourneyDetailAnalysisVo[]
    surgingSceneTopData: SurgingSceneTopVo[]
    highFreqSceneTopData: HighFreqSceneTopVo[]
    dataType: MentionNegativeRateType
    tagPath?: Array<{ code: string; name: string; level?: number }>
  }>()

const emit = defineEmits<{
  'data-type-change': [dataType: MentionNegativeRateType, data: any]
  'chart-click': [data: any]
  'scene-click': [data: any]
}>()

const curTag = ref<any>()

// 内部标签路径状态
const localTagPath = ref<TagPathItem[]>([])

// 计算第一级是否是 'all'
const firstLevelIsAll = computed(() => {
  if (!tagPath || !Array.isArray(tagPath) || tagPath.length === 0) {
    return false
  }
  return isFirstLevelAll(tagPath)
})

// 监听 tagPath prop 的变化，同步到 localTagPath
watch(
  () => tagPath,
  newTagPath => {
    if (newTagPath && Array.isArray(newTagPath) && newTagPath.length > 0) {
      localTagPath.value = cloneDeep(newTagPath)
      // 默认不触发 chart-click，只有用户点击标题或图表时才触发
      curTag.value = undefined
    } else {
      localTagPath.value = []
      curTag.value = undefined
    }
  },
  { immediate: true, deep: true }
)

// 计算标题列表（转换为 BreadcrumbItem 格式）
const titleList = computed(() => {
  const list = computeTitleList(localTagPath.value, tagPath)
  // 确保返回的类型与 BreadcrumbItem 兼容
  return list.map(tag => ({
    code: tag.code,
    name: tag.name,
    level: tag.level
  }))
})

// 处理标题点击下钻（回退）
const handleClickCurTag = (item: { code?: string; name: string; level?: number }, index: number) => {
  console.log('item-->handleClickCurTag', item, index)

  if (!localTagPath.value || localTagPath.value.length === 0 || !item.code) {
    return
  }

  // 找到被点击标签在 localTagPath 中的位置
  const clickedTagIndex = localTagPath.value.findIndex(tag => tag.code === item.code)
  if (clickedTagIndex === -1) {
    return
  }

  const clickedLevel = item.level || 1

  // 回退逻辑：保留该标签及其之前的所有标签（包括同级和上级），移除该标签之后的所有标签
  // 需要找到该 level 的最后一个标签位置（包括所有同级标签）
  let lastIndexAtLevel = clickedTagIndex
  for (let i = clickedTagIndex + 1; i < localTagPath.value.length; i++) {
    const tag = localTagPath.value[i]
    const tagLevel = tag.level || 1
    if (tagLevel === clickedLevel) {
      // 如果是同级标签，也保留
      lastIndexAtLevel = i
    } else if (tagLevel < clickedLevel) {
      // 如果是上级标签，也保留
      lastIndexAtLevel = i
    } else {
      // 如果是下级标签，停止
      break
    }
  }

  // 保留到该 level 的最后一个标签
  const newTagPath = localTagPath.value.slice(0, lastIndexAtLevel + 1)

  // 更新 localTagPath（使用深拷贝）
  localTagPath.value = cloneDeep(newTagPath)

  // 映射查询参数
  // 关键：在多选场景下，当用户点击面包屑中的标签时，应该使用被点击标签的 code 作为 tag1Code
  // 例如：点击"选择"时，应该使用 "voc-journey-004" 作为 tag1Code，而不是默认的第一个标签
  const clickedTagCode = item.code
  const queryParams = mapTagPathToQueryParams(newTagPath, firstLevelIsAll.value, clickedTagCode, tagPath)

  curTag.value = queryParams

  // 触发图表数据重新获取
  emit('chart-click', queryParams)
}

// 处理图表点击事件
const handleChartClick = (chartData: JourneyDetailAnalysisVo) => {
  console.log('handleChartClick data', chartData)
  console.log('tagPath', tagPath)
  console.log('localTagPath.value', localTagPath.value)

  if (!localTagPath.value || localTagPath.value.length === 0) {
    return
  }

  // 检查是否可以下钻
  if (!canDrillDown(localTagPath.value, firstLevelIsAll.value)) {
    ElMessage.warning('当前已到最末级')
    return
  }

  // 计算下一个层级
  const nextLevel = getNextDrillDownLevel(localTagPath.value, firstLevelIsAll.value)

  // 添加新标签
  const newTagPath = [...localTagPath.value]
  newTagPath.push({
    code: chartData.tagCode,
    name: chartData.tagName,
    level: nextLevel
  })
  localTagPath.value = newTagPath

  // 映射查询参数
  // 关键：当第一级是 'all' 时，如果点击的是查询条件中的标签（如"认知"或"选择"），
  // 应该直接使用该标签的 code 作为 tag1Code
  // 所以直接使用 chartData.tagCode，因为这就是用户点击的标签
  const clickedTagCode = chartData.tagCode
  console.log('clickedTagCode', clickedTagCode)
  const queryParams = mapTagPathToQueryParams(
    newTagPath,
    firstLevelIsAll.value,
    clickedTagCode,
    tagPath // 传入查询条件中的原始标签路径，用于多选时查找点击的标签
  )
  console.log('queryParams', queryParams)

  curTag.value = queryParams

  // 触发事件
  emit('chart-click', queryParams)
}

// 处理场景点击事件
const handleSceneClick = (data: any) => {
  emit('scene-click', data)
}

// 高频/飙升场景TOP查看更多
const handleSceneViewMore = (source: 'highFreq' | 'surging') => {
  handleSceneClick({
    __viewMore: true,
    source,
    sceneName: source === 'highFreq' ? '高频场景TOP' : '飙升场景TOP'
  })
}

const handleDataTypeChange = (option: any) => {
  const dataType = option.value as MentionNegativeRateType

  emit('data-type-change', dataType, curTag.value)
}
</script>

<template>
  <div class="journey-detailed mt-24">
    <FCard title="旅程分析" :height="'382px'" class="f-card-border">
      <template #title>
        <FDdbreadcrumb
          :breadcrumb-list="titleList"
          suffix="旅程分析"
          @breadcrumb-click="handleClickCurTag"
        />
      </template>
      <template #more>
        <SwitchButton
          :options="[
            { value: 'negativeRate', label: '负面率' },
            { value: 'mention', label: '提及量' }
          ]"
          :modelValue="dataType"
          @change="handleDataTypeChange"
        ></SwitchButton>
      </template>
      <JAChart
        v-if="dataType === 'negativeRate'"
        :data="journeyDetailAnalysisData"
        @chart-click="handleChartClick"
      ></JAChart>
      <JABarChart
        v-if="dataType === 'mention'"
        :data="journeyDetailAnalysisData"
        :dataType="dataType"
        @chart-click="handleChartClick"
      ></JABarChart>
    </FCard>

    <div class="jd-content mt-24">
      <FCard
        title="高频场景TOP"
        titleSize="middle"
        :height="'335px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleSceneViewMore('highFreq')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopFrequentScenarios
          :data="highFreqSceneTopData"
          @scene-click="handleSceneClick"
        ></TopFrequentScenarios>
      </FCard>
      <FCard
        title="飙升场景TOP"
        titleSize="middle"
        :height="'335px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleSceneViewMore('surging')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopSurgingScenarios
          :data="surgingSceneTopData"
          @scene-click="handleSceneClick"
        ></TopSurgingScenarios>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.journey-detailed {
  .jd-content {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
}
</style>
