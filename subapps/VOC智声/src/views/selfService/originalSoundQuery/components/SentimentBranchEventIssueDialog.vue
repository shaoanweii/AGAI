<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type CascaderProps, type FormInstance, type FormRules } from 'element-plus'

import { getTagLibClientTree } from '@/api/common'
import { taskDistribution } from '@/api/unlabeledTags'
import type { InsReportAccountInfoVo, InsReportSysDepartVo } from '@/api/common/index.d'
import type { TaskDistributionModel } from '@/api/unlabeledTags/types'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import FDialog from '@/components/UI/FDialog/index.vue'
import { TagType } from '@/constants'
import useSingleEventStore from '@/store/modules/singleEvent'

defineOptions({
  name: 'SentimentBranchEventIssueDialog'
})

type EventIssueMode = 'single' | 'batch'

interface VoiceIssueSelectionItem extends Record<string, any> {
  id?: string | number
  originalId?: string | number
}

interface ExperienceCodeNode {
  tagCode?: string | number
  tagName?: string
  child?: ExperienceCodeNode[]
}

interface MainRespUserDepartInfo {
  account: InsReportAccountInfoVo
  depart: InsReportSysDepartVo
}

interface SentimentBranchEventIssueFormValue {
  eventName: string
  priority: string
  eventLevel: string
  brandCode: string
  mainRespUserId: string
  intention: string
  experienceCode: string
  description: string
}

interface SentimentBranchEventIssuePayload {
  mode: EventIssueMode
  selectedIds: string[]
  selectedOriginalIds: string[]
  formData: SentimentBranchEventIssueFormValue
}

const props = withDefaults(
  defineProps<{
    visible: boolean
    mode?: EventIssueMode
    selection?: VoiceIssueSelectionItem[]
  }>(),
  {
    mode: 'single',
    selection: () => []
  }
)

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: SentimentBranchEventIssuePayload): void
}>()

const visibleModel = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

const singleEventStore = useSingleEventStore()
const formRef = ref<FormInstance>()

const form = reactive<SentimentBranchEventIssueFormValue>({
  eventName: '',
  priority: '',
  eventLevel: '',
  brandCode: '',
  mainRespUserId: '',
  intention: '',
  experienceCode: '',
  description: ''
})

const dialogTitle = computed(() => {
  return props.mode === 'batch' ? '批量下发' : '事件下发'
})

/**
 * 统一将后端/缓存中的可选值转成字符串，便于下拉值比较和表单回填。
 *
 * @param value 原始可选值
 * @returns 字符串值，空值返回空字符串
 */
const normalizeString = (value: unknown) => {
  if (value === undefined || value === null) return ''
  return String(value).trim()
}

/**
 * 统一把空字符串转为 undefined，避免下发 JSON 中出现无意义空字段。
 *
 * @param value 原始可选值
 * @returns 非空字符串；空值返回 undefined
 */
const normalizeOptionalString = (value: unknown): string | undefined => {
  const normalizedValue = normalizeString(value)
  return normalizedValue || undefined
}

const selectedPrimaryIdList = computed(() => {
  return props.selection.map(item => normalizeString(item?.id)).filter(Boolean)
})

const selectedOriginalIdList = computed(() => {
  return props.selection.map(item => normalizeString(item?.originalId)).filter(Boolean)
})

const priorityOptions = computed(() => {
  return singleEventStore.closed_rule_priority || []
})

const levelOptions = computed(() => {
  return singleEventStore.closed_rule_level || []
})

const brandOptions = computed(() => {
  return singleEventStore.brandOptions || []
})

const intentionOptions = computed(() => {
  return singleEventStore.voc_intention || []
})

const mainDepartmentCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: true
} satisfies CascaderProps

const mainRespUserOptions = computed<Array<Record<string, any>>>(() => {
  /**
   * 复用单点事件处理弹窗抄送人员同源的部门账号树。
   * 部门节点只用于展开定位，buildDepartAccountCascaderOptions 会禁用部门节点，
   * 保证“主责部门”下拉最终只能选择具体人员。
   */
  return singleEventStore.buildDepartAccountCascaderOptions(
    singleEventStore.departAccountTree || []
  )
})

const experienceCodeOptions = ref<ExperienceCodeNode[]>([])

const experienceCodeCascaderProps = {
  value: 'tagCode',
  label: 'tagName',
  children: 'child',
  emitPath: false,
  checkStrictly: false
} satisfies CascaderProps

/**
 * 单独加载情感分支事件下发体验代码。
 * 该弹窗需要五级体验代码，不复用单点事件 conditions 中的 tagTreeList，避免污染单点事件缓存。
 */
