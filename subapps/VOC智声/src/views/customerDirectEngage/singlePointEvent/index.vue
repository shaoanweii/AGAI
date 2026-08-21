<script setup lang="ts">
import { useTable } from '@/hooks/useTable'
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import DoubleConfirmatio from '@/components/Business/EventHandle/DoubleConfirmatio.vue'
import CloseRejectEvent from '@/components/Business/EventHandle/CloseRejectEvent.vue'
import AddAssociatedWorkOrder from '@/components/Business/EventHandle/AddAssociatedWorkOrder.vue'
import BatchProcessing from '@/components/Business/EventHandle/BatchProcessing/index.vue'
import EventDetail from '@/components/Business/EventHandle/EventDetail/index.vue'
import SameLevelExperienceCodeCascader from '../shared/components/SameLevelExperienceCodeCascader.vue'
import {
  DoubleConfirmatioTypeEnum,
  CloseRejectEventEnum,
  BatchProcessingTypeEnum,
  EventType
} from '@/components/Business/EventHandle/ehConstants'
import { exportSingleEvent, getList, getTopicsByTagId } from '@/api/singlePointEvent'
import type { SingleEventQueryModel } from '@/api/singlePointEvent/types'
import { getTagLibClientTree } from '@/api/common'
import type { LabelTag } from '@/api/common/index.d'
import { useSingleEventStore } from '@/store/modules/singleEvent'
import { ElMessage, ElCheckbox, ElButton, ElTooltip, TableV2FixedDir } from 'element-plus'
import type { Column, Placement } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import EventPriorityTag from './components/EventPriorityTag.vue'
import { TaskStatusColorMap } from './constants'
import useMiddlewareStore from '@/store/modules/middleware'
import { useUserStore } from '@/store'
import { cleanEmptyParams } from '@/utils'
import { getCustomerDirectEngageDefaultFilter } from '../shared/defaultFilter'
import { useDownloadAction } from '@/hooks/useDownloadAction'
import { FunctionPermission } from '@/constants/btnPermMap'

defineOptions({
  name: 'singlePointEvent'
})

type ExperienceCodeCascaderOption = LabelTag & Record<string, any>

const SINGLE_POINT_DEFAULT_TIME_SHORTCUT = '近7天'
const route = useRoute()
const getSinglePointEventDefaultFilter = () =>
  getCustomerDirectEngageDefaultFilter(
    String(route.name || 'CDESinglePointEvent'),
    SINGLE_POINT_DEFAULT_TIME_SHORTCUT
  )
const roleDefaultFilter = getSinglePointEventDefaultFilter()

const times = ref<any>([...roleDefaultFilter.time.times])
const shortcutValue = ref<any>(roleDefaultFilter.time.shortcutValue)
const isExpanded = ref(false)

const queryTimeRange = computed(() => {
  return {
    startTime: times.value[0] ? `${times.value[0]} 00:00:00` : '',
    endTime: times.value[1] ? `${times.value[1]} 23:59:59` : ''
  }
})

const singleEventStore = useSingleEventStore()
const middlewareStore = useMiddlewareStore()
const userStore = useUserStore()
const tableSortState = ref<any>({})
const { downloading: exportLoading, downloadByRequest: downloadSingleEvent } = useDownloadAction()

/**
 * 判断路由是否显式要求清空品牌，兼容 brandCodes 与 brandCodes= 两种空值形态。
 * @returns 是否使用空品牌覆盖角色默认品牌
 */
const hasExplicitEmptyBrandCodes = () => {
  if (!Object.prototype.hasOwnProperty.call(route.query, 'brandCodes')) {
    return false
  }

  const brandCodes = route.query.brandCodes

  if (Array.isArray(brandCodes)) {
    return brandCodes.every(value => value === '' || value === null)
  }

  return brandCodes === '' || brandCodes === null
}

/**
 * 构建单点事件筛选表单默认值，路由显式传入空品牌时覆盖角色默认品牌。
 * @returns 单点事件筛选默认值
 */
const createSinglePointEventInitialFormData = () => ({
  order: undefined,
  startTime: undefined,
  endTime: undefined,
  brandCodes: hasExplicitEmptyBrandCodes() ? [] : [...roleDefaultFilter.brandCodes],
  channelCodes: [],
  taskStatuses: [],
  subjectCategoryIds: [],
  eventPriorities: [],
  carSeriesCodes: [],
  mainRespOrgId: [],
  eventLevel: [],
  authorName: undefined,
  isProcessed: undefined,
  handler: undefined,
  custName: undefined,
  custPhone: undefined,
  privateMsgProgressCode: undefined,
  privateMsgCount: undefined,
  reviewProgressCode: undefined,
  relationWorkNo: undefined,
  eventName: undefined,
  warningEventNo: undefined,
  sensitiveType: undefined,
  eventClarity: undefined,
  eventValidityList: [],
  titleContentKey: undefined,
  eventAttributeList: [],
  firstCodeTag: [],
  secondCodeTag: [],
  threeCodeTag: [],
  fourCodeTag: [],
  topicList: []
})

