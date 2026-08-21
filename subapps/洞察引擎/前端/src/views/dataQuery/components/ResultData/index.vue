<script setup lang="ts">
import dayjs from 'dayjs'
import { inject, h } from 'vue'
import { useTable } from '@/hooks/table'
import type { ConditionsDetailItem } from '@/types'
import { computedCardHeight } from '@/utils'
// import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { ElMessage, ElButton, ElCheckbox, TableV2FixedDir, ElTooltip } from 'element-plus'
import type { Column } from 'element-plus'
import ErrorCorrectionDialog from './ErrorCorrectionDialog.vue'
import { findAllFinalTagLib } from '@/api/tag'
import { getTagLibClientTree } from '@/api/main'
import FDatePicker from '@/components/FDatePicker/index.vue'
import { showOverflowTooltipConfig } from '@/constant/index'
import FtCard from '@/components/FtCard.vue'
import { exportResultData } from '@/api/downloadManagement'
import to from 'await-to-js'
import DownloadDialog from '../RawData/DownloadDialog.vue'
import { hasPermission } from '@/utils/permission'
import { DATAQUERY_DOWNLOAD_MAP } from '@/constant'
import CarSceneFilter from '@/components/Business/CarSceneFilter.vue'
import SelectV2WithSelectAll from '@/components/Business/SelectV2WithSelectAll.vue'
import {
  useLazyExperienceCodeCascader,
  normalizeExperienceCodeSelectionPaths,
  type ExperienceCodeSourceNode
} from '@/hooks/useLazyExperienceCodeCascader'

defineOptions({
  name: 'ResultData'
})

interface CarSceneQueryFilters {
  usageScenarioFirstList: string[]
  usageScenarioSecondList: string[]
}

// const { refDom: channelRef, formatLabelHandle } = useComputedCascaderWidth()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const channelOptions = inject('channelOptions') as Ref<any[]>

// 统一处理conditions为空时的默认值
const getConditionOptions = (key: string) => {
  return conditions?.[key] || []
}

const {
  table,
  form,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  getTableData,
  getFirstPageTableData,
  handleEdit
} = useTable(
  {
    method: 'POST',
    url: '/insights/insCqCaDataSource/getResultData'
  },
  res => {
    return res.result
  }
)

const times = ref<any[]>([])
const defaultShortcutValue = '近7天'
const shortcutValue = ref(defaultShortcutValue)
// 业务要求：结果数据筛选时间不能晚于今天，避免提交未来日期范围。
const maxSelectableDate = dayjs().format('YYYY-MM-DD')
// const shortcutValue = ref('本月')
const multipleSelection = ref<any[]>([])
const isExpanded = ref(false)
const downloadDialogVisible = ref(false)
const exporting = ref(false)
const experienceCodeLoading = ref(false)
const standardOpinionRequestSeq = ref(0)

// 中文注释：纠错弹窗仍复用标准观点下拉数据，这里继续保留原有加载逻辑。
const topicOptions = ref<any[]>([])
const carSeriesOptions = ref<any[]>([])
const experienceCodeSelection = ref<string[][]>([])
const standardOpinionSelection = ref<string[]>([])
const experienceCodeLegacyFilters = ref({
  firstCodeTag: [] as string[],
  secondCodeTag: [] as string[],
  threeCodeTag: [] as string[],
  fourCodeTag: [] as string[],
  topicList: [] as string[]
})
// 中文注释：级联多选开启 emitPath 后，场景值需要保留完整路径，便于区分同名节点。
const carSceneSelection = ref<string[][]>([])
const carSceneFilters = ref<CarSceneQueryFilters>({
  usageScenarioFirstList: [],
  usageScenarioSecondList: []
})

const channelSelection = ref<string[][]>([])
const channelCascaderProps = {
  label: 'name',
  value: 'code',
  children: 'child',
  multiple: true,
  emitPath: true,
  checkStrictly: true
}

const syncChannelFilter = () => {
  const firstCodes = new Set<string>()
  const secondCodes = new Set<string>()
  const thirdCodes = new Set<string>()

  ;(channelSelection.value || []).forEach(path => {
    if (!Array.isArray(path) || path.length === 0) return
    if (path[0]) firstCodes.add(path[0])
    if (path[1]) secondCodes.add(path[1])
    if (path[2]) thirdCodes.add(path[2])
  })

  table.filter.firstChannelCodeList = Array.from(firstCodes)
  table.filter.secondChannelCodeList = Array.from(secondCodes)
  table.filter.threeChannelCodeList = Array.from(thirdCodes)
}

