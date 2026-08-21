<script setup lang="ts">
import { computed, reactive, ref, shallowRef, watch } from 'vue'
import { showLoadingToast, showSuccessToast, showToast } from 'vant'
import {
  approveBatchEvent,
  ccBatchEvent,
  closeBatchEvent,
  confirmBatchEvent,
  createBatchEventTask,
  deleteBatchEventTask,
  editBatchEventTask,
  getBatchEventCcUserList,
  getBatchEventConditions,
  getBatchEventOpeLogList,
  getBatchEventPermission,
  getBatchEventTaskList,
  handleCompleteBatchEvent,
  initBatchEvent,
  reassignBatchEvent,
  reassignBatchEventTask,
  rejectBatchEvent,
  updateBatchEventTaskProgress
} from '@h5/api/batchEvent'
import type {
  BatchEventBriefDetailVo,
  BatchEventCcUserVo,
  BatchEventConditionsVo,
  BatchEventOpeLogVo,
  BatchEventOptionVo,
  BatchEventPermissionVo,
  BatchEventTaskCreateModel,
  BatchEventTaskVo,
  BatchEventUserModel
} from '@h5/api/batchEvent/types'
import { useTaskEventStore } from '@h5/store'
import EventInfoPanel from '../statistics/EventInfoPanel.vue'
import BusinessResponseHandleCard from './BusinessResponseHandleCard.vue'
import CarbonCopySection from './CarbonCopySection.vue'
import ClosedLoopHandleCard from './ClosedLoopHandleCard.vue'
import EventClosedHandleCard from './EventClosedHandleCard.vue'
import OperationRecordSection from './OperationRecordSection.vue'
import ProcessActionDialog from './ProcessActionDialog.vue'
import ProcessSectionCard from './ProcessSectionCard.vue'
import ProgressStepsCard from './ProgressStepsCard.vue'
import WarningReviewHandleCard from './WarningReviewHandleCard.vue'
import { buildBatchEventDetailPcShareLink } from '@h5/utils/batchEventDetailShare'
import type {
  BatchBusinessResponseHandleForm,
  BatchBusinessResponseHandleState,
  BatchClosedLoopMode,
  BatchProcessActionDialogMode,
  BatchProcessStage,
  BatchProcessStep,
  BatchProcessSelectOption
} from './types'

defineOptions({
  name: 'BatchEventProcessProgress'
})

const props = defineProps<{
  /** 批量事件 ID */
  eventId?: string | number
  /** 外层已加载的简报数据 */
  detail?: BatchEventBriefDetailVo
}>()
const emit = defineEmits<{
  /** 处理进度操作成功后通知父级刷新详情上下文 */
  (e: 'operation-success'): void
}>()

type DialogFormData = {
  closeReason?: string
  rejectReason?: string
  description?: string
  copyUserIds?: string[]
  taskName?: string
  taskDesc?: string
  deptType?: string
  assigneeId?: string
  progressStatus?: string
  progressRemark?: string
}

type BatchEventPermissionKey = keyof Pick<
  BatchEventPermissionVo,
  | 'approve'
  | 'approveClose'
  | 'closeEvent'
  | 'confirm'
  | 'createTask'
  | 'addCcUser'
  | 'rejectEvent'
  | 'reassignHandler'
>

interface ProcessActionButton {
  label: string
  mode: BatchProcessActionDialogMode
  variant: 'primary' | 'secondary'
}

const taskStatusStageMap: Record<BatchProcessStage, string[]> = {
  warningReview: ['10', '11'],
  businessResponse: ['20'],
  closedLoop: ['30', '40'],
  eventClosed: ['90']
}

const actionPermissionMap: Partial<Record<BatchProcessActionDialogMode, BatchEventPermissionKey>> =
  {
    approve: 'approve',
    close: 'approveClose',
    reject: 'rejectEvent',
    confirm: 'confirm',
    closedLoopClose: 'closeEvent',
    createTask: 'createTask',
    transferHandler: 'reassignHandler',
    copy: 'addCcUser'
  }

const taskEventStore = useTaskEventStore()
const loading = shallowRef(false)
const dialogVisible = shallowRef(false)
const dialogMode = shallowRef<BatchProcessActionDialogMode>('close')
const selectedTask = ref<BatchEventTaskVo | null>(null)
const remoteTaskStatus = shallowRef('')
const briefData = ref<BatchEventBriefDetailVo>({})
const conditions = ref<BatchEventConditionsVo>({})
const taskList = ref<BatchEventTaskVo[]>([])
const ccUserList = ref<BatchEventCcUserVo[]>([])
const operationLogList = ref<BatchEventOpeLogVo[]>([])
const eventPermission = ref<BatchEventPermissionVo>({})
const warningReviewOwnerId = shallowRef('')
const warningReviewOwnerFallbackLabel = shallowRef('')
const warningReviewDescription = shallowRef('')
const businessResponseState = reactive<BatchBusinessResponseHandleState>({
  mainRespUserId: '',
  coordinatingUserIds: [],
  handleMode: 'voc',
  custTypeValues: [],
  usageScenarioValues: [],
  topicTextValues: [],
  description: ''
})
let requestSeq = 0
let permissionRequestSeq = 0
let warningReviewOwnerResolveSeq = 0

