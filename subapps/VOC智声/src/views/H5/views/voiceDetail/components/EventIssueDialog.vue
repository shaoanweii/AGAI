<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { showLoadingToast, showSuccessToast, showToast } from 'vant'
import HDialog from '@h5/components/UI/HDialog'
import HListSingleSelect from '@h5/components/UI/HListSingleSelect'
import HFilterMultiSelect from '@h5/views/taskEvent/components/HFilterMultiSelect.vue'
import HFilterPersonnelSelect from '@h5/views/taskEvent/components/HFilterPersonnelSelect.vue'
import { usePermissionsStore, useTaskEventStore } from '@h5/store'
import { createBatchEvent, getBatchEventConditions } from '@h5/api/batchEvent'
import type { BatchEventConditionsVo, BatchEventCreateModel } from '@h5/api/batchEvent'
import { EVENT_ISSUE_PRIORITY_OPTIONS } from '@/constants'

defineOptions({
  name: 'H5VoiceEventIssueDialog'
})

type EventIssuePriority = 'P0' | 'P1' | 'P2' | 'P3' | 'P4'

interface SelectOption {
  label: string
  value: string
}

interface VoiceEventIssuePayload {
  selectedIds: string[]
  formData: {
    eventName: string
    brandCode: string
    priority: EventIssuePriority
    mainRespUserId: string
    focusTopics: string[]
    description: string
  }
}

const visible = defineModel<boolean>('visible', {
  default: false
})

const props = withDefaults(
  defineProps<{
    issueIds?: string[]
    channelName?: string
  }>(),
  {
    issueIds: () => [],
    channelName: ''
  }
)

const emit = defineEmits<{
  (e: 'success', payload: VoiceEventIssuePayload): void
}>()

const taskEventStore = useTaskEventStore()
const permissionsStore = usePermissionsStore()
const responseConditions = ref<BatchEventConditionsVo>({})
const loadingConditions = ref(false)
const requestSeq = ref(0)

const form = reactive({
  eventName: '',
  brandCode: '',
  priority: 'P0' as EventIssuePriority,
  mainRespUserIds: [] as string[],
  focusTopics: [] as string[],
  description: ''
})

const departAccountTree = computed(() => taskEventStore.departAccountTree)
const departAccountTreeLoading = computed(() => taskEventStore.departAccountTreeLoading)
const brandOptions = computed(() => permissionsStore.brands?.details || [])

const focusTopicOptions = computed<SelectOption[]>(() => {
  return (responseConditions.value.topicTextList || [])
    .map(item => {
      const label = String(item?.name ?? item?.label ?? '').trim()
      const value = String(item?.code ?? item?.value ?? '').trim()
      if (!label || !value) return null
      return { label, value }
    })
    .filter((item): item is SelectOption => Boolean(item))
})

/**
 * 获取聚焦观点默认选中值，默认覆盖当前原声可用的全部有效观点。
 * @returns 聚焦观点编码列表
 */
const getDefaultFocusTopicValues = () => {
  return focusTopicOptions.value.map(item => item.value)
}

/**
 * 重置事件下发表单，确保每次打开弹窗都是干净状态。
 */
const resetForm = () => {
  form.eventName = ''
  form.brandCode = ''
  form.priority = 'P0'
  form.mainRespUserIds = []
  form.focusTopics = []
  form.description = ''
}

/**
 * 获取详情 soundslist 中全部有效下发 ID，保持接口入参字段类型不变。
 * @returns 去重后的原声结果 ID 列表
 */
const getIssueIds = () => {
  const ids = Array.isArray(props.issueIds) ? props.issueIds : []
  return [...new Set(ids.map(item => String(item || '').trim()).filter(Boolean))]
}

/**
 * 按事件下发 ID 集合拉取 H5 事件下发依赖条件。
 */
