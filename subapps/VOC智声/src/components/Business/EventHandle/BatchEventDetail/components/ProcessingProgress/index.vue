<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useLoading } from '@/hooks/useLoading'
import { useBatchEventOptions } from '@/views/customerDirectEngage/batchEvent/hooks/useBatchEventOptions'
import {
  approveBatchEvent,
  ccBatchEvent,
  closeBatchEvent,
  confirmBatchEvent,
  createBatchEventTask,
  deleteBatchEventTask,
  editBatchEventTask,
  getBatchEventCcUserList,
  getBatchEventOperationLogs,
  getBatchEventTaskList,
  handleCompleteBatchEvent,
  initBatchEvent,
  reassignBatchEvent,
  reassignBatchEventTask,
  rejectBatchEvent,
  updateBatchEventTaskProgress
} from '@/api/batchEvent'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import type {
  BatchEventBriefDetailVo,
  BatchEventConditionsVo,
  BatchEventCcUserVo,
  BatchEventOpeLogVo,
  BatchEventOptionVo,
  BatchEventPermissionVo,
  BatchEventTaskCreateModel,
  BatchEventTaskProgressModel,
  BatchEventTaskReassignModel,
  BatchEventTaskVo,
  BatchEventUserModel
} from '@/api/batchEvent/types'
import {
  batchEventProcessingProgressConfig,
  buildBatchEventProcessingSteps,
  getBatchEventProcessingBusinessTag,
  getBatchEventProcessingStageByTaskStatus
} from '../../config'
import { BatchEventTaskProgressOptions, BatchEventTaskProgressValue } from '../../beConstants'
import type {
  BatchEventProcessingHandleStageModuleConfig,
  BatchEventProcessingLoopMode,
  BatchEventProcessingTaskProgressValue,
  BatchEventProcessingStageKey,
  BatchEventProcessingCloseTaskItem,
  BatchEventProcessingConfirmActionType,
  BatchEventProcessingCcPersonnelItem,
  BatchEventProcessingEventHandleModule,
  BatchEventProcessingOperationLogItem,
  BatchEventProcessingSelectOption,
  BatchEventProcessingVocTaskItem
} from '../../types'
import { formatBatchEventDetailTitle, formatBatchEventMainRespUser } from '../../utils'
import ProcessingProgressAddCcDialog from './ProcessingProgressAddCcDialog.vue'
import ProcessingProgressCcPersonnel from './ProcessingProgressCcPersonnel.vue'
import ProcessingProgressConfirmDialog from './ProcessingProgressConfirmDialog.vue'
import ProcessingProgressCloseDialog from './ProcessingProgressCloseDialog.vue'
import ProcessingProgressEventHandle from './ProcessingProgressEventHandle.vue'
import ProcessingProgressHandleConfirmDialog from './ProcessingProgressHandleConfirmDialog.vue'
import ProcessingProgressOperationLog from './ProcessingProgressOperationLog.vue'
import ProcessingProgressRejectDialog from './ProcessingProgressRejectDialog.vue'
import ProcessingProgressTaskDialog from './ProcessingProgressTaskDialog.vue'
import ProcessingProgressTaskProgressDialog from './ProcessingProgressTaskProgressDialog.vue'
import ProcessingProgressSteps from './ProcessingProgressSteps.vue'
import ProcessingProgressSummaryCard from './ProcessingProgressSummaryCard.vue'
import ProcessingProgressTransferDialog from './ProcessingProgressTransferDialog.vue'
import type { ProcessingProgressCcSelectionItem, ProcessingProgressExpose } from './types'
import type { ProcessingProgressEventHandleFormValue } from './ProcessingProgressEventHandle.vue'
import {
  findAccountByUserId,
  findAccountPathByUserId,
  findAccountInDepartAccountTree,
  findDepartmentPathInDepartAccountTree,
  getAccountDepartmentNamesByUserId
} from './personnelTree'
import { buildBatchEventDetailShareLink } from '../../share'

defineOptions({
  name: 'ProcessingProgress'
})

const props = defineProps<{
  row?: Record<string, any>
  briefData?: BatchEventBriefDetailVo
  loopMode?: BatchEventProcessingLoopMode
  departAccountTree?: InsReportSysDepartVo[]
  permission?: BatchEventPermissionVo
  readOnly?: boolean
}>()
const emit = defineEmits<{
  'task-status-change': [taskStatus: string]
  'loop-mode-change': [mode: BatchEventProcessingLoopMode]
  'operation-success': []
  'operation-refresh': []
}>()

type EventHandleValidationStage = Extract<BatchEventProcessingStageKey, 'approve' | 'confirm'>
type ProcessingProgressTaskAction =
  | 'update-progress'
  | 'transfer-task'
  | 'edit-task'
  | 'delete-task'
type ProcessingProgressTaskFormData = {
  taskName: string
  description: string
  roleValue: string
  departmentValue: string
}

const progressData = batchEventProcessingProgressConfig
const batchEventOptions = useBatchEventOptions()
const { showLoading, hideLoading } = useLoading()

const closeDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const addCcDialogVisible = ref(false)
const handleCloseDialogVisible = ref(false)
const createTaskDialogVisible = ref(false)
const editTaskDialogVisible = ref(false)
const transferTaskDialogVisible = ref(false)
const transferHandlerDialogVisible = ref(false)
const updateTaskProgressDialogVisible = ref(false)
const deleteTaskDialogVisible = ref(false)
const currentConfirmDialogAction = ref<BatchEventProcessingConfirmActionType>('confirm')
const eventHandleRef = ref<{ getFormState: () => ProcessingProgressEventHandleFormValue }>()
const eventConditions = ref<BatchEventConditionsVo>({})
const remoteTaskStatus = ref<string>()
const operationLogItems = ref<BatchEventProcessingOperationLogItem[]>([])
const currentLoopMode = ref<BatchEventProcessingLoopMode>(props.loopMode || 'voc-loop')
const selectedVocTaskId = ref<string>()

const ccPersonnelSelections = ref<ProcessingProgressCcSelectionItem[]>([])
const ccPersonnelItems = ref<BatchEventProcessingCcPersonnelItem[]>([])
const unmatchedCcPersonnelItems = ref<BatchEventProcessingCcPersonnelItem[]>([])
const ccSelectionInitialized = ref(false)
const vocTaskItems = ref<BatchEventProcessingVocTaskItem[]>([])

const rowData = computed(() => props.row || {})
const departAccountTree = computed(() => props.departAccountTree || [])
const eventId = computed(() => rowData.value.id || '')
const briefData = computed(() => props.briefData || {})
const permission = computed(() => props.permission || {})
const hasInitTaskStatus = computed(() => Boolean(remoteTaskStatus.value))
const currentStage = computed(() =>
  getBatchEventProcessingStageByTaskStatus(remoteTaskStatus.value)
)
const currentSteps = computed(() => buildBatchEventProcessingSteps(remoteTaskStatus.value))
const currentBusinessTag = computed(() => {
  if (!hasInitTaskStatus.value) {
    return ''
  }

  return getBatchEventProcessingBusinessTag(remoteTaskStatus.value)
})

const handleModeOptions: BatchEventProcessingSelectOption[] = [
  { label: 'VOC系统闭环', value: 'voc-loop' },
  { label: '天枢星链系统闭环', value: 'sword-loop' }
]

const taskRoleOptions: BatchEventProcessingSelectOption[] = [
  { label: '主责部门', value: 'primary' },
  { label: '协办部门', value: 'assist' }
]

type BatchEventPermissionKey = keyof Pick<
  BatchEventPermissionVo,
  | 'approve'
  | 'approveClose'
  | 'closeEvent'
  | 'confirm'
  | 'createTask'
  | 'editTask'
  | 'deleteTask'
  | 'updateTaskProgress'
  | 'addCcUser'
  | 'rejectEvent'
  | 'reassignHandler'
>

/**
 * 判断当前事件是否具备指定功能权限。
 * @param permissionKey Swagger 权限字段
 * @returns 是否允许展示或触发对应操作
 */
const hasBatchEventPermission = (permissionKey: BatchEventPermissionKey) => {
  return permission.value?.[permissionKey] === true
}