const loadExperienceCodeOptions = async () => {
  try {
    const res = await getTagLibClientTree({
      tagLibType: TagType.Domain,
      level: 5
    })
    experienceCodeOptions.value = Array.isArray(res.result)
      ? (res.result as ExperienceCodeNode[])
      : []
  } catch (error) {
    console.error('loadExperienceCodeOptions error:', error)
    experienceCodeOptions.value = []
  }
}

/**
 * 按 tagCode 在体验代码树中查找节点，用于兜底校验是否选中了末级。
 *
 * @param code 当前选择的体验代码
 * @param list 体验代码树
 * @returns 匹配的节点
 */
const findExperienceCodeNode = (
  code: string,
  list: ExperienceCodeNode[] = []
): ExperienceCodeNode | undefined => {
  for (const item of list) {
    if (normalizeString(item?.tagCode) === code) return item
    const matched = findExperienceCodeNode(code, item?.child || [])
    if (matched) return matched
  }

  return undefined
}

/**
 * 按 tagCode 查找完整体验代码路径，便于把五级选择拆成后端下发字段。
 *
 * @param code 当前选择的体验代码
 * @param list 体验代码树
 * @param parentPath 当前递归路径
 * @returns 从一级到当前节点的路径，未命中时返回空数组
 */
const findExperienceCodePath = (
  code: string,
  list: ExperienceCodeNode[] = [],
  parentPath: ExperienceCodeNode[] = []
): ExperienceCodeNode[] => {
  for (const item of list) {
    const currentPath = [...parentPath, item]
    if (normalizeString(item?.tagCode) === code) return currentPath

    const matchedPath = findExperienceCodePath(code, item?.child || [], currentPath)
    if (matchedPath.length) return matchedPath
  }

  return []
}

const validateExperienceCode = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void
) => {
  if (!value) {
    callback()
    return
  }

  const node = findExperienceCodeNode(value, experienceCodeOptions.value)
  if (node?.child?.length) {
    callback(new Error('体验代码必须选至末级'))
    return
  }

  callback()
}

const rules: FormRules<SentimentBranchEventIssueFormValue> = {
  eventName: [
    { required: true, message: '请输入事件名称', trigger: ['blur', 'change'] },
    { max: 50, message: '事件名称不能超过50字', trigger: ['blur', 'change'] }
  ],
  priority: [{ required: true, message: '请选择处理优先级', trigger: 'change' }],
  eventLevel: [{ required: true, message: '请选择事件等级', trigger: 'change' }],
  brandCode: [{ required: true, message: '请选择品牌归属', trigger: 'change' }],
  mainRespUserId: [{ required: true, message: '请选择主责部门', trigger: 'change' }],
  experienceCode: [{ validator: validateExperienceCode, trigger: 'change' }]
}

/**
 * 设置默认品牌为“智行”，若单点事件品牌数据未包含该品牌，则保持为空等待用户选择。
 */
const applyDefaultBrand = () => {
  const defaultBrand = brandOptions.value.find((item: any) => item.value === '智行')
  form.brandCode = defaultBrand?.key || ''
}

/**
 * 单点事件字典加载完成后设置处理优先级与事件等级默认值。
 */
const applyDefaultIssueOptions = () => {
  form.priority = priorityOptions.value[0]?.value || ''
  form.eventLevel = levelOptions.value[0]?.value || ''
}

/**
 * 每次打开弹窗都重置为干净表单，避免上一次下发残留字段影响当前操作。
 */
const initializeForm = () => {
  form.eventName = ''
  applyDefaultIssueOptions()
  form.mainRespUserId = ''
  form.intention = ''
  form.experienceCode = ''
  form.description = ''
  applyDefaultBrand()
  formRef.value?.clearValidate()
}

/**
 * 加载弹窗所需下拉依赖。
 * 部门树与部门账号树分别用于还原主责组织编号/名称与主责人信息。
 */
const loadDialogOptions = async () => {
  await Promise.all([
    singleEventStore.getConditions(),
    singleEventStore.getDepartTree(),
    singleEventStore.getDepartAccountTree(),
    loadExperienceCodeOptions()
  ])
  if (!form.priority || !form.eventLevel) {
    applyDefaultIssueOptions()
  }
  if (!form.brandCode) {
    applyDefaultBrand()
  }
}

/**
 * 根据选中的品牌编码还原品牌名称，保证下发时编码和名称来自同一份字典。
 *
 * @returns 品牌相关下发字段
 */
const buildBrandPayload = (): Pick<TaskDistributionModel, 'brandCode' | 'brandName'> => {
  const selectedBrand = brandOptions.value.find((item: any) => {
    return normalizeString(item?.key) === form.brandCode
  })

  return {
    brandCode: normalizeOptionalString(form.brandCode),
    brandName: normalizeOptionalString(selectedBrand?.value)
  }
}