const loadIssueConditions = async () => {
  const issueIds = getIssueIds()
  const seq = ++requestSeq.value
  responseConditions.value = {}

  if (!issueIds.length) return

  loadingConditions.value = true
  try {
    const [conditionsRes] = await Promise.all([
      getBatchEventConditions({ soundIds: issueIds }),
      taskEventStore.fetchDepartAccountTree(),
      permissionsStore.initUserPermissions()
    ])

    if (seq !== requestSeq.value || !visible.value) return
    responseConditions.value = conditionsRes.result || {}
    form.focusTopics = getDefaultFocusTopicValues()
  } finally {
    if (seq === requestSeq.value) {
      loadingConditions.value = false
    }
  }
}

/**
 * 在部门人员树中按 userId 查找主责人员和所属部门。
 * @param userId 主责人员 ID
 * @returns 命中的人员、部门与部门路径
 */
const findUserInDepartTree = (userId?: string) => {
  const targetId = String(userId || '').trim()
  if (!targetId) return null

  const walk = (list: any[] = [], parents: any[] = []): any | null => {
    for (const dept of list) {
      const path = [...parents, dept]
      const accounts = Array.isArray(dept?.account) ? dept.account : []
      const matched = accounts.find((account: any) => {
        return String(account?.id || account?.userId || '').trim() === targetId
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
 * 根据选中的主责人员生成 create-event 所需主责字段。
 * @param userId 主责人员 ID
 * @returns 主责人员与部门字段
 */
const buildPrimaryPayload = (
  userId?: string
): Pick<
  BatchEventCreateModel,
  | 'primarySecondDepId'
  | 'primarySecondDepName'
  | 'primaryDepId'
  | 'primaryDepName'
  | 'primaryId'
  | 'primaryEmpNo'
  | 'primaryName'
> | null => {
  const matched = findUserInDepartTree(userId)
  const account = matched?.account
  const dept = matched?.dept
  const departmentPath = Array.isArray(matched?.path) ? matched.path : []
  const currentDepartment =
    departmentPath.find(
      (item: any) => String(item?.id || '').trim() === String(account?.deptId || '').trim()
    ) ||
    departmentPath[departmentPath.length - 1] ||
    dept
  const secondDepartment = departmentPath[1] || departmentPath[0] || currentDepartment
  const primaryId = String(userId || '').trim()
  const primaryName = String(account?.name || account?.userName || '').trim()
  const primaryEmpNo = String(account?.employeeId || account?.userEmpNo || '').trim()
  const primarySecondDepId = String(secondDepartment?.id || '').trim()
  const primarySecondDepName = String(secondDepartment?.name || '').trim()
  const primaryDepId = String(account?.deptId || currentDepartment?.id || '').trim()
  const primaryDepName = String(account?.deptName || currentDepartment?.name || '').trim()

  if (
    !primarySecondDepId ||
    !primarySecondDepName ||
    !primaryId ||
    !primaryName ||
    !primaryDepId ||
    !primaryDepName
  ) {
    return null
  }

  return {
    primarySecondDepId,
    primarySecondDepName,
    primaryDepId,
    primaryDepName,
    primaryId,
    primaryEmpNo,
    primaryName
  }
}

/**
 * 获取选中聚焦观点的名称，保证编码和名称按同一顺序提交。
 * @param values 已选择的聚焦观点编码
 * @returns 聚焦观点名称列表
 */
const getFocusTopicNames = (values: string[]) => {
  const optionMap = new Map(focusTopicOptions.value.map(item => [item.value, item.label]))
  return values.map(value => optionMap.get(value) || '').filter(Boolean)
}

/**
 * 根据当前单选的品牌编码获取完整品牌信息，保证请求中的编码和名称一致。
 *
 * @returns 当前选中的品牌；未命中时返回 undefined
 */
const getSelectedBrand = () => {
  return brandOptions.value.find(item => item?.key === form.brandCode)
}

/**
 * 校验事件下发表单。
 * @returns 是否通过校验
 */
const validateForm = () => {
  if (!getIssueIds().length) {
    showToast('当前原声缺少 id，无法执行事件下发')
    return false
  }

  if (!form.eventName.trim()) {
    showToast('请输入事件名称')
    return false
  }

  if (!getSelectedBrand()) {
    showToast('请选择品牌归属')
    return false
  }

  if (!form.priority) {
    showToast('请选择处理优先级')
    return false
  }

  if (!form.mainRespUserIds.length) {
    showToast('请选择主责部门')
    return false
  }

  if (!form.focusTopics.length) {
    showToast('请选择聚焦观点')
    return false
  }

  return true
}

/**
 * 生成 H5 事件下发接口入参。
 * @returns create-event 请求体
 */
const createIssuePayload = async (): Promise<BatchEventCreateModel | null> => {
  if (!departAccountTree.value.length) {
    await taskEventStore.fetchDepartAccountTree()
  }

  const primaryPayload = buildPrimaryPayload(form.mainRespUserIds[0])
  if (!primaryPayload) {
    showToast('未获取到主责部门完整信息')
    return null
  }

  const topicValues = [...form.focusTopics]
  const topicNames = getFocusTopicNames(topicValues)
  const issueIds = getIssueIds()
  const selectedBrand = getSelectedBrand()

  if (!selectedBrand) {
    showToast('请选择品牌归属')
    return null
  }

  return {
    ids: issueIds.join(','),
    dataSourceType: 'RESULT',
    eventName: form.eventName.trim(),
    brandCode: selectedBrand.key,
    brandName: selectedBrand.value,
    eventPriority: form.priority,
    ...primaryPayload,
    topic: topicValues.join(','),
    topicName: topicNames.join(','),
    channelNames: String(props.channelName || '').trim(),
    issueDescription: form.description.trim()
  }
}

/**
 * 提交事件下发。
 * @param close 关闭弹窗回调
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!validateForm()) return

  const requestPayload = await createIssuePayload()
  if (!requestPayload) return

  const toast = showLoadingToast({
    message: '提交中...',
    forbidClick: true,
    duration: 0
  })

  try {
    await createBatchEvent(requestPayload)
    showSuccessToast('事件下发成功')
    emit('success', {
      selectedIds: getIssueIds(),
      formData: {
        eventName: form.eventName.trim(),
        brandCode: getSelectedBrand()?.key || '',
        priority: form.priority,
        mainRespUserId: form.mainRespUserIds[0] || '',
        focusTopics: [...form.focusTopics],
        description: form.description.trim()
      }
    })
    close()
  } finally {
    toast.close()
  }
}

watch(
  () => visible.value,
  nextVisible => {
    if (nextVisible) {
      resetForm()
      void loadIssueConditions()
      return
    }

    requestSeq.value += 1
    responseConditions.value = {}
    loadingConditions.value = false
  }
)

watch(
  () => props.issueIds,
  () => {
    if (!visible.value) return
    resetForm()
    void loadIssueConditions()
  }
)
</script>

<template>
  <HDialog
    v-model:visible="visible"
    title="事件下发"
    width="335px"
    destroy-on-close
    :close-on-click-overlay="false"
    :confirm="handleConfirm"
  >
    <div class="event-issue-form">
      <div class="event-issue-form__item">
        <div class="event-issue-form__label is-required">事件名称</div>
        <van-field
          v-model.trim="form.eventName"
          class="event-issue-form__control"
          placeholder="请输入"
          maxlength="20"
          clearable
          :border="false"
        />
      </div>

      <div class="event-issue-form__item">
        <div class="event-issue-form__label is-required">品牌归属</div>
        <HListSingleSelect
          v-model="form.brandCode"
          class="event-issue-form__select"
          :options="brandOptions"
          :fields="{ label: 'value', value: 'key' }"
          title="品牌归属"
          placeholder="请选择"
          :search-fields="['value', 'key']"
        />
      </div>

      <div class="event-issue-form__item">
        <div class="event-issue-form__label is-required">处理优先级</div>
        <div class="event-issue-priority">
          <button
            v-for="item in EVENT_ISSUE_PRIORITY_OPTIONS"
            :key="item.value"
            class="event-issue-priority__item"
            :class="{ 'is-active': form.priority === item.value }"
            type="button"
            @click="form.priority = item.value as EventIssuePriority"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="event-issue-form__item">
        <div class="event-issue-form__label is-required">主责部门</div>
        <HFilterPersonnelSelect
          v-model="form.mainRespUserIds"
          class="event-issue-form__select"
          :options="departAccountTree"
          :loading="departAccountTreeLoading"
          :max-selected="1"
          title="主责部门"
          placeholder="请选择"
          display-mode="departmentPath"
        />
      </div>

      <div class="event-issue-form__item">
        <div class="event-issue-form__label is-required">聚焦观点</div>
        <HFilterMultiSelect
          v-model="form.focusTopics"
          class="event-issue-form__select"
          :options="focusTopicOptions"
          :disabled="loadingConditions"
          title="聚焦观点"
          placeholder="请选择"
          :search-fields="['label', 'value']"
        />
      </div>

      <div class="event-issue-form__item event-issue-form__item--textarea">
        <div class="event-issue-form__label">添加说明</div>
        <van-field
          v-model.trim="form.description"
          class="event-issue-form__control event-issue-form__textarea"
          type="textarea"
          placeholder="请添加说明"
          maxlength="200"
          show-word-limit
          rows="2"
          autosize
          :border="false"
        />
      </div>
    </div>
  </HDialog>
</template>

<style scoped lang="scss">
.event-issue-form {
  padding-top: 2px;
}

.event-issue-form__item {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  align-items: center;
  column-gap: 10px;
  min-height: 46px;
}

.event-issue-form__item + .event-issue-form__item {
  margin-top: 10px;
}

.event-issue-form__item--textarea {
  align-items: flex-start;

  .event-issue-form__label {
    padding-top: 7px;
  }
}

.event-issue-form__label {
  position: relative;
  min-width: 0;
  padding-left: 10px;
  font-size: 14px;
  line-height: 20px;
  color: #1f2733;
  white-space: nowrap;
}

.event-issue-form__label.is-required::before {
  content: '*';
  position: absolute;
  left: 0;
  top: 0;
  color: #ff4b4c;
}

.event-issue-form__control {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #e5e6eb;
  border-radius: 2px;
  background: #ffffff;
  font-size: 14px;

  :deep(.van-field__control) {
    min-height: 34px;
    color: #1f2733;
  }
}

.event-issue-form__textarea {
  height: auto;
  min-height: 66px;
  padding-top: 6px;
  padding-bottom: 4px;

  :deep(.van-field__control) {
    min-height: 40px;
    line-height: 20px;
  }
}

.event-issue-form__select {
  width: 100%;

  :deep(.hfps-trigger),
  :deep(.hfms-trigger),
  :deep(.hlss-trigger) {
    height: 36px;
    border-color: #e5e6eb;
    border-radius: 2px;
  }

  :deep(.hfps-trigger__text),
  :deep(.hfms-trigger__text),
  :deep(.hlss-trigger__text) {
    font-size: 14px;
  }
}

.event-issue-priority {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.event-issue-priority__item {
  flex: 1;
  min-width: 0;
  height: 32px;
  padding: 0;
  border: 1px solid #f2f3f5;
  border-radius: 4px;
  background: #f7f8fa;
  color: #1f2733;
  font-size: 14px;
  line-height: 30px;

  &.is-active {
    border-color: #1677ff;
    background: #f2f8ff;
    color: #1677ff;
    font-weight: 500;
  }
}
</style>
