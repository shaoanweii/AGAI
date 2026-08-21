<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { cloneDeep } from 'lodash-es'
import CcPersonnelSelect from '../../components/CcPersonnelSelect.vue'
import { batchEventStore, batchEventActions } from '../store'
import { useBatchRuleBusinessValidation } from '../../hooks/useBatchRuleBusinessValidation'
import { useBatchRuleValidation } from '../../hooks/useBatchRuleValidation'
import AlertCycleConfig from './AlertCycleConfig.vue'
import DimensionConfig from './DimensionConfig.vue'
import MetricConfig from './MetricConfig.vue'
import { BATCH_DIMENSION_FIELD_CODE, BATCH_METRIC_FIELD_CODE } from '../fieldCode'
import { canUseTopRankMetric as resolveCanUseTopRankMetric } from '../dimension'
import { hasTopRankMetric } from '../metric'
import {
  createDefaultBatchRule,
  fetchBatchEventRuleDetail,
  saveBatchEventRule,
  buildBatchRuleFormOptions
} from '../ruleApi'
import type {
  BatchCcPersonnelItem,
  BatchDeptModel,
  BatchSelectOption,
  BatchRuleFormOptions,
  BatchRuleRecord,
  BatchRuleStatus,
  BatchUserModel
} from '../types'

defineOptions({
  name: 'BatchEventRuleForm'
})

interface Props {
  ruleData?: Partial<BatchRuleRecord> | null
  visible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  ruleData: null,
  visible: false
})

const emit = defineEmits<{
  success: []
}>()

const formRef = ref<FormInstance>()
const detailLoading = ref(false)
const submitLoading = ref(false)
const showFormContent = ref(false)
// loading 统一挂在稳定的外层容器上，避免第二次编辑时临时节点承载 spinner 导致动画丢失。
const loading = computed(() => detailLoading.value || submitLoading.value)
const formOptions = ref<BatchRuleFormOptions | null>(null)
const form = reactive<BatchRuleRecord>(createDefaultBatchRule())
const { rules } = useBatchRuleValidation(form)
const { validateBusinessForm } = useBatchRuleBusinessValidation(form)
let visibleTaskSeed = 0

type BatchConditionDictKey =
  | 'closedRuleAuditMethod'
  | 'closedRuleEnabledStatus'
  | 'closedRulePriority'
  | 'selfBrand'

interface BatchConditionDictItem {
  key: string
  value: string
}

interface SinglePersonnelValue {
  department: BatchDeptModel | null
  user: BatchUserModel | null
}

const isEdit = computed(() => !!props.ruleData?.ruleId)
const batchConditions = computed(() => batchEventStore.batchConditions || {})
const canUseTopRankMetric = computed(() => {
  // TOP 排行是否可选除了依赖体验代码维度本身，还受“仅一条数据 / 独立计算”约束。
  return resolveCanUseTopRankMetric(form.dimensions)
})

/**
 * 批量规则表单统一消费批量规则字典，并在组件层转成 Element Plus 选项格式。
 * @param dictKey 字典字段名
 * @param options 映射选项
 * @returns 下拉或单选组件可直接使用的选项
 */
const mapBatchDictOptions = (
  dictKey: BatchConditionDictKey,
  options: { reverse?: boolean } = {}
): BatchSelectOption[] => {
  const source = (batchConditions.value[dictKey] || []) as BatchConditionDictItem[]
  const list = options.reverse ? [...source].reverse() : [...source]

  return list
    .map(item => ({
      label: String(item.value ?? ''),
      value: String(item.key ?? '')
    }))
    .filter(item => item.value)
}

/**
 * 审核方式直接取批量规则字典，避免继续复用单点规则接口导致枚举错位。
 */
const auditMethodOptions = computed(() =>
  mapBatchDictOptions('closedRuleAuditMethod', { reverse: true })
)
/**
 * 启用状态直接复用批量规则后端字典，确保列表与表单的状态枚举始终一致。
 */
const enabledOptions = computed(() => mapBatchDictOptions('closedRuleEnabledStatus'))
/**
 * 处理优先级与启用状态保持同一取值方式，直接消费批量规则字典，避免表单配置缓存干扰页面展示。
 */
const processPriorityOptions = computed(() => mapBatchDictOptions('closedRulePriority'))
const brandOptions = computed(() => mapBatchDictOptions('selfBrand'))

