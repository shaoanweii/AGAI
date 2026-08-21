<script setup lang="ts">
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'
import { ElMessage, type FormInstance } from 'element-plus'
import { BatchTitleMap, ReportBatchType, reviewOptions } from '../constants'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import { getSpecialZoneOptions } from '@/api/overview'
import { updateCustomReport, reviewReport } from '@/api/sceneAnalysis'
import { useLoading } from '@/hooks/useLoading'

// 批量处理弹窗
defineOptions({
  name: 'BatchDialog'
})

const emits = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const visible = defineModel({ default: false })
const { type, selection } = defineProps<{
  type: ReportBatchType
  selection: any[]
}>()

const { showLoading, hideLoading } = useLoading()

const formDataRef = ref<FormInstance>()
const formData = ref<{
  firstLevelZoneId: string | undefined
  specialTypeId: string | undefined
  status: string
}>({
  firstLevelZoneId: undefined,
  specialTypeId: undefined,
  status: '1'
})

// 分类选项
const zoneOptions = ref<any[]>([])

// 专区选项 - 根据选中的分类动态计算
const specialZoneOptions = computed(() => {
  if (!formData.value.firstLevelZoneId || !zoneOptions.value?.length) return []
  const selectedZone = zoneOptions.value.find(zone => zone.id === formData.value.firstLevelZoneId)
  return selectedZone?.children || []
})

const ids = computed(() => {
  return selection?.map(item => item.id) || []
})

// 获取分类和专区选项
const getZoneOptions = async () => {
  try {
    const res = await getSpecialZoneOptions({})
    if (res.success) {
      zoneOptions.value = res.result
    } else {
      zoneOptions.value = []
    }
  } catch (error: any) {
    ElMessage.error(error.message)
  }
}

// 分类变化时清空专区
const handleZoneChange = () => {
  formData.value.specialTypeId = undefined
}

const clearFormData = () => {
  formData.value = {
    firstLevelZoneId: undefined,
    specialTypeId: undefined,
    status: '1'
  }
  formDataRef.value?.clearValidate()
}

const handleCancel = () => {
  visible.value = false
  emits('cancel')
}

// 通用批量操作处理函数
const executeBatchOperation = async (
  loadingText: string,
  successText: string,
  errorText: string,
  params: Record<string, any>,
  close: () => void,
  needValidate = false
) => {
  if (needValidate) {
    try {
      await formDataRef.value?.validate()
    } catch (error) {
      return
    }
  }

  showLoading({ text: loadingText })
  try {
    const res = await updateCustomReport({ ids: ids.value, ...params })
    if (res.success) {
      ElMessage.success(successText)
      emits('confirm')
      close()
    } else {
      ElMessage.error(res.message || errorText)
    }
  } catch (error: any) {
    ElMessage.error(error.message || errorText)
  } finally {
    hideLoading()
  }
}

// 审核操作
const handleReview = async (close: () => void) => {
  showLoading({ text: '审核中...' })
  try {
    const res = await reviewReport({ ids: ids.value, status: formData.value.status })
    if (res.success) {
      ElMessage.success('审核成功')
      emits('confirm')
      close()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    hideLoading()
  }
}

// 发布操作
const handleRelease = (close: () => void) =>
  executeBatchOperation('发布中...', '发布成功', '发布失败', { status: '1' }, close)

// 下架操作
const handleDelisted = (close: () => void) =>
  executeBatchOperation('下架中...', '下架成功', '下架失败', { status: '2' }, close)

// 移动操作
const handleMove = (close: () => void) =>
  executeBatchOperation(
    '移动中...',
    '移动成功',
    '移动失败',
    {
      firstLevelZoneId: formData.value.firstLevelZoneId,
      specialTypeId: formData.value.specialTypeId
    },
    close,
    true
  )

const handleConfirm = debounce(
  async ({ close }) => {
    if (type === ReportBatchType.Review) {
      await handleReview(close)
    } else if (type === ReportBatchType.Release) {
      await handleRelease(close)
    } else if (type === ReportBatchType.Delisted) {
      await handleDelisted(close)
    } else if (type === ReportBatchType.Move) {
      await handleMove(close)
    }
  },
  300,
  { leading: true, trailing: false }
)

const handleOpen = async () => {
  await getZoneOptions()
}

const handleClose = () => {
  clearFormData()
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
      <span>{{ BatchTitleMap[type] }}</span>
    </template>
    <div>
      <el-form :model="formData" ref="formDataRef" @submit.prevent>
        <!-- 审核 -->
        <template v-if="type === ReportBatchType.Review">
          <el-form-item label="审核条数" prop="">
            <div class="selected-tag">{{ ids?.length }}</div>
          </el-form-item>
          <el-form-item label="审核方式" prop="">
            <OptionToggleGroup v-model="formData.status" :options="reviewOptions" />
          </el-form-item>
        </template>
        <!-- 发布 -->
        <template v-if="type === ReportBatchType.Release">
          <el-form-item label="发布条数" prop="">
            <div class="selected-tag">{{ ids?.length }}</div>
          </el-form-item>
        </template>
        <!-- 下架 -->
        <template v-if="type === ReportBatchType.Delisted">
          <el-form-item label="下架条数" prop="">
            <div class="selected-tag">{{ ids?.length }}</div>
          </el-form-item>
        </template>
        <!-- 移动 -->
        <template v-if="type === ReportBatchType.Move">
          <!-- 分类 -->
          <el-form-item
            label="选择分类"
            prop="firstLevelZoneId"
            :rules="[{ required: true, message: '请选择分类', trigger: 'change' }]"
            label-width="90px"
          >
            <el-select
              v-model="formData.firstLevelZoneId"
              placeholder="请选择分类"
              clearable
              filterable
              :options="zoneOptions"
              :props="{ label: 'name', value: 'id' }"
              class="flex-1"
              @change="handleZoneChange"
            >
            </el-select>
          </el-form-item>
          <!-- 专区 -->
          <el-form-item label="选择专区" prop="specialTypeId" label-width="90px">
            <el-select
              v-model="formData.specialTypeId"
              placeholder="请选择专区"
              clearable
              filterable
              :options="specialZoneOptions"
              :props="{ label: 'name', value: 'id' }"
              class="flex-1"
            >
            </el-select>
          </el-form-item>
        </template>
      </el-form>
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