const eventId = computed(() => String(props.eventId || '').trim())
const departAccountTree = computed(() => taskEventStore.departAccountTree)
const departAccountTreeLoading = computed(() => taskEventStore.departAccountTreeLoading)
const closeReasonOptions = computed(() =>
  normalizeDictOptions(taskEventStore.allDictItems?.batchEvent_close_reason_type)
)
const rejectReasonOptions = computed(() =>
  normalizeDictOptions(taskEventStore.allDictItems?.batchEvent_reject_reason_type)
)
const progressOptions = computed<BatchEventOptionVo[]>(() => [
  { label: '未开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' }
])

const currentStage = computed<BatchProcessStage>(() => {
  const status = String(remoteTaskStatus.value || '')

  if (taskStatusStageMap.warningReview.includes(status)) return 'warningReview'
  if (taskStatusStageMap.businessResponse.includes(status)) return 'businessResponse'
  if (taskStatusStageMap.closedLoop.includes(status)) return 'closedLoop'
  if (taskStatusStageMap.eventClosed.includes(status)) return 'eventClosed'

  return 'businessResponse'
})

const hasInitTaskStatus = computed(() => Boolean(remoteTaskStatus.value))
const isEventClosed = computed(() => currentStage.value === 'eventClosed')

const displaySteps = computed<BatchProcessStep[]>(() => {
  const definitions: BatchProcessStep[] = [
    { key: 'voiceInsight', label: '声音洞察', status: 'completed' },
    { key: 'eventWarning', label: '事件预警', status: 'completed' },
    { key: 'warningReview', label: '预警审核', status: 'pending', stage: 'warningReview' },
    { key: 'businessResponse', label: '业务响应', status: 'pending', stage: 'businessResponse' },
    { key: 'closedLoop', label: '闭环处理', status: 'pending', stage: 'closedLoop' },
    { key: 'eventClosed', label: '事件关闭', status: 'pending', stage: 'eventClosed' }
  ]

  if (taskStatusStageMap.eventClosed.includes(remoteTaskStatus.value)) {
    return definitions.map(step => ({ ...step, status: 'completed' }))
  }

  const currentIndex = definitions.findIndex(step => step.stage === currentStage.value)

  return definitions.map((step, index) => {
    if (index < 2) return { ...step, status: 'completed' }
    if (currentIndex < 0) return step
    if (index < currentIndex) return { ...step, status: 'completed' }
    if (index === currentIndex) return { ...step, status: 'current' }
    return { ...step, status: 'pending' }
  })
})

const closedLoopMode = computed<BatchClosedLoopMode>(() => {
  const handleMode = String(briefData.value.handleMode || '').toUpperCase()
  return handleMode && !handleMode.includes('VOC') ? 'sword' : 'voc'
})

const businessResponseForm = computed<BatchBusinessResponseHandleForm>(() => ({
  handleModeOptions: [
    { label: 'VOC系统闭环', value: 'voc' },
    { label: '天枢星链系统闭环', value: 'sword' }
  ],
  selectedHandleMode: businessResponseState.handleMode,
  userTypeOptions: toSelectOptions(conditions.value.custTypeList),
  carSceneOptions: toSelectOptions(conditions.value.usageScenarioList),
  focusTopicOptions: toSelectOptions(conditions.value.topicTextList),
  description: businessResponseState.description
}))

const actionButtons = computed<ProcessActionButton[]>(() => {
  let buttons: ProcessActionButton[] = []

  if (!hasInitTaskStatus.value || isEventClosed.value) {
    return []
  }

  if (currentStage.value === 'closedLoop') {
    if (closedLoopMode.value === 'voc') {
      buttons = [
        {
          label: '关闭事件',
          mode: 'closedLoopClose',
          variant: 'secondary'
        },
        {
          label: '新建任务',
          mode: 'createTask',
          variant: 'primary'
        },
        {
          label: '转派处理人',
          mode: 'transferHandler',
          variant: 'primary'
        }
      ]
      return filterActionButtons(buttons)
    }

    buttons = [
      {
        label: '关闭事件',
        mode: 'closedLoopClose',
        variant: 'primary'
      }
    ]
    return filterActionButtons(buttons)
  }

  if (currentStage.value === 'businessResponse') {
    buttons = [
      {
        label: '驳回事件',
        mode: 'reject',
        variant: 'secondary'
      },
      {
        label: '确认处理',
        mode: 'confirm',
        variant: 'primary'
      }
    ]
    return filterActionButtons(buttons)
  }

  buttons = [
    {
      label: '关闭事件',
      mode: 'close',
      variant: 'secondary'
    },
    {
      label: '通过审核',
      mode: 'approve',
      variant: 'primary'
    }
  ]
  return filterActionButtons(buttons)
})

const hasActionButtons = computed(() => actionButtons.value.length > 0)
const canAddCcUser = computed(() => !isEventClosed.value && hasBatchEventPermission('addCcUser'))
const defaultCopyUserIds = computed(() => {
  return Array.from(
    new Set(
      ccUserList.value
        .map(person => resolveCcUserId(person))
        .map(userId => String(userId || '').trim())
        .filter(Boolean)
    )
  )
})