const {
  cascaderOptions: experienceCodeOptions,
  nodeMap: experienceCodeNodeMap,
  cascaderProps: experienceCodeCascaderProps,
  setSourceTree: setExperienceCodeTree,
  beforeFilter: handleExperienceCodeBeforeFilter,
  filterMethod: filterExperienceCodeMethod,
  formatSuggestionPath: formatExperienceCodeSuggestionPath
} = useLazyExperienceCodeCascader()

const isDownload = computed(() => {
  return hasPermission(DATAQUERY_DOWNLOAD_MAP.DOWNLOAD)
})

/**
 * @description: 统一清洗体验代码级联路径，避免空值和异常结构污染查询口径。
 * @param {unknown} path 级联返回的单条路径
 * @return {string[]} 清洗后的 id 路径
 */
const normalizeExperienceCodePath = (path: unknown): string[] => {
  if (!Array.isArray(path)) return []

  return path.map(item => String(item ?? '').trim()).filter(Boolean)
}

/**
 * @description: 从接口固定返回结构中提取真实一级体验代码节点。
 * @param {ExperienceCodeSourceNode | null | undefined} rootNode 接口根节点
 * @return {ExperienceCodeSourceNode[]} 根节点下的一级分类
 */
const getExperienceCodeRootChildren = (rootNode?: ExperienceCodeSourceNode | null) => {
  // 中文注释：当前接口首项固定为“全领域业务”根节点，筛选区只展示它的 child 作为真实一级分类。
  return Array.isArray(rootNode?.child) ? rootNode.child : []
}

/**
 * @description: 返回空的体验代码旧接口筛选结构，重置和初始化时保持查询参数稳定。
 * @return {Record<string, string[]>} 空筛选结构
 */
const createEmptyExperienceCodeLegacyFilters = () => {
  return {
    firstCodeTag: [] as string[],
    secondCodeTag: [] as string[],
    threeCodeTag: [] as string[],
    fourCodeTag: [] as string[],
    topicList: [] as string[]
  }
}

/**
 * @description: 将级联多选结果还原为旧接口沿用的 1-4 级体验代码筛选字段。
 * @param {string[][]} selection 当前体验代码选中路径
 * @return {Record<string, string[]>} 旧接口筛选参数
 */
const buildExperienceCodeLegacyFilters = (selection: string[][]) => {
  const legacyFilters = createEmptyExperienceCodeLegacyFilters()
  if (!Array.isArray(selection) || !selection.length) {
    return legacyFilters
  }

  const firstCodeTag = new Set<string>()
  const secondCodeTag = new Set<string>()
  const threeCodeTag = new Set<string>()
  const fourCodeTag = new Set<string>()

  normalizeExperienceCodeSelectionPaths(selection).forEach(path => {
    const normalizedPath = normalizeExperienceCodePath(path)
    if (!normalizedPath.length) return
    const selectedNode = experienceCodeNodeMap.value[normalizedPath[normalizedPath.length - 1]]
    if (!selectedNode?.tagCode) return

    switch (selectedNode.level) {
      case 1:
        firstCodeTag.add(selectedNode.tagCode)
        break
      case 2:
        secondCodeTag.add(selectedNode.tagCode)
        break
      case 3:
        threeCodeTag.add(selectedNode.tagCode)
        break
      default:
        fourCodeTag.add(selectedNode.tagCode)
        break
    }
  })

  return {
    firstCodeTag: Array.from(firstCodeTag),
    secondCodeTag: Array.from(secondCodeTag),
    threeCodeTag: Array.from(threeCodeTag),
    fourCodeTag: Array.from(fourCodeTag),
    topicList: []
  }
}

/**
 * @description: 取体验代码多选结果的末级节点 id，供标准观点接口按分类范围过滤。
 * @param {string[][]} selection 当前体验代码选中路径
 * @return {string[]} 去重后的分类 id 列表
 */
