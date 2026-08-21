<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { saveTagLibClient, updateTagLibClient } from '@/api/tag'
import { appDialogConfirm } from '@/components/appDialog'
import { shouldBlockDisableSubmit, showDisableBlockedDialog } from './disableGuards'
import {
  type CategoryDialogForm,
  type ExperienceCategoryItem,
  type ExperienceCategorySubmitResult,
  type ExperienceCategoryTypeSummary,
  type ExperienceCodeType
} from './types'
import {
  createNameValidator,
  normalizeDialogStatus,
  normalizeDialogText,
  normalizeSynonymsField,
  useExperienceCodeTypeField,
  useExperienceCodeStatusField,
  validateSynonymsField
} from './dialogFormUtils'

defineOptions({
  name: 'ExperienceCategoryFormDialog'
})

interface Props {
  visible: boolean
  categoryData: ExperienceCategoryItem | null
  categoryList: ExperienceCategoryItem[]
  typeSummaries: ExperienceCategoryTypeSummary[]
  defaultTypeCode?: ExperienceCodeType
  defaultParentId?: string
}

const props = withDefaults(defineProps<Props>(), {
  defaultTypeCode: '',
  defaultParentId: '',
  typeSummaries: () => []
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: ExperienceCategorySubmitResult): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.categoryData?.id ? '编辑分类' : '新建分类'))
/**
 * 编辑态下锁定体验代码类型，避免已有分类被误切换到其他类型分组。
 */
const isEditMode = computed(() => Boolean(props.categoryData?.id))
const { statusOptions, enabledStatusValue } = useExperienceCodeStatusField()
const { typeOptions, firstTypeValue } = useExperienceCodeTypeField()
const CATEGORY_IDENTIFIER = 'Category'

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)
const formState = reactive<CategoryDialogForm>({
  tagName: '',
  tagDescription: '',
  tagType: '',
  tagParentId: '',
  synonyms: '',
  tagStatus: enabledStatusValue.value
})

/**
 * 统一在当前可用字典中解析默认类型，避免弹框先打开、字典后返回时落成空值。
 */
const resolveAvailableTypeCode = (preferredTypeCode = '') => {
  const normalizedPreferredTypeCode = normalizeDialogText(preferredTypeCode)
  if (normalizedPreferredTypeCode) {
    const matchedTypeOption = typeOptions.value.some(
      item => item.value === normalizedPreferredTypeCode
    )
    if (matchedTypeOption) {
      return normalizedPreferredTypeCode
    }
  }

  return firstTypeValue.value
}

/**
 * 一级分类在界面上允许“不选上级分类”，但提交给后端时仍需回填当前代码类型根节点 id。
 */
const resolveTypeRootNodeId = (typeCode: ExperienceCodeType) => {
  return props.typeSummaries.find(item => item.typeCode === typeCode)?.nodeId || ''
}

/**
 * 分类索引和父子关系统一按当前完整分类列表构建，避免每次校验都重复遍历原数组。
 */
const categoryMap = computed(() => {
  return new Map(props.categoryList.map(item => [item.id, item]))
})

const categoryChildrenMap = computed(() => {
  const map = new Map<string, ExperienceCategoryItem[]>()
  props.categoryList.forEach(item => {
    const key = item.tagParentId || 'ROOT'
    const children = map.get(key) || []
    children.push(item)
    map.set(key, children)
  })
  return map
})

const getChildCategories = (categoryId = '') => {
  return categoryChildrenMap.value.get(categoryId || 'ROOT') || []
}

/**
 * 收集某个分类下的全部后代 ID，用于编辑时阻止循环挂载。
 */
const collectDescendantIds = (categoryId: string): string[] => {
  const children = getChildCategories(categoryId)
  return children.flatMap(item => [item.id, ...collectDescendantIds(item.id)])
}

/**
 * 计算当前分类子树的最大深度，保证编辑后仍满足最多三级的约束。
 */
const getSubtreeDepth = (categoryId?: string) => {
  if (!categoryId) return 1
  const children = getChildCategories(categoryId)
  if (!children.length) {
    return 1
  }
  return 1 + Math.max(...children.map(item => getSubtreeDepth(item.id)))
}

/**
 * 新建分类时若默认父级已经落在三级或更深层级，则回退到最近的可选父级，保证默认仍按三级新增。
 */
const resolveCreatableParentId = (parentId: string) => {
  if (!parentId) return ''

  let currentParent = categoryMap.value.get(parentId) || null
  while (currentParent) {
    if (currentParent.tagType === formState.tagType && currentParent.level < 3) {
      return currentParent.id
    }
    const nextParentId = currentParent.tagParentId
    currentParent = nextParentId ? categoryMap.value.get(nextParentId) || null : null
  }

  return ''
}

