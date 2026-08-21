<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { saveTagLibClient, updateTagLibClient } from '@/api/tag'
import { shouldBlockDisableSubmit, showDisableBlockedDialog } from './disableGuards'
import {
  type ExperienceCategoryItem,
  type ExperienceCodeDialogForm,
  type ExperienceCodeItem,
  type ExperienceCodeType
} from './types'
import { resolveLeafCategoryOptions } from './categoryUtils'
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
  name: 'ExperienceCodeFormDialog'
})

interface Props {
  visible: boolean
  codeData: ExperienceCodeItem | null
  categoryList: ExperienceCategoryItem[]
  defaultTypeCode?: ExperienceCodeType
  defaultCategoryId?: string
}

const props = withDefaults(defineProps<Props>(), {
  defaultTypeCode: '',
  defaultCategoryId: ''
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.codeData?.id ? '编辑标签' : '新增标签'))
/**
 * 编辑态下锁定体验代码类型，避免已有代码被误切换到其他类型分组。
 */
const isEditMode = computed(() => Boolean(props.codeData?.id))
const { statusOptions, enabledStatusValue } = useExperienceCodeStatusField()
const { typeOptions, firstTypeValue } = useExperienceCodeTypeField()
const EXPERIENCE_CODE_IDENTIFIER = 'FinalCategory'

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)
const formState = reactive<ExperienceCodeDialogForm>({
  tagName: '',
  tagDescription: '',
  tagType: '',
  tagParentId: '',
  synonyms: '',
  tagStatus: enabledStatusValue.value
})

/**
 * 统一在当前可用字典中解析默认类型，避免弹框先打开、字典后返回时出现空白类型。
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
 * 分类索引在当前弹框生命周期内复用，避免回填和提交时反复遍历分类数组。
 */
const categoryMap = computed(() => {
  return new Map(props.categoryList.map(item => [item.id, item]))
})

/**
 * 根据当前代码类型筛出可选的末级分类，顺序完全跟随接口返回。
 */
const leafCategoryOptions = computed(() => {
  return resolveLeafCategoryOptions(props.categoryList, formState.tagType)
})

/**
 * 体验代码的层级依赖所属末级分类，提交前按分类 level 顺延一层。
 */
const selectedCategory = computed(() => {
  return categoryMap.value.get(formState.tagParentId) || null
})

/**
 * 所属分类只能落在当前代码类型下的末级分类，切换类型后需要及时清掉失效值。
 */
const syncCategorySelection = () => {
  const validCategory = leafCategoryOptions.value.some(item => item.value === formState.tagParentId)
  if (!validCategory) {
    formState.tagParentId = ''
  }
}

/**
 * 初始化弹框表单，确保编辑态与新建态默认值一致，并与提交字段口径保持统一。
 */
const initForm = () => {
  if (props.codeData) {
    formState.tagName = normalizeDialogText(props.codeData.tagName)
    formState.tagDescription = normalizeDialogText(props.codeData.tagDescription)
    formState.tagType = (normalizeDialogText(props.codeData.tagType) ||
      props.defaultTypeCode) as ExperienceCodeType
    formState.tagParentId = normalizeDialogText(props.codeData.tagParentId)
    formState.synonyms = normalizeDialogText(props.codeData.synonyms)
    formState.tagStatus = normalizeDialogStatus(props.codeData.tagStatus)
    return
  }

  formState.tagName = ''
  formState.tagDescription = ''
  // 新建体验代码默认采用当前字典首个类型，不再使用本地写死值。
  formState.tagType = resolveAvailableTypeCode(props.defaultTypeCode)
  formState.tagParentId = props.defaultCategoryId
  formState.synonyms = ''
  formState.tagStatus = enabledStatusValue.value
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    initForm()
    syncCategorySelection()
    formRef.value?.clearValidate()
  }
)

watch(
  () => typeOptions.value,
  () => {
    if (!dialogVisible.value || props.codeData || formState.tagType) {
      return
    }

    const nextTypeCode = resolveAvailableTypeCode(props.defaultTypeCode)
    if (!nextTypeCode) {
      return
    }

    // 类型字典异步返回后，为新建弹框补默认类型，保证后续末级分类联动能立即工作。
    formState.tagType = nextTypeCode
  },
  { deep: true }
)