const showAddCcButton = computed(() => {
  return !props.readOnly && hasBatchEventPermission('addCcUser')
})

/**
 * 规范化处理进度初始化接口返回状态。
 * 仅 null、undefined 和空字符串视为无状态，避免数字状态码被误判。
 * @param taskStatus 初始化接口返回的状态
 * @returns 标准任务状态码
 */
const normalizeInitTaskStatus = (taskStatus: unknown) => {
  if (taskStatus === null || taskStatus === undefined) {
    return ''
  }

  return String(taskStatus).trim()
}

/**
 * 将简报接口返回的处理方式转换为页面闭环模式。
 * 后端当前以 VOC 表示 VOC 系统闭环，其他非空值按非 VOC 闭环处理。
 * @param handleMode 简报接口处理方式
 * @returns 页面闭环模式；空值返回 null 表示不覆盖当前模式
 */
const mapApiHandleModeToLoopMode = (handleMode?: string): BatchEventProcessingLoopMode | null => {
  const normalizedMode = String(handleMode || '').trim()
  if (!normalizedMode) {
    return null
  }

  return normalizedMode.toUpperCase().includes('VOC') ? 'voc-loop' : 'sword-loop'
}

/**
 * 将接口责任部门类型转换为页面任务角色。
 * @param deptType 接口责任部门类型
 * @returns 页面任务角色
 */
const mapApiDeptTypeToTaskRole = (
  deptType?: string
): BatchEventProcessingVocTaskItem['roleValue'] => {
  return String(deptType || '').toUpperCase() === 'COOP' ? 'assist' : 'primary'
}

/**
 * 将页面任务角色转换为接口责任部门类型。
 * @param roleValue 页面任务角色
 * @returns 接口责任部门类型
 */
const mapTaskRoleToApiDeptType = (roleValue?: string) => {
  return roleValue === 'assist' ? 'COOP' : 'MAIN'
}

/**
 * 将接口任务进度编码转换为页面任务进度值。
 * @param progressStatus 接口进度状态编码
 * @param progressStatusName 接口进度状态名称
 * @returns 页面任务进度值
 */
const mapTaskProgressToUiValue = (
  progressStatus?: string,
  progressStatusName?: string
): BatchEventProcessingTaskProgressValue => {
  const normalizedStatus = String(progressStatus || '')
    .trim()
    .toUpperCase()
  const normalizedName = String(progressStatusName || '').trim()

  if (
    ['0', '00', '10', 'NOT_STARTED', 'NOT-STARTED'].includes(normalizedStatus) ||
    normalizedName.includes('未')
  ) {
    return BatchEventTaskProgressValue.NotStarted
  }

  if (
    ['2', '20', '30', '100', 'DONE', 'COMPLETED', 'COMPLETE'].includes(normalizedStatus) ||
    normalizedName.includes('完成') ||
    normalizedName.includes('已完成')
  ) {
    return BatchEventTaskProgressValue.Completed
  }

  return BatchEventTaskProgressValue.InProgress
}

const userTypeOptions = computed<BatchEventOptionVo[]>(() => {
  return eventConditions.value.custTypeList || []
})

const vehicleSceneOptions = computed<BatchEventOptionVo[]>(() => {
  return eventConditions.value.usageScenarioList || []
})

const closeReasonOptions = computed<BatchEventProcessingSelectOption[]>(() => {
  return (batchEventOptions.batchEvent_close_reason_type.value || []).map((item: any) => ({
    label: item.text || item.label || item.value,
    value: item.value
  }))
})

const rejectReasonOptions = computed<BatchEventProcessingSelectOption[]>(() => {
  return (batchEventOptions.batchEvent_reject_reason_type.value || []).map((item: any) => ({
    label: item.text || item.label || item.value,
    value: item.value
  }))
})

const handleReasonOptions = computed<BatchEventProcessingSelectOption[]>(() => {
  return (batchEventOptions.task_event_approve_process_mode.value || []).map((item: any) => ({
    label: item.text || item.label || item.value,
    value: item.value
  }))
})

const focusTopicOptions = computed<BatchEventOptionVo[]>(() => {
  return eventConditions.value.topicTextList || []
})

const summaryTitle = computed(() => {
  return formatBatchEventDetailTitle(
    briefData.value.eventName || rowData.value.warningEventName,
    briefData.value.warningEventNo || rowData.value.warningEventNo
  )
})

const priorityTag = computed(() => {
  return (
    rowData.value.eventPriority ||
    briefData.value.eventPriorityName ||
    ''
  ).toLocaleUpperCase()
})

const isRejected = computed(() => {
  // 驳回需要审核，审核驳回期间状态可能被推进；进入闭环处理后不再展示驳回标识。
  return briefData.value.isReject === '1' && briefData.value.taskStatusName !== '闭环处理'
})

const rejectReasonText = computed(() => {
  return briefData.value.rejectReason || rowData.value.rejectReason || '暂无驳回原因'
})

const summaryItems = computed(() => {
  const warningInfo = [
    briefData.value.warningPeriod || rowData.value.warningPeriod,
    briefData.value.warningTime || rowData.value.warningTime
  ]
    .filter(Boolean)
    .join(' / ')
  const mainRespUserName = briefData.value.mainRespUserName || rowData.value.mainRespUserName
  const mainRespUserEmpNo = briefData.value.mainRespUserEmpNo || rowData.value.mainRespUserEmpNo

  return [
    {
      label: '事件信息',
      value: briefData.value.eventName || rowData.value.warningEventName || '-'
    },
    { label: '预警频率/时间', value: warningInfo || '-' },
    {
      label: '主题分类',
      value: briefData.value.subjectCategoryName || rowData.value.subjectCategoryName || '-'
    },
    { label: '品牌范围', value: briefData.value.brandName || rowData.value.brandName || '-' },
    {
      label: '业务责任人',
      value: formatBatchEventMainRespUser(mainRespUserName, mainRespUserEmpNo)
    },
    {
      label: '主责部门',
      value: briefData.value.primaryDepName || '-'
    }
  ]
})

const summaryFocusTopics = computed(() => {
  if (Array.isArray(briefData.value.focusTopics)) {
    return briefData.value.focusTopics.filter(Boolean)
  }

  return []
})

/**
 * 从详情和列表行中解析审核阶段默认业务责任人。
 * 优先使用后端直接下发的 userId；只有工号时再从人员树中兜底匹配。
 * @returns 业务责任人 userId
 */
const approveDefaultMainOwner = computed(() => {
  const rawBriefData = briefData.value as Record<string, any>
  const rawRowData = rowData.value as Record<string, any>
  const ownerId = String(
    rawBriefData.mainRespUserId ||
      rawBriefData.mainRespUser?.userId ||
      rawBriefData.mainRespUser?.id ||
      rawRowData.mainRespUserId ||
      rawRowData.mainRespUser?.userId ||
      rawRowData.mainRespUser?.id ||
      ''
  ).trim()

  if (ownerId) {
    return ownerId
  }

  const employeeNo = String(
    rawBriefData.mainRespUserEmpNo ||
      rawBriefData.mainRespUser?.userEmpNo ||
      rawBriefData.mainRespUser?.employeeId ||
      rawRowData.mainRespUserEmpNo ||
      rawRowData.mainRespUser?.userEmpNo ||
      rawRowData.mainRespUser?.employeeId ||
      ''
  ).trim()

  if (!employeeNo) {
    return ''
  }

  return (
    findAccountInDepartAccountTree(departAccountTree.value, account => {
      return account.employeeId === employeeNo
    })?.userId || ''
  )
})