/**
 * 判断当前事件是否具备指定事件级功能权限。
 * @param permissionKey Swagger 权限字段
 * @returns 是否允许展示或触发对应操作
 */
function hasBatchEventPermission(permissionKey: BatchEventPermissionKey) {
  return eventPermission.value?.[permissionKey] === true
}

/**
 * 判断弹窗动作是否通过事件级权限校验；无 Swagger 字段的动作保持原业务判断。
 * @param mode 弹窗类型
 * @returns 是否允许继续操作
 */
function hasActionPermission(mode: BatchProcessActionDialogMode) {
  const permissionKey = actionPermissionMap[mode]
  return !permissionKey || hasBatchEventPermission(permissionKey)
}

/**
 * 按事件级权限过滤底部操作按钮。
 * @param buttons 当前阶段候选按钮
 * @returns 可展示按钮
 */
function filterActionButtons(buttons: ProcessActionButton[]) {
  return buttons.filter(button => hasActionPermission(button.mode))
}

/**
 * 将字典接口项按原字段透传，只补齐 H5 选择器必须读取的 label/value。
 * @param list 接口选项列表
 * @returns 选择器可读取的接口选项
 */
function normalizeDictOptions(list?: any[]): BatchEventOptionVo[] {
  return (Array.isArray(list) ? list : [])
    .map(item => ({
      ...item,
      label: String(item?.label || item?.name || item?.text || item?.value || item?.code || ''),
      value: String(item?.value || item?.code || item?.label || item?.name || item?.text || '')
    }))
    .filter(item => item.label && item.value)
}

/**
 * 将接口选项提取为旧展示组件所需的窄 UI 选项。
 * @param list 接口选项列表
 * @returns UI 选择器选项
 */
function toSelectOptions(list?: any[]): BatchProcessSelectOption[] {
  return (Array.isArray(list) ? list : [])
    .map(item => ({
      label: String(item?.label || item?.name || item?.text || item?.value || item?.code || ''),
      value: String(item?.value || item?.code || item?.label || item?.name || item?.text || ''),
      rawData: item
    }))
    .filter(item => item.label && item.value)
}

/**
 * 重置业务响应表单本地状态。
 * @param mode 接口初始化后的当前闭环方式
 */
function resetBusinessResponseState(mode: BatchClosedLoopMode = closedLoopMode.value) {
  businessResponseState.mainRespUserId = ''
  businessResponseState.coordinatingUserIds = []
  businessResponseState.handleMode = mode
  businessResponseState.custTypeValues = []
  businessResponseState.usageScenarioValues = []
  businessResponseState.topicTextValues = []
  businessResponseState.description = ''
}

/**
 * 将非空字符串数组去重后拼接为接口需要的逗号分隔字符串。
 * @param values 原始字符串数组
 * @returns 逗号分隔字符串；空数组返回 undefined 表示不传字段
 */
function joinUniqueValues(values: Array<string | undefined>) {
  const normalizedValues = Array.from(
    new Set(values.map(value => String(value || '').trim()).filter(Boolean))
  )

  return normalizedValues.length > 0 ? normalizedValues.join(',') : undefined
}

/**
 * 按已选 value 提取选项名称并拼接为接口需要的逗号分隔字符串。
 * @param options 选择器选项
 * @param values 已选值
 * @returns 接口名称逗号分隔字符串字段
 */
function joinSelectedOptionNames(options: BatchProcessSelectOption[], values?: string[]) {
  const selectedValues = new Set((Array.isArray(values) ? values : []).map(String))
  if (selectedValues.size === 0) return undefined

  const selectedNames = options
    .filter(item => selectedValues.has(String(item.value)))
    .map(item => item.label)
    .filter(Boolean)

  return joinUniqueValues(selectedNames)
}

/**
 * 规范化初始化接口任务状态。
 * @param value 接口返回状态
 * @returns 状态字符串
 */
function normalizeTaskStatus(value: unknown) {
  if (value === undefined || value === null) return ''
  return String(value).trim()
}

/**
 * 格式化预警审核业务责任人选择框的兜底展示文案。
 * @param userName 人员姓名
 * @param employeeNo 人员工号
 * @returns 姓名-工号展示文案
 */
function formatWarningReviewOwnerFallbackLabel(userName?: string, employeeNo?: string) {
  const normalizedUserName = String(userName || '').trim()
  const normalizedEmployeeNo = String(employeeNo || '').trim()

  return normalizedUserName
    ? `${normalizedUserName}${normalizedEmployeeNo ? `-${normalizedEmployeeNo}` : ''}`
    : ''
}

/**
 * 从简报接口中读取预警审核默认业务责任人。
 * 业务责任人以 mainRespUser* 为准。
 * @param detail 简报接口数据
 */