const collectSelectedExperienceCodeIds = (selection: string[][]) => {
  const tagParentIds = new Set<string>()

  normalizeExperienceCodeSelectionPaths(selection).forEach(path => {
    const normalizedPath = normalizeExperienceCodePath(path)
    const selectedId = normalizedPath[normalizedPath.length - 1]
    if (selectedId) {
      tagParentIds.add(selectedId)
    }
  })

  return Array.from(tagParentIds)
}

/**
 * @description: 页面内直接获取 1-4 级体验代码树，避免再依赖公共弹层组件。
 * @return {*}
 */
const getExperienceCodeOptions = async () => {
  experienceCodeLoading.value = true
  try {
    const res = await getTagLibClientTree({ level: '4', tagAttribute: 'Category', tagType: 'CA' })
    const sourceTree = getExperienceCodeRootChildren(res?.result?.[0])
    setExperienceCodeTree(sourceTree)
    experienceCodeSelection.value = experienceCodeSelection.value
      .map(path => normalizeExperienceCodePath(path))
      .filter(path => {
        const selectedId = path[path.length - 1]
        return !!selectedId && !!experienceCodeNodeMap.value[selectedId]
      })
    experienceCodeLegacyFilters.value = buildExperienceCodeLegacyFilters(
      experienceCodeSelection.value
    )
  } catch {
    setExperienceCodeTree([])
    experienceCodeSelection.value = []
    experienceCodeLegacyFilters.value = createEmptyExperienceCodeLegacyFilters()
  } finally {
    experienceCodeLoading.value = false
  }
}

/**
 * @description: 获取标准观点选项；首次进入页面拉全量，体验代码变化后再按选中分类 id 过滤。
 * @param {{ tagParentIds?: string[] }} params 标准观点查询参数
 * @return {*}
 */
const getLastTagOptions = async (params: { tagParentIds?: string[] } = {}) => {
  const requestSeq = standardOpinionRequestSeq.value + 1
  standardOpinionRequestSeq.value = requestSeq
  try {
    const res = await findAllFinalTagLib(params)
    if (requestSeq !== standardOpinionRequestSeq.value) return
    topicOptions.value = Array.isArray(res?.result) ? res.result : []
    const availableCodes = new Set(
      topicOptions.value.map((item: any) => String(item?.tagCode ?? '').trim()).filter(Boolean)
    )
    // 中文注释：体验代码变更后仅保留当前新结果集仍然存在的标准观点，避免提交失效编码。
    standardOpinionSelection.value = standardOpinionSelection.value.filter(code =>
      availableCodes.has(String(code))
    )
  } catch {
    if (requestSeq !== standardOpinionRequestSeq.value) return
    topicOptions.value = []
    standardOpinionSelection.value = []
  }
}

// 品牌变化事件处理
const handleBrandChange = () => {
  table.filter.carSeries = []

  if (!table.filter.brandCode?.length || !getConditionOptions('brandCar')?.length) {
    carSeriesOptions.value = []
    return
  }

  const allCarSeries: any[] = []
  table.filter.brandCode.forEach((brandCode: string) => {
    const brand = getConditionOptions('brandCar').find((item: any) => item.key === brandCode)
    if (brand?.children?.length) {
      allCarSeries.push(...brand.children)
    }
  })

  carSeriesOptions.value = allCarSeries
}

/**
 * @description: 点击纠错
 * @param {*} row
 * @return {*}
 */
const customerHandleEdit = (row?: any) => {
  handleEdit([row])
}

/**
 * @description: 批量纠错
 * @return {*}
 */
const handleBatchEc = () => {
  if (!multipleSelection.value?.length) {
    ElMessage.error('请先选择数据')
    return
  }
  handleEdit(multipleSelection.value)
}

/**
 * @description: 体验代码变化后，同步旧接口筛选字段并按当前分类 id 重新拉取标准观点。
 * @param {string[][]} selection 当前体验代码级联选中路径
 * @return {*}
 */
const handleExperienceCodeChange = (selection: string[][]) => {
  experienceCodeLegacyFilters.value = buildExperienceCodeLegacyFilters(selection)
  void getLastTagOptions({
    tagParentIds: collectSelectedExperienceCodeIds(selection)
  })
}

/**
 * @description: 返回空的用车场景筛选字段，重置时同步清理查询和导出参数。
 * @return {CarSceneQueryFilters} 空筛选字段
 */
