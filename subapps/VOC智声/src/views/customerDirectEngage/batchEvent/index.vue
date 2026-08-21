<script setup lang="ts">
import { computed, h, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElButton, ElCheckbox, ElMessage, ElTooltip, TableV2FixedDir } from 'element-plus'
import type { Column, Placement } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { useTable } from '@/hooks/useTable'
import BatchEventDetail from '@/components/Business/EventHandle/BatchEventDetail/index.vue'
import CustomerDirectEngageFilterForm from './components/CustomerDirectEngageFilterForm.vue'
import BatchEventBatchAuditDialog from './components/BatchEventBatchAuditDialog.vue'
import BatchEventBatchResponseDialog from './components/BatchEventBatchResponseDialog.vue'
import BatchEventBatchCloseDialog from './components/BatchEventBatchCloseDialog.vue'
import { EventType } from '@/components/Business/EventHandle/ehConstants'
import {
  batchApproveBatchEvent,
  batchCloseBatchEvent,
  batchConfirmBatchEvent,
  batchRejectBatchEvent,
  exportBatchEvent,
  exportBatchEventDetail,
  exportBatchEventRaw,
  getBatchEventList
} from '@/api/batchEvent'
import type {
  BatchEventDataSourceType,
  BatchEventQueryModel,
  BatchRiskEventPageVo
} from '@/api/batchEvent/types'
import useMiddlewareStore from '@/store/modules/middleware'
import useUserStore from '@/store/modules/user'
import EventPriorityTag from '../singlePointEvent/components/EventPriorityTag.vue'
import { TaskStatusColorMap } from '../singlePointEvent/constants'
import { fmtFix, fmtNum } from '@/utils/index'
import { cleanEmptyParams } from '@/utils'
import { useLoading } from '@/hooks/useLoading'
import { useDownloadAction } from '@/hooks/useDownloadAction'
import { FunctionPermission } from '@/constants/btnPermMap'
import { useCustomerDirectEngageFilter } from './hooks/useCustomerDirectEngageFilter'
import {
  BatchEventActionTypeEnum,
  BatchEventAuditModeEnum,
  BatchEventResponseHandleModeEnum,
  BatchEventResponseModeEnum,
  createCustomerDirectEngageFilterFormData,
  type BatchEventBatchActionPayload
} from './types'
import { useBatchEventOptions } from './hooks/useBatchEventOptions'
import { getCustomerDirectEngageDefaultFilter } from '../shared/defaultFilter'
import { getAccountDepartmentNamesByUserId } from '@/components/Business/EventHandle/BatchEventDetail/components/ProcessingProgress/personnelTree'
import { normalizeBatchEventDetailType } from '@/components/Business/EventHandle/BatchEventDetail/share'
import { formatBatchEventMainRespUser } from '@/components/Business/EventHandle/BatchEventDetail/utils'

defineOptions({
  name: 'CDEBatchEvent'
})

type BatchEventRow = BatchRiskEventPageVo & Record<string, unknown>
type BatchEventOperationButton = {
  label: string
  type: EventType
}

const batchOperationOptions = [
  { label: '批量审核', value: BatchEventActionTypeEnum.Audit },
  { label: '批量响应', value: BatchEventActionTypeEnum.Response },
  { label: '批量关闭', value: BatchEventActionTypeEnum.Close }
]

const batchEventOperationButtons: BatchEventOperationButton[] = [
  { label: '查看', type: EventType.VIEW },
  { label: '审核', type: EventType.APPROVE },
  { label: '确认', type: EventType.CONFIRM },
  { label: '处理', type: EventType.HANDLE }
]

const emptyCellValue = '-'

const batchType = ref<BatchEventActionTypeEnum>(BatchEventActionTypeEnum.Audit)
const curRow = ref<BatchEventRow>()
const detailEventType = ref<EventType>(EventType.VIEW)
const batchDetailVisible = ref(false)
const bpVisible = ref(false)

const batchEventOptions = useBatchEventOptions()
const middlewareStore = useMiddlewareStore()
const userStore = useUserStore()
const route = useRoute()
const BATCH_EVENT_DEFAULT_TIME_SHORTCUT = '近30天'
const getBatchEventDefaultFilter = () =>
  getCustomerDirectEngageDefaultFilter(
    String(route.name || 'CDEBatchEvent'),
    BATCH_EVENT_DEFAULT_TIME_SHORTCUT
  )
