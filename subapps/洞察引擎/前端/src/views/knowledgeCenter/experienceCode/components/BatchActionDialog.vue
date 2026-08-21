<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { batchMoveTagLibClient, batchUpdateStatusTagLibClient } from '@/api/tag'
import useUserStore from '@/stores/modules/user'
import {
  BATCH_ACTION_LABEL_MAP,
  type BatchActionType,
  type BatchMoveDialogForm,
  type ExperienceCategoryItem,
  type ExperienceCodeTableRow,
  type ExperienceCodeType
} from './types'
import { resolveLeafCategoryOptions } from './categoryUtils'

defineOptions({
  name: 'ExperienceCodeBatchActionDialog'
})

interface Props {
  visible: boolean
  actionType: BatchActionType
  selectedRows: ExperienceCodeTableRow[]
  categoryList: ExperienceCategoryItem[]
  targetTypeCode: ExperienceCodeType
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const userStore = useUserStore()
const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => BATCH_ACTION_LABEL_MAP[props.actionType])

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)
const formState = reactive<BatchMoveDialogForm>({
  categoryId: ''
})

/**
 * 目标末级分类必须跟随页面外部当前选中的体验代码类型，避免弹框内再次切换类型后误移动到别的分组。
 * 外部类型偶发为空时，再兜底使用首条勾选记录的类型，保证弹框仍可继续操作。
 */
const currentTargetTypeCode = computed(() => {
  return (
    String(props.targetTypeCode || '').trim() || String(props.selectedRows[0]?.tagType ?? '').trim()
  )
})

/**
 * 移动场景下仅提供当前类型的末级分类作为目标位置，顺序完全跟随接口返回。
 */
const leafCategoryOptions = computed(() => {
  return resolveLeafCategoryOptions(props.categoryList, currentTargetTypeCode.value)
})

/**
 * 切换所属分类后重建末级分类下拉，避免 Element Plus 复用旧弹层时保留上一次滚动位置。
 */
const leafCategorySelectKey = computed(() => `batch-move-category-${currentTargetTypeCode.value}`)

/**
 * 批量移动后的层级始终挂在目标末级分类下，因此需要沿用分类 level 并顺延一层。
 */
const selectedCategory = computed(() => {
  return props.categoryList.find(item => item.id === formState.categoryId) || null
})

/**
 * 每次打开弹框时只重置目标末级分类，体验代码类型直接跟随外部选中结果，不再允许弹框内修改。
 */
const initForm = () => {
  formState.categoryId = ''
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    initForm()
    formRef.value?.clearValidate()
  }
)

watch(
  () => [currentTargetTypeCode.value, leafCategoryOptions.value],
  () => {
    if (!dialogVisible.value) return
    const validCategory = leafCategoryOptions.value.some(
      item => item.value === formState.categoryId
    )
    if (!validCategory) {
      formState.categoryId = ''
    }
  },
  { deep: true }
)

const formRules: FormRules<BatchMoveDialogForm> = {
  categoryId: [{ required: true, message: '请选择目标末级分类', trigger: 'change' }]
}

/**
 * 批量状态接口要求使用 0/1 口径，这里统一做一次显式映射，避免页面状态文案和接口值混用。
 */
const resolveBatchStatusValue = (actionType: Exclude<BatchActionType, 'move'>) => {
  return actionType === 'enable' ? '1' : '0'
}

/**
 * 批量操作统一走真实接口；移动仅提交目标分类 id，状态变更仅提交接口约定的 tagStatus。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (confirmLoading.value) return
  confirmLoading.value = true
  try {
    const ids = props.selectedRows
      .map(item => String(item.id ?? '').trim())
      .filter(id => Boolean(id))
    if (props.actionType === 'move') {
      await formRef.value?.validate()
      await batchMoveTagLibClient({
        appClient: userStore.clientId,
        ids,
        tagParentId: formState.categoryId,
        level: (selectedCategory.value?.level || 0) + 1
      })
      ElMessage.success('批量移动成功')
    } else {
      await batchUpdateStatusTagLibClient({
        appClient: userStore.clientId,
        ids,
        tagStatus: resolveBatchStatusValue(props.actionType)
      })
      ElMessage.success(props.actionType === 'enable' ? '批量启用成功' : '批量禁用成功')
    }
    emit('success')
    close()
  } finally {
    confirmLoading.value = false
  }
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="actionType === 'move' ? '560px' : '400px'"
    :confirm="handleConfirm"
  >
    <div v-if="actionType !== 'move'">
      是否确认{{ BATCH_ACTION_LABEL_MAP[actionType] }}选中的体验代码？
    </div>

    <el-form
      v-else
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="84px"
      class="dialog-form"
    >
      <el-form-item label="所属分类" prop="categoryId" required>
        <el-select
          :key="leafCategorySelectKey"
          v-model="formState.categoryId"
          style="width: 100%"
          placeholder="请选择目标末级分类"
          filterable
        >
          <el-option
            v-for="item in leafCategoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.dialog-form {
  width: 100%;
  margin: 0 auto;
}
</style>
