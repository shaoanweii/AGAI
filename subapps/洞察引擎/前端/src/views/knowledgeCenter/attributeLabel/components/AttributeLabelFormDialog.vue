<script setup lang="ts">
import { computed, inject, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import {
  attributeLabelEnableStatus,
  createAttributeLabel,
  updateAttributeLabel
} from '@/api/attributeLabel'

defineOptions({
  name: 'AttributeLabelFormDialog'
})

interface Props {
  visible: boolean
  formData: Api.AttributeLabel.RecordItem | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

interface AttributeLabelFormState {
  id: string
  name: string
  status: Api.Common.EnableStatus
}

interface StatusOption {
  label: string
  value: Api.Common.EnableStatus
}

const injectedConditions = inject<Record<string, ConditionsDetailItem[]>>('conditions', {})

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.formData?.id ? '编辑属性标签' : '新建属性标签'))

const formRef = ref<FormInstance>()
const formState = reactive<AttributeLabelFormState>({
  id: '',
  name: '',
  status: attributeLabelEnableStatus.ENABLED
})

/**
 * 状态选项仅复用页面已加载的字典，缺失时返回空数组，避免前端擅自兜底污染后端口径。
 */
const statusOptions = computed<StatusOption[]>(() => {
  const options = Array.isArray(injectedConditions.stopOrEnable)
    ? injectedConditions.stopOrEnable
    : []
  const normalizedOptions = options
    .map(item => {
      const value = String(item?.key ?? '').trim()
      const label = String(item?.value ?? '').trim()
      return {
        label,
        value: value
      }
    })
    .filter((item): item is StatusOption => Boolean(item))

  return normalizedOptions
})

/**
 * 默认值固定优先选择“启用”，避免后续字典顺序调整后影响新建态默认状态。
 */
const resolveDefaultStatus = () => {
  return (
    statusOptions.value.find(item => item.value === attributeLabelEnableStatus.ENABLED)?.value ||
    attributeLabelEnableStatus.ENABLED
  )
}

/**
 * 弹框每次打开时统一重建表单数据，确保新增/编辑共用同一套校验和提交流程。
 */
const initForm = () => {
  formState.id = String(props.formData?.id || '')
  formState.name = String(props.formData?.name || '')
  formState.status =
    props.formData?.status === attributeLabelEnableStatus.DISABLED
      ? attributeLabelEnableStatus.DISABLED
      : resolveDefaultStatus()
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    initForm()
    formRef.value?.clearValidate()
  }
)

/**
 * 标签名称仅允许提交去空格后的有效内容，并控制在 30 个字符以内。
 */
const validateName = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  const normalizedValue = value?.trim()
  if (!normalizedValue) {
    callback(new Error('请输入标签名称'))
    return
  }
  if (normalizedValue.length > 30) {
    callback(new Error('标签名称不能超过30个字符'))
    return
  }
  callback()
}

const formRules: FormRules<AttributeLabelFormState> = {
  name: [{ required: true, validator: validateName, trigger: 'blur' }]
  // status: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}

/**
 * 保存成功后仅向父组件抛出刷新信号，列表刷新策略统一由页面层收口。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value) return

  await formRef.value.validate()

  const payload = {
    id: formState.id || undefined,
    name: formState.name.trim(),
    status: formState.status
  }
  const isEdit = Boolean(props.formData?.id)

  if (isEdit) {
    await updateAttributeLabel(payload)
    ElMessage.success('编辑成功')
  } else {
    await createAttributeLabel(payload)
    ElMessage.success('创建成功')
  }

  emit('success')
  close()
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    width="400px"
    :confirm="handleConfirm"
  >
    <el-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="80px"
      class="attribute-label-form"
    >
      <el-form-item label="标签名称" prop="name" required>
        <el-input v-model.trim="formState.name" maxlength="30" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="是否启用" prop="status">
        <el-radio-group v-model="formState.status">
          <el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.attribute-label-form {
  width: 100%;
  margin: 0 auto;
}
</style>
