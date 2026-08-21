<script setup lang="ts">
import { computed, ref } from 'vue'
import RuleForm from './RuleForm.vue'

defineOptions({ name: 'RuleFormDialog' })

interface Props {
  ruleData?: any
}

// 接收父组件传入的参数
const props = withDefaults(defineProps<Props>(), { ruleData: null })

// 弹框显示状态，由父级使用 v-model:visible 控制
const dialogVisible = defineModel<boolean>('visible', { default: false })

// 向父级抛出事件（例如保存成功后刷新列表）
const emit = defineEmits<{ (e: 'success'): void }>()

// 是否为编辑态（兼容 id 与 ruleId 两种主键字段）
const isEdit = computed(
  () => !!props.ruleData && (!!(props.ruleData as any).id || !!(props.ruleData as any).ruleId)
)

// 子表单组件实例，用于调用内部暴露的方法
const ruleFormRef = ref<InstanceType<typeof RuleForm> | null>(null)

// 弹框确认时，直接转发调用子组件的 onConfirm 方法
const onConfirm = (payload: { close: () => void }) => {
  return ruleFormRef.value?.onConfirm(payload)
}

// 子组件保存成功后，向外层透传 success 事件
const handleSuccess = () => {
  emit('success')
}
</script>

<template>
  <!-- 基于 AppDialog 的统一弹框 -->
  <AppDialog
    v-model:visible="dialogVisible"
    :title="isEdit ? '编辑规则' : '新建规则'"
    width="800px"
    body-class="rule-form-dialog-body"
    style="display: flex; flex-direction: column; height: 894px"
    :confirm="onConfirm"
  >
    <!-- 将所有入参透传给 RuleForm，并传入当前可见状态 -->
    <RuleForm
      ref="ruleFormRef"
      :visible="dialogVisible"
      :ruleData="props.ruleData"
      @success="handleSuccess"
    />
  </AppDialog>
</template>

<style lang="scss">
.rule-form-dialog-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