const createEmptyCarSceneFilters = (): CarSceneQueryFilters => {
  return {
    usageScenarioFirstList: [],
    usageScenarioSecondList: []
  }
}

/**
 * @description: 接收用车场景组件拆分后的一级、二级筛选字段，统一给查询和导出复用。
 * @param {CarSceneQueryFilters} filters 用车场景筛选参数
 * @return {*}
 */
const handleCarSceneFilterChange = (filters: CarSceneQueryFilters) => {
  carSceneFilters.value = {
    usageScenarioFirstList: filters.usageScenarioFirstList || [],
    usageScenarioSecondList: filters.usageScenarioSecondList || []
  }
}

const syncDerivedFilter = () => {
  const [startTime, endTime] = times.value

  table.filter.startTime = startTime
  table.filter.endTime = endTime
  syncChannelFilter()
  table.filter.labelType = []
  table.filter.endTag = []
  table.filter.firstCodeTag = experienceCodeLegacyFilters.value.firstCodeTag
  table.filter.secondCodeTag = experienceCodeLegacyFilters.value.secondCodeTag
  table.filter.threeCodeTag = experienceCodeLegacyFilters.value.threeCodeTag
  table.filter.fourCodeTag = experienceCodeLegacyFilters.value.fourCodeTag
  table.filter.topicList = standardOpinionSelection.value
  table.filter.usageScenarioFirstList = carSceneFilters.value.usageScenarioFirstList
  table.filter.usageScenarioSecondList = carSceneFilters.value.usageScenarioSecondList
  delete table.filter.topic
}

const getSelectedDataIds = () => {
  return (multipleSelection.value || [])
    .map(item => item?.id ?? item?.dataId)
    .filter((id: any) => id !== undefined && id !== null && String(id).trim() !== '')
}

const handleExport = async () => {
  if (exporting.value) return

  const selectedIds = getSelectedDataIds()
  if (!selectedIds.length && Number(table.total || 0) <= 0) {
    ElMessage.warning('暂无可导出的数据')
    return
  }

  if (selectedIds.length > 100000 || (!selectedIds.length && Number(table.total || 0) > 100000)) {
    ElMessage.warning('当前系统仅支持导出数据上限为10万条，请合理筛选数据范围后重试。')
    return
  }

  syncDerivedFilter()

  const params: Record<string, any> = {
    ...table.filter
  }
  if (selectedIds.length) {
    params.idList = selectedIds
  }

  exporting.value = true
  const [errs] = await to(exportResultData(params))
  exporting.value = false

  if (errs) {
    ElMessage.error(errs?.message || '导出失败，请稍后重试')
    return
  }
  downloadDialogVisible.value = true
}

const init = () => {
  form.data = []
  void Promise.all([getExperienceCodeOptions(), getLastTagOptions({})])
}
init()

onMounted(() => {
  query()
})

const query = (resetPage = true) => {
  multipleSelection.value = []
  syncDerivedFilter()

  if (resetPage) {
    getFirstPageTableData()
  } else {
    getTableData(false)
  }
}
const reset = () => {
  handleReset(() => {
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
    shortcutValue.value = defaultShortcutValue

    multipleSelection.value = []
    experienceCodeSelection.value = []
    standardOpinionSelection.value = []
    experienceCodeLegacyFilters.value = createEmptyExperienceCodeLegacyFilters()
    void getLastTagOptions({ tagParentIds: [] })
    carSceneSelection.value = []
    carSceneFilters.value = createEmptyCarSceneFilters()
    channelSelection.value = []
  })
}

const handleCorrectionSuccess = () => {
  multipleSelection.value = []
  query(false)
}

const tableFcardHeight = computed(() => {
  return computedCardHeight(isExpanded.value ? 275 : 155)
})

const RESULT_DATA_HEADER_HEIGHT = 56
const RESULT_DATA_ROW_HEIGHT = 64

type ResultDataColumnDef = {
  key: string
  title: string
  dataKeys: string[]
  width?: number
  fixed?: true | TableV2FixedDir
  twoLine?: boolean
}

const correctionRows = computed(() => {
  return Array.isArray(form.data) ? form.data : []
})

const formatTableText = (value: unknown) => {
  return value === undefined || value === null ? '' : String(value)
}

