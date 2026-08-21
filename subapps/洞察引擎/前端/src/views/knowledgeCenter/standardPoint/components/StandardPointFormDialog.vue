<script setup lang="ts">
/**
 * 标准观点表单弹窗组件
 * 支持三种模式：新建(create)、编辑(edit)、批量编辑(batch)
 */
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import { standardPointStore, type TagCategoryNode } from '../store'
import { batchUpdateTopicClient, findTopicByCodeClient, saveTopicClient } from '@/api/tag'
import { fetchAssociationTreeData } from '../../experienceCode/service'
import { createSynonymValidator, normalizeSynonyms } from '../../shared/synonym'
import cloneDeep from 'lodash-es/cloneDeep'

defineOptions({ name: 'StandardPointFormDialog' })

// 弹窗模式类型：create-新建 | edit-编辑 | batch-批量编辑
export type StandardPointDialogMode = 'create' | 'edit' | 'batch'

// 体验代码模型（用于关联不同体验系统的标签）
export type InsTopicExperienceCodeModel = {
  // 标签类型（如 CA/JOUR/PRO...）
  type: string
  // 末级节点 id（用于后端关联）
  parentId: string
}

// 标准观点表单数据结构
export interface StandardPointFormData {
  tagName: string
  tagDescription: string
  synonyms: string

  // 关联标签与关联旅程仍沿用后端 experienceCode 协议，分别使用 CA/JOUR 类型。
  experienceCode: InsTopicExperienceCodeModel[]

  emotion: string | null
  intention: string | null
  tagStatus: string | null

  topicCodes: string[]
  tagCode: string
  tagType: string
}

// 组件Props定义
interface Props {
  mode: StandardPointDialogMode
  // 编辑时回显数据：字段命名与 StandardPointFormData 保持一致即可（多余字段会被忽略）
  editData?: Partial<StandardPointFormData> | null
  // 数据字典（由父组件统一拉取，避免弹框重复请求）
  conditions?: Record<string, ConditionsDetailItem[]>
  // 外部提交：返回 false 表示不关闭弹框
  onSubmit?: (
    payload: {
      mode: StandardPointDialogMode
      data: StandardPointFormData
      submitData: Record<string, any>
    },
    ctx: { close: () => void }
  ) => Promise<boolean | void> | boolean | void
}

const props = withDefaults(defineProps<Props>(), {
  editData: null,
  onSubmit: undefined,
  conditions: () => ({})
})

// 弹窗显示状态（双向绑定）
const visible = defineModel<boolean>('visible', { default: false })

// 事件定义
const emit = defineEmits<{
  (
    e: 'submit',
    payload: {
      mode: StandardPointDialogMode
      data: StandardPointFormData
      submitData: Record<string, any>
    }
  ): void
  (e: 'success'): void
}>()

// 判断是否为批量编辑模式
const isBatchMode = computed(() => props.mode === 'batch')
// 判断是否为新建或编辑模式
const isCreateOrEditMode = computed(() => props.mode === 'create' || props.mode === 'edit')

// 弹窗标题（根据模式动态显示）
const dialogTitle = computed(() => {
  if (props.mode === 'create') return '新增观点'
  if (props.mode === 'edit') return '编辑观点'
  return '批量编辑'
})

// 创建空表单数据
const createEmptyForm = (): StandardPointFormData => ({
  tagName: '',
  tagDescription: '',
  synonyms: '',
  experienceCode: [],

  emotion: null,
  intention: null,
  tagStatus: null,

  topicCodes: [],
  tagCode: '',
  tagType: ''
})

// 表单数据
const form = reactive<StandardPointFormData>(createEmptyForm())
// 表单引用
const formRef = ref()
// 滚动容器引用
const scrollWrapRef = ref<HTMLElement | null>(null)

// 数据字典条件
const conditions = computed<Record<string, ConditionsDetailItem[]>>(() => props.conditions || {})
// 根据key获取条件选项
const getConditionOptions = (key: string) => conditions.value?.[key] || []
// 根据多个key获取条件选项（返回第一个有数据的）
const getConditionOptionsByKeys = (keys: string[]) => {
  for (const key of keys) {
    const list = getConditionOptions(key)
    if (Array.isArray(list) && list.length) return list
  }
  return []
}

// 启用状态选项
const enableStatusOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['stopOrEnable'])
  return fromConditions || []
})

// 情感选项
const sentimentOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['vocSentiment'])
  return fromConditions || []
})

// 意图选项
const intentionOptions = computed(() => {
  const fromConditions = getConditionOptionsByKeys(['vocIntention'])
  return fromConditions || []
})