async function syncWarningReviewOwnerFromDetail(detail?: BatchEventBriefDetailVo) {
  const seq = ++warningReviewOwnerResolveSeq
  const rawDetail = (detail || {}) as Record<string, any>
  const ownerId = String(
    rawDetail.mainRespUserId || rawDetail.mainRespUser?.userId || rawDetail.mainRespUser?.id || ''
  ).trim()
  const userName = String(
    rawDetail.mainRespUserName || rawDetail.mainRespUser?.userName || rawDetail.mainRespUser?.name || ''
  ).trim()
  const employeeNo = String(
    rawDetail.mainRespUserEmpNo ||
      rawDetail.mainRespUser?.userEmpNo ||
      rawDetail.mainRespUser?.employeeId ||
      ''
  ).trim()

  warningReviewOwnerId.value = ownerId
  warningReviewOwnerFallbackLabel.value = ownerId
    ? formatWarningReviewOwnerFallbackLabel(userName, employeeNo)
    : ''

  if (ownerId || !employeeNo) return

  await taskEventStore.fetchDepartAccountTree()
  if (seq !== warningReviewOwnerResolveSeq || warningReviewOwnerId.value) return

  const matched = findUserInDepartTreeByEmpNo(employeeNo)
  const matchedOwnerId = String(matched?.account?.id || matched?.account?.userId || '').trim()
  if (!matchedOwnerId) return

  const matchedUserName = String(
    userName || matched?.account?.name || matched?.account?.userName || ''
  ).trim()
  const matchedEmployeeNo = String(
    employeeNo ||
      matched?.account?.employeeId ||
      matched?.account?.userEmpNo ||
      matched?.account?.empNo ||
      ''
  ).trim()

  warningReviewOwnerId.value = matchedOwnerId
  warningReviewOwnerFallbackLabel.value = formatWarningReviewOwnerFallbackLabel(
    matchedUserName,
    matchedEmployeeNo
  )
}

/**
 * 按 userId 从部门-人员树中查找人员及所属部门。
 * @param userId 人员 ID
 * @returns 人员与部门字段
 */
function findUserInDepartTree(userId?: string) {
  const targetId = String(userId || '')
  if (!targetId) return null

  const walk = (list: any[] = [], parents: any[] = []): any | null => {
    for (const dept of list) {
      const path = [...parents, dept]
      const accounts = Array.isArray(dept?.account) ? dept.account : []
      const matched = accounts.find(
        (account: any) => String(account?.id || account?.userId || '') === targetId
      )

      if (matched) {
        return {
          account: matched,
          dept,
          path
        }
      }

      const childMatched = walk(Array.isArray(dept?.child) ? dept.child : [], path)
      if (childMatched) return childMatched
    }

    return null
  }

  return walk(departAccountTree.value)
}

/**
 * 按工号从部门-人员树中查找人员及所属部门。
 * @param employeeNo 人员工号
 * @returns 人员与部门字段
 */
function findUserInDepartTreeByEmpNo(employeeNo?: string) {
  const targetEmpNo = String(employeeNo || '').trim()
  if (!targetEmpNo) return null

  const walk = (list: any[] = [], parents: any[] = []): any | null => {
    for (const dept of list) {
      const path = [...parents, dept]
      const accounts = Array.isArray(dept?.account) ? dept.account : []
      const matched = accounts.find((account: any) => {
        const empNo = String(
          account?.employeeId || account?.userEmpNo || account?.empNo || ''
        ).trim()
        return empNo === targetEmpNo
      })

      if (matched) {
        return {
          account: matched,
          dept,
          path
        }
      }

      const childMatched = walk(Array.isArray(dept?.child) ? dept.child : [], path)
      if (childMatched) return childMatched
    }

    return null
  }

  return walk(departAccountTree.value)
}

/**
 * 将抄送列表项解析为人员选择器使用的 userId。
 * @param person 抄送列表接口项
 * @returns 人员 ID
 */
function resolveCcUserId(person?: BatchEventCcUserVo) {
  const rawPerson = (person || {}) as Record<string, any>
  const directUserId = String(rawPerson.nodeUserId || rawPerson.userId || '').trim()
  if (directUserId && findUserInDepartTree(directUserId)) return directUserId

  const employeeNo = String(
    rawPerson.nodeUserEmpNo || rawPerson.userEmpNo || rawPerson.employeeId || ''
  ).trim()
  const matchedByEmpNo = findUserInDepartTreeByEmpNo(employeeNo)
  const userIdByEmpNo = String(
    matchedByEmpNo?.account?.id || matchedByEmpNo?.account?.userId || ''
  ).trim()
  if (userIdByEmpNo) return userIdByEmpNo

  const fallbackId = String(rawPerson.id || '').trim()
  return fallbackId && findUserInDepartTree(fallbackId) ? fallbackId : ''
}

/**
 * 按已有任务接口入参生成处理人字段。
 * @param userId 人员 ID
 * @returns 任务处理人字段
 */
function buildTaskAssigneePayload(
  userId?: string
): Pick<
  BatchEventTaskCreateModel,
  'handleDeptId' | 'handleDeptName' | 'assigneeId' | 'assigneeName'