/**
 * 将“部门 + 人员”结构映射为抄送组件复用的单选结构，减少同一套人员树维护两份逻辑。
 * @param department 部门对象
 * @param user 人员对象
 * @returns 兼容抄送组件的数据结构
 */
const buildSinglePersonnelValue = (
  department: BatchDeptModel | null,
  user: BatchUserModel | null
): BatchCcPersonnelItem[] => {
  if (!department?.id || !user?.id) {
    return []
  }

  return [
    {
      id: department.id,
      deptNo: department.deptNo,
      deptName: department.name,
      isAll: false,
      userId: user.id,
      employeeId: user.employeeId,
      userName: user.name
    }
  ]
}

/**
 * 将抄送组件返回的标准结构回写为表单中的“部门 + 人员”字段。
 * @param list 抄送组件返回的单选数组
 * @returns 回写后的部门和人员对象
 */
const parseSinglePersonnelValue = (list: BatchCcPersonnelItem[] = []): SinglePersonnelValue => {
  const selected = Array.isArray(list) ? list[0] : undefined

  if (!selected?.userId) {
    return {
      department: null,
      user: null
    }
  }

  return {
    department: {
      id: selected.id,
      deptNo: selected.deptNo,
      name: selected.deptName
    },
    user: {
      id: selected.userId,
      employeeId: selected.employeeId,
      name: selected.userName
    }
  }
}

/**
 * 创建单选人员字段的双向模型，屏蔽抄送组件结构和表单部门/人员双字段之间的差异。
 * @param readValue 读取当前表单人员值
 * @param writeValue 回写当前表单人员值
 * @returns 供 CcPersonnelSelect 使用的 v-model
 */
const createSinglePersonnelModel = (
  readValue: () => SinglePersonnelValue,
  writeValue: (value: SinglePersonnelValue) => void
) =>
  computed<BatchCcPersonnelItem[]>({
    get: () => {
      const { department, user } = readValue()
      return buildSinglePersonnelValue(department, user)
    },
    set: value => {
      writeValue(parseSinglePersonnelValue(value))
    }
  })

const auditPersonnel = createSinglePersonnelModel(
  () => ({
    department: form.auditDepartment,
    user: form.auditor
  }),
  ({ department, user }) => {
    form.auditDepartment = department
    form.auditor = user
  }
)

const mainPersonnel = createSinglePersonnelModel(
  () => ({
    department: form.mainDepartment,
    user: form.mainResponder
  }),
  ({ department, user }) => {
    form.mainDepartment = department
    form.mainResponder = user
  }
)

/**
 * 依据外部上下文生成默认表单，主要保留当前分类 ID 与分类名称。
 * @param payload 外部传入的规则上下文
 * @returns 默认表单模型
 */
const buildDefaultFormData = (payload?: Partial<BatchRuleRecord> | null) =>
  createDefaultBatchRule(payload?.categoryId || '', payload?.categoryName || '')

/**
 * 新建规则可从分类树携带部分默认值，先生成完整默认模型再覆盖，避免响应式对象缺字段。
 * @param payload 外部传入的规则上下文
 * @returns 完整表单模型
 */
const buildCreateFormData = (payload?: Partial<BatchRuleRecord> | null) => {
  return {
    ...buildDefaultFormData(payload),
    ...cloneDeep(payload || {})
  } as BatchRuleRecord
}

/**
 * 保持 reactive 引用不变，仅替换内部字段，避免模板和校验规则丢失响应式绑定。
 * @param payload 完整表单模型
 */
const assignFormData = (payload: BatchRuleRecord) => {
  Object.assign(form, cloneDeep(payload))
}

/**
 * 重置表单时回到当前分类的默认值，保证新建规则时分类前缀始终正确。
 */
const resetForm = () => {
  assignFormData(buildDefaultFormData(props.ruleData))
  formRef.value?.clearValidate()
}

/**
 * 从 store 重新构建当前表单所需选项，确保弹窗打开时拿到最新资源。
 * @returns 表单选项配置
 */
const refreshFormOptions = () => {
  const options = buildBatchRuleFormOptions()
  formOptions.value = options
  return options
}

/**
 * 判断当前异步任务是否仍属于最近一次打开弹窗，避免关闭后旧请求回写表单。
 * @param taskSeed 弹窗异步任务编号
 * @returns 当前任务是否仍有效
 */
const isCurrentVisibleTask = (taskSeed: number) => taskSeed === visibleTaskSeed

/**
 * 加载编辑详情并回填表单，选项由弹窗初始化阶段统一传入。
 * @param ruleId 规则 ID
 * @param options 表单选项配置
 */
