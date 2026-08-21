<template>
  <FDialog
    v-model:visible="visible"
    width="480px"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <template #header>
      <span>{{ title }}</span>
    </template>
    <div class="flex items-center">
      <el-icon class="info-icon" :size="20">
        <InfoFilled />
      </el-icon>
      <span class="ml-8">{{ message }}</span>
    </div>
  </FDialog>
</template>

<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue'
import FDialog from '@/components/UI/FDialog/index.vue'

defineOptions({
  name: 'ConfirmDialog'
})

interface Props {
  title: string
  message: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const visible = defineModel<boolean>('visible', { default: false })

const handleConfirm = () => {
  visible.value = false
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style lang="scss" scoped>
.info-icon {
  color: #1677ff;
}
</style>