watch(
  () => formState.tagType,
  () => {
    syncCategorySelection()

    if (dialogVisible.value && formState.tagName) {
      formRef.value?.validateField('tagName')
    }
  }
)

const validateName = createNameValidator('末级标签名称')
/**
 * 上级分类必须显式选择当前类型下的末级分类，避免默认空值或失效值被提交给后端。
 */
const validateCategory = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  void rule

  if (!value) {
    callback(new Error('请选择上级分类'))
    return
  }

  const validCategory = leafCategoryOptions.value.some(item => item.value === value)
  if (!validCategory) {
    callback(new Error('请选择当前类型下的最末级分类'))
    return
  }

  callback()
}

const formRules: FormRules<ExperienceCodeDialogForm> = {
  tagName: [{ required: true, validator: validateName, trigger: 'blur' }],
  tagDescription: [{ max: 200, message: '标签描述不能超过200个字符', trigger: 'blur' }],
  tagType: [{ required: true, message: '请选择标签类型', trigger: 'change' }],
  tagParentId: [{ required: true, validator: validateCategory, trigger: 'change' }],
  synonyms: [{ validator: validateSynonymsField, trigger: 'blur' }],
  tagStatus: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}

/**
 * 体验代码新增和编辑统一补充 FinalCategory 标识与 level，避免接口误判节点层级。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value || confirmLoading.value) return
  confirmLoading.value = true

  try {
    await formRef.value.validate()

    // 编辑体验代码时若已绑定启用状态的标准观点，则禁止本次禁用提交，只允许继续调整其他字段。
    if (
      shouldBlockDisableSubmit({
        hasFinalTopic: props.codeData?.hasFinalTopic,
        nextStatus: formState.tagStatus
      })
    ) {
      await showDisableBlockedDialog()
      return
    }

    const payload = {
      ...formState,
      tagName: formState.tagName.trim(),
      tagDescription: formState.tagDescription.trim(),
      tagParentId: formState.tagParentId,
      // 同义词统一归一成英文逗号，避免后端按分隔符拆词时出现口径偏差。
      synonyms: normalizeSynonymsField(formState.synonyms),
      tagCode: normalizeDialogText(props.codeData?.tagCode) || undefined,
      identifier: EXPERIENCE_CODE_IDENTIFIER,
      // 体验代码挂在分类节点下，因此层级固定取所属分类 level + 1。
      level: (selectedCategory.value?.level || 0) + 1
    }

    if (props.codeData?.id) {
      await updateTagLibClient({
        id: props.codeData.id,
        ...payload
      })
      ElMessage.success('编辑成功')
    } else {
      await saveTagLibClient(payload)
      ElMessage.success('创建成功')
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
    width="640px"
    :confirm="handleConfirm"
  >
    <el-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      label-width="112px"
      class="dialog-form"
    >
      <el-form-item label="末级标签名称" prop="tagName">
        <el-input v-model.trim="formState.tagName" maxlength="50" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="标签描述" prop="tagDescription">
        <el-input
          v-model.trim="formState.tagDescription"
          type="textarea"
          :rows="2"
          maxlength="200"
          placeholder="请输入..."
        />
      </el-form-item>
      <el-form-item label="所属分类" required class="dialog-form__double">
        <el-form-item
          prop="tagType"
          class="dialog-form__double-item dialog-form__double-item--type"
        >
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
        </el-form-item>
        <el-form-item
          prop="tagParentId"
          class="dialog-form__double-item dialog-form__double-item--category"
        >
          <el-select
            v-model="formState.tagParentId"
            style="width: 100%"
            placeholder="请选择最末级分类"
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
    gap: 12px;
    align-items: flex-start;
  }
}

.dialog-form__double-item {
  margin-bottom: 0;
}

.dialog-form__double-item--category {
  flex: 1;
}

.dialog-tip {
  padding-left: 124px;
  color: #86909c;
  font-size: 12px;
  line-height: 20px;
}
</style>