/**
 * 构建父级分类选项，只允许选择当前类型下一二级分类，且编辑后不能超过三级。
 */
const parentOptions = computed(() => {
  const editingId = props.categoryData?.id
  const subtreeDepth = getSubtreeDepth(editingId)
  const blockedIds = editingId ? [editingId, ...collectDescendantIds(editingId)] : []

  return props.categoryList
    .filter(item => item.tagType === formState.tagType && item.level < 3)
    .filter(item => !blockedIds.includes(item.id))
    .filter(item => item.level + subtreeDepth <= 3)
    .sort((left, right) => left.order - right.order)
    .map(item => ({
      label: item.pathLabel,
      value: item.id
    }))
})

/**
 * 当前父级分类用于换算提交层级；未选择父级时按一级分类处理。
 */
const selectedParentCategory = computed(() => {
  return categoryMap.value.get(formState.tagParentId) || null
})

/**
 * 一级分类未选择父级时，仍然是挂在对应类型根节点下，因此根节点的直属末级标记也需要参与新增判断。
 */
const selectedTypeSummary = computed(() => {
  return props.typeSummaries.find(item => item.typeCode === formState.tagType) || null
})

/**
 * 新增分类前统一判断当前所属上级是否存在直属末级体验代码。
 */
const currentParentHasFinalCategory = computed(() => {
  return (
    selectedParentCategory.value?.hasFinalCategory ??
    selectedTypeSummary.value?.hasFinalCategory ??
    false
  )
})

const submitParentId = computed(() => {
  if (formState.tagParentId) {
    return formState.tagParentId
  }

  return resolveTypeRootNodeId(formState.tagType)
})

/**
 * 编辑场景下需要把“一级分类挂在类型根节点”的隐式关系也换算出来，才能准确判断是否真的改了挂载位置。
 */
const originalSubmitParentId = computed(() => {
  const currentCategory = props.categoryData
  if (!currentCategory) {
    return ''
  }

  const currentParentId = normalizeDialogText(currentCategory.tagParentId)
  return currentParentId || resolveTypeRootNodeId(currentCategory.tagType)
})

/**
 * 只有新建，或编辑时确实改了类型/父级，才需要触发直属末级冲突确认。
 */
const shouldConfirmFinalCategoryConflict = computed(() => {
  if (!currentParentHasFinalCategory.value) {
    return false
  }

  if (!props.categoryData) {
    return true
  }

  return (
    normalizeDialogText(props.categoryData.tagType) !== formState.tagType ||
    originalSubmitParentId.value !== submitParentId.value
  )
})

/**
 * 初始化弹框表单，区分编辑态和新建态的默认值来源。
 */
const initForm = () => {
  if (props.categoryData) {
    formState.tagName = normalizeDialogText(props.categoryData.tagName)
    formState.tagDescription = normalizeDialogText(props.categoryData.tagDescription)
    // 编辑态即使禁用类型下拉，也必须先回填当前类型，才能驱动第二个下拉的所属分类数据源。
    formState.tagType = (normalizeDialogText(props.categoryData.tagType) ||
      props.defaultTypeCode) as ExperienceCodeType
    formState.tagParentId = normalizeDialogText(props.categoryData.tagParentId)
    formState.synonyms = normalizeDialogText(props.categoryData.synonyms)
    formState.tagStatus = normalizeDialogStatus(props.categoryData.tagStatus)
    return
  }

  formState.tagName = ''
  formState.tagDescription = ''
  // 新建时默认选中当前页面字典里的首个类型，确保所有来源统一走 tagLibeType。
  formState.tagType = resolveAvailableTypeCode(props.defaultTypeCode)
  formState.tagParentId = resolveCreatableParentId(props.defaultParentId)
  formState.synonyms = ''
  formState.tagStatus = enabledStatusValue.value
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
  () => typeOptions.value,
  () => {
    if (!dialogVisible.value || props.categoryData || formState.tagType) {
      return
    }

    const nextTypeCode = resolveAvailableTypeCode(props.defaultTypeCode)
    if (!nextTypeCode) {
      return
    }

    // 类型字典异步返回后，为新建弹框自动补上首个可用类型，避免用户看到空下拉。
    formState.tagType = nextTypeCode
  },
  { deep: true }
)

watch(
  () => formState.tagType,
  () => {
    const validParent = parentOptions.value.some(item => item.value === formState.tagParentId)
    if (!validParent) {
      formState.tagParentId = ''
    }
  }
)

const validateName = createNameValidator('分类名称')