const roleDefaultFilter = getBatchEventDefaultFilter()
const { showLoading, hideLoading } = useLoading()
const { downloading: exportLoading, downloadByRequest: downloadBatchEvent } = useDownloadAction()
const { downloading: voiceExportLoading, downloadByRequest: downloadBatchEventDetail } =
  useDownloadAction()

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
 * 构建批量事件筛选表单默认值，路由显式传入空品牌时覆盖角色默认品牌。
 * @returns 批量事件筛选默认值
 */
const createBatchEventInitialFormData = () => ({
  ...createCustomerDirectEngageFilterFormData(),
  brandCodes: hasExplicitEmptyBrandCodes() ? [] : [...roleDefaultFilter.brandCodes]
})

/**
 * 获取当前页面完整 URL，供执剑者系统回跳使用。
 * @returns 当前浏览器页面 URL
 */
const getCurrentPageUrl = () => window.location.href

/**
 * 根据处理方式决定是否需要向后端传入页面 URL。
 * @param handleMode 批量事件确认处理方式
 * @returns 执剑者闭环时返回当前页面 URL，否则不传
 */
const getPageUrlByHandleMode = (handleMode?: string) => {
  return handleMode === BatchEventResponseHandleModeEnum.Sword ? getCurrentPageUrl() : undefined
}

/**
 * 按已选 value 提取批量事件条件名称并用英文逗号拼接。
 * @param options 条件选项
 * @param values 已选编码
 * @returns 后端需要的逗号分隔名称字符串
 */
const joinSelectedOptionNames = (options: any[] = [], values?: string[] | string) => {
  const selectedValues = new Set(
    (Array.isArray(values) ? values : values ? [values] : []).map(value => String(value))
  )
  if (selectedValues.size === 0) return undefined

  const selectedNames = options
    .filter(item => selectedValues.has(String(item?.code ?? item?.value ?? '')))
    .map(item => item?.name ?? item?.label ?? item?.text)
    .map(value => String(value || '').trim())
    .filter(Boolean)

  return selectedNames.length ? Array.from(new Set(selectedNames)).join(',') : undefined
}

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
  initialFormData: createBatchEventInitialFormData() as any,
  fetchDataApi: async () => {
    const queryFormData = {
      ...formData.value,
      handlerUserIds: batchEventOptions.filterValidHandlerUserIds(formData.value.handlerUserIds)
    }

    const response = await getBatchEventList(
      cleanEmptyParams({
        isMine: middlewareStore.batchEventPageType,
        pageSize: pageSize.value,
        pageNum: currentPage.value,
        ...queryFormData,
        startTime: queryTimeRange.value.startTime,
        endTime: queryTimeRange.value.endTime
      }) as any
    )

    return {
      list: response.result?.list || [],
      total: response.result?.total || 0
    }
  },
  resetBefore: () => {
    resetFilterState()
  }
})

const {
  times,
  shortcutValue,
  isExpanded,
  queryTimeRange,
  carSeriesOptions,
  carSeriesOptionProps,
  experienceCodeOptions,
  topicOptions,
  resetFilterState
} = useCustomerDirectEngageFilter(formData as any, {
  defaultTime: roleDefaultFilter.time,
  getDefaultTime: () => getBatchEventDefaultFilter().time
})

/**
 * 构造批量事件导出查询参数，复用当前筛选条件但不携带分页字段。
 * @returns 导出接口查询参数
 */