const currentEventHandleModule = computed<BatchEventProcessingEventHandleModule | null>(() => {
  if (!hasInitTaskStatus.value) {
    return null
  }

  if (currentStage.value === 'approve' && progressData.stageModules.approve.stage === 'approve') {
    return {
      ...progressData.stageModules.approve,
      mainOwner: {
        ...progressData.stageModules.approve.mainOwner,
        value: approveDefaultMainOwner.value,
        options: []
      },
      description: {
        ...progressData.stageModules.approve.description,
        value: ''
      }
    }
  }

  if (currentStage.value === 'confirm' && progressData.stageModules.confirm.stage === 'confirm') {
    return {
      ...progressData.stageModules.confirm,
      mainDepartment: {
        ...progressData.stageModules.confirm.mainDepartment,
        value: '',
        options: []
      },
      cooperationDepartment: {
        ...progressData.stageModules.confirm.cooperationDepartment,
        value: [],
        options: []
      },
      handleMode: {
        ...progressData.stageModules.confirm.handleMode,
        value: currentLoopMode.value,
        options: handleModeOptions
      },
      userType: {
        ...progressData.stageModules.confirm.userType,
        value: [],
        options: userTypeOptions.value
      },
      vehicleScene: {
        ...progressData.stageModules.confirm.vehicleScene,
        value: [],
        options: vehicleSceneOptions.value
      },
      pointIssue: {
        ...progressData.stageModules.confirm.pointIssue,
        value: [],
        options: focusTopicOptions.value
      },
      description: {
        ...progressData.stageModules.confirm.description,
        value: ''
      }
    }
  }

  if (currentStage.value === 'handle' && progressData.stageModules.handle.stage === 'handle') {
    return {
      ...progressData.stageModules.handle,
      vocLoop: {
        ...progressData.stageModules.handle.vocLoop,
        departmentRole: {
          ...progressData.stageModules.handle.vocLoop.departmentRole,
          value: '',
          options: taskRoleOptions
        },
        departmentOwner: {
          ...progressData.stageModules.handle.vocLoop.departmentOwner,
          value: '',
          options: []
        },
        progress: {
          ...progressData.stageModules.handle.vocLoop.progress,
          value: BatchEventTaskProgressValue.NotStarted,
          options: BatchEventTaskProgressOptions
        },
        handler: {
          ...progressData.stageModules.handle.vocLoop.handler,
          value: '',
          options: []
        },
        tasks: vocTaskItems.value
      },
      swordLoop: {
        tasks: vocTaskItems.value
      }
    }
  }

  if (progressData.stageModules.close.stage === 'close') {
    return {
      ...progressData.stageModules.close,
      handleMode: {
        ...progressData.stageModules.close.handleMode,
        value: currentLoopMode.value,
        options: handleModeOptions
      },
      handleReason: {
        ...progressData.stageModules.close.handleReason,
        value: handleReasonOptions.value[0]?.value || '',
        options: handleReasonOptions.value
      },
      handler: {
        ...progressData.stageModules.close.handler,
        value: '',
        options: []
      },
      description: {
        ...progressData.stageModules.close.description,
        value: ''
      },
      taskTable: mapTasksToCloseTaskItems(vocTaskItems.value)
    }
  }

  return progressData.stageModules[currentStage.value]
})

const currentConfirmDialogModule = computed(() => {
  return progressData.confirmDialogModules[currentConfirmDialogAction.value]
})
const currentCloseDialogModule = computed(() => {
  return {
    ...progressData.closeDialogModule,
    closeReason: {
      ...progressData.closeDialogModule.closeReason,
      value: '',
      options: closeReasonOptions.value
    }
  }
})
const currentRejectDialogModule = computed(() => {
  return {
    ...progressData.rejectDialogModule,
    rejectReason: {
      ...progressData.rejectDialogModule.rejectReason,
      value: '',
      options: rejectReasonOptions.value
    }
  }
})
const currentHandleStageModule = computed<BatchEventProcessingHandleStageModuleConfig | null>(
  () => {
    if (!currentEventHandleModule.value) {
      return null
    }

    return currentEventHandleModule.value.stage === 'handle' ? currentEventHandleModule.value : null
  }
)
const currentSelectedVocTask = computed(() => {
  if (!selectedVocTaskId.value) {
    return null
  }

  return vocTaskItems.value.find(item => item.id === selectedVocTaskId.value) || null
})

/**
 * 判断任务行操作是否满足任务列表接口返回的行级权限。
 * @param action 任务行操作
 * @param task 当前任务行
 * @returns 是否允许打开弹窗或提交接口
 */
const canRunTaskRowAction = (
  action: ProcessingProgressTaskAction,
  task: BatchEventProcessingVocTaskItem | null
) => {
  if (!task) {
    return false
  }

  if (action === 'update-progress') {
    return task.progressEditable !== false
  }

  if (action === 'transfer-task') {
    return task.reassignable !== false
  }

  if (action === 'edit-task') {
    return task.editable !== false
  }

  if (action === 'delete-task') {
    return task.deletable !== false
  }

  return true
}
const currentHandleConfirmDialogModule = computed(() => {
  if (currentLoopMode.value === 'voc-loop') {
    return progressData.handleDialogModules.vocLoop.closeEvent
  }

  return progressData.handleDialogModules.swordLoop.closeEvent
})
const currentTransferHandlerDialogModule = computed(() => {
  return {
    ...progressData.handleDialogModules.vocLoop.transferHandler,
    handler: {
      ...progressData.handleDialogModules.vocLoop.transferHandler.handler,
      value: ''
    }
  }
})
const currentUpdateProgressDialogModule = computed(() => {
  return {
    ...progressData.handleDialogModules.vocLoop.updateProgress,
    progress: {
      ...progressData.handleDialogModules.vocLoop.updateProgress.progress,
      value: '',
      options: BatchEventTaskProgressOptions
    },
    description: {
      ...progressData.handleDialogModules.vocLoop.updateProgress.description,
      value: ''
    }
  }
})

/**
 * 将抄送人员选择模型标准化，避免空值在回填与去重时产生不稳定结果。
 * @param item CcPersonnelSelect 的标准选择项
 * @returns 规范化后的选择项
 */
const normalizeCcSelectionItem = (
  item: ProcessingProgressCcSelectionItem
): ProcessingProgressCcSelectionItem => {
  return {
    orgId: item.orgId || '',
    orgNo: item.orgNo || '',
    orgName: item.orgName || '',
    allFlag: Boolean(item.allFlag),
    userId: item.userId || '',
    userEmpNo: item.userEmpNo || '',
    userName: item.userName || ''
  }
}

/**
 * 生成抄送选择项的唯一键。
 * 部门全选按部门维度去重，单个人员按 userId / 工号去重，避免重复添加。
 * @param item 抄送选择项
 * @returns 唯一键
 */
const getCcSelectionKey = (item: ProcessingProgressCcSelectionItem) => {
  if (item.allFlag) {
    return `dept:${item.orgId || item.orgNo || item.orgName || ''}`
  }

  return `user:${item.userId || item.userEmpNo || item.userName || ''}`
}

/**
 * 对抄送选择结果做稳定去重。
 * @param items 抄送选择项列表
 * @returns 去重后的抄送选择项列表
 */
const dedupeCcSelections = (items: ProcessingProgressCcSelectionItem[]) => {
  const selectionMap = new Map<string, ProcessingProgressCcSelectionItem>()

  items.forEach(item => {
    const normalizedItem = normalizeCcSelectionItem(item)
    const key = getCcSelectionKey(normalizedItem)

    if (!selectionMap.has(key)) {
      selectionMap.set(key, normalizedItem)
    }
  })

  return Array.from(selectionMap.values())
}

/**
 * 生成抄送表格项的唯一键。
 * @param item 抄送人员表格项
 * @returns 唯一键
 */
const getCcPersonnelItemKey = (item: BatchEventProcessingCcPersonnelItem) => {
  return `${item.secondaryDepartment}|${item.tertiaryDepartment}|${item.userName}|${item.employeeId}`
}

/**
 * 对抄送人员表格项做去重，保证新增时不会出现重复行。
 * @param items 抄送人员表格项列表
 * @returns 去重后的表格数据
 */
const dedupeCcPersonnelItems = (items: BatchEventProcessingCcPersonnelItem[]) => {
  const itemMap = new Map<string, BatchEventProcessingCcPersonnelItem>()

  items.forEach(item => {
    const key = getCcPersonnelItemKey(item)
    if (!itemMap.has(key)) {
      itemMap.set(key, item)
    }
  })

  return Array.from(itemMap.values())
}

/**
 * 从部门路径中提取页面表格使用的“二级部门 / 三级部门”展示值。
 * 若路径不足两级，则尽量退化为现有的部门名展示，避免空白行。
 * @param selection 抄送选择项
 * @returns 二级/三级部门文案
 */
