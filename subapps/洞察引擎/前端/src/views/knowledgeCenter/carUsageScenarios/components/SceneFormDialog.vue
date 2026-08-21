<script setup lang="ts">
import { computed, inject, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import {
  carUsageScenarioEnableStatus,
  createCarScene,
  updateCarScene
} from '@/api/carUsageScenarios'
import { carUsageScenarioPageContextKey } from '../context'
import { normalizeSynonyms, validateSynonyms } from '../../shared/synonym'
import { resolveCarUsageScenarioStatusOptions } from './statusOptions'
import type { CarUsageScenarioSceneForm, CarUsageScenarioSceneSubmitResult } from './types'

defineOptions({
  name: 'CarUsageScenarioSceneFormDialog'
})

interface Props {
  visible: boolean
  sceneData: Api.CarUsageScenarios.SceneRecord | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: CarUsageScenarioSceneSubmitResult): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const dialogTitle = computed(() => (props.sceneData?.id ? '编辑用车场景' : '新建用车场景'))
const pageContext = inject(carUsageScenarioPageContextKey, null)

const statusOptions = computed<ConditionsDetailItem[]>(() => {
  const remoteOptions = resolveCarUsageScenarioStatusOptions(pageContext?.conditionMap.value || {})
  if (remoteOptions.length) return remoteOptions
  return []
})

const formRef = ref<FormInstance>()
const confirmLoading = ref(false)
const formState = reactive<CarUsageScenarioSceneForm>({
  sceneName: '',
  sceneDescription: '',
  synonyms: '',
  status: carUsageScenarioEnableStatus.ENABLED
})

/**
 * 新建与编辑共享一套初始化逻辑，避免弹框重复打开时残留上次输入。
 */
const initForm = () => {
  if (props.sceneData) {
    formState.sceneName = String(props.sceneData.sceneName || '')
    formState.sceneDescription = String(props.sceneData.sceneDescription || '')
    formState.synonyms = String(props.sceneData.synonyms || '')
    formState.status = props.sceneData.status || carUsageScenarioEnableStatus.ENABLED
    return
  }

  formState.sceneName = ''
  formState.sceneDescription = ''
  formState.synonyms = ''
  formState.status = carUsageScenarioEnableStatus.ENABLED
}

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    initForm()
    formRef.value?.clearValidate()
    // 用车场景已取消分类关系，弹框打开时仅初始化场景自身字段。
  }
)

/**
 * 场景名称要求非空且长度可控，判重校验交由接口检索保证名称全局唯一。
 */
const validateSceneName = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  const input = value?.trim()
  if (!input) {
    callback(new Error('用车场景名称不能为空'))
    return
  }
  if (input.length > 50) {
    callback(new Error('用车场景名称不能超过50个字符'))
    return
  }
  callback()
}

const formRules: FormRules<CarUsageScenarioSceneForm> = {
  sceneName: [{ required: true, validator: validateSceneName, trigger: 'blur' }],
  sceneDescription: [{ max: 200, message: '用车场景描述不能超过200个字符', trigger: 'blur' }],
  // 同义词校验走共享逻辑，避免场景和分类弹框后续出现规则漂移。
  synonyms: [{ validator: validateSynonyms, trigger: 'blur' }],
  status: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}

/**
 * 保存接口统一收敛在弹框组件中，外层只关心成功回调和列表刷新。
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (!formRef.value || confirmLoading.value) return

  confirmLoading.value = true
  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    const payload = {
      id: props.sceneData?.id || '',
      sceneName: formState.sceneName.trim(),
      sceneDescription: formState.sceneDescription.trim(),
      synonyms: normalizeSynonyms(formState.synonyms),
      status: formState.status
    }
    const isEdit = Boolean(props.sceneData?.id)
    const response: any = isEdit ? await updateCarScene(payload) : await createCarScene(payload)

    ElMessage.success(isEdit ? '编辑成功' : '创建成功')
    emit('success', {
      sceneId: String(response?.result?.id || props.sceneData?.id || ''),
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
      props.sceneData?.id ? '编辑用车场景失败，请稍后重试' : '创建用车场景失败，请稍后重试'
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
      label-width="108px"
      class="scene-form-dialog"
    >
      <el-form-item label="用车场景名称" prop="sceneName" required>
        <el-input v-model.trim="formState.sceneName" maxlength="50" placeholder="请输入" />
      </el-form-item>
      <!-- <el-form-item label="用车场景描述" prop="sceneDescription">
        <el-input
          v-model.trim="formState.sceneDescription"
          type="textarea"
          :rows="3"
          maxlength="200"
          resize="none"
          placeholder="请输入..."
        />
      </el-form-item> -->
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
.scene-form-dialog {
  margin: 0 auto;
}

.scene-form-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
}

.scene-form-dialog :deep(.el-form-item__content) {
  max-width: 618px;
}

.scene-form-dialog :deep(.el-textarea__inner) {
  min-height: 68px !important;
}

.scene-form-dialog :deep(.el-radio-group) {
  display: flex;
  align-items: center;
  gap: 32px;
}

.scene-form-dialog :deep(.el-radio) {
  margin-right: 0;
}

.scene-form-dialog__select {
  width: 100%;
}
</style>