/**
 * 根据主责人 userId 在部门账号树中查找账号及其直属部门。
 * findDepartAccountTree 的账号节点不稳定携带 deptId，因此以账号所在父级部门作为主责部门真源。
 *
 * @param userId 当前选中的主责人 userId
 * @param tree 部门账号树
 * @returns 命中的账号和账号所在部门，未命中返回 null
 */
const findMainRespUserDepartInfo = (
  userId: string,
  tree: InsReportSysDepartVo[] = []
): MainRespUserDepartInfo | null => {
  for (const depart of tree) {
    const matchedAccount = depart.account?.find(account => {
      return normalizeString(account?.userId) === userId
    })

    if (matchedAccount) {
      return {
        account: matchedAccount,
        depart
      }
    }

    const matchedChild = findMainRespUserDepartInfo(userId, depart.child || [])
    if (matchedChild) return matchedChild
  }

  return null
}

/**
 * 根据主责人 userId 还原主责组织与人员信息。
 * 级联组件只保存人员 userId，因此提交前必须从部门账号树中补齐人员及其所属部门。
 *
 * @returns 主责组织与主责人下发字段；缺少关键组织信息时返回 null
 */
const buildMainRespPayload = (): Pick<
  TaskDistributionModel,
  | 'mainRespOrgId'
  | 'mainRespOrgNo'
  | 'mainRespOrgName'
  | 'mainRespUserId'
  | 'mainRespUserEmpNo'
  | 'mainRespUserName'
> | null => {
  const mainRespUserDepartInfo = findMainRespUserDepartInfo(
    form.mainRespUserId,
    singleEventStore.departAccountTree || []
  )

  if (!mainRespUserDepartInfo) {
    ElMessage.warning('主责部门信息缺失，请重新选择主责部门')
    return null
  }

  const { account: mainRespUser, depart: mainRespDepart } = mainRespUserDepartInfo
  const mainRespOrgId = normalizeOptionalString(mainRespDepart.id)
  const mainRespOrgNo = normalizeOptionalString(mainRespDepart.code)
  const mainRespOrgName = normalizeOptionalString(mainRespDepart.name)

  if (!mainRespOrgId || !mainRespOrgNo || !mainRespOrgName) {
    ElMessage.warning('主责部门信息缺失，请重新选择主责部门')
    return null
  }

  return {
    mainRespOrgId,
    mainRespOrgNo,
    mainRespOrgName,
    mainRespUserId: normalizeOptionalString(mainRespUser.userId),
    mainRespUserEmpNo: normalizeOptionalString(mainRespUser.employeeId),
    mainRespUserName: normalizeOptionalString(mainRespUser.userName)
  }
}

/**
 * 将五级体验代码路径拆成下发接口要求的领域标签和观点字段。
 * 约定：1-4 级对应 domTagFirst~Four，第 5 级对应 topic/topicCode。
 *
 * @returns 体验代码相关下发字段
 */
const buildExperienceCodePayload = (): Pick<
  TaskDistributionModel,
  | 'domTagFirstCode'
  | 'domTagFirst'
  | 'domTagSecondCode'
  | 'domTagSecond'
  | 'domTagThreeCode'
  | 'domTagThree'
  | 'domTagFourCode'
  | 'domTagFour'
  | 'topic'
  | 'topicCode'
> => {
  const experienceCodePath = form.experienceCode
    ? findExperienceCodePath(form.experienceCode, experienceCodeOptions.value)
    : []
  const [firstTag, secondTag, thirdTag, fourthTag, topicTag] = experienceCodePath

  return {
    domTagFirstCode: normalizeOptionalString(firstTag?.tagCode),
    domTagFirst: normalizeOptionalString(firstTag?.tagName),
    domTagSecondCode: normalizeOptionalString(secondTag?.tagCode),
    domTagSecond: normalizeOptionalString(secondTag?.tagName),
    domTagThreeCode: normalizeOptionalString(thirdTag?.tagCode),
    domTagThree: normalizeOptionalString(thirdTag?.tagName),
    domTagFourCode: normalizeOptionalString(fourthTag?.tagCode),
    domTagFour: normalizeOptionalString(fourthTag?.tagName),
    topicCode: normalizeOptionalString(topicTag?.tagCode),
    topic: normalizeOptionalString(topicTag?.tagName)
  }
}

/**
 * 组装事件下发请求体。
 * 这里保持字段语义与 Swagger 一致：id 为视图主键，dataId 为原始数据 id。
 *
 * @returns 完整下发参数；主责信息缺失时返回 null
 */