/**
 * 从路由 query 中读取单值字符串，兼容 vue-router 的数组类型。
 * @param value 原始 query 值
 * @returns 第一个有效字符串
 */
const getRouteQueryString = (value: unknown): string => {
  if (Array.isArray(value)) {
    return typeof value[0] === 'string' ? value[0] : ''
  }

  return typeof value === 'string' ? value : ''
}

const overviewRouteQuery = {
  startDate: getRouteQueryString(route.query.startDate),
  endDate: getRouteQueryString(route.query.endDate),
  taskStatus: getRouteQueryString(route.query.taskStatus)
}

const {
  formData,
  tableState: { pageSize, currentPage, total, loading, dataList },
  tableMethods,
  selection
} = useTable({
  immediate: true,
  initialFormData: createSinglePointEventInitialFormData() as any,
  fetchDataApi: async () => {
    const queryParams = cleanEmptyParams({
      isMine: middlewareStore.singlePointEventPageType,
      pageSize: pageSize.value,
      pageNum: currentPage.value,
      ...formData.value,
      startTime: queryTimeRange.value.startTime,
      endTime: queryTimeRange.value.endTime
    })

    const res = await getList(queryParams)

    return {
      list: res.result.list,
      total: res.result.total
    } as any
  },
  resetBefore: () => {
    const defaultFilter = getSinglePointEventDefaultFilter()
    shortcutValue.value = defaultFilter.time.shortcutValue
    times.value = [...defaultFilter.time.times]
    tableSortState.value = {}
  }
})

/**
 * 构造单点事件导出查询参数，复用当前筛选条件但不携带分页字段。
 * @returns 导出接口查询参数
 */
const buildSingleEventExportQuery = (): SingleEventQueryModel | undefined => {
  return cleanEmptyParams({
    isMine: middlewareStore.singlePointEventPageType,
    ...formData.value,
    startTime: queryTimeRange.value.startTime,
    endTime: queryTimeRange.value.endTime
  }) as SingleEventQueryModel | undefined
}

/** 按当前筛选条件创建单点事件异步导出任务。 */
const handleExport = async () => {
  await downloadSingleEvent({
    request: params => exportSingleEvent(params as SingleEventQueryModel),
    params: () => buildSingleEventExportQuery() || {},
    exportMenu: '单点事件-事件列表',
    errorMessage: '导出事件失败，请稍后重试'
  })
}

/**
 * 应用从总览页跳转过来的日期和状态筛选，确保首次列表请求携带参数。
 */
const applyOverviewRouteQuery = () => {
  if (overviewRouteQuery.startDate && overviewRouteQuery.endDate) {
    times.value = [overviewRouteQuery.startDate, overviewRouteQuery.endDate]
    shortcutValue.value = '自定义'
  }

  if (overviewRouteQuery.taskStatus) {
    formData.value.taskStatuses = [overviewRouteQuery.taskStatus]
  }
}

applyOverviewRouteQuery()

const dbVisible = ref(false)
const setDbVisible = (val: boolean) => {
  dbVisible.value = val
}

const crVisible = ref(false)
const setCrVisible = (val: boolean) => {
  crVisible.value = val
}

const aawoVisible = ref(false)
const setAawoVisible = (val: boolean) => {
  aawoVisible.value = val
}

const edVisible = ref(false)
const setEdVisible = (val: boolean) => {
  edVisible.value = val
}

const bpVisible = ref(false)
// 批量操作弹窗
const setBpVisible = (val: boolean) => {
  console.log('selection.value', selection.value)

  if (batchType.value !== BatchProcessingTypeEnum.SingleDispatch && selection.value.length === 0) {
    ElMessage.warning('请先选择事件')
    return
  }

  // 当点击批量分派的时候,需要去检验所选中的数据中的。mainRespOrgId字段是否都一致, 不一致则提示: 多个主责部门无法进行批量分派，请重新选择后再进行操作. 然后结束这个函数
  if (
    selection.value.length > 0 &&
    [BatchProcessingTypeEnum.Dispatch, BatchProcessingTypeEnum.Handle].includes(batchType.value)
  ) {
    // 提取所有选中的mainRespOrgId
    const mainRespOrgIds = selection.value.map(item => item.mainRespOrgId).filter(Boolean)

    if (mainRespOrgIds.length === 0) {
      ElMessage.warning('选中的数据中未找到主责部门信息')
      return
    }

    // 检查是否所有mainRespOrgId都一致
    const uniqueMainRespOrgIds = [...new Set(mainRespOrgIds)]
    if (uniqueMainRespOrgIds.length > 1) {
      const btnStr: any = {
        [BatchProcessingTypeEnum.Dispatch]: '批量分派',
        [BatchProcessingTypeEnum.Handle]: '批量确认'
      }
      ElMessage.warning(`多个主责部门无法进行${btnStr[batchType.value]}，请重新选择后再进行操作`)
      return
    }
  }

  bpVisible.value = val
}