const formRules: FormRules<CategoryDialogForm> = {
  tagName: [{ required: true, validator: validateName, trigger: 'blur' }],
  tagDescription: [{ max: 200, message: '分类描述不能超过200个字符', trigger: 'blur' }],
  tagType: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  synonyms: [{ validator: validateSynonymsField, trigger: 'blur' }],
  tagStatus: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}

/**
 * 上级存在直属末级时，只有用户明确同意迁移后才允许继续提交，避免后端静默调整造成认知偏差。
 */
const confirmSubmitWhenParentHasFinalCategory = async () => {
  if (!shouldConfirmFinalCategoryConflict.value) {
    return true
  }

  try {
    await appDialogConfirm(
      '选择所属分类已绑定末级标签，如需增加当前分类，系统将自动绑定对应末级至当前分类下，是否确认并创建？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        dialogAttrs: {
          width: '480px'
        }
      }
    )
    return true
  } catch (error: any) {
    if (error === 'cancel' || error === 'close' || error?.message === 'cancel') {
      return false
    }
    throw error
  }
}

/**
 * 提交分类表单时同时补齐分类标识与层级，保证后端能准确识别节点类型和树层级。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value || confirmLoading.value) return
  confirmLoading.value = true

  try {
    await formRef.value.validate()

    // 编辑分类时若当前分类下已绑定启用状态的标准观点，则不允许改为禁用，避免影响已有绑定关系。
    if (
      shouldBlockDisableSubmit({
        hasFinalTopic: props.categoryData?.hasFinalTopic,
        nextStatus: formState.tagStatus
      })
    ) {
      await showDisableBlockedDialog()
      return
    }

    const payload = {
      tagName: formState.tagName.trim(),
      tagDescription: formState.tagDescription.trim(),
      // 一级分类未选择父级时，接口要求挂在当前代码类型根节点下，而不是继续传空字符串。
      tagParentId: submitParentId.value,
      tagStatus: formState.tagStatus,
      // 同义词输入支持中英文逗号混输，这里统一转成英文逗号后再提交给接口。
      synonyms: normalizeSynonymsField(formState.synonyms),
      tagType: formState.tagType,
      identifier: CATEGORY_IDENTIFIER,
      // 根分类固定为 1 级，其余分类按父节点层级顺延，保证树结构口径稳定。
      level: selectedParentCategory.value ? selectedParentCategory.value.level + 1 : 1
    }

    const currentCategory = props.categoryData
    const isEdit = Boolean(currentCategory?.id)
    let response: any = null

    if (isEdit) {
      const allowSubmit = await confirmSubmitWhenParentHasFinalCategory()
      if (!allowSubmit) {
        return
      }

      await updateTagLibClient({
        id: currentCategory!.id,
        ...payload,
        hasFinalCategory: currentParentHasFinalCategory.value
      })
      ElMessage.success('编辑成功')
    } else {
      const allowSubmit = await confirmSubmitWhenParentHasFinalCategory()
      if (!allowSubmit) {
        return
      }

      response = await saveTagLibClient({
        ...payload,
        hasFinalCategory: currentParentHasFinalCategory.value
      })
      ElMessage.success('创建成功')
    }

    emit('success', {
      categoryId: String(response?.result?.id || currentCategory?.id || ''),
      categoryName: formState.tagName.trim(),
      typeCode: formState.tagType
    })
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
    width="640px"
    :confirm="handleConfirm"
  >
    <el-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="84px"
      class="dialog-form"
    >
      <el-form-item label="分类名称" prop="tagName">
        <el-input v-model.trim="formState.tagName" maxlength="50" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="分类描述" prop="tagDescription">
        <el-input
          v-model.trim="formState.tagDescription"
          type="textarea"
          :rows="2"
          maxlength="200"
          placeholder="请输入..."
        />
      </el-form-item>
      <el-form-item label="所属分类" prop="tagType" class="dialog-form__double">
        <el-select
          v-model="formState.tagType"
          :disabled="isEditMode"
          filterable
          style="width: 140px"
        >
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="formState.tagParentId"
          clearable
          filterable
          style="flex: 1"
          placeholder="默认不选则新增一级分类"
        >
          <el-option
            v-for="item in parentOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="同义词" prop="synonyms">
        <el-input
          v-model.trim="formState.synonyms"
          type="textarea"
          :rows="4"
          maxlength="10000"
          resize="none"
          placeholder="多个同义词请使用英文逗号分隔"
        />
      </el-form-item>
      <el-form-item label="是否启用" prop="tagStatus">
        <el-radio-group v-model="formState.tagStatus">
          <el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.dialog-form {
  width: 100%;
  margin: 0 auto;
}

.dialog-form__double {
  :deep(.el-form-item__content) {
    display: flex;
    gap: 16px;
  }
}
</style>
