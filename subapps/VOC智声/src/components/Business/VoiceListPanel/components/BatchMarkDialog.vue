<template>
  <AppDialog
    v-model:visible="visible"
    width="600px"
    destroy-on-close
    :confirm="handleConfirm"
  >
    <template #header>批量标记</template>
    
    <div class="batch-mark-dialog__content py-24 px-32">
      <div class="flex-y-center">
        <span class="fs-14 fw-400 text-secondary mr-16" style="white-space: nowrap;">标记类型</span>
        <el-radio-group v-model="markType">
          <el-radio-button :value="1">标记为高质量</el-radio-button>
          <el-radio-button :value="0">取消高质量标记</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <HighQualityConfirmDialog
      v-if="confirmVisible"
      v-model:visible="confirmVisible"
      :is-marking="markType === 1"
      :confirm-func="confirmFunc"
      @success="handleConfirmSuccess"
    />
  </AppDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import HighQualityConfirmDialog from './HighQualityConfirmDialog.vue'
import { highQuality, highQualityDel } from '@/api/overview/leader'

const visible = defineModel<boolean>('visible', { default: false })

const props = withDefaults(
  defineProps<{
    ids: (string | number)[]
  }>(),
  { ids: () => [] }
)

const emit = defineEmits<{
  (e: 'success'): void
}>()

const markType = ref(1)

const confirmVisible = ref(false)
const confirmFunc = ref<() => Promise<any>>(async () => {})

const submitBatch = async () => {
  const api = markType.value === 1 ? highQuality : highQualityDel

  // Convert all IDs to string as API likely expects strings
  const strIds = props.ids.map(String)

  const res = await api(strIds)
  if (res.success) {
    ElMessage.success(
      markType.value === 1
        ? '高质量标记申请已提交，预计下一轮数据更新时生效，请勿重复操作。'
        : '取消标记成功，预计下一轮数据更新时生效，请勿重复操作。'
    )
    emit('success')
    visible.value = false
  } else {
    ElMessage.error(res.message || '操作失败')
    throw new Error(res.message || '操作失败')
  }
}

const handleConfirm = ({ close }: { close: () => void }) => {
  if (!props.ids.length) {
    close()
    return
  }
  
  confirmFunc.value = submitBatch
  confirmVisible.value = true
}

const handleConfirmSuccess = () => {
  // logic handled in submitBatch, just close parent if needed, but submitBatch already closes parent
}
</script>

<style scoped lang="scss">
.text-secondary {
  color: #4E5969;
}
.fs-14 {
  font-size: 14px;
}
.fw-400 {
  font-weight: 400;
}
</style>
