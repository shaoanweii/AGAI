<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, nextTick, reactive, ref, watch } from 'vue'
import AppDialog from '@/components/AppDialog.vue'
import { findFinalTagLibClientVoListByTagId, saveCorpus } from '@/api/common'
import type { CorpusAuditSavePayload } from '@/api/common/index.d'
import { TagType } from '@/constants'
import { useLoading } from '@/hooks/useLoading'
import { useAppStore } from '@/store'

defineOptions({
  name: 'CorpusCreateDialog'
})

type CorpusType = 'text' | 'survey'
type EnableStatus = 'enabled' | 'disabled'

interface StandardOpinionOption {
  tagName: string
  tagCode: string
  [key: string]: any
}

interface CorpusCreateFormValue {
  corpusType: CorpusType
  subject: string
  description: string
  standardOpinionId: string
  enableStatus: EnableStatus
}

const visible = defineModel<boolean>('visible', {
  default: false
})

const emit = defineEmits<{
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const standardOpinionLoading = ref(false)
const standardOpinionOptions = ref<StandardOpinionOption[]>([])
const hasLoadedStandardOpinions = ref(false)
const appStore = useAppStore()
const { showLoading, hideLoading } = useLoading()

/**
 * 拼接当前登录人的姓名与工号，避免字段缺失时出现多余空格。
 */
const operatorName = computed(() => {
  const { name, userName } = appStore.userInfo
  return `${name || ''} ${userName || ''}`.trim() || undefined
})

const form = reactive<CorpusCreateFormValue>({
  corpusType: 'text',
  subject: '',
  description: '',
  standardOpinionId: '',
  enableStatus: 'enabled'
})

/**
 * 重置新增语料表单，确保每次打开都从默认态开始。
 */
const resetForm = () => {
  form.corpusType = 'text'
  form.subject = ''
  form.description = ''
  form.standardOpinionId = ''
  form.enableStatus = 'enabled'
}

/**
 * 文本语料要求“主体”和“描述”至少填写一项。
 */
const validateTextCorpusContent = (
  _rule: unknown,
  _value: unknown,
  callback: (error?: Error) => void
) => {
  if (!form.subject.trim() && !form.description.trim()) {
    callback(new Error('语料主体和语料描述至少填写一项'))
    return
  }
  callback()
}

/**
 * 过滤标准观点下拉选项，避免缺少名称或编码的脏数据进入表单。
 */
const normalizeStandardOpinionOptions = (list: unknown): StandardOpinionOption[] => {
  if (!Array.isArray(list)) return []

  return list
    .map(item => {
      if (!item || typeof item !== 'object') return null
      const option = item as Record<string, any>
      const tagName = String(option.tagName ?? '').trim()
      const tagCode = String(option.tagCode ?? '').trim()
      if (!tagName || !tagCode) return null
      return {
        ...option,
        tagName,
        tagCode
      }
    })
    .filter((item): item is StandardOpinionOption => Boolean(item))
}

/**
 * 查询全部标准观点，供新增语料弹窗使用。
 */
const fetchStandardOpinionOptions = async () => {
  standardOpinionLoading.value = true

  try {
    const response = await findFinalTagLibClientVoListByTagId({
      tagType: TagType.Domain
    })
    standardOpinionOptions.value = normalizeStandardOpinionOptions(response.result)
    hasLoadedStandardOpinions.value = true
  } catch (error) {
    console.error('获取标准观点失败:', error)
    standardOpinionOptions.value = []
    hasLoadedStandardOpinions.value = false
    ElMessage.error('获取标准观点失败，请稍后重试')
  } finally {
    standardOpinionLoading.value = false
  }
}

/**
 * 懒加载标准观点选项，避免详情组件初始化时额外请求。
 */
const ensureStandardOpinionOptions = async () => {
  if (hasLoadedStandardOpinions.value || standardOpinionLoading.value) return
  await fetchStandardOpinionOptions()
}

/**
 * 将新增语料表单映射为语料审核接口所需 payload。
 */
const buildCreateCorpusPayload = (formValue: CorpusCreateFormValue): CorpusAuditSavePayload => {
  const selectedOpinion = standardOpinionOptions.value.find(
    item => String(item.tagCode) === String(formValue.standardOpinionId)
  )

  if (!selectedOpinion?.tagCode || !selectedOpinion?.tagName) {
    throw new Error('请选择有效的标准观点')
  }

  return {
    clientId: '',
    corpusAuditList: [
      {
        pageSize: 0,
        pageNum: 0,
        order: '',
        id: '',
        modelId: '',
        ids: [],
        corpusSubject: '',
        corpusDesc: '',
        startTime: '',
        endTime: '',
        modifiedCorpusSubject: formValue.subject,
        modifiedCorpusDesc: formValue.description,
        opinionCode: '',
        opinionCodeList: [],
        tagCodeList: [],
        opinionName: '',
        modifiedOpinionCode: selectedOpinion.tagCode,
        modifiedOpinionName: selectedOpinion.tagName,
        status: '',
        modifiedStatus: formValue.enableStatus,
        corpusType: formValue.corpusType === 'survey' ? '0' : '1',
        operateType: 'add',
        auditStatus: '',
        auditStatusList: [],
        auditUser: '',
        auditTime: '',
        initiator: operatorName.value || '',
        initiatorList: [],
        initiateTime: '',
        clientId: '',
        modelIds: []
      }
    ]
  }
}

/**
 * 提交新增语料。
 * 成功后由弹窗自身提示并关闭，同时通知父层可按需做后续处理。
 */
const submitCorpusCreate = async (formValue: CorpusCreateFormValue) => {
  try {
    const payload = buildCreateCorpusPayload(formValue)
    await saveCorpus(payload)
    ElMessage.success('新增语料成功')
    emit('success')
  } catch (error: any) {
    console.error('新增语料失败:', error)
    ElMessage.error(error?.message || '新增语料失败，请稍后重试')
    throw error
  }
}

const rules = computed<FormRules>(() => ({
  subject:
    form.corpusType === 'text' ? [{ validator: validateTextCorpusContent, trigger: 'blur' }] : [],
  description:
    form.corpusType === 'survey'
      ? [{ required: true, message: '请输入语料描述', trigger: 'blur' }]
      : [{ validator: validateTextCorpusContent, trigger: 'blur' }],
  standardOpinionId: [{ required: true, message: '请选择标准观点', trigger: 'change' }],
  enableStatus: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}))

/**
 * 切换语料类型时，仅重置校验态，不主动清空已输入内容，避免用户误操作导致内容丢失。
 */
const handleCorpusTypeChange = (type: CorpusType) => {
  if (form.corpusType === type) return
  form.corpusType = type
}

/**
 * 请求中拦截弹窗关闭，避免用户误触遮罩或关闭入口导致提交状态不一致。
 */
const handleBeforeClose = (done: () => void) => {
  if (submitLoading.value) return
  done()
}

/**
 * 取消新增语料；接口提交中不允许关闭弹窗。
 */
const handleCancel = () => {
  if (submitLoading.value) return
  visible.value = false
}

/**
 * 校验通过后提交新增语料，并使用全屏 Loading 阻止请求期间的误操作。
 */
const handleConfirm = async () => {
  if (submitLoading.value) return

  if (!formRef.value) {
    visible.value = false
    return
  }

  try {
    await formRef.value.validate()
    submitLoading.value = true
    showLoading({ text: '新增语料中...' })
    await submitCorpusCreate({
      corpusType: form.corpusType,
      subject: form.subject.trim(),
      description: form.description.trim(),
      standardOpinionId: form.standardOpinionId,
      enableStatus: form.enableStatus
    })
    visible.value = false
  } catch {
    // 表单校验或接口提交失败时都不关闭弹窗，提示由校验规则或提交方法负责。
  } finally {
    if (submitLoading.value) {
      submitLoading.value = false
      hideLoading()
    }
  }
}

watch(
  () => visible.value,
  val => {
    if (!val) return
    resetForm()
    void ensureStandardOpinionOptions()
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  }
)

watch(
  () => form.corpusType,
  () => {
    nextTick(() => {
      formRef.value?.clearValidate(['subject', 'description'])
    })
  }
)
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    width="680px"
    destroy-on-close
    :before-close="handleBeforeClose"
    :close-on-click-modal="!submitLoading"
    :close-on-press-escape="!submitLoading"
    :show-close="!submitLoading"
  >
    <template #header>添加语料</template>

    <div class="corpus-create-dialog">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="92px"
        class="corpus-create-dialog__form"
      >
        <el-form-item label="语料类型" required>
          <div class="corpus-create-dialog__type-list">
            <button
              type="button"
              class="corpus-create-dialog__type-btn"
              :class="{ 'is-active': form.corpusType === 'text' }"
              @click="handleCorpusTypeChange('text')"
            >
              文本
            </button>
            <button
              type="button"
              class="corpus-create-dialog__type-btn"
              :class="{ 'is-active': form.corpusType === 'survey' }"
              @click="handleCorpusTypeChange('survey')"
            >
              问卷
            </button>
          </div>
        </el-form-item>

        <el-form-item v-if="form.corpusType === 'text'" label="语料主体" prop="subject">
          <el-input v-model.trim="form.subject" placeholder="请输入" maxlength="100" />
        </el-form-item>

        <el-form-item label="语料描述" prop="description">
          <el-input v-model.trim="form.description" placeholder="请输入" maxlength="100" />
        </el-form-item>

        <el-form-item label="标准观点" prop="standardOpinionId">
          <el-select-v2
            v-model="form.standardOpinionId"
            :options="standardOpinionOptions"
            :props="{ label: 'tagName', value: 'tagCode' }"
            :loading="standardOpinionLoading"
            filterable
            clearable
            placeholder="请选择"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="是否启用" prop="enableStatus">
          <el-radio-group v-model="form.enableStatus">
            <el-radio value="enabled">启用</el-radio>
            <el-radio value="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button class="app-dialog__btn-cancel" :disabled="submitLoading" @click="handleCancel">
        取消
      </el-button>
      <el-button
        class="app-dialog__btn-confirm"
        type="primary"
        :disabled="submitLoading"
        @click="handleConfirm"
      >
        确定
      </el-button>
    </template>
  </AppDialog>
</template>

<style scoped lang="scss">
.corpus-create-dialog {
  padding-top: 8px;
}

.corpus-create-dialog__form {
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
}

.corpus-create-dialog__type-list {
  display: flex;
  align-items: center;
  gap: 16px;
}

.corpus-create-dialog__type-btn {
  min-width: 90px;
  height: 40px;
  padding: 0 24px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  cursor: pointer;
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.corpus-create-dialog__type-btn.is-active {
  color: #1677ff;
  border-color: #1677ff;
  box-shadow: inset 0 0 0 1px #1677ff;
}

.corpus-create-dialog__type-btn:hover {
  border-color: #1677ff;
}
</style>
