<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import BarGroupChart from './BarGroupChart.vue'
import TopTable from './TopTable.vue'
import type { IntentionOpinionTopVo } from '@/api/productAnalysis/types'
import type { ServiceIntentionOpinionTopVo } from '@/api/serviceAnalysis/types'
import FDdbreadcrumb from '@components/UI/FDdbreadcrumb/index.vue'
import { ElMessage } from 'element-plus'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'

defineOptions({
  name: 'FocusOnSceneDistribution'
})

interface TagPathItem {
  code: string
  name: string
  level?: number
}

// 接收从父组件传递的数据
interface Props {
  focusSceneAnalysisData?: any // 接收完整的接口返回数据
  intentionOpinionTopData?: {
    complaint: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
    consultation: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
    suggestion: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
    praise: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
  }
  brandName?: string
  ddBreadcrumb?: any //下钻面屑数据
  searchParams?: any // 查询参数
  tagPath?: TagPathItem[] // 标签路径信息
  fixedRootTag?: TagPathItem // 固定展示在标题首位的业务域节点
}

const {
  focusSceneAnalysisData = null,
  intentionOpinionTopData = {
    complaint: [],
    consultation: [],
    suggestion: [],
    praise: []
  },
  brandName = '当前品牌',
  searchParams = {}, // 查询参数，保留用于将来扩展
  tagPath = undefined,
  fixedRootTag = undefined
} = defineProps<Props>()

// 注意：searchParams 目前未直接使用，但保留作为 prop 用于将来扩展

// 定义emits
const emit = defineEmits<{
  'intention-top-sort': [{ intention: string; prop: string; order: string }]
  'data-type-change': [dataType: string, data: any]
  'chart-click': [data: any]
  'table-row-click': [{ intention: string; data: any }]
}>()

// Store和下钻相关状态
const curTag = ref<any>()
const isFixedRootScope = ref(false)

// 内部标签路径状态
const localTagPath = ref<TagPathItem[]>([])

// 监听 tagPath prop 的变化，同步到 localTagPath
watch(
  [() => tagPath, () => fixedRootTag],
  ([newTagPath, newFixedRootTag]) => {
    const nextTagPath = Array.isArray(newTagPath) ? cloneDeep(newTagPath) : []
    localTagPath.value = newFixedRootTag
      ? [
          { ...cloneDeep(newFixedRootTag), level: 1 },
          ...nextTagPath.filter(tag => tag.code !== newFixedRootTag.code)
        ]
      : nextTagPath

    // 新查询恢复顶部筛选范围，等待用户重新点击标题或图表下钻。
    curTag.value = undefined
    isFixedRootScope.value = false
  },
  { immediate: true, deep: true }
)

// 计算标题列表
// 同级标签分别显示，但用逗号分隔；不同级标签用 > 分隔
const titleList = computed(() => {
  const rootTitleTag = fixedRootTag ? [{ ...fixedRootTag, level: 1 }] : []

  if (!localTagPath.value || localTagPath.value.length === 0) {
    return rootTitleTag
  }

  // 找到查询条件中客户体验代码的最后一级（从 tagPath prop 传入的）
  // 如果 tagPath 存在，找到其中的最大 level；否则使用 localTagPath 中的最小 level
  let startLevel = 1
  if (tagPath && Array.isArray(tagPath) && tagPath.length > 0) {
    const queryMaxLevel = Math.max(...tagPath.map(tag => tag.level || 1))
    startLevel = queryMaxLevel
  } else {
    // 如果没有查询条件，从最小 level 开始
    startLevel = Math.min(...localTagPath.value.map(tag => tag.level || 1))
  }

  // 显示从查询条件最后一级开始到当前最大 level 的所有标签
  // 这样可以包含图表点击添加的子级标签
  const filteredTags = localTagPath.value.filter(
    tag => (tag.level || 1) >= startLevel && tag.code !== fixedRootTag?.code
  )

  // 将每个标签转换为 BreadcrumbItem，保留 level 信息用于判断是否同级
  return [
    ...rootTitleTag,
    ...filteredTags.map(tag => ({
      code: tag.code,
      name: tag.name,
      level: tag.level || 1
    }))
  ]
})

/**
 * 在固定一级范围内忽略顶部体验代码筛选，但保留其他查询条件。
 * @param params 当前场景下钻参数
 * @returns 可覆盖页面查询条件的模块请求参数
 */
const applyFixedRootScope = (params: Record<string, any>) => {
  if (!isFixedRootScope.value) {
    return params
  }

  return {
    ...params,
    firstCodeTag: undefined,
    secondCodeTag: undefined,
    threeCodeTag: undefined,
    fourCodeTag: undefined
  }
}

