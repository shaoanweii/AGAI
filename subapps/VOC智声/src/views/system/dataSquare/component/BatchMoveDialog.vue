<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, type CascaderOption, type FormInstance } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import { batchMoveDataPlazaReport } from '@/api/dataPlaza'
import type { DataPlazaReportItem } from '@/api/dataPlaza/types'
import { dataSquareStore } from '../store'

defineOptions({
  name: 'BatchMoveDialog'
})

const props = defineProps<{
  visible: boolean
  selection: DataPlazaReportItem[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: { selectedParentId: string; selectedCategoryId: string }): void
}>()

const innerVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const formData = ref({
  categoryPath: [] as string[]
})

const categoryOptions = computed<CascaderOption[]>(() => {
  return (dataSquareStore.categoryTree || []).map(parent => ({
    value: parent.id,
    label: parent.categoryName,
    children: (parent.children || []).map(child => ({
      value: child.id,
      label: child.categoryName
    }))
  }))
})

const selectedIds = computed(() => {
  return props.selection.map(item => item.id).filter(Boolean)
})

/**
 * 重置弹窗表单，避免重复打开时保留上次选择。
 */
const resetForm = () => {
  formData.value.categoryPath = []
  formRef.value?.clearValidate()
}

/**
 * 提交批量移动。
 * @param close 关闭弹窗方法
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (submitting.value) return

  if (!selectedIds.value.length) {
    ElMessage.warning('请选择报告')
    return
  }

  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const response = await batchMoveDataPlazaReport({
      ids: selectedIds.value,
      targetCategoryId: formData.value.categoryPath[1] || ''
    })

    if (!response.success) {
      ElMessage.error(response.message || '操作失败')
      return
    }

    const [selectedParentId = '', selectedCategoryId = ''] = formData.value.categoryPath

    ElMessage.success(response.message || '操作成功')
    emit('success', {
      selectedParentId,
      selectedCategoryId
    })
    close()
  } catch (error) {
    console.error('批量移动数据广场报告失败:', error)
    // ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.visible,
  value => {
    if (value) {
      resetForm()
    }
  }
)
</script>

<template>
  <AppDialog v-model:visible="innerVisible" width="480px" destroy-on-close :confirm="handleConfirm">
    <template #header>批量移动</template>

    <el-form ref="formRef" :model="formData" label-width="70px" class="batch-move-form">
      <el-form-item
        label="所属分类"
        prop="categoryPath"
        :rules="[
          {
            validator: (_rule, value: string[], callback) => {
              if (!Array.isArray(value) || value.length !== 2 || !value[1]) {
                callback(new Error('请选择所属分类'))
                return
              }
              callback()
            },
            trigger: 'change'
          }
        ]"
      >
        <el-cascader
          v-model="formData.categoryPath"
          :options="categoryOptions"
          :props="{
            value: 'value',
            label: 'label',
            children: 'children',
            emitPath: true,
            checkStrictly: false
          }"
          clearable
          style="width: 100%"
          class="batch-move-form__select"
          placeholder="请选择"
        />
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.batch-move-form {
  padding-top: 8px;
}

.batch-move-form__select {
  width: 100%;
}
</style>