> | null {
  const matched = findUserInDepartTree(userId)
  const account = matched?.account
  const dept = matched?.dept
  const assigneeId = String(userId || '')
  const assigneeName = String(account?.name || account?.userName || '')
  const handleDeptId = String(dept?.id || account?.deptId || '')
  const handleDeptName = String(dept?.name || account?.deptName || '')

  if (!assigneeId || !assigneeName || !handleDeptId || !handleDeptName) {
    showToast('未获取到处理人员完整信息')
    return null
  }

  return {
    handleDeptId,
    handleDeptName,
    assigneeId,
    assigneeName
  }
}

/**
 * 从人员树中生成批量事件流程接口人员字段。
 * @param userId 人员 ID
 * @returns 人员、当前部门与部门路径
 */
function buildSelectedUserPayload(userId?: string) {
  const matched = findUserInDepartTree(userId)
  const account = matched?.account
  const dept = matched?.dept
  const userIdValue = String(userId || '')
  const userName = String(account?.name || account?.userName || '')
  const userEmpNo = String(account?.employeeId || account?.userEmpNo || '')
  const deptId = String(dept?.id || '')
  const deptName = String(dept?.name || '')

  if (!userIdValue || !userName) {
    return null
  }

  return {
    userId: userIdValue,
    userName,
    userEmpNo,
    deptId,
    deptName,
    path: Array.isArray(matched?.path) ? matched.path : []
  }
}

/**
 * 按移动端批量事件 Swagger 组装抄送人模型。
 * @param userId 人员 ID
 * @returns 抄送人接口模型
 */
function buildBatchEventUserPayload(userId?: string): BatchEventUserModel | null {
  const user = buildSelectedUserPayload(userId)
  if (!user) return null

  const path = user.path
  const level2Dept = path[path.length - 2] || path[0] || {}
  const level3Dept = path[path.length - 1] || {}

  return {
    userId: user.userId,
    userName: user.userName,
    userEmpNo: user.userEmpNo,
    leve2DeptId: String(level2Dept?.id || user.deptId || ''),
    leve2DeptName: String(level2Dept?.name || user.deptName || ''),
    leve3DeptId: String(level3Dept?.id || ''),
    leve3DeptName: String(level3Dept?.name || ''),
    allFlag: 0
  }
}

/**
 * 批量生成接口人员模型，过滤人员树中不存在的 ID。
 * @param userIds 人员 ID 列表
 * @returns 接口人员模型列表
 */
function buildBatchEventUsersPayload(userIds?: string[]) {
  return (Array.isArray(userIds) ? userIds : [])
    .map(userId => buildBatchEventUserPayload(userId))
    .filter((item): item is BatchEventUserModel => Boolean(item))
}

/**
 * 根据协同人员选择结果生成确认处理接口的协同部门与人员字段。
 * @param userIds 已选协同人员 userId 列表
 * @returns Swagger 新协同字段，空字段不传
 */