// 处理标题点击下钻
const handleClickCurTag = (item: any, index: number) => {
  console.log('item-->handleClickCurTag', item, index)

  if (!localTagPath.value || localTagPath.value.length === 0) {
    return
  }

  // 使用被点击的标签信息（item 包含 code 和 level）
  const clickedCode = item.code
  const clickedLevel = item.level || 1
  if (clickedCode === fixedRootTag?.code) {
    isFixedRootScope.value = true
  }

  // 找到被点击标签在 localTagPath 中的位置
  const clickedTagIndex = localTagPath.value.findIndex(tag => tag.code === clickedCode)
  if (clickedTagIndex === -1) {
    return
  }

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

  // 构建 curTag（使用被点击的标签的 code，而不是固定使用第一个）
  const curTagValue: any = {}

  // 按级别分组标签
  const level1Tags: Array<{ code: string; name: string }> = []
  const level2Tags: Array<{ code: string; name: string }> = []
  const level3Tags: Array<{ code: string; name: string }> = []

  newTagPath.forEach(tag => {
    const tagLevel = tag.level || 1
    if (tagLevel === 1) {
      level1Tags.push(tag)
    } else if (tagLevel === 2) {
      level2Tags.push(tag)
    } else if (tagLevel === 3) {
      level3Tags.push(tag)
    }
  })

  // 设置被点击级别的 code（使用被点击的标签的 code）
  if (clickedLevel === 1 && level1Tags.length > 0) {
    // 找到被点击的标签，如果找不到则使用第一个
    const clickedTag = level1Tags.find(tag => tag.code === clickedCode)
    curTagValue.tag1Code = clickedTag ? clickedTag.code : level1Tags[0].code
  } else if (clickedLevel === 2 && level2Tags.length > 0) {
    const clickedTag = level2Tags.find(tag => tag.code === clickedCode)
    curTagValue.tag2Code = clickedTag ? clickedTag.code : level2Tags[0].code
  } else if (clickedLevel === 3 && level3Tags.length > 0) {
    const clickedTag = level3Tags.find(tag => tag.code === clickedCode)
    curTagValue.tag3Code = clickedTag ? clickedTag.code : level3Tags[0].code
  }

  // 设置其他级别的 code（使用第一个标签的 code）
  if (clickedLevel > 1 && level1Tags.length > 0) {
    curTagValue.tag1Code = level1Tags[0].code
  }
  if (clickedLevel > 2 && level2Tags.length > 0) {
    curTagValue.tag2Code = level2Tags[0].code
  }

  const nextCurTagValue = applyFixedRootScope(curTagValue)
  curTag.value = nextCurTagValue

  // 触发图表数据重新获取
  emit('chart-click', nextCurTagValue)
}

// 数据类型：负面率或提及量
const dataType = ref<MentionNegativeRateType>('negativeRate')

// SwitchButton配置选项
const switchOptions = [
  { value: 'negativeRate', label: '负面率' },
  { value: 'mention', label: '提及量' }
]

// 处理SwitchButton切换事件
const handleDataTypeChange = (option: any) => {
  dataType.value = option.value
  emit('data-type-change', option.value, curTag.value)
}

// 获取品牌名称和均值名称
const brandDisplayName = computed(() => {
  return focusSceneAnalysisData?.name || brandName
})

const avgDisplayName = computed(() => {
  return focusSceneAnalysisData?.avgName || '集团均值'
})

// 处理TopTable的排序事件
const handleTopTableSort = ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  emit('intention-top-sort', { intention, prop, order })
}

// 查看更多（观点TOP）
const handleTableViewMore = (intention: string) => {
  emit('table-row-click', {
    intention,
    data: {
      __viewMore: true,
      tableTitle: `${intention}观点TOP`
    }
  })
}