const batchType = ref<BatchProcessingTypeEnum>(BatchProcessingTypeEnum.Pass)
// 批量操作指令
const handleBatchCommand = (command: BatchProcessingTypeEnum) => {
  console.log('command', command)
  batchType.value = command
  setBpVisible(true)
}

// 监听数据列表变化，自动清空选择
watch(
  () => dataList.value,
  () => {
    selection.value = []
  },
  { flush: 'post' }
)

// 监听页面类型变化，自动刷新列表
watch(
  () => middlewareStore.singlePointEventPageType,
  () => {
    tableMethods.handleQuery()
  }
)

// 品牌选项直接取登录态中的品牌树，保持与其他筛选页一致的数据源
const brandOptions = computed(() => {
  return userStore.getBrandService || []
})

/**
 * 将筛选值统一转换为可比较的字符串数组，兼容空值、数字和历史缓存值。
 */
const normalizeSelectedValues = (values: unknown): string[] => {
  if (Array.isArray(values)) {
    return values
      .filter(value => value !== '' && value !== null && value !== undefined)
      .map(value => String(value))
  }
  if (values === '' || values === null || values === undefined) {
    return []
  }
  return [String(values)]
}

/**
 * 品牌数据在不同页面里可能同时存在 key/value/brandCode 三套字段，
 * 联动时统一兼容它们，避免多选改造后因字段不一致拿不到车系。
 */
const isMatchedBrandValue = (brand: any, selectedBrandValue: string) => {
  const brandValueCandidates = [brand?.key, brand?.value, brand?.brandCode]
    .filter(value => value !== '' && value !== null && value !== undefined)
    .map(value => String(value))

  return brandValueCandidates.includes(selectedBrandValue)
}

/**
 * 车系也兼容多套值字段，既用于去重，也用于回收失效选中项。
 */
const getSeriesOptionValue = (series: any) => {
  // const value = series?.key ?? series?.value ?? series?.carSeriesCode
  const value = series?.key
  return value === '' || value === null || value === undefined ? '' : String(value)
}

/**
 * 根据已选品牌聚合车系选项。
 * 这里做去重是为了兼容多个品牌下出现同编码车系的场景，避免下拉中出现重复项。
 */
const buildCarSeriesOptionsByBrandCodes = (brandCodes: string[] = []) => {
  const selectedBrandValues = normalizeSelectedValues(brandCodes)
  if (!selectedBrandValues.length) return []

  const seriesMap = new Map<string, any>()

  brandOptions.value.forEach(brand => {
    const isSelectedBrand = selectedBrandValues.some(selectedBrandValue =>
      isMatchedBrandValue(brand, selectedBrandValue)
    )
    if (!isSelectedBrand) {
      return
    }

    brand?.children?.forEach((series: any) => {
      const seriesValue = getSeriesOptionValue(series)
      if (seriesValue && !seriesMap.has(seriesValue)) {
        seriesMap.set(seriesValue, series)
      }
    })
  })

  return Array.from(seriesMap.values())
}

const carSeriesOptions = computed(() => {
  return buildCarSeriesOptionsByBrandCodes(formData.value.brandCodes || [])
})

/**
 * 当品牌变化后，仅保留当前品牌集合下仍然合法的车系编码。
 * 这样可以避免多品牌切换后把失效车系继续带给后端。
 */
const syncCarSeriesCodesByBrandCodes = (brandCodes: string[] = []) => {
  const validCarSeriesCodeSet = new Set(
    buildCarSeriesOptionsByBrandCodes(brandCodes)
      .map((item: any) => getSeriesOptionValue(item))
      .filter(Boolean)
  )
  const selectedCarSeriesCodes = normalizeSelectedValues(formData.value.carSeriesCodes || [])

  formData.value.carSeriesCodes = selectedCarSeriesCodes.filter(code =>
    validCarSeriesCodeSet.has(code)
  )
}

watch(
  () => formData.value.brandCodes,
  brandCodes => {
    syncCarSeriesCodesByBrandCodes(brandCodes || [])
  },
  { deep: true }
)

watch(
  carSeriesOptions,
  () => {
    syncCarSeriesCodesByBrandCodes(formData.value.brandCodes || [])
  },
  { deep: true }
)

const sortableColumnOrderFieldMap: Record<string, string> = {
  updateTime: 'update_time',
  warningTime: 'warning_time'
}

const sortableColumnKeySet = new Set(Object.keys(sortableColumnOrderFieldMap))

/**
 * 对齐普通 el-table 的三态排序：
 * 空状态 -> 升序 -> 降序 -> 空状态。
 * 空状态下不向后端传 order 字段。
 */
const getNextSortOrder = (currentOrder?: string) => {
  if (currentOrder === 'asc') return 'desc'
  if (currentOrder === 'desc') return undefined
  return 'asc'
}

