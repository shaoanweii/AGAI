<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { CascaderProps } from 'element-plus'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import type { ProcessingProgressCcSelectionItem } from './types'
import {
  buildProcessingProgressCcCascaderOptionMap,
  buildProcessingProgressCcCascaderOptions,
  mapProcessingProgressCcSelectionsToCascaderValues,
  mapProcessingProgressCcValuesToSelections,
  type ProcessingProgressCcCascaderOption
} from './personnelTree'

defineOptions({
  name: 'ProcessingProgressAddCcDialog'
})

const visible = defineModel<boolean>('visible', { default: false })

const props = defineProps<{
  selectedItems: ProcessingProgressCcSelectionItem[]
  departAccountTree: InsReportSysDepartVo[]
}>()

const emit = defineEmits<{
  confirm: [selectedItems: ProcessingProgressCcSelectionItem[]]
}>()

const draftValues = ref<string[]>([])

const ccCascaderOptions = computed(() => {
  return buildProcessingProgressCcCascaderOptions(props.departAccountTree)
})

const ccCascaderOptionMap = computed(() => {
  return buildProcessingProgressCcCascaderOptionMap(ccCascaderOptions.value)
})

const ccCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  multiple: true,
  emitPath: false,
  checkStrictly: true
} satisfies CascaderProps

const draftSelections = computed(() => {
  return mapProcessingProgressCcValuesToSelections(draftValues.value, ccCascaderOptionMap.value)
})

/**
 * 深拷贝当前抄送人员草稿，避免弹窗内编辑直接污染父层已确认数据。
 * @param items 当前已确认的抄送人员
 * @returns 可在弹窗中安全编辑的副本
 */
const cloneSelections = (
  items: ProcessingProgressCcSelectionItem[]
): ProcessingProgressCcSelectionItem[] => {
  return items.map(item => ({
    orgId: item.orgId || '',
    orgNo: item.orgNo || '',
    orgName: item.orgName || '',
    allFlag: Boolean(item.allFlag),
    userId: item.userId || '',
    userEmpNo: item.userEmpNo || '',
    userName: item.userName || ''
  }))
}

/**
 * 按父层已确认抄送人员同步 Cascader 回显值。
 */
const syncDraftValuesFromSelectedItems = () => {
  draftValues.value = mapProcessingProgressCcSelectionsToCascaderValues(
    cloneSelections(props.selectedItems),
    ccCascaderOptionMap.value
  )
}

/**
 * 过滤部门、人员、工号和完整层级路径。
 * @param node Element Plus Cascader 节点
 * @param keyword 搜索关键字
 * @returns 是否命中
 */
const filterCcNode = (node: unknown, keyword: string) => {
  const q = String(keyword || '')
    .trim()
    .toLowerCase()
  if (!q) return true

  const cascaderNode = node as {
    data?: ProcessingProgressCcCascaderOption
    text?: string
    pathLabels?: string[]
  }
  const option = cascaderNode.data
  const searchableText = [
    cascaderNode.text,
    ...(cascaderNode.pathLabels || []),
    option?.label,
    option?.orgName,
    option?.userName,
    option?.userEmpNo,
    option?.fullLabel,
    option?.filterText
  ]
    .filter(Boolean)
    .join('#')
    .toLowerCase()

  return searchableText.includes(q)
}

watch(
  () => visible.value,
  newValue => {
    if (!newValue) {
      return
    }

    syncDraftValuesFromSelectedItems()
  }
)

watch(
  () => [props.selectedItems, ccCascaderOptionMap.value],
  () => {
    if (visible.value) {
      syncDraftValuesFromSelectedItems()
    }
  },
  { deep: true }
)

watch(
  () => draftValues.value,
  values => {
    if (!Array.isArray(values)) {
      draftValues.value = []
      return
    }

    const uniqueValues = Array.from(new Set(values))
    if (uniqueValues.length !== values.length) {
      draftValues.value = uniqueValues
    }
  }
)

/**
 * 确认抄送人员选择。
 * 至少保留一个抄送对象，避免“添加抄送人员”动作落空。
 * @param close 关闭弹窗回调
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!draftSelections.value.length) {
    ElMessage.warning('请选择抄送人员')
    return
  }

  emit('confirm', cloneSelections(draftSelections.value))
  close()
}

/**
 * 取消时仅关闭弹窗，不回写草稿态。
 */
const handleCancel = () => {
  visible.value = false
}
</script>

<template>
  <FDialog v-model:visible="visible" width="720px" :confirm="handleConfirm" @cancel="handleCancel">
    <template #header>
      <span>添加抄送人员</span>
    </template>

    <div class="processing-progress-add-cc-dialog">
      <el-form label-width="84px" @submit.prevent>
        <el-form-item
          label="抄送人员"
          required
          class="processing-progress-add-cc-dialog__form-item"
        >
          <el-cascader
            v-model="draftValues"
            :options="ccCascaderOptions"
            :props="ccCascaderProps"
            :filter-method="filterCcNode"
            placeholder="请选择抄送人员"
            filterable
            clearable
            collapse-tags
            :max-collapse-tags="1"
            separator="#"
            class="processing-progress-add-cc-dialog__cascader"
          />
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style scoped lang="scss">
.processing-progress-add-cc-dialog {
  padding-top: 8px;
}

.processing-progress-add-cc-dialog__form-item {
  margin-bottom: 0;
}

.processing-progress-add-cc-dialog__form-item :deep(.el-form-item__content) {
  width: 100%;
}

.processing-progress-add-cc-dialog__cascader {
  display: block;
  width: 100%;
}

.processing-progress-add-cc-dialog :deep(.el-cascader) {
  width: 100%;
}

.processing-progress-add-cc-dialog :deep(.el-cascader .el-input__wrapper) {
  min-height: 36px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}
</style>