const getDepartmentDisplayBySelection = (selection: ProcessingProgressCcSelectionItem) => {
  const departmentPath = selection.orgId
    ? findDepartmentPathInDepartAccountTree(departAccountTree.value, selection.orgId)
    : []
  const departmentNames = departmentPath.map(item => item.name || '').filter(Boolean) as string[]

  if (departmentNames.length >= 2) {
    return {
      secondaryDepartment: departmentNames[departmentNames.length - 2],
      tertiaryDepartment: departmentNames[departmentNames.length - 1]
    }
  }

  if (departmentNames.length === 1) {
    return {
      secondaryDepartment: departmentNames[0],
      tertiaryDepartment: '-'
    }
  }

  return {
    secondaryDepartment: selection.orgName || '-',
    tertiaryDepartment: '-'
  }
}

/**
 * 将抄送选择结果转换为表格项。
 * @param selections 抄送选择项列表
 * @returns 用于表格展示的抄送人员列表
 */
const mapCcSelectionsToTableItems = (
  selections: ProcessingProgressCcSelectionItem[]
): BatchEventProcessingCcPersonnelItem[] => {
  return selections.map(selection => {
    const departmentDisplay = getDepartmentDisplayBySelection(selection)

    return {
      secondaryDepartment: departmentDisplay.secondaryDepartment,
      tertiaryDepartment: departmentDisplay.tertiaryDepartment,
      userName: selection.allFlag ? '全部' : selection.userName || '-',
      employeeId: selection.allFlag ? '-' : selection.userEmpNo || '-'
    }
  })
}

/**
 * 根据当前表格内容尽量回推出可在弹窗中回显的抄送人员选择值。
 * 只要员工编号能够匹配，就优先以部门账号树中的真实数据作为回显来源。
 */
const syncInitialCcSelections = () => {
  if (ccSelectionInitialized.value || departAccountTree.value.length === 0) {
    return
  }

  const matchedEmployeeIds = new Set<string>()
  const matchedSelections = ccPersonnelItems.value
    .map(item => {
      const matchedUser = findAccountInDepartAccountTree(departAccountTree.value, user => {
        return (
          user.employeeId === item.employeeId ||
          (user.userName === item.userName &&
            (user.deptName === item.tertiaryDepartment ||
              user.secondDeptName === item.secondaryDepartment ||
              user.thirdDeptName === item.secondaryDepartment))
        )
      })

      if (!matchedUser?.userId) {
        return null
      }

      if (matchedUser.employeeId) {
        matchedEmployeeIds.add(matchedUser.employeeId)
      }

      return normalizeCcSelectionItem({
        orgId: matchedUser.deptId || '',
        orgNo: matchedUser.deptId || '',
        orgName: matchedUser.deptName || '',
        allFlag: false,
        userId: matchedUser.userId,
        userEmpNo: matchedUser.employeeId,
        userName: matchedUser.userName
      } as ProcessingProgressCcSelectionItem)
    })
    .filter(Boolean) as ProcessingProgressCcSelectionItem[]

  ccPersonnelSelections.value = dedupeCcSelections(matchedSelections)
  unmatchedCcPersonnelItems.value = ccPersonnelItems.value.filter(item => {
    return !matchedEmployeeIds.has(item.employeeId)
  })
  ccSelectionInitialized.value = true
}

/**
 * 格式化批量事件操作记录的操作人展示文案。
 * @param item 后端操作记录
 * @returns 操作人展示文案
 */
const formatOperationLogOperator = (item: BatchEventOpeLogVo): string => {
  const userName = item.operateUserName || item.operatorName || ''
  if (!userName) return ''

  const orgPrefix = item.operateOrgName ? `${item.operateOrgName}-` : ''
  const empNoSuffix = item.operateUserEmpNo ? `(${item.operateUserEmpNo})` : ''
  return `${orgPrefix}${userName}${empNoSuffix}`
}

/**
 * 将后端操作记录转换为处理进度展示结构。
 * @param logs 后端操作记录
 * @returns 展示操作记录
 */
const mapOperationLogs = (logs: BatchEventOpeLogVo[]): BatchEventProcessingOperationLogItem[] => {
  return logs.map(item => {
    const contentDetails = Array.isArray(item.content)
      ? item.content
          .filter(detail => detail.contentType || detail.content)
          .map(detail => ({
            label: detail.contentType,
            value: detail.content || ''
          }))
      : []

    return {
      id: String(
        item.id || `${item.operateType || 'log'}-${item.operateTime || item.createTime || ''}`
      ),
      title: item.operateTypeName || item.operateType || '操作记录',
      details: [
        ...contentDetails,
        ...(item.operateContent ? [{ label: '操作内容', value: item.operateContent }] : []),
        ...(item.remark ? [{ label: '备注', value: item.remark }] : [])
      ],
      operator: formatOperationLogOperator(item),
      time: item.operateTime || item.createTime || ''
    }
  })
}

/**
 * 将后端抄送人转换为表格展示结构。
 * @param users 后端抄送人
 * @returns 抄送人员表格
 */
const mapCcUsers = (users: BatchEventCcUserVo[]): BatchEventProcessingCcPersonnelItem[] => {
  return users.map(item => ({
    secondaryDepartment: item.leve2DeptName || item.nodeOrgName || '-',
    tertiaryDepartment: item.leve3DeptName || '-',
    userName: item.nodeUserName || '-',
    employeeId: item.nodeUserEmpNo || '-'
  }))
}

/**
 * 将后端任务列表转换为处理进度任务表格结构。
 * @param tasks 后端任务列表
 * @returns 页面任务表格
 */
const mapBatchEventTasks = (tasks: BatchEventTaskVo[]): BatchEventProcessingVocTaskItem[] => {
  return tasks.map(item => {
    const fallbackTaskId = `${item.eventId || eventId.value || 'task'}-${item.createTime || ''}`
    const progress = mapTaskProgressToUiValue(item.progressStatus, item.progressStatusName)

    return {
      id: String(item.taskId || fallbackTaskId),
      taskName: item.taskName || '-',
      description: item.taskDesc || item.progressRemark || '',
      roleValue: mapApiDeptTypeToTaskRole(item.deptType),
      departmentValue: item.assigneeId || '',
      departmentLabel: item.handleDeptName || '-',
      handlerValue: item.assigneeId || '',
      handlerLabel: item.assigneeName || '-',
      progress,
      progressText: item.progressStatusName || '',
      processTime: item.handleTime || item.updateTime || item.createTime || '-',
      editable: item.editable,
      deletable: item.deletable,
      reassignable: item.reassignable,
      progressEditable: item.progressEditable
    }
  })
}

/**
 * 将通用任务表格项转换为事件关闭阶段的只读展示项。
 * @param tasks 处理进度任务列表
 * @returns 事件关闭阶段任务表格
 */
const mapTasksToCloseTaskItems = (
  tasks: BatchEventProcessingVocTaskItem[]
): BatchEventProcessingCloseTaskItem[] => {
  return tasks.map(item => ({
    id: item.id,
    taskName: item.taskName,
    description: item.description,
    departmentLabel: item.departmentLabel || '-',
    handlerLabel: item.handlerLabel || '-',
    processTime: item.processTime,
    progressText:
      item.progressText ||
      BatchEventTaskProgressOptions.find(option => option.value === item.progress)?.label ||
      '-'
  }))
}

/**
 * 加载处理进度真实数据。
 */