// 获取默认启用状态（优先选择“启用”选项）
const resolveDefaultEnableStatus = () => {
  const options = enableStatusOptions.value || []
  const enabled = options.find((i: any) => String(i.value) === '启用')
  return (enabled || options[0])?.key ?? null
}

// 体验系统配置项类型
type AssociationSystemItem = {
  tagType: string
  label: string
}

// 标准观点仅保留与标签体系、用户旅程的两个独立关联入口。
const ASSOCIATION_SYSTEMS: AssociationSystemItem[] = [
  { tagType: 'CA', label: '关联标签' },
  { tagType: 'JOUR', label: '关联旅程' }
]

// 体验代码加载状态
const associationLoading = ref(false)
const expLoading = computed(() => associationLoading.value)

// 体验代码选项映射（按标签类型分组）
const expOptionsMap = computed(() => {
  const map: Record<string, TagCategoryNode[]> = {}
  ASSOCIATION_SYSTEMS.forEach(s => {
    const list = standardPointStore.tagCategoryTreeMap?.[String(s.tagType)]
    map[String(s.tagType)] = Array.isArray(list) ? (list as TagCategoryNode[]) : []
  })
  return map
})

// 读取标签体系、用户旅程页面正在使用的独立快照，避免关联下拉成为空壳。
const fetchExperienceTreeOnce = async () => {
  associationLoading.value = true
  try {
    const [tagTree, journeyTree] = await Promise.all([
      fetchAssociationTreeData('tagSystem'),
      fetchAssociationTreeData('userJourney')
    ])
    standardPointStore.tagCategoryTreeMap.CA = tagTree
    standardPointStore.tagCategoryTreeMap.JOUR = journeyTree
  } catch (error) {
    console.error('获取关联标签或关联旅程数据失败', error)
    standardPointStore.tagCategoryTreeMap.CA = []
    standardPointStore.tagCategoryTreeMap.JOUR = []
    ElMessage.error('获取关联数据失败')
  } finally {
    associationLoading.value = false
  }
}

// 获取指定类型的体验代码选中值
const getSelectedExperienceCode = (tagType: string) => {
  const type = String(tagType)
  const current = Array.isArray(form.experienceCode) ? form.experienceCode : []
  const item = current.find(i => String((i as any)?.type) === type)
  return item?.parentId ? String(item.parentId) : null
}

// 设置指定类型的体验代码选中值
const setSelectedExperienceCode = (tagType: string, selectedId: string | null) => {
  const type = String(tagType)
  const nextId = selectedId ? String(selectedId) : ''

  const current = Array.isArray(form.experienceCode) ? [...form.experienceCode] : []
  const idx = current.findIndex(i => String((i as any)?.type) === type)

  if (!nextId) {
    if (idx >= 0) current.splice(idx, 1)
    form.experienceCode = current
    return
  }

  const nextItem: InsTopicExperienceCodeModel = { type, parentId: nextId }

  if (idx >= 0) current[idx] = nextItem
  else current.push(nextItem)
  form.experienceCode = current
}

// 根据模式重置表单
const resetFormByMode = () => {
  const empty = createEmptyForm()
  Object.keys(empty).forEach((k: string) => ((form as any)[k] = (empty as any)[k]))
}

// 编辑详情加载状态
const editDetailLoading = ref(false)

// 获取编辑详情数据
const fetchEditDetail = async () => {
  const fromEditData = props.editData as any
  const code = String(fromEditData.topicCode || '').trim()
  if (!code) return

  editDetailLoading.value = true
  try {
    const res: any = await findTopicByCodeClient({ tagCode: code })

    const detail = res?.result || {}
    Object.assign(form, cloneDeep(detail))
    if (form.tagStatus == null) form.tagStatus = resolveDefaultEnableStatus()
  } catch (e: any) {
    console.error('获取观点详情失败', e)
    ElMessage.error('获取观点详情失败')
  } finally {
    editDetailLoading.value = false
  }
}

// 监听模式变化，重置表单并清空校验
watch(
  () => props.mode,
  async () => {
    if (!visible.value) return

    // 弹框打开期间切换模式时，表单结构与 rules 会变化，避免触发自动校验导致“必填项”提示残留
    resetFormByMode()
    if (props.mode === 'create') {
      form.tagStatus = resolveDefaultEnableStatus()
    }
    await nextTick()
    formRef.value?.clearValidate?.()
  }
)