const buildTaskDistributionPayload = (): TaskDistributionModel | null => {
  const mainRespPayload = buildMainRespPayload()
  if (!mainRespPayload) return null

  return {
    id: selectedPrimaryIdList.value,
    dataId: selectedOriginalIdList.value,
    riskName: normalizeOptionalString(form.eventName),
    eventPriority: normalizeOptionalString(form.priority),
    eventLevel: normalizeOptionalString(form.eventLevel),
    ...buildBrandPayload(),
    ...mainRespPayload,
    intention: normalizeOptionalString(form.intention),
    ...buildExperienceCodePayload(),
    remark: normalizeOptionalString(form.description)
  }
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return

    initializeForm()
    void loadDialogOptions()
  },
  { immediate: true }
)

watch(
  [priorityOptions, levelOptions],
  () => {
    if (props.visible && (!form.priority || !form.eventLevel)) {
      applyDefaultIssueOptions()
    }
  },
  { deep: true }
)

watch(
  brandOptions,
  () => {
    if (props.visible && !form.brandCode) {
      applyDefaultBrand()
    }
  },
  { deep: true }
)

/**
 * 统一执行表单校验。
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
 * 确认事件下发。
 * 完成前端必填校验后，按接口契约组装主键、原始数据、品牌、主责人与体验代码字段。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!props.selection.length) {
    ElMessage.warning('请选择需要下发的原声')
    return
  }

  if (
    selectedPrimaryIdList.value.length !== props.selection.length ||
    selectedOriginalIdList.value.length !== props.selection.length
  ) {
    ElMessage.warning('选中的原声存在缺少主键或原始数据 id 的记录，无法执行事件下发')
    return
  }

  const isValid = await validateForm()
  if (!isValid) return

  const payload = buildTaskDistributionPayload()
  if (!payload) return

  try {
    const res = await taskDistribution(payload)
    if (!res.success) {
      ElMessage.error(res.message || '事件下发失败')
      return
    }

    emit('success', {
      mode: props.mode,
      selectedIds: selectedPrimaryIdList.value,
      selectedOriginalIds: selectedOriginalIdList.value,
      formData: { ...form }
    })
    ElMessage.success(res.message || '事件下发成功')
    close()
  } catch (error) {
    console.error('taskDistribution error:', error)
  }
}
</script>

<template>
  <FDialog
    v-model:visible="visibleModel"
    width="680px"
    destory-on-close
    :close-on-click-modal="false"
    :confirm="handleConfirm"
  >
    <template #header>
      <span>{{ dialogTitle }}</span>
    </template>

    <div class="sentiment-event-issue-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" @submit.prevent>
        <el-form-item label="事件名称" prop="eventName">
          <el-input
            v-model.trim="form.eventName"
            clearable
            maxlength="50"
            show-word-limit
            placeholder="请输入事件名称"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="处理优先级" prop="priority">
          <OptionToggleGroup
            v-model="form.priority"
            :options="priorityOptions"
            label-key="text"
            value-key="value"
          />
        </el-form-item>

        <el-form-item label="事件等级" prop="eventLevel">
          <OptionToggleGroup
            v-model="form.eventLevel"
            :options="levelOptions"
            label-key="text"
            value-key="value"
          />
        </el-form-item>

        <el-form-item label="品牌归属" prop="brandCode">
          <el-select
            v-model="form.brandCode"
            clearable
            filterable
            placeholder="请选择品牌归属"
            class="w-full"
          >
            <el-option
              v-for="item in brandOptions"
              :key="item.key"
              :label="item.value"
              :value="item.key"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="主责部门" prop="mainRespUserId">
          <el-cascader
            v-model="form.mainRespUserId"
            :options="mainRespUserOptions"
            :props="mainDepartmentCascaderProps"
            clearable
            filterable
            separator="#"
            placeholder="请选择主责部门"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="意图" prop="intention">
          <el-select
            v-model="form.intention"
            clearable
            filterable
            placeholder="请选择"
            class="w-full"
          >
            <el-option
              v-for="item in intentionOptions"
              :key="item.value"
              :label="item.text"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="体验代码" prop="experienceCode">
          <el-cascader
            v-model="form.experienceCode"
            :options="experienceCodeOptions"
            :props="experienceCodeCascaderProps"
            clearable
            filterable
            separator="#"
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="添加说明" prop="description">
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
.sentiment-event-issue-dialog {
  min-height: 0;
}

.sentiment-event-issue-dialog :deep(.el-form-item) {
  margin-bottom: 18px;
}

.sentiment-event-issue-dialog :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.sentiment-event-issue-dialog :deep(.el-input__wrapper),
.sentiment-event-issue-dialog :deep(.el-select__wrapper),
.sentiment-event-issue-dialog :deep(.el-textarea__inner) {
  min-height: 32px;
}

.sentiment-event-issue-dialog :deep(.el-textarea__inner) {
  min-height: 96px;
}
</style>
