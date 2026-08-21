<script setup lang="ts">
import { computed, inject, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import {
  carUsageScenarioEnableStatus,
  createCarSceneCategory,
  updateCarSceneCategory
} from '@/api/carUsageScenarios'
import { carUsageScenarioPageContextKey } from '../context'
import { normalizeSynonyms, validateSynonyms } from '../../shared/synonym'
import { resolveCarUsageScenarioStatusOptions } from './statusOptions'
import type {
  CarUsageScenarioCategoryForm,
  CarUsageScenarioCategoryItem,
  CarUsageScenarioCategorySubmitResult
} from './types'

defineOptions({
  name: 'CarUsageScenarioCategoryFormDialog'
})

interface Props {
  visible: boolean
  categoryData: CarUsageScenarioCategoryItem | null
  categoryList: CarUsageScenarioCategoryItem[]
  parentCategory: CarUsageScenarioCategoryItem | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: CarUsageScenarioCategorySubmitResult): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.categoryData?.id ? '编辑分类' : '新建分类'))

const pageContext = inject(carUsageScenarioPageContextKey, null)

const statusOptions = computed<ConditionsDetailItem[]>(() => {
  const remoteOptions = resolveCarUsageScenarioStatusOptions(pageContext?.conditionMap.value || {})
  if (remoteOptions.length) return remoteOptions
  return []
})

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)
const formState = reactive<CarUsageScenarioCategoryForm>({
  categoryName: '',
  categoryDescription: '',
  synonyms: '',
  status: carUsageScenarioEnableStatus.ENABLED
})

/**
 * 弹框打开时回填表单，保证编辑态与新建态共享同一套校验和提交流程。
 */
const initForm = () => {
  if (props.categoryData) {
    formState.categoryName = props.categoryData.categoryName
    formState.categoryDescription = props.categoryData.categoryDescription
    formState.synonyms = props.categoryData.synonyms
    formState.status = props.categoryData.status || carUsageScenarioEnableStatus.ENABLED
    return
  }

  formState.categoryName = ''
  formState.categoryDescription = ''
  formState.synonyms = ''
  formState.status = carUsageScenarioEnableStatus.ENABLED
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    initForm()
    formRef.value?.clearValidate()
  }
)

/**
 * 分类名称按整棵树做唯一校验，避免不同层级出现同名节点造成右侧场景归属歧义。
 */
const validateCategoryName = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  const input = value?.trim()
  if (!input) {
    callback(new Error('分类名称不能为空'))
    return
  }
  if (input.length > 50) {
    callback(new Error('分类名称不能超过50个字符'))
    return
  }

  const duplicate = props.categoryList.some(item => {
    if (props.categoryData?.id && item.id === props.categoryData.id) {
      return false
    }
    return item.categoryName.trim() === input
  })

  if (duplicate) {
    callback(new Error('分类名称不可重复'))
    return
  }

  callback()
}

const formRules: FormRules<CarUsageScenarioCategoryForm> = {
  categoryName: [{ required: true, validator: validateCategoryName, trigger: 'blur' }],
  categoryDescription: [{ max: 200, message: '分类描述不能超过200个字符', trigger: 'blur' }],
  // 同义词校验统一复用共享实现，保证分类、场景、观点的提示和拆分口径保持一致。
  synonyms: [{ validator: validateSynonyms, trigger: 'blur' }],
  status: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}

/**
 * 当前页面暂未提供父级选择器，因此新建时根据当前选中分类换算真实父级和目标层级。
 */
const buildPayload = () => {
  const categoryName = formState.categoryName.trim()
  const categoryDescription = formState.categoryDescription.trim()
  const synonyms = normalizeSynonyms(formState.synonyms)

  if (props.categoryData) {
    return {
      id: props.categoryData.id,
      patentId: props.categoryData.parentId || props.categoryData.patentId || '',
      categoryName,
      categoryDescription,
      level: props.categoryData.level || 1,
      synonyms,
      status: formState.status
    }
  }

  // 选中一级分类时在其下创建二级；选中二级分类时回退到父级下创建同级二级，统一限制为两层结构。
  const patentId =
    props.parentCategory?.level === 1
      ? props.parentCategory.id
      : props.parentCategory?.parentId || ''
  const level = props.parentCategory ? 2 : 1

  return {
    patentId,
    categoryName,
    categoryDescription,
    level,
    synonyms,
    status: formState.status
  }
}

/**
 * 保存成功后统一抛出 success 事件，由列表组件负责刷新与高亮同步。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value || confirmLoading.value) return

  confirmLoading.value = true
  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    const payload = buildPayload()

    const isEdit = Boolean(props.categoryData?.id)
    const response: any = isEdit
      ? await updateCarSceneCategory(payload)
      : await createCarSceneCategory(payload)

    ElMessage.success(isEdit ? '编辑成功' : '创建成功')
    emit('success', {
      categoryId: String(response?.result?.id || props.categoryData?.id || ''),
      categoryName: formState.categoryName.trim(),
      mode: isEdit ? 'edit' : 'create'
    })
    close()
  } catch (error: any) {
    if (error?.code) {
      return
    }
    if (error?.message) {
      ElMessage.error(error.message)
      return
    }
    ElMessage.error(
      props.categoryData?.id ? '编辑分类失败，请稍后重试' : '创建分类失败，请稍后重试'
    )
  } finally {
    confirmLoading.value = false
  }
}
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    width="640px"
    :confirm="handleConfirm"
  >
    <el-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="88px"
      class="category-form-dialog"
    >
      <el-form-item label="分类名称" prop="categoryName" required>
        <el-input v-model.trim="formState.categoryName" maxlength="50" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="分类描述" prop="categoryDescription">
        <el-input
          v-model.trim="formState.categoryDescription"
          type="textarea"
          :rows="2"
          maxlength="200"
          placeholder="请输入..."
        />
      </el-form-item>
      <el-form-item label="同义词" prop="synonyms">
        <el-input
          v-model.trim="formState.synonyms"
          type="textarea"
          :rows="4"
          maxlength="10000"
          placeholder="多个同义词请使用英文逗号分隔"
        />
      </el-form-item>
      <el-form-item label="是否启用" prop="status" required>
        <el-radio-group v-model="formState.status">
          <el-radio v-for="item in statusOptions" :key="item.key" :label="item.key">
            {{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.category-form-dialog {
  margin: 0 auto;
}

.category-form-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
}

.category-form-dialog :deep(.el-form-item__content) {
  max-width: 655px;
}

.category-form-dialog :deep(.el-textarea__inner) {
  min-height: 68px !important;
}

.category-form-dialog :deep(.el-radio-group) {
  display: flex;
  align-items: center;
  gap: 32px;
}

.category-form-dialog :deep(.el-radio) {
  margin-right: 0;
}
</style>
