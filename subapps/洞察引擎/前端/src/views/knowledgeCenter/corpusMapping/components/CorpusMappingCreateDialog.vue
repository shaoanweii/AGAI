<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, nextTick, reactive, ref, watch } from 'vue'

defineOptions({
  name: 'CorpusMappingCreateDialog'
})

const visible = defineModel<boolean>('visible', {
  default: false
})

interface BaseFormValue {
  subject?: string
  description?: string
  standardOpinionId?: string
  enableStatus?: 'enabled' | 'disabled'
}

type CorpusVariant = 'text' | 'survey'

interface Props {
  mode?: 'create' | 'edit'
  initialValues?: BaseFormValue
  variant?: CorpusVariant
  defaultVariant?: CorpusVariant
  allowVariantSwitch?: boolean
  standardOpinions?: {
    tagName: string
    tagCode: string
    [key: string]: any
  }[]
  standardOpinionLoading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'create',
  initialValues: () => ({} as BaseFormValue),
  variant: 'text',
  defaultVariant: 'text',
  allowVariantSwitch: false,
  standardOpinions: () => [],
  standardOpinionLoading: false
})

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑语料' : '新建语料'))

/**
 * 统一解析弹窗当前生效的语料类型，确保 RawData 可切换类型而语料中心编辑态仍沿用外部传值。
 * @returns 当前弹窗应展示的语料类型
 */
const resolveDialogVariant = (): CorpusVariant => {
  if (props.mode === 'edit') {
    return props.variant
  }
  if (props.allowVariantSwitch) {
    return props.defaultVariant
  }
  return props.variant
}

const activeVariant = ref<CorpusVariant>(resolveDialogVariant())
const isVariantSwitchEnabled = computed(() => props.mode === 'create' && props.allowVariantSwitch)
const currentVariant = computed<CorpusVariant>(() => {
  return isVariantSwitchEnabled.value ? activeVariant.value : props.variant
})
const isSurveyCorpus = computed(() => currentVariant.value === 'survey')
const subjectLabel = computed(() => (isSurveyCorpus.value ? '语料描述' : '语料主体'))

// 弹窗内标准观点加载时同步展示明确占位文案，避免用户误以为无可选项。
const standardOpinionPlaceholder = computed(() => {
  return props.standardOpinionLoading ? '标准观点加载中...' : '请选择标准观点'
})

interface Emits {
  (
    e: 'submit',
    payload: {
      subject: string
      description: string
      standardOpinionId: string
      enableStatus: 'enabled' | 'disabled'
      variant: CorpusVariant
    }
  ): void
}

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()

const form = reactive({
  subject: '',
  description: '',
  standardOpinionId: '',
  enableStatus: 'enabled' as 'enabled' | 'disabled'
})

const rules = computed<FormRules>(() => {
  const subjectMessage = isSurveyCorpus.value ? '请输入语料描述' : '请输入语料主体'
  const baseRules: FormRules = {
    standardOpinionId: [{ required: true, message: '请选择标准观点', trigger: 'change' }],
    enableStatus: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
  }
  if (isSurveyCorpus.value) {
    baseRules.subject = [{ required: true, message: subjectMessage, trigger: 'blur' }]
  } else {
    const validateSubjectOrDescription = (_rule: any, _value: any, callback: any) => {
      if (!form.subject && !form.description) {
        callback(new Error('主体和描述至少填写一项'))
        return
      }
      callback()
    }
    baseRules.subject = [{ validator: validateSubjectOrDescription, trigger: 'blur' }]
    baseRules.description = [{ validator: validateSubjectOrDescription, trigger: 'blur' }]
  }
  return baseRules
})

const resetForm = () => {
  form.subject = ''
  form.description = ''
  form.standardOpinionId = ''
  form.enableStatus = 'enabled'
}

const fillFormFromInitialValues = (values?: BaseFormValue) => {
  if (!values) {
    resetForm()
    return
  }
  form.subject = values.subject || ''
  form.description = values.description || ''
  form.standardOpinionId = values.standardOpinionId || ''
  form.enableStatus = values.enableStatus || 'enabled'
}