const handleColumnSort = ({ key }: { key: string | number | symbol; order?: string }) => {
  const sortKey = String(key)
  if (!sortableColumnKeySet.has(sortKey)) return

  const orderField = sortableColumnOrderFieldMap[sortKey]
  if (!orderField) return

  const currentOrder = tableSortState.value?.[sortKey]
  const nextSortOrder = getNextSortOrder(currentOrder)

  if (!nextSortOrder) {
    tableSortState.value = {}
    formData.value.order = undefined
  } else {
    tableSortState.value = { [sortKey]: nextSortOrder }
    formData.value.order = `${orderField} ${nextSortOrder}`
  }

  if (currentPage.value === 1) {
    tableMethods.getList()
  } else {
    currentPage.value = 1
  }
}

/**
 * 将 TableV2 的 asc/desc 状态映射为普通 el-table 的表头类名，
 * 仅用于排序箭头的视觉表现，不影响实际排序参数。
 */
const getDefaultTableSortClass = (sortKey: string) => {
  const order = tableSortState.value?.[sortKey]
  if (order === 'asc') return 'ascending'
  if (order === 'desc') return 'descending'
  return ''
}

/**
 * 复用普通 el-table 的 caret DOM 结构，替换 TableV2 默认排序图标。
 * 点击行为仍由 TableV2 外层表头容器处理，这里只负责渲染视觉。
 */
const renderDefaultTableSortHeader = (title: string, sortKey: string) => {
  return h('div', { class: ['single-point-sort-header', getDefaultTableSortClass(sortKey)] }, [
    h('span', { class: 'single-point-sort-header__text' }, title),
    h(
      'button',
      {
        type: 'button',
        class: 'caret-wrapper',
        tabindex: -1,
        'aria-hidden': 'true'
      },
      [h('i', { class: 'sort-caret ascending' }), h('i', { class: 'sort-caret descending' })]
    )
  ])
}

const singlePointEventTooltipFallbackPlacements: Placement[] = [
  'top-start',
  'top-end',
  'bottom-start',
  'bottom-end',
  'right',
  'left'
]

const singlePointEventTooltipPopperOptions = {
  modifiers: [
    {
      name: 'offset',
      options: {
        offset: [0, 8]
      }
    },
    {
      name: 'preventOverflow',
      options: {
        padding: 12,
        altAxis: true,
        tether: true
      }
    }
  ]
}

/**
 * 为标题、原始声音等长文本列统一提供省略展示和浅色 Tooltip。
 * TableV2 不支持直接复用普通 el-table 的 show-overflow-tooltip，这里用 cellRenderer 做兼容。
 * @param cellData 当前单元格文本
 * @returns 带省略和 Tooltip 的单元格节点
 */
const renderTextTooltipCell = (cellData: unknown) => {
  const content = cellData == null ? '' : String(cellData)

  return h(
    ElTooltip,
    {
      placement: 'top',
      disabled: !content,
      fallbackPlacements: singlePointEventTooltipFallbackPlacements,
      popperClass: 'single-point-event__text-tooltip text-tooltip-light',
      popperOptions: singlePointEventTooltipPopperOptions
    },
    {
      content: () => h('div', { class: 'single-point-event__text-tooltip-content' }, content),
      default: () => h('div', { class: 'single-point-event__text-ellipsis' }, content)
    }
  )
}

const FCardHeight = computed(() => {
  return isExpanded.value ? `calc(100vh - 84px - 252px - 20px)` : `calc(100vh - 34px - 252px)`
})
// 批量弹窗确认
const batchHandleConfirm = () => {
  selection.value = []
  tableMethods.getList()
}