const loadProgressData = async () => {
  eventConditions.value = {}
  remoteTaskStatus.value = undefined
  emit('task-status-change', '')
  operationLogItems.value = []
  vocTaskItems.value = []
  ccPersonnelItems.value = []
  unmatchedCcPersonnelItems.value = []
  ccPersonnelSelections.value = []
  ccSelectionInitialized.value = false

  if (!eventId.value) return

  const query = { id: eventId.value }
  const [initRes, logRes, ccRes, conditionsRes] = await Promise.allSettled([
    initBatchEvent(query),
    getBatchEventOperationLogs(query),
    getBatchEventCcUserList(query),
    batchEventOptions.loadBatchEventConditionsById(eventId.value)
  ])

  if (initRes.status === 'fulfilled') {
    const initTaskStatus = normalizeInitTaskStatus(initRes.value.result)
    remoteTaskStatus.value = initTaskStatus || undefined
    emit('task-status-change', initTaskStatus)
  }

  if (logRes.status === 'fulfilled' && Array.isArray(logRes.value.result)) {
    operationLogItems.value = mapOperationLogs(logRes.value.result)
  }

  if (ccRes.status === 'fulfilled' && Array.isArray(ccRes.value.result)) {
    ccPersonnelItems.value = mapCcUsers(ccRes.value.result)
  }

  if (conditionsRes.status === 'fulfilled') {
    eventConditions.value = conditionsRes.value || {}
  }

  const taskStage = getBatchEventProcessingStageByTaskStatus(remoteTaskStatus.value)
  if (taskStage === 'handle' || taskStage === 'close') {
    try {
      const taskRes = await getBatchEventTaskList(query)
      if (Array.isArray(taskRes.result)) {
        vocTaskItems.value = mapBatchEventTasks(taskRes.result)
      }
    } catch {
      vocTaskItems.value = []
    }
  }

  syncInitialCcSelections()
}

watch(eventId, () => void loadProgressData(), { immediate: true })

watch(
  () => props.briefData?.handleMode,
  value => {
    const loopMode = mapApiHandleModeToLoopMode(value)
    if (loopMode && loopMode !== currentLoopMode.value) {
      currentLoopMode.value = loopMode
    }
  },
  { immediate: true }
)

watch(
  () => currentLoopMode.value,
  value => {
    emit('loop-mode-change', value)
  }
)

watch(
  () => props.loopMode,
  value => {
    if (value && value !== currentLoopMode.value) {
      currentLoopMode.value = value
    }
  }
)

/**
 * 打开“添加抄送人员”弹窗。
 */
const openAddCcDialog = async () => {
  if (props.readOnly || !hasBatchEventPermission('addCcUser')) {
    return
  }

  await batchEventOptions.loadDepartAccountTree()
  syncInitialCcSelections()
  addCcDialogVisible.value = true
}

/**
 * 判断当前是否允许打开阶段动作弹窗。
 * 阶段动作只能跟随处理进度初始化接口返回状态，状态缺失时不开放入口。
 */
const canOpenStageAction = () => {
  return !props.readOnly && hasInitTaskStatus.value
}

/**
 * 打开“通过审核”确认弹窗。
 */
const openApproveDialog = async () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('approve')) {
    return
  }

  if (!validateEventHandleForm('approve')) {
    return
  }

  currentConfirmDialogAction.value = 'approve'
  confirmDialogVisible.value = true
}

/**
 * 打开“关闭事件”弹窗。
 */
const openCloseDialog = () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('approveClose')) {
    return
  }

  closeDialogVisible.value = true
}

/**
 * 打开闭环阶段的关闭事件确认弹窗。
 */
const openHandleCloseDialog = () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('closeEvent')) {
    return
  }

  handleCloseDialogVisible.value = true
}

/**
 * 打开“新建任务”弹窗。
 */
const openCreateTaskDialog = async () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('createTask')) {
    return
  }

  selectedVocTaskId.value = undefined
  createTaskDialogVisible.value = true
}

/**
 * 打开“转派处理人”弹窗。
 */
const openTransferHandlerDialog = async () => {
  if (!canOpenStageAction()) {
    return
  }

  selectedVocTaskId.value = undefined
  transferHandlerDialogVisible.value = true
}

/**
 * 打开“驳回事件”弹窗。
 */
const openRejectDialog = () => {
  if (!canOpenStageAction()) {
    return
  }

  rejectDialogVisible.value = true
}

/**
 * 打开“确认处理”确认弹窗。
 */
const openConfirmDialog = async () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('confirm')) {
    return
  }

  if (!validateEventHandleForm('confirm')) {
    return
  }

  currentConfirmDialogAction.value = 'confirm'
  confirmDialogVisible.value = true
}

/**
 * 打开“更新进度”确认弹窗。
 */
const openUpdateProgressDialog = () => {
  if (!canOpenStageAction() || !hasBatchEventPermission('closeEvent')) {
    return
  }

  currentConfirmDialogAction.value = 'updateProgress'
  confirmDialogVisible.value = true
}

/**
 * 记录当前选中的 VOC 任务，便于行内弹窗回填与更新。
 * @param taskId VOC 任务 id
 */
const selectVocTask = (taskId?: string) => {
  selectedVocTaskId.value = taskId
}

/**
 * 将业务响应阶段的处理方式作为闭环处理的分支来源统一接管。
 * @param mode 闭环处理方式
 */
const handleLoopModeChange = (mode: BatchEventProcessingLoopMode) => {
  currentLoopMode.value = mode
}

/**
 * 将页面内部闭环模式映射为批量事件接口约定值。
 * @param mode 页面内部处理方式
 * @returns 接口处理方式
 */
const mapLoopModeToApiValue = (mode?: string) => {
  return mode === 'sword-loop' ? 'ZJZ' : 'VOC'
}

/**
 * 获取事件处理表单当前值。
 * @returns 表单值
 */
const getCurrentEventHandleForm = () => {
  return eventHandleRef.value?.getFormState()
}

/**
 * 根据 userId 获取用户姓名。
 * @param userId 用户 ID
 * @returns 用户姓名
 */
const getUserNameById = (userId?: string) => {
  if (!userId) return ''
  return findAccountByUserId(departAccountTree.value, userId)?.userName || ''
}

/**
 * 根据 userId 获取批量事件接口使用的人员模型。
 * @param userId 用户 ID
 * @returns 接口人员模型
 */
const mapUserIdToApiUser = (userId?: string): BatchEventUserModel => {
  const userInfo = findAccountByUserId(departAccountTree.value, userId)

  return {
    orgId: userInfo?.deptId,
    orgNo: userInfo?.deptId,
    orgName: userInfo?.deptName,
    deptName: userInfo?.deptName,
    allFlag: 0,
    userId,
    userEmpNo: userInfo?.employeeId,
    userName: userInfo?.userName
  }
}

/**
 * 校验并生成审核接口需要的业务责任人人员字段。
 * @param userId 业务责任人用户 ID
 * @returns 审核人员字段；未选择或人员信息不完整时返回 null
 */
const buildApproveReviewUserPayload = (userId?: string) => {
  const normalizedUserId = String(userId || '').trim()
  if (!normalizedUserId) {
    ElMessage.warning('请选择业务责任人')
    return null
  }

  const reviewUser = mapUserIdToApiUser(normalizedUserId)
  if (!reviewUser.userName || !reviewUser.userEmpNo) {
    ElMessage.warning('未获取到业务责任人完整信息')
    return null
  }

  return reviewUser
}

/**
 * 根据主责人 userId 组装确认处理接口需要的主责人和直属上级部门字段。
 * @param userId 主责人用户 ID
 * @returns 确认处理主责字段；未匹配到人员或直属上级部门时返回 null
 */
const buildMainRespConfirmPayload = (userId?: string) => {
  const matched = findAccountPathByUserId(departAccountTree.value, userId)
  const account = matched?.account
  const parentDepartment = matched?.path[matched.path.length - 1]
  const mainRespOrgId = parentDepartment?.id || ''
  const mainRespOrgName = parentDepartment?.name || ''

  if (!userId || !account || !mainRespOrgId || !mainRespOrgName) {
    return null
  }

  return {
    mainRespUserId: userId,
    mainRespUserEmpNo: account.employeeId,
    mainRespUserName: account.userName,
    mainRespOrgId,
    mainRespOrgName
  }
}

/**
 * 将非空字符串数组去重后拼接为接口需要的逗号分隔字符串。
 * @param values 原始字符串数组
 * @returns 逗号分隔字符串；空数组返回 undefined 表示不传字段
 */
const joinUniqueValues = (values: Array<string | undefined>) => {
  const normalizedValues = Array.from(
    new Set(values.map(value => String(value || '').trim()).filter(Boolean))
  )

  return normalizedValues.length > 0 ? normalizedValues.join(',') : undefined
}