const buildBatchEventExportQuery = (): BatchEventQueryModel | undefined => {
  const queryFormData = {
    ...formData.value,
    handlerUserIds: batchEventOptions.filterValidHandlerUserIds(formData.value.handlerUserIds)
  }

  return cleanEmptyParams({
    isMine: middlewareStore.batchEventPageType,
    ...queryFormData,
    startTime: queryTimeRange.value.startTime,
    endTime: queryTimeRange.value.endTime
  }) as BatchEventQueryModel | undefined
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

const operationDisabled = computed(() => {
  if (selection.value.length === 0) {
    return true
  }

  const taskStatuses = selection.value.map(item => item.taskStatus)
  const uniqueTaskStatuses = [...new Set(taskStatuses)]
  return uniqueTaskStatuses.length > 1 || taskStatuses.includes('90')
})

const FCardHeight = computed(() => {
  return isExpanded.value ? 'calc(100vh - 466px)' : 'calc(100vh - 402px)'
})

const clearSelection = () => {
  selection.value = []
}

const batchEventTooltipFallbackPlacements: Placement[] = [
  'top-start',
  'top-end',
  'bottom-start',
  'bottom-end',
  'right',
  'left'
]

const batchEventTooltipPopperOptions = {
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
 * 为 TableV2 文本列提供省略展示和浅色 Tooltip，避免默认单元格原生 title 浮层。
 * @param cellData 当前单元格文本
 * @returns 带省略和 Tooltip 的文本节点
 */
const renderTextCell = (cellData: unknown) => {
  const content = cellData == null ? '' : String(cellData)

  return h(
    ElTooltip,
    {
      placement: 'top',
      disabled: !content,
      fallbackPlacements: batchEventTooltipFallbackPlacements,
      popperClass: 'batch-event__text-tooltip text-tooltip-light',
      popperOptions: batchEventTooltipPopperOptions
    },
    {
      content: () => h('div', { class: 'batch-event__text-tooltip-content' }, content),
      default: () => h('div', { class: 'batch-event__cell batch-event__cell--ellipsis' }, content)
    }
  )
}

/**
 * 格式化标准观点列，兼容后端返回 JSON 数组字符串或普通字符串。
 * @param value 标准观点原始值
 * @returns 逗号分隔后的展示文本
 */
const formatTopicName = (value: unknown): string => {
  if (Array.isArray(value)) {
    return value
      .map(item => String(item ?? '').trim())
      .filter(Boolean)
      .join('，')
  }

  if (value == null) {
    return ''
  }

  const rawValue = String(value).trim()
  if (!rawValue) {
    return ''
  }

  try {
    const parsedValue = JSON.parse(rawValue)

    if (Array.isArray(parsedValue)) {
      return parsedValue
        .map(item => String(item ?? '').trim())
        .filter(Boolean)
        .join('，')
    }
  } catch {
    // 非 JSON 字符串按后端原始文本展示，避免误伤普通标准观点名称。
  }

  return rawValue
}

/**
 * 将协同部门名称字段解析为有序文本列表，兼容 Swagger 约定的 JSON 数组字符串和异常兜底值。
 * @param value 协同部门名称原始值
 * @returns 清理后的部门名称列表
 */
const parseCollaboratingDepartmentNames = (value: unknown): string[] => {
  if (Array.isArray(value)) {
    return value.map(item => String(item ?? '').trim()).filter(Boolean)
  }

  if (value == null) {
    return []
  }

  const rawValue = String(value).trim()
  if (!rawValue) {
    return []
  }

  try {
    const parsedValue = JSON.parse(rawValue)

    if (Array.isArray(parsedValue)) {
      return parsedValue.map(item => String(item ?? '').trim()).filter(Boolean)
    }
  } catch {
    // 后端异常返回普通字符串时，保留原值，避免协同部门整列为空。
  }

  return [rawValue]
}

/**
 * 按接口数组下标配对协同部门二、三级名称，生成表格单行展示文本。
 * @param secondDeptValue 协同二级部门名称字段
 * @param thirdDeptValue 协同三级部门名称字段
 * @returns 多个协同部门以中文分号连接的路径文本
 */
const formatCollaboratingDepartments = (
  secondDeptValue: unknown,
  thirdDeptValue: unknown
): string => {
  const secondDepartmentNames = parseCollaboratingDepartmentNames(secondDeptValue)
  const thirdDepartmentNames = parseCollaboratingDepartmentNames(thirdDeptValue)
  const departmentCount = Math.max(secondDepartmentNames.length, thirdDepartmentNames.length)

  return Array.from({ length: departmentCount }, (_, index) => {
    const departmentPath = [secondDepartmentNames[index], thirdDepartmentNames[index]].filter(
      Boolean
    )
    return departmentPath.join(' / ')
  })
    .filter(Boolean)
    .join('；')
}

/**
 * 标准化事件优先级编码，保证标签样式命中单点事件 P0-P4 色板。
 * @param value 优先级编码
 * @returns 展示文案与样式 class
 */
const formatEventPriorityTag = (value: unknown) => {
  const priority = value == null ? '' : String(value).trim()

  return {
    tagName: priority.toLocaleUpperCase(),
    type: priority.toLocaleLowerCase()
  }
}

const columns: Column<BatchEventRow>[] = [
  {
    key: 'selection',
    width: 55,
    fixed: true,
    cellRenderer: ({ rowData }) => {
      const isSelected = selection.value.some(item => item.id === rowData.id)

      return h(ElCheckbox, {
        modelValue: isSelected,
        'onUpdate:modelValue': checked => {
          selection.value =
            checked === true
              ? [...selection.value, rowData]
              : selection.value.filter(item => item.id !== rowData.id)
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
        indeterminate,
        'onUpdate:modelValue': checked => {
          selection.value = checked === true ? [...dataList.value] : []
        }
      })
    }
  },
  {
    key: 'warningEventNo',
    title: '事件编号',
    dataKey: 'warningEventNo',
    width: 180,
    fixed: true,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'warningEventName',
    title: '事件名称',
    dataKey: 'warningEventName',
    width: 250,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'subjectCategoryName',
    title: '主题分类',
    dataKey: 'subjectCategoryName',
    width: 180,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'eventPriority',
    title: '处理优先级',
    width: 100,
    cellRenderer: ({ rowData }) => {
      const priorityTag = formatEventPriorityTag(rowData.eventPriority)

      return h(EventPriorityTag, {
        tagName: priorityTag.tagName,
        type: priorityTag.type
      })
    }
  },
  {
    key: 'dataSource',
    title: '数据源',
    dataKey: 'channelNames',
    width: 150,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'brandName',
    title: '品牌',
    dataKey: 'brandName',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'carSeriesName',
    title: '车系',
    dataKey: 'carSeriesName',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'topicText',
    title: '标准观点',
    dataKey: 'topicText',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(formatTopicName(cellData))
  },
  {
    key: 'intention',
    title: '意图',
    dataKey: 'intention',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'mentionCount',
    title: '提及量',
    dataKey: 'mentionCount',
    width: 100,
    cellRenderer: ({ rowData }) => {
      return h('div', {}, rowData.mentionCount ? fmtNum(rowData.mentionCount) : '')
    }
  },
  {
    key: 'mentionCountRate',
    title: '提及量环比',
    dataKey: 'mentionCountRate',
    width: 120,
    cellRenderer: ({ rowData }) => {
      return h('div', {}, rowData.mentionCountRate ? fmtFix(rowData.mentionCountRate) : '')
    }
  },
  {
    key: 'createUserName',
    title: '创建人员',
    dataKey: 'createUserName',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(!cellData ? emptyCellValue : cellData)
  },
  {
    key: 'mainRespUserName',
    title: '业务责任人',
    dataKey: 'mainRespUserName',
    width: 180,
    cellRenderer: ({ rowData }) =>
      renderTextCell(
        formatBatchEventMainRespUser(rowData.mainRespUserName, rowData.mainRespUserEmpNo)
      )
  },
  {
    key: 'mainRespOrgName',
    title: '主责部门',
    dataKey: 'primaryDepName',
    width: 140,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'collaboratingDepartments',
    title: '协同部门',
    width: 160,
    cellRenderer: ({ rowData }) =>
      renderTextCell(
        formatCollaboratingDepartments(
          rowData.coordinateSecondDeptName,
          rowData.coordinateThirdDeptName
        )
      )
  },
  {
    key: 'processedUserName',
    title: '处理人员',
    dataKey: 'processedUserName',
    width: 140,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },

  {
    key: 'updateTime',
    title: '更新时间',
    dataKey: 'updateTime',
    width: 180,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'warningPeriod',
    title: '预警频率',
    dataKey: 'warningPeriod',
    width: 100,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'warningTime',
    title: '预警时间',
    dataKey: 'warningTime',
    width: 180,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'eventAttributes',
    title: '事件属性',
    dataKey: 'eventAttributes',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'eventValidity',
    title: '事件有效性',
    dataKey: 'eventValidity',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'isRejected',
    title: '是否驳回',
    dataKey: 'isRejected',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'rejectReason',
    title: '驳回原因',
    dataKey: 'rejectReason',
    width: 120,
    cellRenderer: ({ cellData }) => renderTextCell(cellData)
  },
  {
    key: 'taskStatus',
    title: '事件状态',
    width: 130,
    dataKey: 'taskStatusName',
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ rowData }) => {
      return h('div', { class: 'status-cell' }, [
        h('span', {
          class: 'status-dot',
          style: {
            backgroundColor: TaskStatusColorMap[rowData.taskStatus] || TaskStatusColorMap.default
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
      const buttons = getOperationButtons(rowData).map(button => {
        return h(
          ElButton,
          {
            key: button.type,
            type: 'primary',
            link: true,
            onClick: () => handleEvent(rowData, button.type)
          },
          () => button.label
        )
      })

      return h('div', { class: 'operation-cell' }, buttons)
    }
  }
]

watch(
  () => dataList.value,
  () => {
    clearSelection()
  },
  { flush: 'post' }
)

watch(
  () => middlewareStore.batchEventPageType,
  () => {
    clearSelection()
    tableMethods.handleQuery()
  }
)

/** 按当前筛选条件创建批量事件异步导出任务。 */
const handleExport = async () => {
  await downloadBatchEvent({
    request: exportBatchEvent,
    params: () => buildBatchEventExportQuery() || {},
    exportMenu: '批量事件-事件列表',
    errorMessage: '导出事件失败，请稍后重试'
  })
}

/**
 * 创建批量事件详情客户原声异步导出任务。
 * @param queryParams 当前客户原声列表筛选条件，兜底读取其中的事件 ID
 * @param dataSourceType 当前事件数据源类型，为空时按结果数据导出
 */
const handleVoiceExport = async (
  queryParams: VocQueryParams,
  dataSourceType: BatchEventDataSourceType = 'RESULT'
) => {
  const eventId = String(curRow.value?.id || (queryParams as Record<string, any>).newId || '')

  if (!eventId) {
    ElMessage.warning('事件ID缺失，无法导出客户原声')
    return
  }

  await downloadBatchEventDetail({
    request: dataSourceType === 'ORIGINAL' ? exportBatchEventRaw : exportBatchEventDetail,
    params: { id: eventId },
    exportMenu: '批量事件-客户原声',
    errorMessage: '导出客户原声失败，请稍后重试'
  })
}

// 批量弹窗显示前校验选中数据，避免无效操作进入弹窗。
const setBpVisible = (visible: boolean) => {
  if (selection.value.length === 0) {
    ElMessage.warning('请先选择事件')
    return
  }

  bpVisible.value = visible
}

const handleBatchCommand = (command: BatchEventActionTypeEnum) => {
  batchType.value = command
  setBpVisible(true)
}

/**
 * 根据选中的业务责任人 ID 获取批量审核接口需要的人员字段。
 * @param userId 业务责任人 ID
 * @returns 业务责任人姓名、工号和上级部门名称
 */
const getReviewUserByUserId = async (userId?: string) => {
  if (!userId) {
    return {
      userName: '',
      userEmpNo: '',
      secondDeptName: '',
      thirdDeptName: ''
    }
  }
  const departAccountTree = await batchEventOptions.loadDepartAccountTree()
  const userInfo = batchEventOptions.getUserInfoById(userId)
  const departmentNames = getAccountDepartmentNamesByUserId(departAccountTree, userId)

  return {
    userName: userInfo?.userName || '',
    userEmpNo: userInfo?.employeeId || '',
    secondDeptName: departmentNames.secondDeptName,
    thirdDeptName: departmentNames.thirdDeptName
  }
}

/**
 * 提交批量操作。
 * @param payload 批量操作弹窗回传载荷
 */
const handleBatchConfirm = async (payload: BatchEventBatchActionPayload) => {
  showLoading()
  try {
    if (payload.actionType === BatchEventActionTypeEnum.Audit) {
      if (payload.mode === BatchEventAuditModeEnum.Pass) {
        const reviewUser = await getReviewUserByUserId(payload.formData.businessOwnerUserId)
        await batchApproveBatchEvent({
          eventIds: payload.selectedIds,
          reviewUserId: payload.formData.businessOwnerUserId,
          reviewUserName: reviewUser.userName,
          reviewUserEmpNo: reviewUser.userEmpNo,
          secondDeptName: reviewUser.secondDeptName,
          thirdDeptName: reviewUser.thirdDeptName,
          description: payload.formData.description
        })
      } else {
        await batchCloseBatchEvent({
          eventIds: payload.selectedIds,
          closeReason: payload.formData.auditCloseReason,
          description: payload.formData.description
        })
      }
    }

    if (payload.actionType === BatchEventActionTypeEnum.Response) {
      if (payload.mode === BatchEventResponseModeEnum.Confirm) {
        const mainRespUser = batchEventOptions.userInfoByUserId(
          payload.formData.responseMainRespUserId
        )
        if (!mainRespUser.mainRespOrgId || !mainRespUser.mainRespOrgName) {
          ElMessage.warning('未获取到主责部门信息')
          return
        }
        const isSwordMode =
          payload.formData.responseHandleMode === BatchEventResponseHandleModeEnum.Sword
        await batchConfirmBatchEvent({
          eventIds: payload.selectedIds,
          mainRespUserId: mainRespUser.mainRespUserId,
          mainRespUserName: mainRespUser.mainRespUserName,
          mainRespUserEmpNo: mainRespUser.mainRespUserEmpNo,
          mainRespOrgId: mainRespUser.mainRespOrgId,
          mainRespOrgName: mainRespUser.mainRespOrgName,
          handleMode: payload.formData.responseHandleMode,
          custType: isSwordMode
            ? joinSelectedOptionNames(
                payload.responseConditions?.custTypeList || [],
                payload.formData.responseUserType
              )
            : undefined,
          usageScenario: isSwordMode
            ? joinSelectedOptionNames(
                payload.responseConditions?.usageScenarioList || [],
                payload.formData.responseCarScene
              )
            : undefined,
          topicText: isSwordMode
            ? joinSelectedOptionNames(
                payload.responseConditions?.topicTextList || [],
                payload.formData.responseFocusTopicValues
              )
            : undefined,
          pageUrl: getPageUrlByHandleMode(payload.formData.responseHandleMode),
          description: payload.formData.description
        })
      } else {
        await batchRejectBatchEvent({
          eventIds: payload.selectedIds,
          rejectReason: payload.formData.responseRejectReason,
          description: payload.formData.description
        })
      }
    }

    if (payload.actionType === BatchEventActionTypeEnum.Close) {
      await batchCloseBatchEvent({
        eventIds: payload.selectedIds,
        closeReason: payload.formData.closeReason,
        description: payload.formData.description
      })
    }

    clearSelection()
    ElMessage.success('操作成功')
    tableMethods.handleQuery()
  } catch (error) {
    console.error('handleBatchConfirm error:', error)
  } finally {
    hideLoading()
  }
}

const handleEvent = (row: BatchEventRow, type: EventType) => {
  curRow.value = row
  detailEventType.value = type
  batchDetailVisible.value = true
}

/**
 * 从路由 query 恢复详情弹窗打开状态。
 * 仅依赖 detailId/detailType，不要求列表当前行已加载完成。
 */
const restoreDetailDialogFromRoute = () => {
  const detailId = getRouteQueryString(route.query.detailId)
  if (!detailId) {
    return
  }

  curRow.value = { id: detailId } as BatchEventRow
  detailEventType.value = normalizeBatchEventDetailType(route.query.detailType)
  batchDetailVisible.value = true
}

watch(
  () => [route.query.detailId, route.query.detailType],
  () => {
    restoreDetailDialogFromRoute()
  },
  { immediate: true }
)

/**
 * 详情内主流程操作成功后刷新批量事件列表，保留当前筛选和分页。
 */
const handleDetailRefresh = () => {
  tableMethods.getList()
}

/**
 * 根据列表行权限生成主表操作按钮。
 * @param row 批量事件列表行
 * @returns 当前行可展示的操作按钮
 */
const getOperationButtons = (row: BatchEventRow): BatchEventOperationButton[] => {
  const permissions = Array.isArray(row.permissions) ? row.permissions : []
  return batchEventOperationButtons.filter(button => permissions.includes(button.type))
}

void Promise.all([
  batchEventOptions.loadConditions(),
  batchEventOptions.loadBatchRuleCategoryTree(),
  batchEventOptions.preloadDepartmentAndHandlerOptions()
])
</script>

<template>
  <div class="batch-event-page">
    <FCard title="筛选条件" titleSize="middle" :is-show-more="false" height="auto">
      <CustomerDirectEngageFilterForm
        v-model:form-data="formData"
        v-model:times="times"
        v-model:shortcut-value="shortcutValue"
        v-model:is-expanded="isExpanded"
        :car-series-options="carSeriesOptions"
        :car-series-option-props="carSeriesOptionProps"
        :experience-code-options="experienceCodeOptions"
        :topic-options="topicOptions"
        @query="tableMethods.handleQuery"
        @reset="tableMethods.handleReset"
      />
    </FCard>

    <FCard title="事件列表" titleSize="middle" :is-show-more="false" class="mt-24" height="auto">
      <template #more>
        <div class="table-actions">
          <!-- <el-input
            v-model="keyword"
            placeholder="请输入关键词搜索"
            class="search-input"
            :prefix-icon="Search"
            disabled
          /> -->
          <el-button
            v-if="userStore.checkfunctionPermission(FunctionPermission.BULK_EVENT_DOWNLOAD)"
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
              批量操作
              <el-icon class="el-icon&#45;&#45;right">
                <arrow-down />
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in batchOperationOptions"
                  :key="item.value"
                  :command="item.value"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>

      <div class="table-wrapper" :style="{ height: FCardHeight }">
        <div class="table-container">
          <el-auto-resizer>
            <template #default="{ width, height }">
              <el-table-v2
                v-loading="loading"
                :columns="columns"
                :data="dataList"
                :width="width"
                :height="height"
                :row-key="'id'"
                fixed
              />
            </template>
          </el-auto-resizer>
        </div>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          class="table-pagination"
        />
      </div>
    </FCard>

    <BatchEventBatchAuditDialog
      v-if="batchType === BatchEventActionTypeEnum.Audit"
      v-model="bpVisible"
      :selection="selection"
      @confirm="handleBatchConfirm"
    />

    <BatchEventBatchResponseDialog
      v-if="batchType === BatchEventActionTypeEnum.Response"
      v-model="bpVisible"
      :selection="selection"
      :depart-account-tree="batchEventOptions.departAccountTree.value"
      @confirm="handleBatchConfirm"
    />

    <BatchEventBatchCloseDialog
      v-if="batchType === BatchEventActionTypeEnum.Close"
      v-model="bpVisible"
      :selection="selection"
      @confirm="handleBatchConfirm"
    />

    <BatchEventDetail
      v-model="batchDetailVisible"
      :row="curRow"
      :event-type="detailEventType"
      :start-time="queryTimeRange.startTime"
      :end-time="queryTimeRange.endTime"
      :depart-account-tree="batchEventOptions.departAccountTree.value"
      :voice-export-loading="voiceExportLoading"
      @refresh="handleDetailRefresh"
      @voice-export="handleVoiceExport"
    />
  </div>
</template>

<style lang="scss" scoped>
.batch-event-page {
  .filter-tip {
    margin-bottom: 16px;
    line-height: 20px;
    font-size: 14px;
    color: #667085;
  }

  .table-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    .search-input {
      width: 240px;
    }
  }

  .table-wrapper {
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .table-container {
    height: calc(100% - 48px);
  }

  .table-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }

  :deep(.batch-event__cell) {
    width: 100%;
    line-height: 20px;
  }

  :deep(.batch-event__cell--ellipsis) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.batch-event__cell--right) {
    text-align: right;
  }

  :deep(.batch-event__text-tooltip) {
    max-width: min(520px, calc(100vw - 32px));
  }

  :deep(.batch-event__text-tooltip-content) {
    max-height: min(320px, calc(100vh - 48px));
    overflow: auto;
    box-sizing: border-box;
    white-space: normal;
    word-break: break-word;
    overflow-wrap: anywhere;
    line-height: 20px;
  }

  :deep(.status-cell) {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.status-dot) {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  :deep(.operation-cell) {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
