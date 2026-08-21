<script setup lang="ts">
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'
import { ElMessage, type FormInstance } from 'element-plus'
import { TipTitleMap, TipInfoMap, TipType } from '../constants'
import { updateCustomReport, reviewReport } from '@/api/sceneAnalysis'
import { useLoading } from '@/hooks/useLoading'

// 二次确认提示弹窗
defineOptions({
  name: 'TipDialog'
})

const emits = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const visible = defineModel({ default: false })
const { type, data } = defineProps<{
  type: TipType
  data: any
}>()

const { showLoading, hideLoading } = useLoading()

const handleCancel = () => {
  visible.value = false
  emits('cancel')
}

const handleConfirm = debounce(
  async ({ close }) => {
    const statusMap = {
      [TipType.Release]: '1',
      [TipType.Delisted]: '2'
    }
    const textMap = {
      [TipType.Release]: { loading: '发布中...', success: '发布成功', error: '发布失败' },
      [TipType.Delisted]: { loading: '下架中...', success: '下架成功', error: '下架失败' }
    }

    const text = textMap[type]
    showLoading({ text: text.loading })
    try {
      const res = await updateCustomReport({ id: data.record.id, status: statusMap[type] })
      if (res.success) {
        ElMessage.success(text.success)
        emits('confirm')
        close()
      } else {
        ElMessage.error(res.message || text.error)
      }
    } catch (error: any) {
      ElMessage.error(error.message || text.error)
    } finally {
      hideLoading()
    }
  },
  300,
  { leading: true, trailing: false }
)

const handleOpen = async () => {}

const handleClose = () => {
  emits('close')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="480px"
    @open="handleOpen"
    :confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #header>
      <span>{{ TipTitleMap[type] }}</span>
    </template>
    <div class="flex items-center">
      <SvgIcon name="info-circle-filled" width="20px" height="20px" />
      <span class="ml-8">{{ TipInfoMap[type] }}</span>
    </div>
  </FDialog>
</template>

<style lang="scss" scoped>
.selected-tag {
  background: #eaf3ff;
  border-radius: 4px 4px 4px 4px;
  padding: 1px 12px;
  font-size: 14px;
  color: #1677ff;
  line-height: 22px;
}
</style>