// Table V2 columns configuration
const columns: Column[] = [
  {
    key: 'selection',
    width: 55,
    fixed: true,
    cellRenderer: ({ rowData }) => {
      return h(ElCheckbox, {
        modelValue: selection.value.some(item => item.id === rowData.id),
        'onUpdate:modelValue': (val: any) => {
          if (val) {
            selection.value = [...selection.value, rowData]
          } else {
            selection.value = selection.value.filter(item => item.id !== rowData.id)
          }
        }
      })
    },
    headerCellRenderer: () => {
      const allSelected =
        dataList.value.length > 0 && selection.value.length === dataList.value.length
      const indeterminate =
        selection.value.length > 0 && selection.value.length < dataList.value.length
      return h(ElCheckbox, {
        modelValue: allSelected,
        indeterminate: indeterminate,
        'onUpdate:modelValue': (val: any) => {
          if (val) {
            selection.value = [...dataList.value]
          } else {
            selection.value = []
          }
        }
      })
    }
  },
  {
    key: 'warningEventNo',
    title: '事件编号',
    dataKey: 'warningEventNo',
    width: 180,
    fixed: true
  },
  {
    key: 'subjectCategoryName',
    title: '主题分类',
    dataKey: 'subjectCategoryName',
    width: 180
  },
  {
    key: 'channelName',
    title: '数据来源',
    dataKey: 'channelName',
    width: 180
  },
  {
    key: 'eventPriority',
    title: '处理优先级',
    dataKey: 'eventPriority',
    width: 180,
    cellRenderer: ({ rowData }) => {
      return h(EventPriorityTag, {
        tagName: rowData.eventPriorityName?.toLocaleUpperCase(),
        type: rowData.eventPriority
      })
    }
  },
  {
    key: 'title',
    title: '标题',
    dataKey: 'title',
    width: 220,
    cellRenderer: ({ cellData }) => {
      return renderTextTooltipCell(cellData)
    }
  },
  {
    key: 'content',
    title: '原始声音',
    dataKey: 'content',
    width: 220,
    cellRenderer: ({ cellData }) => {
      return renderTextTooltipCell(cellData)
    }
  },
  {
    key: 'originalTextScene',
    title: '声音片段',
    dataKey: 'originalTextScene',
    width: 220
  },
  {
    key: 'topicText',
    title: '标准观点',
    dataKey: 'topicText',
    width: 220
  },
  {
    key: 'brandName',
    title: '品牌',
    dataKey: 'brandName',
    width: 180
  },
  {
    key: 'carSeriesName',
    title: '车系',
    dataKey: 'carSeriesName',
    width: 180
  },
  {
    key: 'authorName',
    title: '发声用户',
    dataKey: 'authorName',
    width: 180
  },
  {
    key: 'mainRespOrgName',
    title: '主责部门',
    dataKey: 'mainRespOrgName',
    width: 180
  },
  {
    key: 'isProcessedName',
    title: '是否处理',
    dataKey: 'isProcessedName',
    width: 180
  },
  {
    key: 'handler',
    title: '处理人员',
    dataKey: 'handler',
    width: 180,
    cellRenderer: ({ rowData }) => {
      return rowData.handler?.userName || ''
    }
  },
  {
    key: 'reviewProgressName',
    title: '回评进度',
    dataKey: 'reviewProgressName',
    width: 180
  },
  {
    key: 'privateMsgProgressName',
    title: '私信进度',
    dataKey: 'privateMsgProgressName',
    width: 180
  },
  {
    key: 'privateMsgCount',
    title: '私信次数',
    dataKey: 'privateMsgCount',
    width: 180
  },
  {
    key: 'updateTime',
    title: '事件更新时间',
    dataKey: 'updateTime',
    width: 180,
    sortable: true,
    headerClass: 'single-point-sortable-header',
    headerCellRenderer: () => renderDefaultTableSortHeader('事件更新时间', 'updateTime')
  },
  {
    key: 'warningTime',
    title: '事件预警时间',
    dataKey: 'warningTime',
    width: 180,
    sortable: true,
    headerClass: 'single-point-sortable-header',
    headerCellRenderer: () => renderDefaultTableSortHeader('事件预警时间', 'warningTime')
  },
  {
    key: 'eventAttribute',
    title: '事件属性',
    dataKey: 'eventAttribute',
    width: 180
  },
  {
    key: 'eventValidityName',
    title: '事件有效性',
    dataKey: 'eventValidityName',
    width: 180
  },
  {
    key: 'taskStatus',
    title: '事件状态',
    dataKey: 'taskStatus',
    width: 100,
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ rowData }) => {
      return h('div', { style: 'display: flex; align-items: center; justify-content: center' }, [
        h('span', {
          style: {
            width: '6px',
            height: '6px',
            borderRadius: '50%',
            backgroundColor: TaskStatusColorMap[rowData.taskStatus] || TaskStatusColorMap.default,
            marginRight: '8px'
          }
        }),
        h('span', rowData.taskStatusName)
      ])
    }
  },
  {
    key: 'operation',
    title: '操作',
    width: 180,
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ rowData }) => {
      const buttons = []

      if (rowData.permissions?.includes(EventType.VIEW)) {
        buttons.push(
          h(
            ElButton,
            {
              type: 'primary',
              link: true,
              onClick: () => handleEvent(rowData, EventType.VIEW)
            },
            () => '查看'
          )
        )
      }

      if (rowData.permissions?.includes(EventType.CONFIRM)) {
        buttons.push(
          h(
            ElButton,
            {
              type: 'primary',
              link: true,
              onClick: () => handleEvent(rowData, EventType.CONFIRM)
            },
            () => '确认'
          )
        )
      }

      if (rowData.permissions?.includes(EventType.ASSIGN)) {
        buttons.push(
          h(
            ElButton,
            {
              type: 'primary',
              link: true,
              onClick: () => handleEvent(rowData, EventType.ASSIGN)
            },
            () => '分派'
          )
        )
      }

      if (rowData.permissions?.includes(EventType.HANDLE)) {
        buttons.push(
          h(
            ElButton,
            {
              type: 'primary',
              link: true,
              onClick: () => handleEvent(rowData, EventType.HANDLE)
            },
            () => '处理'
          )
        )
      }

      if (rowData.permissions?.includes(EventType.APPROVE)) {
        buttons.push(
          h(
            ElButton,
            {
              type: 'primary',
              link: true,
              onClick: () => handleEvent(rowData, EventType.APPROVE)
            },
            () => '审核'
          )
        )
      }

      return h('div', { style: 'display: flex; gap: 8px; justify-content: center' }, buttons)
    }
  }
]

// 事件详情确认
const handleEventConfirm = () => {
  tableMethods.getList()
}
// 刷新列表
const handleEventRefresh = () => {
  console.log('handleEventRefresh--->刷新列表')
  tableMethods.getList()
}