const loadRuleDetail = async (ruleId: string, options: BatchRuleFormOptions) => {
  const detail = await fetchBatchEventRuleDetail(ruleId, options)
  assignFormData(detail)
}

/**
 * 打开弹框时根据场景决定回填默认值还是编辑详情，避免新建和编辑数据串用。
 */
const initForm = async (options: BatchRuleFormOptions) => {
  resetForm()

  const sourceRule = props.ruleData

  if (!sourceRule) {
    return
  }

  if (sourceRule.ruleId) {
    await loadRuleDetail(sourceRule.ruleId, options)
    return
  }

  assignFormData(buildCreateFormData(sourceRule))
}

/**
 * 弹窗打开时确保资源已加载，表单选项直接从 store 获取。
 */
const loadVisibleData = async () => {
  const currentSeed = ++visibleTaskSeed
  detailLoading.value = true

  try {
    // 表单独立打开时也要具备完整资源，避免继续隐式依赖列表页初始化顺序。
    await batchEventActions.initFormResources()
    const options = refreshFormOptions()

    if (!isCurrentVisibleTask(currentSeed)) {
      return
    }

    await initForm(options)
  } catch (error: any) {
    if (isCurrentVisibleTask(currentSeed)) {
      ElMessage.error(error?.message || '加载表单数据失败')
    }
  } finally {
    if (isCurrentVisibleTask(currentSeed)) {
      // 表单数据准备完成后再重新挂载表单内容，避免打开阶段 reset/回填触发字段 change 校验 warning。
      showFormContent.value = !!formOptions.value
      detailLoading.value = false
    }
  }
}

watch(
  () => props.visible,
  async visible => {
    if (!visible) {
      // 关闭阶段不再主动 reset 表单：
      // 当前表单包含多个 trigger=change 的字段，若在关闭过渡期间回写默认值，
      // Element Plus 会触发校验并被项目的 debugWarn 打印为 warning。
      // 下次打开时 initForm 内会先 resetForm，因此这里仅回收异步状态即可。
      visibleTaskSeed += 1
      detailLoading.value = false
      return
    }

    // 先卸载表单内容，再执行 reset/详情回填，避免已挂载字段在打开阶段触发 change 校验 warning。
    showFormContent.value = false
    await nextTick()
    detailLoading.value = true
    await loadVisibleData()
  },
  { immediate: true }
)

watch(
  () => form.brand,
  (brand, prevBrand) => {
    if (!prevBrand || brand === prevBrand) {
      return
    }

    form.dimensions.forEach(item => {
      if (item.field === BATCH_DIMENSION_FIELD_CODE.CAR_SERIES) {
        item.value = []
      }
    })
  }
)

watch(
  () => form.auditMethod,
  () => {
    // 审核人员改为必填后，不再因审核方式切换清空已选人员，避免用户重复选择。
    validatePersonnelField('auditDepartment')
  }
)

/**
 * TOP 排行指标和体验代码维度强联动：
 * - 未配置体验代码时不可保留
 * - 体验代码既不是单条选择，也不是独立计算时同样不可保留
 */
watch(
  () => [canUseTopRankMetric.value, form.metrics.map(item => item.metric).join('|')] as const,
  ([canUseTopRank]) => {
    if (!canUseTopRank && hasTopRankMetric(form.metrics)) {
      form.metrics = form.metrics.filter(item => item.metric !== BATCH_METRIC_FIELD_CODE.TOP_RANK)
    }
  },
  { immediate: true }
)

const validateAuditPersonnelField = () => {
  validatePersonnelField('auditDepartment')
}

const validateMainPersonnelField = () => {
  validatePersonnelField('mainDepartment')
}

/**
 * 人员选择组件更新后只触发对应字段校验，避免整表校验造成其他未填项提前报错。
 * @param field 表单中需要重新校验的人员字段
 */
function validatePersonnelField(field: 'auditDepartment' | 'mainDepartment') {
  formRef.value?.validateField?.(field)?.catch(() => undefined)
}

