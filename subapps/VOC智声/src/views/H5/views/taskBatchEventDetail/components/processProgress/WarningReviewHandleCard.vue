<script setup lang="ts">
import { computed } from 'vue'
import HFilterPersonnelSelect from '@h5/views/taskEvent/components/HFilterPersonnelSelect.vue'
import ProcessSectionCard from './ProcessSectionCard.vue'

defineOptions({
  name: 'BatchWarningReviewHandleCard'
})

interface WarningReviewHandleCardProps {
  /** 业务责任人 userId */
  ownerId?: string
  /** 接口暂未返回 userId 时的默认展示文案 */
  ownerFallbackLabel?: string
  /** 添加说明 */
  description?: string
  /** 部门-人员树 */
  departAccountTree?: any[]
  /** 部门-人员树加载状态 */
  departAccountTreeLoading?: boolean
}

const props = withDefaults(defineProps<WarningReviewHandleCardProps>(), {
  ownerId: '',
  ownerFallbackLabel: '',
  description: '',
  departAccountTree: () => [],
  departAccountTreeLoading: false
})

const emit = defineEmits<{
  /** 更新业务责任人 userId */
  (e: 'update:ownerId', value: string): void
  /** 更新添加说明 */
  (e: 'update:description', value: string): void
  /** 打开人员弹窗前通知父级按需加载树数据 */
  (e: 'open-owner-select'): void
}>()

const selectedOwnerIds = computed({
  get: () => (props.ownerId ? [props.ownerId] : []),
  set: value => {
    emit('update:ownerId', Array.isArray(value) ? value[0] || '' : '')
  }
})

const descriptionValue = computed({
  get: () => props.description,
  set: value => emit('update:description', value)
})
</script>

<template>
  <ProcessSectionCard title="事件处理" collapsible>
    <div class="warning-review-form">
      <div class="form-row">
        <div class="form-label is-required">业务责任人</div>
        <div class="form-control">
          <HFilterPersonnelSelect
            v-model="selectedOwnerIds"
            :options="props.departAccountTree"
            :loading="props.departAccountTreeLoading"
            :max-selected="1"
            display-mode="departmentPath"
            :fallback-selected-label="props.ownerFallbackLabel"
            title="处理人员"
            placeholder="请选择"
            @click.capture="emit('open-owner-select')"
          />
        </div>
      </div>

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
.warning-review-form {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
}

.form-row {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  column-gap: 10px;
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
  flex: 1;
  min-width: 0;
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

.form-control :deep(.hfps-trigger) {
  height: 32px;
  border-radius: 4px;
}
</style>