const curRow = ref()
const eventType = ref<EventType>(EventType.VIEW)

// 处理事件, 查看， 审核， 确认， 分派， 处理， 关闭
const handleEvent = (row: any, type: EventType) => {
  // 处理分派
  if (type === EventType.ASSIGN) {
    batchType.value = BatchProcessingTypeEnum.SingleDispatch
    selection.value = [row]
    setBpVisible(true)
  } else {
    curRow.value = row
    eventType.value = type
    setEdVisible(true)
  }
}

// 标准观点选项
const topicOptions = ref<any[]>([])

const fallbackExperienceCodeOptions = ref<ExperienceCodeCascaderOption[]>([])
const experienceCodeTreeLoading = ref(false)

const experienceCodeOptions = computed<ExperienceCodeCascaderOption[]>(() => {
  const storeOptions = Array.isArray(singleEventStore.tagTreeList)
    ? (singleEventStore.tagTreeList as ExperienceCodeCascaderOption[])
    : []
  return storeOptions.length > 0 ? storeOptions : fallbackExperienceCodeOptions.value
})

// 确定体验代码末级（四级 > 三级 > 二级 > 一级）
const getLastLevelCodes = (): string[] => {
  if (formData.value.fourCodeTag && formData.value.fourCodeTag.length > 0) {
    return formData.value.fourCodeTag
  }
  if (formData.value.threeCodeTag && formData.value.threeCodeTag.length > 0) {
    return formData.value.threeCodeTag
  }
  if (formData.value.secondCodeTag && formData.value.secondCodeTag.length > 0) {
    return formData.value.secondCodeTag
  }
  if (formData.value.firstCodeTag && formData.value.firstCodeTag.length > 0) {
    return formData.value.firstCodeTag
  }
  return []
}

/**
 * 加载单点事件体验代码树；优先使用 conditions 已有树，缺失时兜底请求 CA 标签树。
 */
const loadExperienceCodeOptions = async () => {
  try {
    experienceCodeTreeLoading.value = true
    await singleEventStore.getConditions()

    if (experienceCodeOptions.value.length > 0) {
      return
    }

    const res = await getTagLibClientTree({ tagLibType: 'CA' })
    fallbackExperienceCodeOptions.value = Array.isArray(res.result)
      ? (res.result as ExperienceCodeCascaderOption[])
      : []
  } catch (error) {
    console.error('获取体验代码失败:', error)
    fallbackExperienceCodeOptions.value = []
  } finally {
    experienceCodeTreeLoading.value = false
  }
}

// 根据末级体验代码获取标准观点
const fetchTopicsByLastLevel = async (codes?: string[]) => {
  const lastLevelCodes = Array.isArray(codes) ? codes : getLastLevelCodes()
  // 如果没有选择体验代码，默认查询所有标准观点
  const codesToFetch = lastLevelCodes.length === 0 ? [] : lastLevelCodes

  try {
    const res = await getTopicsByTagId(codesToFetch)
    if (res.success && res.result && Array.isArray(res.result)) {
      // 去重处理，避免重复的观点
      // const seenTopics = new Set<string>()
      // const allTopics: any[] = []
      // res.result.forEach((topic: any) => {
      //   const topicCode = topic.tagCode || topic.code
      //   if (topicCode && !seenTopics.has(topicCode)) {
      //     seenTopics.add(topicCode)
      //     allTopics.push(topic)
      //   }
      // })
      topicOptions.value = res.result
    } else {
      topicOptions.value = []
    }
  } catch (error) {
    console.error('获取标准观点失败:', error)
    topicOptions.value = []
  }
}

// 监听体验代码变化，自动获取标准观点
watch(
  [
    () => formData.value.fourCodeTag,
    () => formData.value.threeCodeTag,
    () => formData.value.secondCodeTag,
    () => formData.value.firstCodeTag
  ],
  () => {
    formData.value.topicList = []
    topicOptions.value = []
    // 当体验代码变化时，重新获取标准观点；如果没有体验代码，则获取所有标准观点。
    fetchTopicsByLastLevel()
  },
  { deep: true, immediate: true }
)

// 批量操作按钮禁用逻辑
const operationDisabled = computed(() => {
  // console.log('selection.value', selection.value)

  if (selection.value.length === 0) return true

  // 检查选中项的taskStatus是否相同
  const taskStatuses = selection.value.map(item => item.taskStatus)
  const uniqueTaskStatuses = [...new Set(taskStatuses)]

  // 如果taskStatus不同或包含"90"状态，则禁用操作
  return uniqueTaskStatuses.length > 1 || taskStatuses.includes('90')
})

// 页面初始化时预加载部门数据
singleEventStore.getDepartTree()
singleEventStore.getDepartAccountTree()

onMounted(() => {
  loadExperienceCodeOptions()
})
</script>

