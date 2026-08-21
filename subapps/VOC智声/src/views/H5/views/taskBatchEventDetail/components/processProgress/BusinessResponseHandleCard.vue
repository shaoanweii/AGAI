<script setup lang="ts">
import { computed } from 'vue'
import HFilterMultiSelect from '@h5/views/taskEvent/components/HFilterMultiSelect.vue'
import HFilterPersonnelSelect from '@h5/views/taskEvent/components/HFilterPersonnelSelect.vue'
import ProcessSectionCard from './ProcessSectionCard.vue'
import type {
  BatchBusinessResponseHandleForm,
  BatchBusinessResponseHandleState,
  BatchClosedLoopMode
} from './types'

defineOptions({
  name: 'BatchBusinessResponseHandleCard'
})

interface BusinessResponseHandleCardProps {
  /** 业务响应事件处理表单配置 */
  form: BatchBusinessResponseHandleForm
  /** 部门-人员树 */
  departAccountTree?: any[]
  /** 部门-人员树加载状态 */
  departAccountTreeLoading?: boolean
}

const props = withDefaults(defineProps<BusinessResponseHandleCardProps>(), {
  departAccountTree: () => [],
  departAccountTreeLoading: false
})

const modelValue = defineModel<BatchBusinessResponseHandleState>({
  required: true
})

const emit = defineEmits<{
  /** 打开人员弹窗前通知父级按需加载树数据 */
  (e: 'open-personnel-select'): void
}>()

const selectedMainRespUserIds = computed({
  get: () => (modelValue.value.mainRespUserId ? [modelValue.value.mainRespUserId] : []),
  set: value => {
    modelValue.value.mainRespUserId = Array.isArray(value) ? value[0] || '' : ''
  }
})

const selectedCoordinatingUserIds = computed({
  get: () => modelValue.value.coordinatingUserIds,
  set: value => {
    modelValue.value.coordinatingUserIds = Array.isArray(value) ? value : []
  }
})

const selectedCustTypeValues = computed({
  get: () => modelValue.value.custTypeValues,
  set: value => {
    modelValue.value.custTypeValues = Array.isArray(value) ? value : []
  }
})

const selectedUsageScenarioValues = computed({
  get: () => modelValue.value.usageScenarioValues,
  set: value => {
    modelValue.value.usageScenarioValues = Array.isArray(value) ? value : []
  }
})

const selectedTopicTextValues = computed({
  get: () => modelValue.value.topicTextValues,
  set: value => {
    modelValue.value.topicTextValues = Array.isArray(value) ? value : []
  }
})

const descriptionValue = computed({
  get: () => modelValue.value.description,
  set: value => {
    modelValue.value.description = value
  }
})

const showSwordFields = computed(() => modelValue.value.handleMode === 'sword')

/**
 * 切换处理方式，并清空天枢星链专属字段残留。
 * @param value 处理方式值
 */
const selectHandleMode = (value: BatchClosedLoopMode) => {
  modelValue.value.handleMode = value
  if (value !== 'sword') {
    modelValue.value.custTypeValues = []
    modelValue.value.usageScenarioValues = []
    modelValue.value.topicTextValues = []
  }
}
</script>

<template>
  <ProcessSectionCard title="事件处理" collapsible>
    <div class="business-response-form">
      <div class="form-row">
        <div class="form-label is-required">主责部门</div>
        <div class="form-control">
          <HFilterPersonnelSelect
            v-model="selectedMainRespUserIds"
            :options="props.departAccountTree"
            :loading="props.departAccountTreeLoading"
            :max-selected="1"
            display-mode="departmentPath"
            title="主责部门"
            placeholder="请选择主责部门"
            @click.capture="emit('open-personnel-select')"
          />
        </div>
      </div>

      <div class="form-row">
        <div class="form-label">协同部门</div>
        <div class="form-control">
          <HFilterPersonnelSelect
            v-model="selectedCoordinatingUserIds"
            :options="props.departAccountTree"
            :loading="props.departAccountTreeLoading"
            display-mode="departmentPath"
            title="协同部门"
            placeholder="请选择协同部门"
            @click.capture="emit('open-personnel-select')"
          />
        </div>
      </div>

      <div class="form-row">
        <div class="form-label is-required">处理方式</div>
        <div class="form-control">
          <div class="handle-mode-group">
            <button
              v-for="option in props.form.handleModeOptions"
              :key="option.value"
              class="handle-mode-button"
              :class="{ 'is-active': modelValue.handleMode === option.value }"
              type="button"
              @click="selectHandleMode(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>

      <template v-if="showSwordFields">
        <div class="form-row">
          <div class="form-label">用户类型</div>
          <div class="form-control">
            <HFilterMultiSelect
              v-model="selectedCustTypeValues"
              :options="props.form.userTypeOptions"
              :max-selected="1"
              title="用户类型"
              placeholder="请选择用户类型"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">用车场景</div>
          <div class="form-control">
            <HFilterMultiSelect
              v-model="selectedUsageScenarioValues"
              :options="props.form.carSceneOptions"
              :max-selected="1"
              title="用车场景"
              placeholder="请选择用车场景"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label is-required">聚焦观点</div>
          <div class="form-control">
            <HFilterMultiSelect
              v-model="selectedTopicTextValues"
              :options="props.form.focusTopicOptions"
              title="聚焦观点"
              placeholder="请选择聚焦观点"
            />
          </div>
        </div>
      </template>

      <div class="form-row form-row--textarea">
        <div class="form-label">添加说明</div>
        <div class="form-control">
          <textarea
            v-model="descriptionValue"
            class="mock-textarea"
            rows="4"
            maxlength="200"
            placeholder="请输入事件说明"
          ></textarea>
        </div>
      </div>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.business-response-form {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
}

.form-row {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  align-items: center;
  column-gap: 8px;
}

.form-row--textarea {
  align-items: flex-start;
}

.form-label {
  text-align: right;
  font-weight: 400;
  font-size: 12px;
  line-height: 22px;
  color: rgba(0, 0, 0, 0.65);
  word-break: keep-all;
  white-space: nowrap;
}

.form-label.is-required::before {
  content: '*';
  margin-right: 4px;
  color: #f53f3f;
}

.form-control {
  min-width: 0;
}

.handle-mode-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 8px;
}

.handle-mode-button {
  height: 32px;
  padding: 0 8px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #ffffff;
  color: #5f6a7a;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
}

.handle-mode-button.is-active {
  border-color: #1677ff;
  background: #e8f2ff;
  color: #1677ff;
}

.mock-textarea {
  width: 100%;
  min-height: 80px;
  padding: 9px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
  background: #ffffff;
  resize: none;
  outline: none;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #1f2733;
}

.mock-textarea::placeholder {
  color: #c9cdd4;
}

.form-control :deep(.hfms-trigger),
.form-control :deep(.hfps-trigger) {
  height: 32px;
  border-radius: 4px;
}
</style>
