<script setup lang="ts">
import { computed, ref } from 'vue'
import RuleForm from './RuleForm.vue'
import type { BatchRuleRecord } from '../types'

defineOptions({
  name: 'BatchEventRuleFormDialog'
})

interface Props {
  ruleData?: Partial<BatchRuleRecord> | null
}

const props = withDefaults(defineProps<Props>(), {
  ruleData: null
})

const dialogVisible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const ruleFormRef = ref<InstanceType<typeof RuleForm> | null>(null)

const isEdit = computed(() => !!props.ruleData?.ruleId)

const handleConfirm = (payload: { close: () => void }) => {
  return ruleFormRef.value?.onConfirm(payload)
}

const handleSuccess = () => {
  emit('success')
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="isEdit ? '编辑规则' : '新建规则'"
    width="1020px"
    body-class="batch-rule-form-dialog-body"
    style="display: flex; flex-direction: column; height: 894px"
    :confirm="handleConfirm"
  >
    <RuleForm
      ref="ruleFormRef"
      :visible="dialogVisible"
      :ruleData="props.ruleData"
      @success="handleSuccess"
    />
  </AppDialog>
</template>

<style lang="scss">
.batch-rule-form-dialog-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