/**
 * 统一初始化弹窗表单，确保每次打开/关闭后都回到当前模式下的基准状态。
 * 新建态清空输入，编辑态按父组件传值回填。
 */
const syncDialogFormState = () => {
  activeVariant.value = resolveDialogVariant()
  if (props.mode === 'edit') {
    fillFormFromInitialValues(props.initialValues)
    return
  }
  resetForm()
}

/**
 * 在表单结构与规则稳定后清理校验，避免二次打开弹窗时残留上一次的红框与错误提示。
 */
const clearFormValidateState = async () => {
  await nextTick()
  formRef.value?.clearValidate()
}

/**
 * 切换语料类型时仅清理主体/描述字段，保留标准观点和启用状态，减少重复录入。
 * @param variant 用户切换后的语料类型
 */
const handleVariantSwitch = async (variant: CorpusVariant) => {
  if (!isVariantSwitchEnabled.value || activeVariant.value === variant) return
  activeVariant.value = variant
  form.subject = ''
  form.description = ''
  await clearFormValidateState()
}

watch(
  () => visible.value,
  async val => {
    if (val) {
      syncDialogFormState()
      await clearFormValidateState()
      return
    }

    // 关闭弹窗时同步重置表单与校验，避免下一次打开仍展示上一次校验结果。
    syncDialogFormState()
    await clearFormValidateState()
  }
)

const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value) {
    close()
    return
  }
  await formRef.value.validate()
  emit('submit', {
    subject: form.subject,
    description: form.description,
    standardOpinionId: form.standardOpinionId,
    enableStatus: form.enableStatus,
    variant: currentVariant.value
  })
  close()
}
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    :title="dialogTitle"
    width="800px"
    destroy-on-close
    :confirm="handleConfirm"
  >
    <div class="corpus-create-dialog">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="corpus-create-dialog__form"
      >
        <el-form-item v-if="isVariantSwitchEnabled" label="语料类型" required>
          <el-radio-group
            :model-value="currentVariant"
            class="corpus-create-dialog__variant-switch"
            @change="handleVariantSwitch"
          >
            <el-radio-button label="文本" value="text" />
            <el-radio-button label="问卷" value="survey" />
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="subjectLabel" prop="subject">
          <el-input v-model.trim="form.subject" placeholder="请输入" maxlength="100" />
        </el-form-item>
        <el-form-item v-if="!isSurveyCorpus" label="语料描述" prop="description">
          <el-input v-model.trim="form.description" placeholder="请输入" maxlength="100" />
        </el-form-item>
        <el-form-item label="标准观点" prop="standardOpinionId">
          <el-select-v2
            v-model="form.standardOpinionId"
            :options="props.standardOpinions"
            :props="{ label: 'tagName', value: 'tagCode' }"
            filterable
            clearable
            :placeholder="standardOpinionPlaceholder"
            class="w-full"
            :loading="props.standardOpinionLoading"
          />
        </el-form-item>
        <el-form-item label="是否启用" prop="enableStatus">
          <el-radio-group v-model="form.enableStatus">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>
  </AppDialog>
</template>

<style lang="scss" scoped>
.corpus-create-dialog {
  padding-top: 8px;
}

.corpus-create-dialog__form {
  width: 560px;
  margin: 0 auto;
}

.corpus-create-dialog__variant-switch {
  .el-radio-button + .el-radio-button {
    margin-left: 16px;
  }

  :deep(.el-radio-button__inner) {
    width: 80px;
    height: 32px;
    line-height: 30px;
    padding: 0;
    border: 1px solid #dfe2e8 !important;
    border-radius: 4px !important;
    color: #606266;
    font-size: 16px;
    font-weight: 500;
    background-color: #fff !important;
    box-shadow: none !important;
  }

  :deep(.el-radio-button.is-active .el-radio-button__inner) {
    color: var(--el-color-primary) !important;
    border-color: var(--el-color-primary) !important;
    background-color: #fff !important;
    box-shadow: none !important;
  }
}
</style>