/**
 * 根据协同人员选择结果生成确认处理接口的协同部门与人员字段。
 * @param userIds 已选协同人员 userId 列表
 * @returns Swagger 新协同字段，空字段不传
 */
const buildCoordinateConfirmFields = (userIds: string[] = []) => {
  const matchedItems = userIds
    .map(userId => findAccountPathByUserId(departAccountTree.value, userId))
    .filter(Boolean) as NonNullable<ReturnType<typeof findAccountPathByUserId>>[]

  const secondDeptIds: Array<string | undefined> = []
  const secondDeptNames: Array<string | undefined> = []
  const thirdDeptIds: Array<string | undefined> = []
  const thirdDeptNames: Array<string | undefined> = []
  const coordinateUserIds: Array<string | undefined> = []
  const coordinateUserEmpNos: Array<string | undefined> = []
  const coordinateUserNames: Array<string | undefined> = []

  matchedItems.forEach(({ account, path }) => {
    const departmentPath = account.deptId
      ? findDepartmentPathInDepartAccountTree(departAccountTree.value, account.deptId)
      : path
    const resolvedPath = departmentPath.length > 0 ? departmentPath : path
    const secondDept = resolvedPath[resolvedPath.length - 2] || resolvedPath[0]
    const thirdDept = resolvedPath[resolvedPath.length - 1]

    secondDeptIds.push(secondDept?.id)
    secondDeptNames.push(account.secondDeptName || secondDept?.name)
    thirdDeptIds.push(thirdDept?.id || account.deptId)
    thirdDeptNames.push(account.thirdDeptName || account.deptName || thirdDept?.name)
    coordinateUserIds.push(account.userId)
    coordinateUserEmpNos.push(account.employeeId)
    coordinateUserNames.push(account.userName)
  })

  return {
    coordinateSecondDeptId: joinUniqueValues(secondDeptIds),
    coordinateSecondDeptName: joinUniqueValues(secondDeptNames),
    coordinateThirdDeptId: joinUniqueValues(thirdDeptIds),
    coordinateThirdDeptName: joinUniqueValues(thirdDeptNames),
    coordinateUserId: joinUniqueValues(coordinateUserIds),
    coordinateUserEmpNo: joinUniqueValues(coordinateUserEmpNos),
    coordinateUserName: joinUniqueValues(coordinateUserNames)
  }
}

/**
 * 根据人员选择结果生成任务接口需要的处理部门和处理人字段。
 * @param userId 处理人 ID
 * @returns 任务接口人员字段
 */
const mapUserIdToTaskAssignee = (
  userId?: string
): Pick<
  BatchEventTaskCreateModel,
  'handleDeptId' | 'handleDeptName' | 'assigneeId' | 'assigneeName'
> => {
  const matched = findAccountPathByUserId(departAccountTree.value, userId)
  const userInfo = matched?.account
  const fallbackDepartment = matched?.path[matched.path.length - 1]

  return {
    handleDeptId: userInfo?.deptId || fallbackDepartment?.id || '',
    handleDeptName: userInfo?.deptName || fallbackDepartment?.name || '',
    assigneeId: userId || '',
    assigneeName: userInfo?.userName || ''
  }
}

/**
 * 校验任务接口需要的处理部门和处理人字段。
 * @param assignee 任务接口人员字段
 * @returns 是否可提交
 */
const validateTaskAssignee = (
  assignee: Pick<
    BatchEventTaskCreateModel,
    'handleDeptId' | 'handleDeptName' | 'assigneeId' | 'assigneeName'
  >
) => {
  if (!assignee.assigneeId || !assignee.assigneeName) {
    ElMessage.warning('请选择处理人员')
    return false
  }

  if (!assignee.handleDeptId || !assignee.handleDeptName) {
    ElMessage.warning('未获取到处理人员所属部门')
    return false
  }

  return true
}

/**
 * 根据任务弹窗表单生成新建/编辑任务接口公共字段。
 * @param formData 任务弹窗表单
 * @returns 任务接口公共字段，返回 null 表示校验未通过
 */
const buildTaskMutationPayload = (
  formData: ProcessingProgressTaskFormData
): Omit<BatchEventTaskCreateModel, 'eventId'> | null => {
  const assignee = mapUserIdToTaskAssignee(formData.departmentValue)
  if (!validateTaskAssignee(assignee)) {
    return null
  }

  return {
    taskName: formData.taskName,
    deptType: mapTaskRoleToApiDeptType(formData.roleValue),
    ...assignee,
    ccUserIds: [],
    taskDesc: formData.description
  }
}

/**
 * 将抄送选择项转换为接口用户模型。
 * @param selectedItems 抄送选择项
 * @returns 接口用户模型
 */
const mapCcSelectionsToApiUsers = (
  selectedItems: ProcessingProgressCcSelectionItem[]
): BatchEventUserModel[] => {
  return selectedItems.map(item => {
    const matched = item.userId
      ? findAccountPathByUserId(departAccountTree.value, item.userId)
      : null
    const path = matched?.account.deptId
      ? findDepartmentPathInDepartAccountTree(departAccountTree.value, matched.account.deptId)
      : matched?.path || []
    const secondDept = path[path.length - 2] || path[0]
    const thirdDept = path[path.length - 1]

    return {
      allFlag: item.allFlag ? 1 : 0,
      userId: item.userId,
      userEmpNo: item.userEmpNo,
      userName: item.userName,
      leve2DeptId: secondDept?.id || item.orgId || '',
      leve2DeptName: secondDept?.name || item.orgName || '',
      leve3DeptId: thirdDept?.id || item.orgId || '',
      leve3DeptName: thirdDept?.name || item.orgName || ''
    }
  })
}

/**
 * 操作成功后统一刷新处理进度数据。
 * @param close 弹窗关闭回调
 * @param message 成功提示
 * @param closeDetail 是否通知外层关闭详情弹窗
 * @description 非关窗操作刷新处理进度后通知父层同步更新事件权限。
 */
const refreshAfterOperation = async (close: () => void, message: string, closeDetail = false) => {
  close()
  ElMessage.success(message)

  if (closeDetail) {
    emit('operation-success')
    return
  }

  await loadProgressData()
  emit('operation-refresh')
}

/**
 * 根据当前处理阶段获取事件处理表单校验阶段。
 * @param action 当前确认动作
 * @returns 校验阶段，返回 null 表示当前动作不需要校验事件处理表单
 */
const getEventHandleValidationStage = (
  action: BatchEventProcessingConfirmActionType
): EventHandleValidationStage | null => {
  if (action === 'updateProgress') {
    return null
  }

  if (currentStage.value === 'approve') {
    return 'approve'
  }

  if (currentStage.value === 'confirm') {
    return 'confirm'
  }

  return null
}

/**
 * 按当前处理阶段校验事件处理表单，避免二次确认后才发现必填缺失。
 * @param action 当前确认动作
 * @returns 是否允许继续提交
 */
const validateEventHandleForm = (action: BatchEventProcessingConfirmActionType) => {
  const validationStage = getEventHandleValidationStage(action)
  if (!validationStage) {
    return true
  }

  const formState = getCurrentEventHandleForm()

  if (validationStage === 'approve' && !formState?.mainOwner) {
    ElMessage.warning('请选择业务责任人')
    return false
  }

  if (validationStage === 'confirm') {
    if (!formState?.mainDepartment) {
      ElMessage.warning('请选择主责部门')
      return false
    }

    if (!formState.confirmHandleMode) {
      ElMessage.warning('请选择处理方式')
      return false
    }

    if (formState.confirmHandleMode === 'sword-loop' && formState.pointIssue.length === 0) {
      ElMessage.warning('请选择观点问题')
      return false
    }
  }

  return true
}

/**
 * 处理闭环处理区的任务操作事件。
 * 任务级新建、编辑、删除与转派统一由对应弹窗确认后提交接口。
 * @param payload 操作事件
 */