<template>
  <div>
    <FCard :title="'筛选条件'" titleSize="middle" :is-show-more="false" :height="'auto'">
      <FFilterLayout
        v-model="isExpanded"
        @query="tableMethods.handleQuery"
        @reset="tableMethods.handleReset"
      >
        <el-form layout="inline" :model="formData" label-position="right">
          <el-row class="w-full" :gutter="24">
            <el-col :span="8">
              <el-form-item label="预警时间">
                <FDatePicker
                  v-model="times"
                  v-model:shortcutValue="shortcutValue"
                  type="daterange"
                  :clearable="false"
                  class="iround-4"
                  size="default"
                ></FDatePicker>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="品牌">
                <el-select
                  v-model="formData.brandCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="brandOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="车系">
                <el-select
                  v-model="formData.carSeriesCodes"
                  placeholder="全部"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="carSeriesOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="数据来源">
                <el-cascader
                  v-model="formData.channelCodes"
                  :options="singleEventStore.dataChannel"
                  :max-collapse-tags="1"
                  collapse-tags
                  :show-all-levels="false"
                  show-checked-strategy="parent"
                  filterable
                  clearable
                  :props="{
                    label: 'name',
                    value: 'code',
                    children: 'child',
                    multiple: true,
                    emitPath: false
                  }"
                  class="w-full"
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="事件状态">
                <el-select
                  v-model="formData.taskStatuses"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_staus"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="主题分类">
                <el-select
                  v-model="formData.subjectCategoryIds"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closedLoopCategory"
                  :props="{ label: 'name', value: 'id' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="处理优先级">
                <el-select
                  v-model="formData.eventPriorities"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closed_rule_priority"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="发声用户">
                <el-input
                  v-model.trim="formData.authorName"
                  clearable
                  placeholder="请输入"
                  maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="主责部门">
                <el-select
                  v-model="formData.mainRespOrgId"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.mainRespOrgs"
                  :props="{ label: 'mainRespOrgName', value: 'mainRespOrgId' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="是否处理">
                <el-select
                  v-model="formData.isProcessed"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_is_handled"
                  :props="{ label: 'text', value: 'value' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="处理人员">
                <el-input
                  v-model.trim="formData.handler"
                  clearable
                  placeholder="请输入"
                  :maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="客户姓名">
                <el-input
                  v-model.trim="formData.custName"
                  clearable
                  placeholder="请输入"
                  :maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="手机号">
                <el-input
                  v-model.trim="formData.custPhone"
                  clearable
                  placeholder="请输入"
                  :maxlength="20"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="私信进度">
                <el-select
                  v-model="formData.privateMsgProgressCode"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_private_mst_staus"
                  :props="{ label: 'text', value: 'value' }"
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="私信次数">
                <el-select
                  v-model="formData.privateMsgCount"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_private_mst_count"
                  :props="{ label: 'text', value: 'value' }"
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="回评进度">
                <el-select
                  v-model="formData.reviewProgressCode"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_review_staus"
                  :props="{ label: 'text', value: 'value' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="关联工单号">
                <el-input
                  v-model.trim="formData.relationWorkNo"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="事件信息">
                <el-input
                  v-model.trim="formData.eventName"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="事件编号">
                <el-input
                  v-model.trim="formData.warningEventNo"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="事件等级">
                <el-select
                  v-model="formData.eventLevel"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.closed_rule_level"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="敏感类型">
                <el-select
                  v-model="formData.sensitiveType"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.sensitiveTypes"
                  :props="{ label: 'name', value: 'code' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="事件清晰度">
                <el-select
                  v-model="formData.eventClarity"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.eventClears"
                  :props="{ label: 'name', value: 'code' }"
                />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="事件有效性">
                <el-select
                  v-model="formData.eventValidityList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.task_event_validity"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="体验代码">
                <SameLevelExperienceCodeCascader
                  v-model:first-code-tag="formData.firstCodeTag"
                  v-model:second-code-tag="formData.secondCodeTag"
                  v-model:three-code-tag="formData.threeCodeTag"
                  v-model:four-code-tag="formData.fourCodeTag"
                  :options="experienceCodeOptions"
                  :loading="experienceCodeTreeLoading"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="标准观点">
                <el-select-v2
                  v-model="formData.topicList"
                  placeholder="请选择"
                  clearable
                  filterable
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                  :options="topicOptions"
                  :props="{ label: 'tagName', value: 'tagName' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="事件属性">
                <el-select
                  v-model="formData.eventAttributeList"
                  placeholder="不限"
                  clearable
                  filterable
                  :options="singleEventStore.event_attribute"
                  :props="{ label: 'text', value: 'value' }"
                  multiple
                  :max-collapse-tags="1"
                  collapse-tags
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="标题/原始声音">
                <el-input
                  v-model.trim="formData.titleContentKey"
                  clearable
                  placeholder="请输入"
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </FFilterLayout>
    </FCard>

    <FCard
      :title="'事件列表'"
      titleSize="middle"
      :is-show-more="false"
      :height="FCardHeight"
      class="mt-24"
    >
      <template #more>
        <div class="table-actions">
          <el-button
            v-if="userStore.checkfunctionPermission(FunctionPermission.SINGLE_POINT_EVENT_DOWNLOAD)"
            :icon="Download"
            color="#1677FF"
            :loading="exportLoading"
            :disabled="exportLoading"
            @click="handleExport"
          >
            导出事件
          </el-button>
          <el-dropdown @command="handleBatchCommand" :disabled="operationDisabled">
            <el-button type="primary" :disabled="operationDisabled">
              批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="BatchProcessingTypeEnum.Pass"
                  >批量审核</el-dropdown-item
                >
                <el-dropdown-item :command="BatchProcessingTypeEnum.Handle"
                  >批量确认</el-dropdown-item
                >
                <el-dropdown-item :command="BatchProcessingTypeEnum.Dispatch"
                  >批量分派</el-dropdown-item
                >
                <el-dropdown-item :command="BatchProcessingTypeEnum.Close"
                  >批量关闭</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <!-- <el-button type="primary" @click="handlePass({})"> 查看</el-button> -->
        <!-- <el-button type="primary" @click="() => setBpVisible(true)"> 批量操作</el-button> -->
        <!-- <el-button type="primary" @click="() => setDbVisible(true)"> 确认</el-button> -->
        <!-- <el-button type="primary" @click="() => setCrVisible(true)"> 关闭事件</el-button> -->
        <!-- <el-button type="primary" @click="() => setAawoVisible(true)"> 添加关联事件</el-button> -->
      </template>
      <!-- 使用div容器包装，高度从FCard动态计算 -->
      <div class="table-container">
        <el-auto-resizer>
          <template #default="slotProps">
            <el-table-v2
              :data-testid="`dataSource-result-table`"
              v-loading="loading"
              :columns="columns"
              :data="dataList"
              :sort-state="tableSortState"
              :on-column-sort="handleColumnSort"
              :width="slotProps.width"
              :height="slotProps.height"
              :row-key="'id'"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </FCard>

    <!-- 二次确认弹窗 -->
    <DoubleConfirmatio
      v-model="dbVisible"
      :type="DoubleConfirmatioTypeEnum.Pass"
    ></DoubleConfirmatio>

    <!-- 关闭、驳回事件 -->
    <CloseRejectEvent v-model="crVisible" :type="CloseRejectEventEnum.Reject"></CloseRejectEvent>
    <!-- 批量操作 -->
    <BatchProcessing
      v-model="bpVisible"
      :type="batchType!"
      :selection="selection"
      @confirm="batchHandleConfirm"
    ></BatchProcessing>
    <!-- 添加关联工单 -->
    <AddAssociatedWorkOrder v-model="aawoVisible"></AddAssociatedWorkOrder>
    <!-- 事件详情 -->
    <EventDetail
      v-model="edVisible"
      :row="curRow"
      :eventType="eventType"
      :start-time="queryTimeRange.startTime"
      :end-time="queryTimeRange.endTime"
      @confirm="handleEventConfirm"
      @refresh="handleEventRefresh"
    ></EventDetail>
  </div>