function buildCoordinateConfirmFields(userIds?: string[]) {
  const selectedUsers = (Array.isArray(userIds) ? userIds : [])
    .map(userId => buildSelectedUserPayload(userId))
    .filter(Boolean) as NonNullable<ReturnType<typeof buildSelectedUserPayload>>[]

  const secondDeptIds: Array<string | undefined> = []
  const secondDeptNames: Array<string | undefined> = []
  const thirdDeptIds: Array<string | undefined> = []
  const thirdDeptNames: Array<string | undefined> = []
  const coordinateUserIds: Array<string | undefined> = []
  const coordinateUserEmpNos: Array<string | undefined> = []
  const coordinateUserNames: Array<string | undefined> = []

  selectedUsers.forEach(user => {
    const path = Array.isArray(user.path) ? user.path : []
    const secondDept = path[path.length - 2] || path[0]
    const thirdDept = path[path.length - 1]

    secondDeptIds.push(String(secondDept?.id || '').trim() || undefined)
    secondDeptNames.push(String(secondDept?.name || user.deptName || '').trim() || undefined)
    thirdDeptIds.push(String(thirdDept?.id || user.deptId || '').trim() || undefined)
    thirdDeptNames.push(String(thirdDept?.name || user.deptName || '').trim() || undefined)
    coordinateUserIds.push(user.userId)
    coordinateUserEmpNos.push(user.userEmpNo)
    coordinateUserNames.push(user.userName)
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
 * 加载当前用户在批量事件中的功能权限。
 * @param id 批量事件 ID
 */
async function loadEventPermission(id = eventId.value) {
  const targetId = String(id || '').trim()
  const seq = ++permissionRequestSeq
  eventPermission.value = {}

  if (!targetId) return

  try {
    const response = await getBatchEventPermission({ eventId: targetId })
    if (seq !== permissionRequestSeq || targetId !== eventId.value) return

    eventPermission.value = response.result || {}
  } catch (error) {
    if (seq === permissionRequestSeq && targetId === eventId.value) {
      eventPermission.value = {}
    }
    console.error('获取批量事件权限失败:', error)
  }
}

/**
 * 加载处理进度数据。
 */
async function loadProgressData() {
  const id = eventId.value
  const seq = ++requestSeq

  briefData.value = props.detail || {}
  remoteTaskStatus.value = ''
  conditions.value = {}
  taskList.value = []
  ccUserList.value = []
  operationLogList.value = []
  eventPermission.value = {}
  resetBusinessResponseState()

  if (!id) return

  loading.value = true
  try {
    const [initRes, taskRes, ccRes, logRes, conditionsRes] = await Promise.allSettled([
      initBatchEvent({ id }),
      getBatchEventTaskList({ id }),
      getBatchEventCcUserList({ id }),
      getBatchEventOpeLogList({ id }),
      getBatchEventConditions({ ids: [id] })
    ])

    if (seq !== requestSeq) return

    if (initRes.status === 'fulfilled') {
      remoteTaskStatus.value = normalizeTaskStatus(initRes.value.result)
    }

    if (taskRes.status === 'fulfilled' && Array.isArray(taskRes.value.result)) {
      taskList.value = taskRes.value.result
    }

    if (ccRes.status === 'fulfilled' && Array.isArray(ccRes.value.result)) {
      ccUserList.value = ccRes.value.result
    }

    if (logRes.status === 'fulfilled' && Array.isArray(logRes.value.result)) {
      operationLogList.value = logRes.value.result
    }

    if (conditionsRes.status === 'fulfilled' && conditionsRes.value.result) {
      conditions.value = conditionsRes.value.result
    }

    await loadEventPermission(id)
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

/**
 * 打开指定业务动作弹窗。
 * @param mode 弹窗类型
 * @param task 当前任务
 */
async function openActionDialog(mode: BatchProcessActionDialogMode, task?: BatchEventTaskVo) {
  if (isEventClosed.value) {
    showToast('事件已关闭，不可操作')
    return
  }

  if (!hasActionPermission(mode)) {
    showToast('暂无操作权限')
    return
  }

  if (mode === 'approve' && !(await validateWarningReviewOwnerBeforeApprove())) {
    return
  }

  selectedTask.value = task || null
  dialogMode.value = mode

  if (['copy', 'createTask', 'editTask', 'transferTask', 'transferHandler'].includes(mode)) {
    await taskEventStore.fetchDepartAccountTree()
  }

  dialogVisible.value = true
}

/**
 * 按需加载人员树。
 */
async function handleOpenPersonnelSelect() {
  await taskEventStore.fetchDepartAccountTree()
}

/**
 * 审核弹窗打开前校验业务责任人，避免二次确认后才提示必填或人员信息缺失。
 * @returns 是否允许继续打开审核确认弹窗
 */
async function validateWarningReviewOwnerBeforeApprove() {
  if (!warningReviewOwnerId.value) {
    showToast('请选择业务责任人')
    return false
  }

  await taskEventStore.fetchDepartAccountTree()

  if (!buildSelectedUserPayload(warningReviewOwnerId.value)) {
    showToast('未获取到业务责任人完整信息')
    return false
  }

  return true
}

/**
 * 根据弹窗类型决定打开方式。
 * @param mode 弹窗类型
 */
function handleFooterAction(mode: BatchProcessActionDialogMode) {
  if (isEventClosed.value) {
    showToast('事件已关闭，不可操作')
    return
  }

  if (!hasActionPermission(mode)) {
    showToast('暂无操作权限')
    return
  }

  void openActionDialog(mode)
}

/**
 * 已有接口动作提交后统一刷新。
 * @param message 成功提示
 * @param notifyParent 是否通知父级同步刷新详情上下文
 */
async function refreshAfterSuccess(message: string, notifyParent = false) {
  showSuccessToast(message)
  await loadProgressData()

  if (notifyParent) {
    emit('operation-success')
  }
}

/**
 * 校验并生成通过审核接口入参中的业务责任人字段。
 * @returns 业务责任人人员字段
 */
function getReviewUserPayload() {
  if (!warningReviewOwnerId.value) {
    showToast('请选择业务责任人')
    return null
  }

  const reviewUser = buildSelectedUserPayload(warningReviewOwnerId.value)
  if (!reviewUser) {
    showToast('未获取到业务责任人完整信息')
    return null
  }

  return reviewUser
}

/**
 * 校验并生成确认处理接口入参中的主责人字段。
 * @returns 主责人人员字段
 */
function getMainRespUserPayload() {
  if (!businessResponseState.mainRespUserId) {
    showToast('请选择主责部门')
    return null
  }

  if (!businessResponseState.handleMode) {
    showToast('请选择处理方式')
    return null
  }

  if (
    businessResponseState.handleMode === 'sword' &&
    businessResponseState.topicTextValues.length === 0
  ) {
    showToast('请选择聚焦观点')
    return null
  }

  const mainRespUser = buildSelectedUserPayload(businessResponseState.mainRespUserId)
  if (!mainRespUser) {
    showToast('未获取到主责人完整信息')
    return null
  }

  if (!mainRespUser.deptId || !mainRespUser.deptName) {
    showToast('未获取到主责部门信息')
    return null
  }

  return mainRespUser
}

/**
 * 将 H5 处理方式状态转换为接口枚举值。
 * @param mode H5 处理方式状态
 * @returns 接口处理方式
 */
function getHandleModeApiValue(mode: BatchClosedLoopMode) {
  return mode === 'sword' ? 'ZJZ' : 'VOC'
}

/**
 * 根据处理方式决定是否需要向后端传入页面 URL。
 * @param mode 闭环处理方式
 * @returns 天枢星链闭环时返回 PC 批量事件详情链接，否则不传
 */
function getPageUrlByHandleMode(mode: BatchClosedLoopMode) {
  return mode === 'sword' ? buildBatchEventDetailPcShareLink(eventId.value) : undefined
}

/**
 * 提交已有接口动作。缺接口动作在弹窗内提示后不会走到这里。
 * @param mode 弹窗类型
 * @param formData 表单值
 */
async function handleDialogConfirm(mode: BatchProcessActionDialogMode, formData: DialogFormData) {
  if (!eventId.value) return

  if (isEventClosed.value) {
    showToast('事件已关闭，不可操作')
    dialogVisible.value = false
    selectedTask.value = null
    return
  }

  if (!hasActionPermission(mode)) {
    showToast('暂无操作权限')
    dialogVisible.value = false
    selectedTask.value = null
    return
  }

  const toast = showLoadingToast({
    message: '提交中...',
    forbidClick: true,
    duration: 0
  })

  try {
    if (mode === 'approve') {
      const reviewUser = getReviewUserPayload()
      if (!reviewUser) return

      await approveBatchEvent({
        id: eventId.value,
        reviewUserId: reviewUser.userId,
        reviewUserEmpNo: reviewUser.userEmpNo,
        reviewUserName: reviewUser.userName,
        ccUsers: [],
        description: warningReviewDescription.value.trim()
      })
      await refreshAfterSuccess('审核已通过')
      return
    }

    if (mode === 'confirm') {
      const mainRespUser = getMainRespUserPayload()
      if (!mainRespUser) return
      const coordinateFields = buildCoordinateConfirmFields(
        businessResponseState.coordinatingUserIds
      )

      await confirmBatchEvent({
        id: eventId.value,
        mainRespUserId: mainRespUser.userId,
        mainRespUserName: mainRespUser.userName,
        mainRespUserEmpNo: mainRespUser.userEmpNo,
        mainRespOrgId: mainRespUser.deptId,
        mainRespOrgName: mainRespUser.deptName,
        handleMode: getHandleModeApiValue(businessResponseState.handleMode),
        ...coordinateFields,
        custType: joinSelectedOptionNames(
          businessResponseForm.value.userTypeOptions,
          businessResponseState.custTypeValues
        ),
        usageScenario: joinSelectedOptionNames(
          businessResponseForm.value.carSceneOptions,
          businessResponseState.usageScenarioValues
        ),
        topicText: joinSelectedOptionNames(
          businessResponseForm.value.focusTopicOptions,
          businessResponseState.topicTextValues
        ),
        pageUrl: getPageUrlByHandleMode(businessResponseState.handleMode),
        description: businessResponseState.description.trim()
      })
      await refreshAfterSuccess('处理已确认')
      return
    }

    if (mode === 'copy') {
      const ccUsers = buildBatchEventUsersPayload(formData.copyUserIds)
      if (!ccUsers.length) {
        showToast('未获取到抄送人员完整信息')
        return
      }

      await ccBatchEvent({
        id: eventId.value,
        ccUsers,
        description: ''
      })
      await refreshAfterSuccess('抄送人员已更新')
      return
    }

    if (mode === 'transferHandler') {
      const handler = buildSelectedUserPayload(formData.assigneeId)
      if (!handler) {
        showToast('未获取到处理人员完整信息')
        return
      }

      await reassignBatchEvent({
        id: eventId.value,
        handlerId: handler.userId,
        handlerName: handler.userName,
        description: formData.description
      })
      await refreshAfterSuccess('处理人已转派', true)
      return
    }

    if (mode === 'closedLoopClose') {
      await handleCompleteBatchEvent({
        id: eventId.value,
        handleResult: '',
        description: formData.description
      })
      await refreshAfterSuccess('事件已关闭')
      return
    }

    if (mode === 'close') {
      await closeBatchEvent({
        id: eventId.value,
        closeReason: formData.closeReason,
        description: formData.description
      })
      await refreshAfterSuccess('事件已关闭')
      return
    }

    if (mode === 'reject') {
      await rejectBatchEvent({
        id: eventId.value,
        rejectReason: formData.rejectReason,
        description: formData.description
      })
      await refreshAfterSuccess('事件已驳回')
      return
    }

    if (mode === 'createTask') {
      const assignee = buildTaskAssigneePayload(formData.assigneeId)
      if (!assignee) return

      await createBatchEventTask({
        eventId: eventId.value,
        taskName: formData.taskName,
        deptType: formData.deptType,
        ...assignee,
        ccUserIds: [],
        taskDesc: formData.taskDesc
      })
      await refreshAfterSuccess('任务已新建')
      return
    }

    if (mode === 'editTask' && selectedTask.value?.taskId) {
      const assignee = buildTaskAssigneePayload(formData.assigneeId)
      if (!assignee) return

      await editBatchEventTask({
        taskId: selectedTask.value.taskId,
        taskName: formData.taskName,
        deptType: formData.deptType,
        ...assignee,
        ccUserIds: [],
        taskDesc: formData.taskDesc
      })
      await refreshAfterSuccess('任务已更新')
      return
    }

    if (mode === 'deleteTask' && selectedTask.value?.taskId) {
      await deleteBatchEventTask({
        taskId: selectedTask.value.taskId
      })
      await refreshAfterSuccess('任务已删除')
      return
    }

    if (mode === 'transferTask' && selectedTask.value?.taskId) {
      const assignee = buildTaskAssigneePayload(formData.assigneeId)
      if (!assignee) return

      await reassignBatchEventTask({
        taskId: selectedTask.value.taskId,
        ...assignee
      })
      await refreshAfterSuccess('任务已转派')
      return
    }

    if (mode === 'updateProgress' && selectedTask.value?.taskId) {
      await updateBatchEventTaskProgress({
        taskId: selectedTask.value.taskId,
        progressStatus: formData.progressStatus,
        progressRemark: formData.progressRemark
      })
      await refreshAfterSuccess('任务进度已更新')
    }
  } finally {
    toast.close()
  }
}

watch(
  () => props.detail,
  value => {
    briefData.value = value || {}
    syncWarningReviewOwnerFromDetail(value)
  },
  { immediate: true }
)

watch(
  eventId,
  () => {
    void loadProgressData()
  },
  { immediate: true }
)

watch(isEventClosed, closed => {
  if (!closed) return
  dialogVisible.value = false
  selectedTask.value = null
})

void taskEventStore.fetchSysAllDictItems()
</script>

<template>
  <div class="batch-event-process-progress" :class="{ 'has-action-bar': hasActionButtons }">
    <van-loading v-if="loading" class="process-loading" size="24px" color="#1677FF">
      加载中...
    </van-loading>

    <template v-else>
      <ProgressStepsCard v-if="hasInitTaskStatus" :steps="displaySteps" />

      <ProcessSectionCard class="process-card-gap" title="事件详情" collapsible>
        <EventInfoPanel :data="briefData" format-main-resp-user-with-emp-no />
      </ProcessSectionCard>

      <BusinessResponseHandleCard
        v-if="currentStage === 'businessResponse'"
        class="process-card-gap process-card-gap--after-more"
        :model-value="businessResponseState"
        :form="businessResponseForm"
        :depart-account-tree="departAccountTree"
        :depart-account-tree-loading="departAccountTreeLoading"
        @open-personnel-select="handleOpenPersonnelSelect"
      />
      <ClosedLoopHandleCard
        v-else-if="currentStage === 'closedLoop'"
        class="process-card-gap process-card-gap--after-more"
        :mode="closedLoopMode"
        :tasks="taskList"
        :read-only="isEventClosed"
        @action="openActionDialog"
      />
      <EventClosedHandleCard
        v-else-if="currentStage === 'eventClosed'"
        class="process-card-gap process-card-gap--after-more"
        :tasks="taskList"
      />
      <WarningReviewHandleCard
        v-else
        class="process-card-gap process-card-gap--after-more"
        v-model:owner-id="warningReviewOwnerId"
        v-model:description="warningReviewDescription"
        :owner-fallback-label="warningReviewOwnerFallbackLabel"
        :depart-account-tree="departAccountTree"
        :depart-account-tree-loading="departAccountTreeLoading"
        @open-owner-select="handleOpenPersonnelSelect"
      />

      <CarbonCopySection
        class="process-card-gap"
        :persons="ccUserList"
        :show-add="canAddCcUser"
        @add="openActionDialog('copy')"
      />

      <OperationRecordSection class="process-card-gap" :records="operationLogList" />

      <div v-if="hasActionButtons" class="process-action-bar">
        <button
          v-for="button in actionButtons"
          :key="button.mode"
          class="process-action-button"
          :class="`process-action-button--${button.variant}`"
          type="button"
          @click="handleFooterAction(button.mode)"
        >
          {{ button.label }}
        </button>
      </div>
    </template>

    <ProcessActionDialog
      v-model:show="dialogVisible"
      :mode="dialogMode"
      :close-reason-options="closeReasonOptions"
      :reject-reason-options="rejectReasonOptions"
      :progress-options="progressOptions"
      :depart-account-tree="departAccountTree"
      :depart-account-tree-loading="departAccountTreeLoading"
      :default-copy-user-ids="defaultCopyUserIds"
      :task="selectedTask"
      @confirm="handleDialogConfirm"
    />
  </div>
</template>

<style scoped lang="scss">
.batch-event-process-progress {
  min-height: 240px;
  padding: 12px 12px 16px;
  background: #f5f7fb;
}

.batch-event-process-progress.has-action-bar {
  padding-bottom: 76px;
}

.process-loading {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.process-card-gap {
  margin-top: 12px;
}

.process-card-gap--after-more {
  margin-top: 24px;
}

.process-action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  background: #ffffff;
  display: flex;
  align-items: center;
  column-gap: 16px;
  box-shadow: 0 -4px 12px rgba(31, 39, 51, 0.04);
}

.process-action-button {
  flex: 1;
  height: 36px;
  border: 0;
  border-radius: 4px;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
}

.process-action-button--secondary {
  background: #f2f3f5;
  color: #5f6a7a;
}

.process-action-button--primary {
  background: #1677ff;
  color: #ffffff;
}
</style>