const resolveResultDataValue = (rowData: Record<string, unknown>, dataKeys: string[]) => {
  const matchedKey = dataKeys.find(key => {
    const value = rowData[key]
    return value !== undefined && value !== null && String(value).trim() !== ''
  })
  return matchedKey ? rowData[matchedKey] : ''
}

const renderResultDataTextCell = (text: unknown, twoLine = false) => {
  const value = formatTableText(text)
  return h(ElTooltip, { content: value, placement: 'top', ...showOverflowTooltipConfig }, () =>
    h('div', { class: twoLine ? 'two-line-ellipsis' : 'text-ellipsis' }, value)
  )
}

const createResultDataColumn = (field: ResultDataColumnDef): Column => ({
  key: field.key,
  title: field.title,
  width: field.width ?? 160,
  fixed: field.fixed,
  cellRenderer: ({ rowData }) =>
    renderResultDataTextCell(resolveResultDataValue(rowData, field.dataKeys), field.twoLine)
})

const resultDataColumnFields: ResultDataColumnDef[] = [
  { key: 'id', title: '声音片段ID', dataKeys: ['id', 'soundsId'], width: 180 },
  {
    key: 'originalTextScene',
    title: '声音片段',
    dataKeys: ['originalTextScene'],
    width: 220,
    twoLine: true
  },
  { key: 'seriesFactory', title: '车企', dataKeys: ['seriesFactory', 'companyName'], width: 140 },
  { key: 'brandName', title: '品牌', dataKeys: ['brandName'], width: 140 },
  { key: 'carSeriesName', title: '车系', dataKeys: ['carSeriesName'], width: 140 },
  { key: 'modelName', title: '车型', dataKeys: ['modelName'], width: 140 },
  { key: 'opinion', title: '原始观点', dataKeys: ['opinion'], width: 220, twoLine: true },
  { key: 'topicText', title: '标准观点', dataKeys: ['topicText'], width: 180 },
  { key: 'topic', title: '标准观点编码', dataKeys: ['topic'], width: 160 },
  { key: 'hotWord', title: '热词', dataKeys: ['hotWord', 'keywords'], width: 140 },
  { key: 'sentiment', title: '情感', dataKeys: ['sentiment'], width: 100 },
  { key: 'intention', title: '意图', dataKeys: ['intention'], width: 100 },
  {
    key: 'usageScenarioFirst',
    title: '用车场景一级',
    dataKeys: ['usageScenarioFirst'],
    width: 140
  },
  {
    key: 'usageScenarioSecond',
    title: '用车场景二级',
    dataKeys: ['usageScenarioSecond'],
    width: 140
  },
  {
    key: 'domTagFirst',
    title: '标签体系一级',
    dataKeys: ['domTagFirst', 'labelSystemFirst'],
    width: 140
  },
  {
    key: 'domTagSecond',
    title: '标签体系二级',
    dataKeys: ['domTagSecond', 'labelSystemSecond'],
    width: 140
  },
  {
    key: 'domTagThree',
    title: '标签体系三级',
    dataKeys: ['domTagThree', 'labelSystemThird'],
    width: 140
  },
  {
    key: 'domTagFour',
    title: '标签体系四级',
    dataKeys: ['domTagFour', 'labelSystemFourth'],
    width: 160
  },
  { key: 'userJourney1', title: '用户旅程一级', dataKeys: ['userJourney1'], width: 140 },
  { key: 'userJourney2', title: '用户旅程二级', dataKeys: ['userJourney2'], width: 140 },
  { key: 'userJourney3', title: '用户旅程三级', dataKeys: ['userJourney3'], width: 140 },
  { key: 'userJourney4', title: '用户旅程四级', dataKeys: ['userJourney4'], width: 140 },
  { key: 'dataId', title: '原声ID', dataKeys: ['dataId', 'rawDataId', 'originalId'], width: 180 },
  { key: 'title', title: '标题', dataKeys: ['title'], width: 220, twoLine: true },
  {
    key: 'originalText',
    title: '内容',
    dataKeys: ['originalText', 'content'],
    width: 320,
    twoLine: true
  },
  {
    key: 'publishTime',
    title: '发布时间',
    dataKeys: ['publishTime', 'dataCreateTime'],
    width: 180
  },
  {
    key: 'firstContentType',
    title: '内容类型一级',
    dataKeys: ['firstContentType', 'contentTypeFirst', 'contentType'],
    width: 140
  },
  {
    key: 'secondContentType',
    title: '内容类型二级',
    dataKeys: ['secondContentType', 'contentTypeSecond'],
    width: 140
  },
  { key: 'isOuter', title: '一级渠道分类', dataKeys: ['firstChannelName', 'isOuter'], width: 140 },
  { key: 'secondChannelName', title: '二级渠道分类', dataKeys: ['secondChannelName'], width: 140 },
  { key: 'channelName', title: '渠道名称', dataKeys: ['channelName'], width: 140 },
  { key: 'viewCount', title: '浏览数', dataKeys: ['viewCount'], width: 100 },
  { key: 'commentCount', title: '评论数', dataKeys: ['commentCount'], width: 100 },
  { key: 'likeCount', title: '点赞数', dataKeys: ['likeCount'], width: 100 },
  { key: 'shareCount', title: '分享数', dataKeys: ['shareCount'], width: 100 },
  { key: 'favoriteCount', title: '收藏数', dataKeys: ['favoriteCount'], width: 100 },
  { key: 'originalLink', title: '原文链接', dataKeys: ['originalLink'], width: 220 },
  { key: 'authorNick', title: '发声用户昵称', dataKeys: ['authorNick'], width: 140 },
  { key: 'authorId', title: '发声用户ID', dataKeys: ['authorId'], width: 140 },
  { key: 'oneId', title: 'ONE_ID', dataKeys: ['oneId', 'oneID'], width: 140 },
  { key: 'idCarNo', title: '证件号', dataKeys: ['idCarNo', 'certificateNo'], width: 160 },
  { key: 'mobile', title: '手机号', dataKeys: ['mobile', 'phone', 'custMainPhone'], width: 140 },
  { key: 'email', title: '邮箱', dataKeys: ['email'], width: 180 },
  { key: 'authorType', title: '发声用户类型', dataKeys: ['authorType', 'userType'], width: 140 },
  { key: 'isWsaterArmy', title: '是否水军', dataKeys: ['isWsaterArmy', 'isWaterArmy'], width: 100 },
  {
    key: 'mainPostAuthorNick',
    title: '主帖用户昵称',
    dataKeys: ['mainPostAuthorNick'],
    width: 150
  },
  { key: 'mainPostAuthorId', title: '主帖用户ID', dataKeys: ['mainPostAuthorId'], width: 150 },
  { key: 'mainPostId', title: '主帖ID', dataKeys: ['mainPostId'], width: 160 },
  {
    key: 'mainPostContent',
    title: '主帖内容',
    dataKeys: ['mainPostContent'],
    width: 320,
    twoLine: true
  },
  { key: 'vhlId', title: '车辆ID', dataKeys: ['vhlId'], width: 150 },
  { key: 'vhlVin', title: '车辆车架号', dataKeys: ['vhlVin'], width: 180 },
  { key: 'weight', title: '内容权重值', dataKeys: ['weight', 'contentWeight'], width: 120 },
  {
    key: 'dataStatus',
    title: '数据状态',
    dataKeys: ['dataStatus'],
    width: 120,
    fixed: TableV2FixedDir.RIGHT
  }
]

