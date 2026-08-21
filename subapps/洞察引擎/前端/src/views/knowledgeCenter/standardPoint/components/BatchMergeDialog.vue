<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { batchMergeTopicClient } from '@/api/tag'

defineOptions({
  name: 'StandardPointBatchMergeDialog'
})

interface Props {
  visible: boolean
  selectedTopics: SelectedTopicOption[]
}

interface MergeFormState {
  tagCode: string
}

interface SelectedTopicOption {
  topicCode?: string
  topicName?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const formRef = ref<FormInstance>()
const formState = reactive<MergeFormState>({
  tagCode: ''
})

const formRules: FormRules<MergeFormState> = {
  tagCode: [{ required: true, message: '请选择合并后保留的观点', trigger: 'change' }]
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return

    formState.tagCode = ''
    formRef.value?.clearValidate()
  }
)

/**
 * 仅展示当前已勾选的观点，名称为空时回退编码，避免下拉项出现空白。
 */
const selectOptions = computed(() => {
  return (props.selectedTopics || [])
    .map(item => ({
      tagCode: String(item?.topicCode || '').trim(),
      optionLabel: String(item?.topicName || item?.topicCode || '').trim()
    }))
    .filter(item => item.tagCode)
})

/**
 * 合并动作统一在弹窗内部提交，成功后由外层列表负责刷新数据。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const topicCodes = (props.selectedTopics || [])
    .map(item => String(item?.topicCode || '').trim())
    .filter(Boolean)
  if (topicCodes.length < 2) {
    ElMessage.warning('至少选择两个观点后再进行批量合并')
    return
  }

  try {
    await batchMergeTopicClient({
      topicCodes,
      tagCode: formState.tagCode
    })
    ElMessage.success('批量合并成功')
    emit('success')
    close()
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message)
      return
    }
    ElMessage.error('批量合并失败，请稍后重试')
  }
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    title="批量合并"
    width="680px"
    :confirm="handleConfirm"
  >
    <el-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="96px"
      class="batch-merge-dialog__form"
    >
      <el-form-item label="合并观点" prop="tagCode" required>
        <el-select-v2
          v-model="formState.tagCode"
          class="batch-merge-dialog__select"
          placeholder="请选择"
          clearable
          filterable
          :options="selectOptions"
          :props="{ label: 'optionLabel', value: 'tagCode' }"
        />
      </el-form-item>
    </el-form>

    <div class="batch-merge-dialog__tips">
      批量合并后，将保留唯一观点，其余观点全部被禁用，并且移动被禁用观点对应语料至当前合并观点下，请选择合并后保留的观点
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.batch-merge-dialog__form {
  margin: 0 auto 20px;
}

.batch-merge-dialog__select {
  width: 100%;
}

.batch-merge-dialog__tips {
  padding: 12px 16px;
  border-radius: 4px;
  background: #e2f3fe;
  color: #1677ff;
  font-weight: 400;
  font-size: 14px;
  color: #1677ff;
  line-height: 22px;
}
</style>
