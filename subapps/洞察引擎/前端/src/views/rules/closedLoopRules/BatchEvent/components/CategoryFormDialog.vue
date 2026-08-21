<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  createBatchRuleCategory,
  findBatchRuleCategoryList,
  updateBatchRuleCategory,
  type BatchRuleCategoryNode as ApiBatchRuleCategoryNode
} from '@/api/batchEventRules'
import type {
  BatchCategoryOption,
  BatchCategorySubmitResult,
  BatchCategoryTreeNode
} from '../types'

defineOptions({
  name: 'BatchEventCategoryFormDialog'
})

interface Props {
  categoryData?: BatchCategoryTreeNode | null
}

const props = withDefaults(defineProps<Props>(), {
  categoryData: null
})

const dialogVisible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  (e: 'success', payload: BatchCategorySubmitResult): void
}>()

const loading = ref(false)
const formRef = ref<FormInstance>()
const parentOptions = ref<BatchCategoryOption[]>([])

const form = reactive({
  name: '',
  parentId: ''
})

const isEdit = computed(() => !!props.categoryData?.id)
const isParentLocked = computed(() => {
  return (
    !!props.categoryData && !props.categoryData.parentId && props.categoryData.children.length > 0
  )
})

const rules = reactive<FormRules<typeof form>>({
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback()
          return
        }

        if (value.length > 10) {
          callback(new Error('分类名称最多输入10个字符'))
          return
        }

        if (!/^[A-Za-z0-9\u4e00-\u9fa5]+$/.test(value)) {
          callback(new Error('分类名称仅支持中英文和数字'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ]
})

/**
 * 分类保存统一按当前接口协议规整父级 ID：空值传 null，其余保留真实父级。
 * @param value 表单中的父级字段
 * @returns string | null
 */
const normalizeParentId = (value: unknown): string | null => {
  if (value === null || value === undefined) {
    return null
  }

  const normalizedValue = String(value).trim()
  return !normalizedValue || ['0', 'null', 'undefined'].includes(normalizedValue)
    ? null
    : normalizedValue
}

/**
 * 新建和编辑弹窗都只允许挂载到一级分类下，
 * 这里直接复用分类树接口结果生成下拉，避免保留额外中转文件。
 * @param currentId 当前编辑中的分类 ID
 * @returns Promise<BatchCategoryOption[]>
 */
const fetchParentOptions = async (currentId?: string): Promise<BatchCategoryOption[]> => {
  const response = await findBatchRuleCategoryList()

  if (!response.success) {
    throw new Error(response.message || '获取所属分类失败，请稍后重试')
  }

  return (response.result || [])
    .map<BatchCategoryOption>((item: ApiBatchRuleCategoryNode) => ({
      label: String(item.name || ''),
      value: String(item.id || '')
    }))
    .filter(item => item.value && item.value !== currentId)
}

/**
 * 分类新增/编辑成功后，接口当前只返回布尔值；
 * 因此列表刷新定位统一基于本次提交参数，避免继续耦合无效的 result 字段。
 * @param payload 本次提交参数
 * @returns BatchCategorySubmitResult
 */
const resolveSubmitResult = (payload: { id?: string; name: string }): BatchCategorySubmitResult => {
  return {
    categoryId: String(payload.id || ''),
    categoryName: String(payload.name),
    mode: isEdit.value ? 'edit' : 'create'
  }
}

/**
 * 打开弹窗时同步父级选项与表单默认值，避免新建和编辑共用状态串场。
 */
const initDialogData = async () => {
  form.name = props.categoryData?.name || ''
  form.parentId = props.categoryData?.parentId || ''

  try {
    parentOptions.value = await fetchParentOptions(props.categoryData?.id)
  } catch (error: any) {
    parentOptions.value = []
    ElMessage.error(error?.message || '获取所属分类失败，请稍后重试')
  }
}

watch(
  () => dialogVisible.value,
  async visible => {
    if (!visible) {
      formRef.value?.clearValidate()
      return
    }

    await initDialogData()
    formRef.value?.clearValidate()
  }
)

/**
 * 提交分类表单，并将创建结果回传给列表页做定位刷新。
 * @param close 关闭弹窗方法
 * @returns Promise<void>
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  const valid = await formRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  loading.value = true

  try {
    const requestPayload = {
      id: props.categoryData?.id,
      name: form.name.trim(),
      parentId: normalizeParentId(form.parentId)
    }
    const response = isEdit.value
      ? await updateBatchRuleCategory(requestPayload)
      : await createBatchRuleCategory(requestPayload)

    if (response.success) {
      ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
      emit('success', resolveSubmitResult(requestPayload))
      close()
    } else {
      ElMessage.error(response.message || '分类保存失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '分类保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="isEdit ? '编辑分类' : '新建分类'"
    width="480px"
    :confirm="handleConfirm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="94px" class="dialog-form">
      <el-form-item label="分类名称" prop="name" required>
        <el-input
          v-model.trim="form.name"
          maxlength="10"
          placeholder="请输入"
          :disabled="loading"
        />
      </el-form-item>
      <el-form-item label="所属分类" prop="parentId">
        <el-select
          v-model="form.parentId"
          class="dialog-form__select"
          clearable
          placeholder="一级"
          :disabled="loading || isParentLocked"
        >
          <el-option
            v-for="item in parentOptions"
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
.dialog-form__select {
  width: 100%;
}
</style>