// 表格列配置
const columns: Column[] = [
  {
    key: 'selection',
    width: 55,
    fixed: true,
    cellRenderer: ({ rowData }) => {
      return h(ElCheckbox, {
        modelValue: multipleSelection.value.some(item => item.id === rowData.id),
        'onUpdate:modelValue': (val: any) => {
          if (val) {
            multipleSelection.value = [...multipleSelection.value, rowData]
          } else {
            multipleSelection.value = multipleSelection.value.filter(item => item.id !== rowData.id)
          }
        }
      })
    },
    headerCellRenderer: () => {
      const tableList = table.list || []
      const allSelected =
        tableList.length > 0 && multipleSelection.value.length === tableList.length
      const indeterminate =
        multipleSelection.value.length > 0 && multipleSelection.value.length < tableList.length
      return h(ElCheckbox, {
        modelValue: allSelected,
        indeterminate: indeterminate,
        'onUpdate:modelValue': (val: any) => {
          if (val) {
            multipleSelection.value = [...tableList]
          } else {
            multipleSelection.value = []
          }
        }
      })
    }
  },
  ...resultDataColumnFields.map(createResultDataColumn),
  {
    key: 'operation',
    title: '操作',
    width: 100,
    fixed: TableV2FixedDir.RIGHT,
    align: 'center',
    cellRenderer: ({ rowData }) => {
      return h(
        ElButton,
        {
          type: 'primary',
          link: true,
          onClick: () => customerHandleEdit(rowData)
        },
        () => '纠错'
      )
    }
  }
]
</script>

