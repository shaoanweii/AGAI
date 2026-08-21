<template>
  <AppDialog
    v-model:visible="visible"
    width="760px"
    destroy-on-close
    :confirm="handleConfirm"
    @close="handleCorrectionDialogClose"
  >
    <template #header>纠错申请</template>
    <div class="correction-dialog__content">
      <div class="correction-dialog__tip">是否确认提交数据纠错？</div>
      <el-table :data="tableData" size="large" class="mt-16">
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="intention" label="用户意图" min-width="120" />
        <el-table-column prop="experience" label="体验代码" min-width="200" />
        <el-table-column prop="topic" label="标准观点" min-width="120" />
      </el-table>
    </div>
  </AppDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppDialog from '@/components/AppDialog.vue'
import { ElMessage } from 'element-plus'
import { insertLabelCorrection } from '@/api/labelCorrection'
import type { InsertLabelCorrectionParams } from '@/api/labelCorrection'
import { formatDate } from '@/utils'
import { updateTagBase } from '@/api/singlePointEvent'

type CorrectionRow = {
  status: string
  intention: string
  experience: string
  topic: string
}

const visible = defineModel<boolean>('visible', { default: false })

type IntentionSnapshot = {
  intentionType: string
  domTagFirstCode: string
  domTagSecondCode: string
  domTagThreeCode: string
  domTagFourCode: string
  topic: string
}

type TagOption = {
  tagCode?: string
  tagName?: string
}

type GetTagOptions = (level: number, item: any) => TagOption[]

const props = withDefaults(
  defineProps<{
    currentItem?: any | null
    originalSnapshot?: IntentionSnapshot | null
    intentionOptions?: Array<{ value: string; text: string }>
    getTagOptions?: GetTagOptions
    startTime?: string
    endTime?: string
    row?: any
  }>(),
  {
    currentItem: null,
    originalSnapshot: null,
    intentionOptions: () => [],
    getTagOptions: () => [],
    row: () => {},
    startTime: undefined,
    endTime: undefined
  }
)

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const handleCorrectionDialogClose = () => {
  emit('close')
}

const getIntentionLabel = (value: string) => {
  if (!value) return ''
  return props.intentionOptions?.find(item => item.value === value)?.text || value
}

const getTagName = (level: number, code: string, item: any) => {
  if (!code) return ''
  const options = props.getTagOptions?.(level, item) || []
  return options.find(opt => opt.tagCode === code)?.tagName || code
}

const formatExperienceCode = (item: any) => {
  if (!item) return ''
  const names = [
    getTagName(1, item.domTagFirstCode, item),
    getTagName(2, item.domTagSecondCode, item),
    getTagName(3, item.domTagThreeCode, item),
    getTagName(4, item.domTagFourCode, item)
  ].filter(Boolean)
  return names.length ? names.join('#') : '-'
}

const getTopicInfo = (item: any) => {
  if (!item?.topic) return { code: '', name: '' }
  const matched = (item.topicOptions || []).find((option: any) => {
    return (
      option.tagName === item.topic ||
      option.name === item.topic ||
      option.tagCode === item.topic ||
      option.code === item.topic
    )
  })
  return {
    code: matched?.tagCode || matched?.code || item.topic,
    name: matched?.tagName || matched?.name || item.topic
  }
}

const formatApiDate = (value?: string) => {
  if (!value) return undefined
  const formatted = formatDate(value, 'YYYY-MM-DD')
  return formatted === '-' ? undefined : formatted
}

const tableData = computed<CorrectionRow[]>(() => {
  const item = props.currentItem
  if (!item) return []
  const originalSnapshot = props.originalSnapshot
  const beforeItem = originalSnapshot
    ? {
        domTagFirstCode: originalSnapshot.domTagFirstCode,
        domTagSecondCode: originalSnapshot.domTagSecondCode,
        domTagThreeCode: originalSnapshot.domTagThreeCode,
        domTagFourCode: originalSnapshot.domTagFourCode
      }
    : item
  const formatValue = (value: string) => value || '-'
  return [
    {
      status: '纠错前',
      intention: originalSnapshot
        ? formatValue(getIntentionLabel(originalSnapshot.intentionType))
        : '-',
      experience: formatExperienceCode(beforeItem),
      topic: formatValue(originalSnapshot?.topic || '')
    },
    {
      status: '纠错后',
      intention: formatValue(getIntentionLabel(item.intentionType || '')),
      experience: formatExperienceCode(item),
      topic: formatValue(item.topic || '')
    }
  ]
})

const updateTag = async () => {
  try {
    const item = props.currentItem
    if (!item) return
    const { topicOptions: _topicOptions, ...rest } = item
    const params = {
      id: props.row.id,
      intentions: [
        {
          ...rest,
          domTagFirst: props
            .getTagOptions?.(1, item)
            .find((opt: any) => opt.tagCode === item.domTagFirstCode)?.tagName,
          domTagSecond: props
            .getTagOptions?.(2, item)
            .find((opt: any) => opt.tagCode === item.domTagSecondCode)?.tagName,
          domTagThree: props
            .getTagOptions?.(3, item)
            .find((opt: any) => opt.tagCode === item.domTagThreeCode)?.tagName,
          domTagFour: props
            .getTagOptions?.(4, item)
            .find((opt: any) => opt.tagCode === item.domTagFourCode)?.tagName
        }
      ]
    }
    const res = await updateTagBase(params)
    if (!res.success) {
      ElMessage.warning(res.message)
    }
  } catch (error) {
    console.log('error', error)
  }
}

const handleConfirm = async ({ close }: { close: () => void }) => {
  const item = props.currentItem
  if (!item) {
    ElMessage.warning('未找到纠错数据')
    return
  }
  const { code: topicCode, name: topicName } = getTopicInfo(item)
  if (!topicCode) {
    ElMessage.warning('标准观点为空，无法提交')
    return
  }
  const params: InsertLabelCorrectionParams = {
    newId: props.currentItem.id ? [props.currentItem.id] : [],
    errorType: '2',
    intention: item.intentionType || '',
    topicCode,
    topicName,
    startTime: formatApiDate(props.startTime),
    endTime: formatApiDate(props.endTime)
  }
  try {
    const res = await insertLabelCorrection(params)
    if (res.success) {
      ElMessage.success('纠错申请已提交')
      await updateTag()
      close()
      emit('success')
      return
    }
    ElMessage.warning(res.message || '操作失败')
  } catch (error: any) {
    // ElMessage.warning(error?.message || '操作失败')
    console.log('error', error)
  }
}
</script>

<style scoped lang="scss">
.correction-dialog__content {
  .correction-dialog__tip {
    font-size: 16px;
    font-weight: 500;
    color: #1f2733;
  }
}
</style>