const handleEventHandleAction = (payload: {
  action: ProcessingProgressTaskAction
  taskId?: string
}) => {
  if (props.readOnly) {
    return
  }

  const task = vocTaskItems.value.find(item => item.id === payload.taskId) || null
  if (!canRunTaskRowAction(payload.action, task)) {
    return
  }

  selectVocTask(payload.taskId)

  switch (payload.action) {
    case 'update-progress':
      updateTaskProgressDialogVisible.value = true
      break
    case 'transfer-task':
      transferTaskDialogVisible.value = true
      break
    case 'edit-task':
      editTaskDialogVisible.value = true
      break
    case 'delete-task':
      deleteTaskDialogVisible.value = true
      break
    default:
      break
  }
}

/**
 * 新建 VOC 任务。
 * @param formData 弹窗表单数据
 * @param close 关闭弹窗回调
 */
const handleCreateTaskConfirm = async (
  formData: ProcessingProgressTaskFormData,
  close: () => void
) => {
  if (!hasBatchEventPermission('createTask')) {
    return
  }

  if (!eventId.value) {
    close()
    return
  }

  const payload = buildTaskMutationPayload(formData)
  if (!payload) {
    return
  }

  showLoading()
  try {
    await createBatchEventTask({
      eventId: eventId.value,
      ...payload
    })
    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.createTask.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 编辑 VOC 任务。
 * @param formData 弹窗表单数据
 * @param close 关闭弹窗回调
 */
const handleEditTaskConfirm = async (
  formData: ProcessingProgressTaskFormData,
  close: () => void
) => {
  if (!canRunTaskRowAction('edit-task', currentSelectedVocTask.value)) {
    return
  }

  if (!currentSelectedVocTask.value) {
    close()
    return
  }

  const payload = buildTaskMutationPayload(formData)
  if (!payload) {
    return
  }

  showLoading()
  try {
    await editBatchEventTask({
      taskId: currentSelectedVocTask.value.id,
      ...payload
    })
    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.editTask.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 更新任务进度。
 * @param formData 进度表单数据
 * @param close 关闭弹窗回调
 */
const handleUpdateTaskProgressConfirm = async (
  formData: { progress: BatchEventProcessingTaskProgressValue; description: string },
  close: () => void
) => {
  const selectedTask = currentSelectedVocTask.value
  if (!canRunTaskRowAction('update-progress', selectedTask)) {
    return
  }

  if (!selectedTask) {
    close()
    return
  }

  showLoading()
  try {
    await updateBatchEventTaskProgress({
      taskId: selectedTask.id,
      progressStatus: formData.progress,
      progressRemark: formData.description
    } satisfies BatchEventTaskProgressModel)
    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.updateProgress.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 转派单个任务。
 * @param formData 转派表单数据
 * @param close 关闭弹窗回调
 */
const handleTransferTaskConfirm = async (formData: { handler: string }, close: () => void) => {
  const selectedTask = currentSelectedVocTask.value
  if (!canRunTaskRowAction('transfer-task', selectedTask)) {
    return
  }

  if (!selectedTask) {
    close()
    return
  }

  const assignee = mapUserIdToTaskAssignee(formData.handler)
  if (!validateTaskAssignee(assignee)) {
    return
  }

  showLoading()
  try {
    await reassignBatchEventTask({
      taskId: selectedTask.id,
      ...assignee
    } satisfies BatchEventTaskReassignModel)
    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.transferTask.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 批量详情页的“转派处理人”操作。
 * 接口成功后同步回写页面内任务处理人，保证当前弹窗状态一致。
 * @param formData 转派表单数据
 * @param close 关闭弹窗回调
 */
const handleTransferHandlerConfirm = async (formData: { handler: string }, close: () => void) => {
  if (!eventId.value) {
    close()
    return
  }

  showLoading()
  try {
    await reassignBatchEvent({
      id: eventId.value,
      handlerId: formData.handler,
      handlerName: getUserNameById(formData.handler),
      description: ''
    })

    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.transferHandler.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 删除当前选中的 VOC 任务。
 * @param close 关闭弹窗回调
 */
const handleDeleteTaskConfirm = async (close: () => void) => {
  if (!canRunTaskRowAction('delete-task', currentSelectedVocTask.value)) {
    return
  }

  if (!currentSelectedVocTask.value) {
    close()
    return
  }

  showLoading()
  try {
    await deleteBatchEventTask({
      taskId: currentSelectedVocTask.value.id
    })
    await refreshAfterOperation(
      close,
      progressData.handleDialogModules.vocLoop.deleteTask.successMessage
    )
  } finally {
    hideLoading()
  }
}

/**
 * 确认闭环阶段关闭事件。
 * @param close 关闭弹窗回调
 */
const handleHandleCloseConfirm = async (close: () => void) => {
  if (!hasBatchEventPermission('closeEvent')) {
    return
  }

  if (!eventId.value) {
    close()
    return
  }

  showLoading()
  try {
    await handleCompleteBatchEvent({
      id: eventId.value,
      handleResult: '',
      description: ''
    })
    await refreshAfterOperation(close, currentHandleConfirmDialogModule.value.successMessage, true)
  } finally {
    hideLoading()
  }
}

/**
 * 确认添加抄送人员后，回写当前表格并保留原本无法回推到选择器的接口行。
 * @param selectedItems 弹窗确认后的抄送人员选择项
 */
const handleAddCcConfirm = async (selectedItems: ProcessingProgressCcSelectionItem[]) => {
  if (!hasBatchEventPermission('addCcUser')) {
    return
  }

  if (!eventId.value) {
    ccPersonnelSelections.value = dedupeCcSelections(selectedItems)
    const mappedItems = mapCcSelectionsToTableItems(ccPersonnelSelections.value)
    ccPersonnelItems.value = dedupeCcPersonnelItems([
      ...mappedItems,
      ...unmatchedCcPersonnelItems.value
    ])
    ElMessage.success('抄送人员已更新')
    return
  }

  showLoading()
  try {
    await ccBatchEvent({
      id: eventId.value,
      ccUsers: mapCcSelectionsToApiUsers(selectedItems),
      description: ''
    })

    ccPersonnelSelections.value = dedupeCcSelections(selectedItems)
    const mappedItems = mapCcSelectionsToTableItems(ccPersonnelSelections.value)
    ccPersonnelItems.value = dedupeCcPersonnelItems([
      ...mappedItems,
      ...unmatchedCcPersonnelItems.value
    ])
    ElMessage.success('抄送人员已更新')
  } finally {
    hideLoading()
  }
}

/**
 * 关闭事件确认后提交关闭接口。
 * @param formData 关闭事件表单数据
 * @param close 关闭弹窗回调
 */
const handleCloseConfirm = async (
  formData: { closeReason?: string; description: string },
  close: () => void
) => {
  if (!hasBatchEventPermission('approveClose')) {
    return
  }

  if (!eventId.value) {
    close()
    return
  }

  showLoading()
  try {
    await closeBatchEvent({
      id: eventId.value,
      closeReason: formData.closeReason,
      description: formData.description
    })
    await refreshAfterOperation(close, progressData.closeDialogModule.successMessage, true)
  } finally {
    hideLoading()
  }
}

/**
 * 驳回事件确认后提交驳回接口。
 * @param formData 驳回事件表单数据
 * @param close 关闭弹窗回调
 */
const handleRejectConfirm = async (
  formData: { rejectReason: string; description: string },
  close: () => void
) => {
  if (!eventId.value) {
    close()
    return
  }

  showLoading()
  try {
    await rejectBatchEvent({
      id: eventId.value,
      rejectReason: formData.rejectReason,
      description: formData.description
    })
    await refreshAfterOperation(close, progressData.rejectDialogModule.successMessage, true)
  } finally {
    hideLoading()
  }
}

/**
 * 根据当前二次确认动作提交对应处理接口。
 * @param close 关闭弹窗回调
 */
const handleConfirmProcess = async (close: () => void) => {
  if (!eventId.value) {
    close()
    return
  }

  const formState = getCurrentEventHandleForm()
  if (currentConfirmDialogAction.value === 'approve' && !hasBatchEventPermission('approve')) {
    return
  }

  if (currentConfirmDialogAction.value === 'confirm' && !hasBatchEventPermission('confirm')) {
    return
  }

  if (
    currentConfirmDialogAction.value === 'updateProgress' &&
    !hasBatchEventPermission('closeEvent')
  ) {
    return
  }

  if (currentConfirmDialogAction.value === 'approve' && !validateEventHandleForm('approve')) {
    return
  }

  if (currentConfirmDialogAction.value === 'confirm' && !validateEventHandleForm('confirm')) {
    return
  }

  showLoading()
  try {
    if (currentConfirmDialogAction.value === 'approve') {
      const reviewUser = buildApproveReviewUserPayload(formState?.mainOwner)
      if (!reviewUser) {
        return
      }

      const reviewUserDepartments = getAccountDepartmentNamesByUserId(
        departAccountTree.value,
        formState?.mainOwner
      )
      await approveBatchEvent({
        id: eventId.value,
        reviewUserId: formState?.mainOwner,
        reviewUserName: reviewUser.userName || '',
        reviewUserEmpNo: reviewUser.userEmpNo || '',
        secondDeptName: reviewUserDepartments.secondDeptName,
        thirdDeptName: reviewUserDepartments.thirdDeptName,
        ccUsers: [],
        description: formState?.description || ''
      })
    }

    if (currentConfirmDialogAction.value === 'confirm') {
      const mainRespUserId = formState?.mainDepartment || ''
      const mainRespPayload = buildMainRespConfirmPayload(mainRespUserId)
      if (!mainRespPayload) {
        ElMessage.warning('未获取到主责部门信息')
        return
      }
      const handleMode = mapLoopModeToApiValue(
        formState?.confirmHandleMode || currentLoopMode.value
      )
      const isSwordMode = handleMode === 'ZJZ'
      const coordinateFields = buildCoordinateConfirmFields(formState?.cooperationDepartment || [])
      await confirmBatchEvent({
        id: eventId.value,
        ...mainRespPayload,
        handleMode,
        ...coordinateFields,
        custType: isSwordMode ? joinUniqueValues(formState?.userType || []) : undefined,
        usageScenario: isSwordMode
          ? joinUniqueValues(formState?.vehicleScene || [])
          : undefined,
        topicText: isSwordMode ? joinUniqueValues(formState?.pointIssue || []) : undefined,
        pageUrl: isSwordMode
          ? buildBatchEventDetailShareLink(String(eventId.value || ''))
          : undefined,
        description: formState?.description || ''
      })
    }

    if (currentConfirmDialogAction.value === 'updateProgress') {
      await handleCompleteBatchEvent({
        id: eventId.value,
        handleResult: '',
        description: formState?.description || ''
      })
    }

    const successMessage = currentConfirmDialogModule.value.successMessage
    await refreshAfterOperation(close, successMessage, true)
  } finally {
    hideLoading()
  }
}

defineExpose<ProcessingProgressExpose>({
  openApproveDialog,
  openCloseDialog,
  openRejectDialog,
  openConfirmDialog,
  openUpdateProgressDialog,
  openHandleCloseDialog,
  openCreateTaskDialog,
  openTransferHandlerDialog
})

void batchEventOptions.loadConditions()
</script>

<template>
  <div class="processing-progress">
    <ProcessingProgressSteps v-if="hasInitTaskStatus" :steps="currentSteps" />

    <div v-if="hasInitTaskStatus" class="processing-progress__divider"></div>

    <ProcessingProgressSummaryCard
      :title="summaryTitle"
      :priority-tag="priorityTag"
      :business-tag="currentBusinessTag"
      :is-rejected="isRejected"
      :reject-reason="rejectReasonText"
      :summary-items="summaryItems"
      :focus-topics="summaryFocusTopics"
    />

    <div class="processing-progress__divider"></div>

    <ProcessingProgressEventHandle
      v-if="currentEventHandleModule"
      ref="eventHandleRef"
      :module="currentEventHandleModule"
      :current-loop-mode="currentLoopMode"
      :depart-account-tree="departAccountTree"
      :read-only="props.readOnly"
      @update:loop-mode="handleLoopModeChange"
      @handle-action="handleEventHandleAction"
    />

    <div v-if="currentEventHandleModule" class="processing-progress__divider"></div>

    <ProcessingProgressCcPersonnel :items="ccPersonnelItems">
      <template v-if="showAddCcButton" #titleExtra>
        <el-button
          type="primary"
          plain
          class="processing-progress__add-button"
          @click="openAddCcDialog"
        >
          <el-icon><Plus /></el-icon>
          <span>添加</span>
        </el-button>
      </template>
    </ProcessingProgressCcPersonnel>

    <div class="processing-progress__divider"></div>

    <ProcessingProgressOperationLog :logs="operationLogItems" />

    <ProcessingProgressAddCcDialog
      v-model:visible="addCcDialogVisible"
      :selected-items="ccPersonnelSelections"
      :depart-account-tree="departAccountTree"
      @confirm="handleAddCcConfirm"
    />

    <ProcessingProgressCloseDialog
      v-model:visible="closeDialogVisible"
      :module="currentCloseDialogModule"
      @confirm="handleCloseConfirm"
    />

    <ProcessingProgressRejectDialog
      v-model:visible="rejectDialogVisible"
      :module="currentRejectDialogModule"
      @confirm="handleRejectConfirm"
    />

    <ProcessingProgressConfirmDialog
      v-model:visible="confirmDialogVisible"
      :module="currentConfirmDialogModule"
      @confirm="handleConfirmProcess"
    />

    <ProcessingProgressTaskDialog
      v-if="currentHandleStageModule"
      v-model:visible="createTaskDialogVisible"
      :module="progressData.handleDialogModules.vocLoop.createTask"
      :handle-module="{
        ...currentHandleStageModule,
        vocLoop: { ...currentHandleStageModule.vocLoop, tasks: vocTaskItems }
      }"
      :depart-account-tree="departAccountTree"
      @confirm="handleCreateTaskConfirm"
    />

    <ProcessingProgressTaskDialog
      v-if="currentHandleStageModule"
      v-model:visible="editTaskDialogVisible"
      :module="progressData.handleDialogModules.vocLoop.editTask"
      :handle-module="{
        ...currentHandleStageModule,
        vocLoop: { ...currentHandleStageModule.vocLoop, tasks: vocTaskItems }
      }"
      :editing-task="currentSelectedVocTask"
      :depart-account-tree="departAccountTree"
      @confirm="handleEditTaskConfirm"
    />

    <ProcessingProgressTransferDialog
      v-model:visible="transferTaskDialogVisible"
      :module="progressData.handleDialogModules.vocLoop.transferTask"
      :task="currentSelectedVocTask"
      :depart-account-tree="departAccountTree"
      @confirm="handleTransferTaskConfirm"
    />

    <ProcessingProgressTransferDialog
      v-model:visible="transferHandlerDialogVisible"
      :module="currentTransferHandlerDialogModule"
      :depart-account-tree="departAccountTree"
      @confirm="handleTransferHandlerConfirm"
    />

    <ProcessingProgressTaskProgressDialog
      v-model:visible="updateTaskProgressDialogVisible"
      :module="currentUpdateProgressDialogModule"
      :task="currentSelectedVocTask"
      @confirm="handleUpdateTaskProgressConfirm"
    />

    <ProcessingProgressHandleConfirmDialog
      v-model:visible="deleteTaskDialogVisible"
      :module="progressData.handleDialogModules.vocLoop.deleteTask"
      @confirm="handleDeleteTaskConfirm"
    />

    <ProcessingProgressHandleConfirmDialog
      v-model:visible="handleCloseDialogVisible"
      :module="currentHandleConfirmDialogModule"
      @confirm="handleHandleCloseConfirm"
    />
  </div>
</template>

<style lang="scss" scoped>
.processing-progress {
  --processing-progress-border: #ebedf0;
  --processing-progress-text-primary: rgba(0, 0, 0, 0.9);
  --processing-progress-text-secondary: rgba(0, 0, 0, 0.4);
  --processing-progress-accent: #1677ff;
  --processing-progress-chip-bg: #fff;

  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  min-width: 0;
  min-height: 100%;
  box-sizing: border-box;
}

.processing-progress__divider {
  width: 100%;
  border-bottom: 2px dashed #ebedf0;
}

.processing-progress__add-button {
  height: 28px;
  padding: 0 12px;
  border-color: #d8e7ff;
  background: #edf5ff;
  color: #1677ff;
}
</style>
