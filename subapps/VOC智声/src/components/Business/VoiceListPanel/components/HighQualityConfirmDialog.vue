<template>
  <AppDialog
    v-model:visible="visible"
    width="600px"
    destroy-on-close
    :confirm="handleConfirm"
  >
    <template #header>{{ dialogTitle }}</template>
    
    <div class="confirm-dialog__content pt-16 pb-16 px-32">
      <div class="fs-14 fw-400 text-primary">{{ dialogContent }}</div>
    </div>
  </AppDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'

const visible = defineModel<boolean>('visible', { default: false })

const props = withDefaults(
  defineProps<{
    isMarking: boolean
    confirmFunc: () => Promise<any>
  }>(),
  { isMarking: true }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const dialogTitle = computed(() => props.isMarking ? '标记为高质量' : '取消高质量标记')
const dialogContent = computed(() => props.isMarking ? '是否确认标记为高质量声音?' : '是否确认取消高质量标记?')

const handleConfirm = async ({ close }: { close: () => void }) => {
  try {
    await props.confirmFunc()
    emit('success')
    close()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>

<style scoped lang="scss">
.text-primary {
  color: #1D2129;
}
.fs-14 {
  font-size: 14px;
}
.fw-400 {
  font-weight: 400;
}
</style>