</template>

<style lang="scss" scoped>
.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-container {
  height: calc(100% - 48px); /* 减去分页组件的高度48px */
}

:deep(.single-point-sortable-header .el-table-v2__sort-icon) {
  display: none;
}

:deep(.single-point-sort-header) {
  width: 100%;
  display: inline-flex;
  align-items: center;
  line-height: 23px;
}

:deep(.single-point-sort-header__text) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.single-point-sort-header .caret-wrapper) {
  cursor: pointer;
  appearance: none;
  border-radius: var(--el-border-radius-base);
  transition: transform var(--el-transition-duration-fast) ease-in-out;
  background-color: transparent;
  border: none;
  outline: none;
  margin: 0;
  padding: 0;
  vertical-align: middle;
  width: 24px;
  height: 14px;
  overflow: initial;
  flex-direction: column;
  align-items: center;
  display: inline-flex;
  position: relative;
  flex-shrink: 0;
  pointer-events: none;
}

:deep(.single-point-sort-header .sort-caret) {
  border: 5px solid transparent;
  width: 0;
  height: 0;
  position: absolute;
  left: 7px;
  pointer-events: none;
}

:deep(.single-point-sort-header .sort-caret.ascending) {
  border-bottom-color: var(--el-text-color-placeholder);
  top: -5px;
}

:deep(.single-point-sort-header .sort-caret.descending) {
  border-top-color: var(--el-text-color-placeholder);
  bottom: -3px;
}

:deep(.single-point-sort-header.ascending .sort-caret.ascending) {
  border-bottom-color: var(--el-color-primary);
}

:deep(.single-point-sort-header.descending .sort-caret.descending) {
  border-top-color: var(--el-color-primary);
}

:deep(.single-point-event__text-ellipsis) {
  width: 100%;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.single-point-event__text-tooltip) {
  max-width: min(520px, calc(100vw - 32px));
}

:deep(.single-point-event__text-tooltip-content) {
  max-height: min(320px, calc(100vh - 48px));
  overflow: auto;
  box-sizing: border-box;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  line-height: 20px;
}
</style>