// 处理图表点击事件
const handleChartClick = (chartData: any) => {
  const data = chartData.data
  console.log('data', data)

  if (!localTagPath.value || localTagPath.value.length === 0) {
    return
  }

  // 获取当前标签路径的最大 level
  const maxLevel = Math.max(...localTagPath.value.map(tag => tag.level || 0))

  if (maxLevel === 1) {
    // 当前是第1级，添加第2级标签
    const newTagPath = cloneDeep(localTagPath.value)
    newTagPath.push({ code: data.tagCode, name: data.tagName, level: 2 })
    localTagPath.value = newTagPath

    // 构建 curTag（保留所有一级标签，添加二级标签）
    const curTagValue: any = {}
    const level1Codes: string[] = []
    newTagPath.forEach(tag => {
      if (tag.level === 1) {
        level1Codes.push(tag.code)
      }
    })

    // 根据点击的 tagCode 找到它的父级标签（通过前缀匹配）
    let parentTagCode: string | undefined = undefined
    if (level1Codes.length > 0) {
      // 找到最长的匹配前缀
      for (const level1Code of level1Codes) {
        if (data.tagCode.startsWith(level1Code)) {
          if (!parentTagCode || level1Code.length > parentTagCode.length) {
            parentTagCode = level1Code
          }
        }
      }
    }

    // 如果找到了父级标签，使用它；否则使用第一个一级标签
    curTagValue.tag1Code = parentTagCode || (level1Codes.length > 0 ? level1Codes[0] : undefined)
    curTagValue.tag2Code = data.tagCode

    const nextCurTagValue = applyFixedRootScope(curTagValue)
    curTag.value = nextCurTagValue
    emit('chart-click', nextCurTagValue)
  } else if (maxLevel === 2) {
    // 当前是第2级，添加第3级标签
    const newTagPath = cloneDeep(localTagPath.value)
    newTagPath.push({ code: data.tagCode, name: data.tagName, level: 3 })
    localTagPath.value = newTagPath

    // 构建 curTag（保留所有一级和二级标签，添加三级标签）
    const curTagValue: any = {}
    const level1Codes: string[] = []
    const level2Codes: string[] = []
    newTagPath.forEach(tag => {
      if (tag.level === 1) {
        level1Codes.push(tag.code)
      } else if (tag.level === 2) {
        level2Codes.push(tag.code)
      }
    })

    // 根据点击的 tagCode 找到它的父级标签（二级标签）
    let parentLevel2Code: string | undefined = undefined
    if (level2Codes.length > 0) {
      // 找到最长的匹配前缀
      for (const level2Code of level2Codes) {
        if (data.tagCode.startsWith(level2Code)) {
          if (!parentLevel2Code || level2Code.length > parentLevel2Code.length) {
            parentLevel2Code = level2Code
          }
        }
      }
    }

    // 根据二级标签找到它的一级父级标签
    let parentLevel1Code: string | undefined = undefined
    if (parentLevel2Code && level1Codes.length > 0) {
      // 找到最长的匹配前缀
      for (const level1Code of level1Codes) {
        if (parentLevel2Code.startsWith(level1Code)) {
          if (!parentLevel1Code || level1Code.length > parentLevel1Code.length) {
            parentLevel1Code = level1Code
          }
        }
      }
    }

    // 如果找到了父级标签，使用它们；否则使用第一个标签
    curTagValue.tag1Code = parentLevel1Code || (level1Codes.length > 0 ? level1Codes[0] : undefined)
    curTagValue.tag2Code = parentLevel2Code || (level2Codes.length > 0 ? level2Codes[0] : undefined)
    curTagValue.tag3Code = data.tagCode

    const nextCurTagValue = applyFixedRootScope(curTagValue)
    curTag.value = nextCurTagValue
    emit('chart-click', nextCurTagValue)
  } else if (maxLevel >= 3) {
    ElMessage.warning('当前已到最末级')
  }
}

// 处理表格行点击事件
const handleTableRowClick = (data: any, intention: string) => {
  emit('table-row-click', { intention, data })
}

// 表格引用
const complaintTableRef = ref<any>(null)
const consultationTableRef = ref<any>(null)
const suggestionTableRef = ref<any>(null)
const praiseTableRef = ref<any>(null)

// 清空所有表格的排序状态
const clearAllSort = () => {
  complaintTableRef.value?.clearSort()
  consultationTableRef.value?.clearSort()
  suggestionTableRef.value?.clearSort()
  praiseTableRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearAllSort
})
</script>

<template>
  <div class="focus-on-scene-distribution">
    <FCard
      title="场景分析"
      :height="'382px'"
      titleSize="middle"
      :isShowMore="true"
      class="mt-24 f-card-border"
    >
      <template #title>
        <FDdbreadcrumb
          :breadcrumb-list="titleList"
          suffix="场景分析"
          @breadcrumb-click="handleClickCurTag"
        />
      </template>
      <template #more>
        <SwitchButton
          v-model="dataType"
          :options="switchOptions"
          @change="handleDataTypeChange"
        ></SwitchButton>
      </template>
      <BarGroupChart
        :focus-scene-analysis-data="focusSceneAnalysisData"
        :brand-name="brandDisplayName"
        :avg-name="avgDisplayName"
        :data-type="dataType"
        @chart-click="handleChartClick"
      ></BarGroupChart>
    </FCard>
    <div class="mt-24 top-container">
      <FCard
        :title="'抱怨观点TOP'"
        titleSize="small"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleTableViewMore('抱怨')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="complaintTableRef"
          :data="intentionOpinionTopData.complaint"
          intention="抱怨"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '抱怨')"
        ></TopTable>
      </FCard>
      <FCard
        :title="'咨询观点TOP'"
        titleSize="small"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleTableViewMore('咨询')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="consultationTableRef"
          :data="intentionOpinionTopData.consultation"
          intention="咨询"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '咨询')"
        ></TopTable>
      </FCard>
      <FCard
        :title="'建议观点TOP'"
        titleSize="small"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleTableViewMore('建议')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="suggestionTableRef"
          :data="intentionOpinionTopData.suggestion"
          intention="建议"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '建议')"
        ></TopTable>
      </FCard>
      <FCard
        :title="'表扬观点TOP'"
        titleSize="small"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleTableViewMore('表扬')"
      >
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="praiseTableRef"
          :data="intentionOpinionTopData.praise"
          intention="表扬"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '表扬')"
        ></TopTable>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss">
.focus-on-scene-distribution {
  .top-container {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
}
</style>