/**
 * 保存前沿用单点规则的接口字段口径，避免批量规则再额外做一轮字段映射。
 * @param close 关闭弹窗方法
 * @returns Promise<void>
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const onConfirm = async ({ close: _close }: { close: () => void }) => {
  const valid = await formRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  const businessError = validateBusinessForm()

  if (businessError) {
    ElMessage.warning(businessError)
    return
  }

  if (!formOptions.value) {
    ElMessage.warning('表单配置未加载完成，请稍后重试')
    return
  }

  submitLoading.value = true

  try {
    const payload = cloneDeep(form)

    await saveBatchEventRule(
      {
        ...payload,
        isEnabled: payload.isEnabled as BatchRuleStatus
      },
      formOptions.value
    )

    ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
    emit('success')
    // 关闭动作统一交给外层 success 链路处理，避免同一次保存触发重复关闭。
  } catch (error: any) {
    ElMessage.error(error?.message || '保存规则失败')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({
  onConfirm
})
</script>

<template>
  <div v-loading="loading" class="batch-rule-form">
    <div v-if="!formOptions || !showFormContent" class="batch-rule-form__loading-placeholder"></div>
    <el-form
      v-if="formOptions && showFormContent"
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="104px"
      class="batch-rule-form__content"
    >
      <el-form-item label="规则名称" prop="ruleName" required>
        <div class="batch-rule-form__rule-name">
          <el-input v-model.trim="form.ruleName" placeholder="请输入" maxlength="30" />
        </div>
      </el-form-item>

      <el-form-item label="品牌" prop="brand" required>
        <el-select v-model="form.brand" class="w-full" placeholder="请选择">
          <el-option
            v-for="item in brandOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="预警周期" required>
        <AlertCycleConfig v-model="form.alertConfig" :options="formOptions" />
      </el-form-item>

      <el-form-item label="维度配置" required>
        <DimensionConfig v-model="form.dimensions" :brand="form.brand" :options="formOptions" />
      </el-form-item>

      <el-form-item label="指标配置" required>
        <MetricConfig
          v-model="form.metrics"
          v-model:logic="form.metricLogic"
          :options="formOptions"
          :can-use-top-rank="canUseTopRankMetric"
        />
      </el-form-item>

      <el-form-item label="处理优先级" prop="processPriority" required>
        <el-select v-model="form.processPriority" class="w-full" placeholder="请选择">
          <el-option
            v-for="item in processPriorityOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="审核人员" prop="auditDepartment" required>
        <CcPersonnelSelect
          v-model="auditPersonnel"
          :multiple="false"
          :allow-dept-select="false"
          :auto-close-on-select="true"
          :hide-dept-in-display="true"
          placeholder="请选择审核人"
          @change="validateAuditPersonnelField"
        />
      </el-form-item>

      <el-form-item label="审核方式" prop="auditMethod" required>
        <el-radio-group v-model="form.auditMethod" class="batch-rule-form__radio-line">
          <el-radio v-for="item in auditMethodOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="业务责任人" prop="mainDepartment" required>
        <CcPersonnelSelect
          v-model="mainPersonnel"
          :multiple="false"
          :allow-dept-select="false"
          :auto-close-on-select="true"
          :hide-dept-in-display="true"
          placeholder="请选择业务责任人"
          @change="validateMainPersonnelField"
        />
      </el-form-item>

      <el-form-item label="抄送人员" prop="ccPersonnel">
        <CcPersonnelSelect v-model="form.ccPersonnel" />
      </el-form-item>

      <el-form-item label="是否启用" prop="isEnabled" required>
        <el-radio-group v-model="form.isEnabled" class="batch-rule-form__radio-line">
          <el-radio v-for="item in enabledOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.batch-rule-form {
  /* loading 高度优先跟随弹窗内容区，空内容加载时保留转圈可见的基础高度 */
  position: relative;
  height: 100%;
  min-height: 100%;
}

.batch-rule-form__content {
  padding-right: 8px;
}

.batch-rule-form__loading-placeholder {
  /* 表单未挂载期间提供稳定高度，既保证系统 loading 可见，也避免打开阶段字段触发 change 校验。 */
  min-height: 680px;
}

.batch-rule-form__rule-name {
  display: flex;
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}

.batch-rule-form__radio-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.w-full {
  width: 100%;
}

:deep(.batch-rule-form__rule-name .el-select__wrapper),
:deep(.batch-rule-form__rule-name .el-input__wrapper) {
  box-shadow: none !important;
}

:deep(.batch-rule-form__rule-name .el-input__wrapper) {
  border: 0;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #1d2129 !important;
}
:deep(.el-radio.is-disabled .el-radio__label) {
  color: var(--el-disabled-text-color) !important;
}
:deep(.el-radio) {
  color: #1d2129 !important;
  margin-right: 16px !important;
}
:deep(.el-radio:last-child) {
  margin-right: 0 !important;
}
</style>