// 监听弹窗显示状态，初始化表单数据
watch(
  () => visible.value,
  async v => {
    if (!v) {
      resetForm()
      return
    }
    if (props.mode === 'create') {
      form.tagStatus = resolveDefaultEnableStatus()
    } else if (props.mode === 'edit') {
      fetchEditDetail()
    }
    await fetchExperienceTreeOnce()
  }
)

// 正则表达式：观点名称支持中文、英文、数字及常用特殊字符
const tagNameCnEnNumSpecialReg =
  /^[\u4e00-\u9fa5A-Za-z0-9 \u3000~!@#$%^&*()（）_\-+=\[\]{}|\\;:'",.，。\/<>《》【】?？、:：;；·—…]+$/

// 校验观点名称（自定义校验器）
const validateTagName = (required: boolean, max: number, label: string) => {
  return (_rule: any, value: string, callback: (err?: Error) => void) => {
    const v = String(value ?? '').trim()
    if (!v) {
      if (required) return callback(new Error(`请输入${label}`))
      return callback()
    }
    if (v.length > max) return callback(new Error(`${label}字数上限为${max}`))
    if (!tagNameCnEnNumSpecialReg.test(v)) {
      return callback(new Error(`${label}仅支持中文英文数字和特殊字符`))
    }
    callback()
  }
}

// 标准观点沿用原有提示文案，但实际校验规则交给共享工具统一维护。
const validateSynonyms = createSynonymValidator({
  maxMessage: '同义词字数上限为10000'
})

// 表单校验规则（根据模式动态生成）
const rules = computed(() => {
  const isRequired = isCreateOrEditMode.value
  //批量编辑场景  不必填
  if (!isRequired) return {}

  const requiredSelect = (message: string) => [{ required: true, message, trigger: 'change' }]

  // 为每个体验代码类型创建独立的校验器
  const createExperienceCodeValidator = (tagType: string, systemName: string) => {
    return (_r: any, _v: any, cb: any) => {
      const current = Array.isArray(form.experienceCode) ? form.experienceCode : []
      const hasValue = current.some(
        i => String((i as any)?.type) === tagType && (i as any)?.parentId
      )

      if (!hasValue) {
        return cb(new Error(`请选择${systemName}`))
      }
      cb()
    }
  }

  const baseRules: Record<string, any> = {
    tagName: [
      { required: true, validator: validateTagName(true, 50, '观点名称'), trigger: 'blur' }
    ],
    // tagDescription: [{ validator: validateCnEnNum(false, 100, '观点描述'), trigger: 'blur' }],
    synonyms: [{ validator: validateSynonyms, trigger: 'blur' }],
    emotion: requiredSelect('请选择情感'),
    intention: requiredSelect('请选择意图'),
    tagStatus: requiredSelect('请选择是否启用')
  }

  ASSOCIATION_SYSTEMS.forEach(sys => {
    baseRules[`experienceCode_${sys.tagType}`] = [
      {
        required: true,
        trigger: 'change',
        validator: createExperienceCodeValidator(sys.tagType, sys.label)
      }
    ]
  })

  return baseRules
})

// 构建提交数据（批量编辑时仅提交非空字段）
const buildSubmitData = (): Record<string, any> => {
  const data: StandardPointFormData = {
    ...form,
    tagName: String(form.tagName || '').trim(),
    tagDescription: String(form.tagDescription || '').trim(),
    synonyms: normalizeSynonyms(form.synonyms),
    experienceCode: Array.isArray(form.experienceCode) ? form.experienceCode : [],
    emotion: form.emotion,
    intention: form.intention,
    tagStatus: form.tagStatus,
    topicCodes: Array.isArray(form.topicCodes) ? form.topicCodes : [],
    tagCode: String(form.tagCode || '').trim(),
    tagType: String(form.tagType || '').trim()
  }

  // 批量编辑：仅提交被填写的字段，避免空值覆盖
  if (props.mode === 'batch') {
    const submit: Record<string, any> = {}
    const setIfNotEmpty = (key: keyof StandardPointFormData, v: any) => {
      const isEmptyArray = Array.isArray(v) && v.length === 0
      if (v == null || v === '' || isEmptyArray) return
      submit[key] = v
    }

    setIfNotEmpty('experienceCode', data.experienceCode)
    setIfNotEmpty('emotion', data.emotion)
    setIfNotEmpty('intention', data.intention)
    setIfNotEmpty('tagStatus', data.tagStatus)
    return submit
  }

  const submitData: Record<string, any> = { ...data } as any
  return submitData
}

// 校验表单
const validateForm = async () => {
  if (!formRef.value) return true
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

// 表单重置：恢复到初始值并清空校验状态
function resetForm() {
  //关闭弹框  重置表单
  formRef.value?.resetFields()
  // 关闭弹框时重置滚动位置，避免下次打开仍停留在上次滚动处
  if (scrollWrapRef.value) scrollWrapRef.value.scrollTop = 0
  resetFormByMode()
}

// 确认按钮处理
const handleConfirm = async ({ close }: { close: () => void }) => {
  const ok = await validateForm()
  if (!ok) return

  const submitData = buildSubmitData()
  if (props.mode === 'batch') {
    const onlyTopicCodes = Object.keys(submitData).every(k => k === 'topicCodes')
    if (onlyTopicCodes) {
      ElMessage.warning('请至少填写一个需要修改的字段')
      return
    }
  }
  const payload = {
    mode: props.mode,
    data: { ...form } as StandardPointFormData,
    submitData
  }

  if (props.onSubmit) {
    const result = await Promise.resolve(props.onSubmit(payload, { close }))
    if (result === false) return
    emit('success')
    close()
    return
  }

  try {
    if (props.mode === 'create' || props.mode === 'edit') {
      const reqData: Record<string, any> = { ...(submitData || {}) }

      await saveTopicClient(reqData)
      ElMessage.success('保存成功')
      emit('success')
      close()
      return
    }

    // 批量编辑：必须传入观点编码集合
    const topicCodes = (props.editData as any)?.topicCodes
    if (!Array.isArray(topicCodes) || topicCodes.length === 0) {
      ElMessage.warning('未选择任何观点，无法批量编辑')
      return
    }
    const reqData: Record<string, any> = {
      ...(submitData || {}),
      topicCodes
    }
    await batchUpdateTopicClient(reqData)
    ElMessage.success('批量编辑成功')
    emit('success')
    close()
  } catch (e: any) {
    console.error('观点保存失败', e)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 取消按钮处理
const handleCancel = () => {
  editDetailLoading.value = false
  resetForm()
}
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    :title="dialogTitle"
    width="800px"
    :confirm="handleConfirm"
    @cancel="handleCancel"
  >
    <div ref="scrollWrapRef" class="standard-point-form-dialog">
      <el-form
        ref="formRef"
        v-loading="editDetailLoading"
        :model="form"
        :rules="rules"
        :validate-on-rule-change="false"
        label-width="138px"
        class="sp-form"
      >
        <template v-if="!isBatchMode">
          <el-form-item label="观点名称" prop="tagName">
            <el-input v-model.trim="form.tagName" placeholder="请输入" :maxlength="50" clearable />
          </el-form-item>

          <el-form-item label="观点描述" prop="tagDescription">
            <el-input
              v-model.trim="form.tagDescription"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="请输入观点描述"
              :maxlength="100"
            />
          </el-form-item>

          <el-form-item label="同义词" prop="synonyms">
            <el-input
              v-model.trim="form.synonyms"
              type="textarea"
              :rows="4"
              resize="none"
              placeholder="多个同义词请使用英文逗号分隔"
              :maxlength="10000"
            />
          </el-form-item>
        </template>
        <el-form-item
          v-for="sys in ASSOCIATION_SYSTEMS"
          :key="sys.tagType"
          :label="sys.label"
          :prop="isCreateOrEditMode ? `experienceCode_${sys.tagType}` : undefined"
        >
          <el-cascader
            v-if="visible"
            :key="sys.tagType"
            :model-value="getSelectedExperienceCode(sys.tagType)"
            class="association-select"
            placeholder="请选择"
            filterable
            separator="#"
            :show-all-levels="true"
            :options="expOptionsMap[sys.tagType] || []"
            :props="{
              label: 'tagName',
              value: 'id',
              children: 'child',
              emitPath: false
            }"
            :clearable="isBatchMode"
            :disabled="expLoading"
            @update:modelValue="
              (v: any) => setSelectedExperienceCode(sys.tagType, v ? String(v) : null)
            "
          />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="情感" prop="emotion">
              <el-select v-model="form.emotion" placeholder="请选择" clearable filterable>
                <el-option
                  v-for="item in sentimentOptions"
                  :key="item.key"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意图" prop="intention">
              <el-select v-model="form.intention" placeholder="请选择" clearable filterable>
                <el-option
                  v-for="item in intentionOptions"
                  :key="item.key"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="是否启用" prop="tagStatus">
              <el-radio-group v-model="form.tagStatus">
                <el-radio v-for="item in enableStatusOptions" :key="item.key" :label="item.key">
                  {{ item.value }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.standard-point-form-dialog {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.sp-form {
  padding-top: 4px;
}

:deep(.association-select.el-cascader) {
  width: 100% !important;
}
</style>
