<script setup lang="ts">
import { ElMessage, type CascaderProps, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import FDialog from '@/components/UI/FDialog/index.vue'
import {
  buildProcessingProgressHandlerCascaderOptions,
  findAccountPathByUserId,
  findDepartmentPathInDepartAccountTree
} from '@/components/Business/EventHandle/BatchEventDetail/components/ProcessingProgress/personnelTree'
import { createBatchEvent } from '@/api/batchEvent'
import type {
  BatchEventConditionsVo,
  BatchEventCreateModel,
  BatchEventDataSourceType,
  BatchEventTopicOption
} from '@/api/batchEvent/types'
import { EVENT_ISSUE_PRIORITY_OPTIONS } from '@/constants'
import useUserStore from '@/store/modules/user'
import { useBatchEventOptions } from '@/views/customerDirectEngage/batchEvent/hooks/useBatchEventOptions'

defineOptions({
  name: 'EventIssueDialog'
})

type EventIssueMode = 'single' | 'batch'
type EventIssuePriority = 'P0' | 'P1' | 'P2' | 'P3' | 'P4'

interface SelectOption {
  label: string
  value: string
}

interface VoiceIssueSelectionItem extends Record<string, any> {
  id?: string | number
  ids?: string | Array<string | number>
  dataId?: string | number
  originalId?: string | number
  title?: string
  mainRespOrgId?: string
}

interface EventIssueFormValue {
  eventName: string
  brandCode: string
  priority: EventIssuePriority
  mainRespOrgId: string
  focusTopics: string[]
  description: string
}

interface EventIssueDialogPayload {
  mode: EventIssueMode
  selectedIds: string[]
  formData: EventIssueFormValue
}

const visible = defineModel<boolean>('visible', {
  default: false
})

const props = withDefaults(
  defineProps<{
    mode?: EventIssueMode
    selection?: VoiceIssueSelectionItem[]
    dataSourceType?: BatchEventDataSourceType
    topicOptions?: BatchEventTopicOption[]
  }>(),
  {
    mode: 'single',
    selection: () => [],
    dataSourceType: 'RESULT',
    topicOptions: () => []
  }
)

const emit = defineEmits<{
  (e: 'success', payload: EventIssueDialogPayload): void
}>()

const batchEventOptions = useBatchEventOptions()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const responseConditions = ref<BatchEventConditionsVo>({})
const priorityOptions = EVENT_ISSUE_PRIORITY_OPTIONS as SelectOption[]

const form = reactive<EventIssueFormValue>({
  eventName: '',
  brandCode: '',
  priority: 'P0',
  mainRespOrgId: '',
  focusTopics: [],
  description: ''
})

const dialogTitle = computed(() => {
  return props.mode === 'batch' ? '批量下发' : '事件下发'
})

/**
 * 解析后端返回的数组 ID 字段，兼容 JSON 字符串、数组和逗号分隔字符串。
 *
 * @param raw 原始 ID 字段
 * @returns 去重后的 ID 列表
 */
const normalizeJsonIdList = (raw: unknown): string[] => {
  const result: string[] = []

  const pushValue = (value: unknown) => {
    if (Array.isArray(value)) {
      value.forEach(pushValue)
      return
    }

    const id = String(value ?? '').trim()
    if (id) result.push(id)
  }

  if (Array.isArray(raw)) {
    pushValue(raw)
  } else if (typeof raw === 'string') {
    const value = raw.trim()
    if (!value) return []

    const tryParseJsonArray = (jsonValue: string) => {
      try {
        const parsed = JSON.parse(jsonValue)
        if (Array.isArray(parsed)) {
          pushValue(parsed)
          return true
        }
      } catch {
        return false
      }

      return false
    }

    if (!tryParseJsonArray(value) && !tryParseJsonArray(value.replace(/,\s*]/g, ']'))) {
      value
        .replace(/^\[/, '')
        .replace(/\]$/, '')
        .split(',')
        .map(id =>
          id
            .trim()
            .replace(/^['"]|['"]$/g, '')
            .trim()
        )
        .forEach(pushValue)
    }
  } else {
    pushValue(raw)
  }

  return [...new Set(result)]
}

/**
 * 从单条选择中提取事件下发 ID 列表。
 * 结果数据使用列表 ids；原始数据优先使用 id，缺失时回退 dataId。
 *
 * @param item 当前选中的原声数据
 * @returns 可用于事件下发的 ID 列表
 */
const getIssueSelectionIds = (item: VoiceIssueSelectionItem) => {
  if (props.dataSourceType !== 'ORIGINAL') {
    return normalizeJsonIdList(item?.ids)
  }

  const id = String(item?.id ?? '').trim()

  return normalizeJsonIdList(id || item?.dataId)
}

/**
 * 统一提取事件下发 ID，并在批量选择中去重。
 *
 * @param selection 当前选中的原声数据
 * @returns 可用于查询下发条件和提交事件的 ID 列表
 */
const normalizeSelectedIds = (selection: VoiceIssueSelectionItem[]): string[] => {
  return [...new Set(selection.flatMap(getIssueSelectionIds))]
}

const normalizedSelectionIds = computed(() => normalizeSelectedIds(props.selection))
const selectedIdParam = computed(() => normalizedSelectionIds.value.join(','))

/**
 * 判断选中项中是否存在缺少下发 ID 的记录。
 *
 * @param selection 当前选中的原声数据
 * @returns 是否存在不可下发记录
 */
const hasMissingIssueIds = (selection: VoiceIssueSelectionItem[]) => {
  return selection.some(item => getIssueSelectionIds(item).length === 0)
}

/**
 * 从当前下发选择中提取渠道名称，按接口要求过滤空值、去重后逗号拼接。
 *
 * @param selection 当前选中的原声数据
 * @returns 事件下发接口需要的渠道名称字符串
 */
const normalizeChannelNames = (selection: VoiceIssueSelectionItem[]): string => {
  const channelNames = selection
    .map(item => String(item?.channel ?? '').trim())
    .filter(channel => Boolean(channel))

  return [...new Set(channelNames)].join(',')
}

const departmentOptions = computed(() => {
  return buildProcessingProgressHandlerCascaderOptions(batchEventOptions.departAccountTree.value)
})

/**
 * 获取当前账号可用的品牌树根节点，直接作为事件下发表单选项使用。
 *
 * @returns 权限接口返回的原始品牌项
 */
const brandOptions = computed(() => userStore.getBrandService || [])

/**
 * 根据选中的品牌编码返回完整选项，保证提交的编码和名称来自同一数据源。
 *
 * @returns 当前选中的品牌；未命中时返回 undefined
 */
const getSelectedBrand = () => {
  return brandOptions.value.find((item: Record<string, unknown>) => item?.key === form.brandCode)
}

/**
 * 归一化事件下发聚焦观点选项，提交值使用 code，展示值使用 name。
 *
 * @param options 原始观点选项
 * @returns el-select-v2 可用选项
 */
const normalizeTopicOptions = (options: BatchEventTopicOption[] = []): SelectOption[] => {
  const optionMap = new Map<string, SelectOption>()

  options
    .map(item => {
      const label = String(item?.name ?? '').trim()
      const value = String(item?.code ?? '').trim()
      if (!label || !value) return null
      return { label, value }
    })
    .filter((item): item is SelectOption => Boolean(item))
    .forEach(item => {
      if (!optionMap.has(item.value)) {
        optionMap.set(item.value, item)
      }
    })

  return [...optionMap.values()]
}

/**
 * 聚焦观点选项：原始数据优先使用结果数据标准观点全量数据源，其他场景沿用条件接口。
 */
const focusTopicOptions = computed<SelectOption[]>(() => {
  if (props.dataSourceType === 'ORIGINAL') {
    return normalizeTopicOptions(props.topicOptions)
  }

  return normalizeTopicOptions(responseConditions.value.topicTextList || [])
})

/**
 * 获取聚焦观点默认选中值。
 * 原始数据场景不默认选中，由用户手动选择；其他场景默认覆盖全部有效观点。
 *
 * @returns 聚焦观点编码列表
 */
const getDefaultFocusTopicValues = () => {
  if (props.dataSourceType === 'ORIGINAL') return []

  return focusTopicOptions.value.map(item => item.value)
}

/**
 * 根据已选聚焦观点编码回填观点名称，确保 create-event 的 code/name 一一对应。
 *
 * @param values 当前表单选中的聚焦观点编码
 * @returns 按选择顺序排列的聚焦观点名称
 */
const getFocusTopicNames = (values: string[]) => {
  const optionMap = new Map(focusTopicOptions.value.map(item => [item.value, item.label]))
  return values.map(value => optionMap.get(value) || '').filter(Boolean)
}

/**
 * 根据主责人员 userId 组装事件下发接口需要的主责人和部门字段。
 *
 * @param userId 级联组件当前选中的人员 userId
 * @returns create-event 主责字段
 */
const getPrimaryFieldsByUserId = (
  userId: string
): Pick<
  BatchEventCreateModel,
  | 'primarySecondDepId'
  | 'primarySecondDepName'
  | 'primaryDepId'
  | 'primaryDepName'
  | 'primaryId'
  | 'primaryEmpNo'
  | 'primaryName'
> => {
  const matched = findAccountPathByUserId(batchEventOptions.departAccountTree.value, userId)
  const account = matched?.account
  const accountDepartmentPath = account?.deptId
    ? findDepartmentPathInDepartAccountTree(
        batchEventOptions.departAccountTree.value,
        account.deptId
      )
    : []
  const departmentPath = accountDepartmentPath.length ? accountDepartmentPath : matched?.path || []
  const lastDepartment = departmentPath[departmentPath.length - 1]
  const secondDepartment = departmentPath[1] || departmentPath[0] || lastDepartment
  const department = departmentPath.find(item => item.id === account?.deptId) || lastDepartment

  return {
    primarySecondDepId: secondDepartment?.id,
    primarySecondDepName: secondDepartment?.name,
    primaryDepId: account?.deptId || department?.id,
    primaryDepName: account?.deptName || department?.name,
    primaryId: account?.userId || userId,
    primaryEmpNo: account?.employeeId,
    primaryName: account?.userName
  }
}

/**
 * 判断当前级联选项中是否存在指定节点值，用于安全回填主责部门。
 *
 * @param value 需要匹配的级联节点值
 * @returns 当前选项中是否存在该值
 */
const hasDepartmentOptionValue = (value: string) => {
  const findValue = (options: Array<Record<string, any>> = []): boolean => {
    return options.some(item => {
      if (String(item?.value ?? '') === value) return true
      return Array.isArray(item?.children) ? findValue(item.children) : false
    })
  }

  return findValue(departmentOptions.value)
}

/**
 * 仅当所有选中原声的主责部门一致时才回填，避免批量场景误带脏值。
 *
 * @returns 可安全回填的主责部门 ID
 */
const getDefaultDepartmentId = () => {
  const defaultIds = props.selection
    .map(item => String(item?.mainRespUserId ?? item?.mainRespOrgId ?? '').trim())
    .filter(deptId => Boolean(deptId))

  if (!defaultIds.length) return ''

  const uniqueDefaultIds = [...new Set(defaultIds)]
  if (uniqueDefaultIds.length !== 1) return ''

  return hasDepartmentOptionValue(uniqueDefaultIds[0]) ? uniqueDefaultIds[0] : ''
}

/**
 * 每次打开弹窗时重置为干净表单，异步条件加载完成后再回填可安全匹配的默认值。
 */
const initializeForm = () => {
  form.eventName = ''
  form.brandCode = ''
  form.priority = 'P0'
  form.mainRespOrgId = ''
  form.focusTopics = []
  form.description = ''
  formRef.value?.clearValidate()
}

/**
 * 按当前选中原声加载事件下发字段依赖，字段来源与批量响应保持一致。
 */
const loadIssueConditions = async () => {
  const ids = [...normalizedSelectionIds.value]
  const idParam = ids.join(',')
  responseConditions.value = {}

  if (!ids.length) {
    return
  }

  const [, conditions] = await Promise.all([
    batchEventOptions.loadDepartAccountTree(),
    batchEventOptions.loadBatchEventConditionsBySoundId(ids, props.dataSourceType)
  ])

  if (!visible.value || selectedIdParam.value !== idParam) {
    return
  }

  responseConditions.value = conditions
  form.mainRespOrgId = getDefaultDepartmentId()
  form.focusTopics = getDefaultFocusTopicValues()
  formRef.value?.clearValidate(['mainRespOrgId', 'focusTopics'])
}

const mainDepartmentCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false
} satisfies CascaderProps

const rules: FormRules<EventIssueFormValue> = {
  eventName: [{ required: true, message: '请输入事件名称', trigger: ['blur', 'change'] }],
  brandCode: [{ required: true, message: '请选择品牌归属', trigger: 'change' }],
  priority: [{ required: true, message: '请选择处理优先级', trigger: 'change' }],
  mainRespOrgId: [{ required: true, message: '请选择主责部门', trigger: 'change' }],
  focusTopics: [
    { required: true, type: 'array', min: 1, message: '请选择聚焦观点', trigger: 'change' }
  ]
}

/**
 * 打开弹窗时才初始化表单，避免异步条件更新反复覆盖用户编辑中的内容。
 */
watch(
  () => visible.value,
  nextVisible => {
    if (nextVisible) {
      initializeForm()
      void loadIssueConditions()
      return
    }

    responseConditions.value = {}
  },
  { immediate: true }
)

watch(selectedIdParam, () => {
  if (visible.value) {
    initializeForm()
    void loadIssueConditions()
  }
})

/**
 * 统一执行前端校验，避免将无效表单透传给父组件。
 *
 * @returns 当前表单是否通过校验
 */
const validateForm = async () => {
  if (!formRef.value) return false

  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

/**
 * 生成事件下发接口参数，聚焦观点编码和名称按选择顺序分别拼接。
 *
 * @param selectedIds 当前选中的原声 ID 列表
 * @returns create-event 请求参数
 */
const createIssuePayload = (selectedIds: string[]): BatchEventCreateModel => {
  const topicValues = [...form.focusTopics]
  const topicNames = getFocusTopicNames(topicValues)
  const selectedBrand = getSelectedBrand()

  return {
    ids: selectedIds.join(','),
    dataSourceType: props.dataSourceType,
    eventName: form.eventName.trim(),
    brandCode: form.brandCode,
    brandName: selectedBrand?.value || '',
    eventPriority: form.priority,
    ...getPrimaryFieldsByUserId(form.mainRespOrgId),
    topic: topicValues.join(','),
    topicName: topicNames.join(','),
    channelNames: normalizeChannelNames(props.selection),
    issueDescription: form.description.trim()
  }
}

/**
 * 表单校验通过后调用批量事件下发接口，成功后通知父组件清理批量态。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!props.selection.length) {
    ElMessage.warning('请选择需要下发的原声')
    return
  }

  if (hasMissingIssueIds(props.selection)) {
    ElMessage.warning('选中的原声存在缺少下发 ID 的记录，无法执行事件下发')
    return
  }

  const submittedIds = [...normalizedSelectionIds.value]

  const isValid = await validateForm()
  if (!isValid) return

  if (!getSelectedBrand()) {
    ElMessage.warning('请选择品牌归属')
    return
  }

  const requestPayload = createIssuePayload(submittedIds)
  await createBatchEvent(requestPayload)

  const payload: EventIssueDialogPayload = {
    mode: props.mode,
    selectedIds: submittedIds,
    formData: {
      eventName: form.eventName.trim(),
      brandCode: form.brandCode,
      priority: form.priority,
      mainRespOrgId: form.mainRespOrgId,
      focusTopics: [...form.focusTopics],
      description: form.description.trim()
    }
  }

  ElMessage.success(props.mode === 'batch' ? '批量下发成功' : '事件下发成功')
  emit('success', payload)
  close()
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="680px"
    destroy-on-close
    :close-on-click-modal="false"
    :confirm="handleConfirm"
  >
    <template #header>
      <span>{{ dialogTitle }}</span>
    </template>

    <div class="event-issue-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent>
        <el-form-item label="事件名称" prop="eventName" required>
          <el-input
            v-model.trim="form.eventName"
            clearable
            maxlength="20"
            show-word-limit
            placeholder="请输入事件名称"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="品牌归属" prop="brandCode" required>
          <el-select-v2
            v-model="form.brandCode"
            :options="brandOptions"
            :props="{ label: 'value', value: 'key' }"
            clearable
            filterable
            placeholder="请选择品牌归属"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="处理优先级" prop="priority" required>
          <OptionToggleGroup v-model="form.priority" :options="priorityOptions" />
        </el-form-item>

        <el-form-item label="主责部门" prop="mainRespOrgId" required>
          <el-cascader
            v-model="form.mainRespOrgId"
            :options="departmentOptions"
            :props="mainDepartmentCascaderProps"
            clearable
            filterable
            separator="#"
            placeholder="请选择主责部门"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="聚焦观点" prop="focusTopics" required>
          <el-select-v2
            v-model="form.focusTopics"
            :options="focusTopicOptions"
            multiple
            clearable
            filterable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="1"
            placeholder="请选择聚焦观点"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="添加说明">
          <el-input
            v-model.trim="form.description"
            type="textarea"
            :rows="4"
            resize="none"
            maxlength="200"
            show-word-limit
            placeholder="请添加说明"
            class="w-full"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.event-issue-dialog {
  min-height: 0;
}

.event-issue-dialog :deep(.el-input__wrapper),
.event-issue-dialog :deep(.el-select__wrapper),
.event-issue-dialog :deep(.el-textarea__inner) {
  min-height: 32px;
}
</style>