<template>
  <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
    <FFilterLayout v-model="isExpanded" @query="query" @reset="reset">
      <el-form
        layout="inline"
        :model="table.filter"
        label-width="150px"
        label-position="right"
        class="custom-form"
      >
        <el-row class="w-full" :gutter="0">
          <el-col :span="8">
            <el-form-item label="发布时间">
              <FDatePicker
                v-model="times"
                v-model:shortcutValue="shortcutValue"
                type="daterange"
                :clearable="false"
                :max-selectable-date="maxSelectableDate"
              ></FDatePicker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="声音片段ID">
              <el-input v-model.trim="table.filter.soundsId" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="声音片段">
              <el-input
                v-model.trim="table.filter.originalTextScene"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="车企">
              <el-input v-model.trim="table.filter.seriesFactory" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品牌">
              <el-select-v2
                v-model="table.filter.brandCode"
                placeholder="请选择"
                clearable
                filterable
                multiple
                collapse-tags
                :options="getConditionOptions('brandCar')"
                :props="{ label: 'value', value: 'key' }"
                @change="handleBrandChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="车系">
              <el-select-v2
                v-model="table.filter.carSeries"
                placeholder="请选择"
                clearable
                filterable
                multiple
                collapse-tags
                :options="carSeriesOptions || []"
                :props="{ label: 'value', value: 'key' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="车型">
              <el-input v-model.trim="table.filter.modelName" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原始观点">
              <el-input v-model.trim="table.filter.opinion" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标准观点">
              <SelectV2WithSelectAll
                v-model="standardOpinionSelection"
                placeholder="请选择标准观点"
                clearable
                filterable
                multiple
                :show-select-all="true"
                collapse-tags
                :max-collapse-tags="1"
                :fit-input-width="false"
                :options="topicOptions"
                :props="{ label: 'tagName', value: 'tagCode' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="情感">
              <el-select
                v-model="table.filter.sentiment"
                multiple
                :max-collapse-tags="1"
                collapse-tags
                placeholder="情感"
                clearable
              >
                <el-option
                  v-for="(item, index) in getConditionOptions('vocSentiment')"
                  :key="index"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="意图">
              <el-select
                v-model="table.filter.intention"
                multiple
                :max-collapse-tags="1"
                collapse-tags
                placeholder="意图"
                clearable
              >
                <el-option
                  v-for="(item, index) in getConditionOptions('vocIntention')"
                  :key="index"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用车场景一级">
              <el-input
                v-model.trim="table.filter.usageScenarioFirstKeyword"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用车场景二级">
              <CarSceneFilter
                v-model="carSceneSelection"
                @filter-change="handleCarSceneFilterChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签体系一级">
              <el-cascader
                v-model="experienceCodeSelection"
                :options="experienceCodeOptions"
                :props="experienceCodeCascaderProps"
                collapse-tags
                collapse-tags-tooltip
                clearable
                filterable
                :disabled="experienceCodeLoading"
                :show-all-levels="false"
                :placeholder="experienceCodeLoading ? '标签体系加载中...' : '请选择标签体系'"
                :before-filter="handleExperienceCodeBeforeFilter"
                :filter-method="filterExperienceCodeMethod"
                style="width: 100%"
                @change="handleExperienceCodeChange"
              >
                <template #suggestion-item="{ item }">
                  <span>{{ formatExperienceCodeSuggestionPath(item) }}</span>
                </template>
              </el-cascader>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签体系二级">
              <el-input
                v-model.trim="table.filter.secondCodeTagKeyword"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签体系三级">
              <el-input
                v-model.trim="table.filter.threeCodeTagKeyword"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签体系四级">
              <el-input
                v-model.trim="table.filter.fourCodeTagKeyword"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户旅程一级">
              <el-input v-model.trim="table.filter.userJourney1" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户旅程二级">
              <el-input v-model.trim="table.filter.userJourney2" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户旅程三级">
              <el-input v-model.trim="table.filter.userJourney3" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户旅程四级">
              <el-input v-model.trim="table.filter.userJourney4" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原声ID">
              <el-input v-model.trim="table.filter.originalId" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标题">
              <el-input
                v-model.trim="table.filter.title"
                placeholder="请输入，支持多关键词逗号分隔"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内容">
              <el-input
                v-model.trim="table.filter.content"
                placeholder="请输入，支持多关键词逗号分隔"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内容类型一级">
              <el-select v-model="table.filter.contentType" placeholder="不限" clearable>
                <el-option
                  v-for="item in getConditionOptions('contentType')"
                  :key="item.key"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内容类型二级">
              <el-input
                v-model.trim="table.filter.secondContentType"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="渠道">
              <el-cascader
                v-model="channelSelection"
                placeholder="不限"
                clearable
                filterable
                collapse-tags
                collapse-tags-tooltip
                :options="channelOptions || []"
                :props="channelCascaderProps"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原文链接">
              <el-input v-model.trim="table.filter.originalLink" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发声用户昵称">
              <el-input v-model.trim="table.filter.authorNick" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发声用户ID">
              <el-input v-model.trim="table.filter.authorId" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发声用户类型">
              <el-input v-model.trim="table.filter.authorType" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否水军">
              <el-select-v2
                v-model="table.filter.isWsaterArmy"
                placeholder="不限"
                clearable
                filterable
                :options="getConditionOptions('dropdownFilter')"
                :props="{ label: 'value', value: 'key' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主帖用户昵称">
              <el-input
                v-model.trim="table.filter.mainPostAuthorNick"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主帖用户ID">
              <el-input
                v-model.trim="table.filter.mainPostAuthorId"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主帖ID">
              <el-input v-model.trim="table.filter.mainPostId" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主帖内容">
              <el-input
                v-model.trim="table.filter.mainPostContent"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </FFilterLayout>
  </FtCard>

  <FtCard
    :style="tableFcardHeight"
    title="数据列表"
    class="mt-24"
    model="titleOperation"
    clear-content-top-padding
  >
    <template #extra>
      <div class="result-data-actions">
        <el-button type="primary" :disabled="!multipleSelection.length" @click="handleBatchEc">
          批量纠错
        </el-button>
        <el-button
          v-if="isDownload"
          :disabled="(table.list?.length || 0) === 0 && !multipleSelection.length"
          :loading="exporting"
          type="primary"
          @click="handleExport"
        >
          <template #icon>
            <SvgIcon name="download" style="width: 20px; height: 20px" />
          </template>
          导出数据
        </el-button>
      </div>
    </template>

    <div class="table-container">
      <el-auto-resizer>
        <template #default="slotProps">
          <el-table-v2
            v-loading="table.loading"
            :columns="columns"
            :data="table.list || []"
            :width="slotProps.width"
            :height="slotProps.height"
            :header-height="RESULT_DATA_HEADER_HEIGHT"
            :row-height="RESULT_DATA_ROW_HEIGHT"
            :row-key="'id'"
            fixed
          />
        </template>
      </el-auto-resizer>
    </div>

    <el-pagination
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </FtCard>

  <DownloadDialog v-model="downloadDialogVisible" />
  <ErrorCorrectionDialog
    v-if="form.visible"
    v-model:visible="form.visible"
    :row-data="correctionRows"
    :topic-options="topicOptions"
    :filter="table.filter"
    @success="handleCorrectionSuccess"
  />
</template>

<style scoped lang="scss">
.result-data-actions {
  display: flex;
  gap: 12px;
}

.table-container {
  height: calc(100% - 48px);
}

:deep(.text-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.two-line-ellipsis) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  width: 100%;
  line-height: 20px;
  max-height: 40px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: break-word;
}

:deep(.cell-wrap-text) {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: auto;
}

:deep(.el-table-v2__row) {
  background-color: transparent;
}

:deep(.el-table-v2__row-cell) {
  background-color: #fff;
}

:deep(.el-table-v2__row:hover .el-table-v2__row-cell) {
  background-color: var(--el-fill-color-light);
}
</style>
