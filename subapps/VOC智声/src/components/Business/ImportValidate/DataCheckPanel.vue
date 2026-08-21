<script setup lang="ts">
import { computed } from 'vue'
import { CircleCheckFilled, CircleCloseFilled, WarningFilled } from '@element-plus/icons-vue'
import {
  ImportValidateCheckStatus,
  type ImportValidateCheckMessage,
  type ImportValidateCheckStatusValue
} from '@/components/Business/ImportValidate/types'

defineOptions({
  name: 'ImportValidateDataCheckPanel'
})

const props = withDefaults(
  defineProps<{
    status?: ImportValidateCheckStatusValue
    messages?: ImportValidateCheckMessage[]
    disabled?: boolean
    startText?: string
    checkingText?: string
    recheckText?: string
    failedText?: string
  }>(),
  {
    status: ImportValidateCheckStatus.Unchecked,
    messages: () => [],
    disabled: false,
    startText: '开始校验',
    checkingText: '校验中...',
    recheckText: '重新校验',
    failedText: '校验失败，请重新校验。'
  }
)

const emit = defineEmits<{
  (e: 'check'): void
}>()

const isChecking = computed(() => props.status === ImportValidateCheckStatus.Checking)
const canCheck = computed(() => !props.disabled && !isChecking.value)
const buttonText = computed(() => {
  if (props.status === ImportValidateCheckStatus.Checking) return props.checkingText
  if (
    props.status === ImportValidateCheckStatus.Success ||
    props.status === ImportValidateCheckStatus.Failed
  ) {
    return props.recheckText
  }
  return props.startText
})

const visibleMessages = computed(() => {
  if (props.messages.length) return props.messages
  if (props.status === ImportValidateCheckStatus.Failed) {
    return [{ type: 'error' as const, text: props.failedText }]
  }
  return []
})
</script>

<template>
  <div class="data-check-panel">
    <div class="data-check-panel__actions">
      <el-button
        :type="isChecking ? 'default' : 'primary'"
        :disabled="!canCheck"
        :class="['data-check-panel__btn', { 'data-check-panel__checking-btn': isChecking }]"
        @click="emit('check')"
      >
        {{ buttonText }}
      </el-button>
    </div>

    <div v-if="visibleMessages.length" class="data-check-panel__result-list">
      <p
        v-for="(item, index) in visibleMessages"
        :key="`${item.type}-${index}`"
        class="data-check-panel__result-line"
        :class="`is-${item.type}`"
      >
        <el-icon class="data-check-panel__result-icon">
          <CircleCheckFilled v-if="item.type === 'success'" />
          <WarningFilled v-else-if="item.type === 'warning'" />
          <CircleCloseFilled v-else />
        </el-icon>
        <span>{{ item.text }}</span>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.data-check-panel {
  width: 100%;
}

.data-check-panel__actions {
  display: flex;
  align-items: center;
}

.data-check-panel__btn {
  width: 140px;
}

.data-check-panel__checking-btn {
  background-color: #E2F3FE !important;
  color: #1677FF !important;
  border: 1px solid #1677FF !important;
}

.data-check-panel__result-list {
  margin-top: 8px;
}

.data-check-panel__result-line {
  min-height: 28px;
  margin: 6px 0;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  border-radius: 4px;
  background: #F5F7FA;
  color: #5F6A7A;
  font-size: 12px;
  line-height: 20px;
}

.data-check-panel__result-line.is-success .data-check-panel__result-icon {
  color: #00b42a;
}

.data-check-panel__result-line.is-warning .data-check-panel__result-icon {
  color: #ff7d00;
}

.data-check-panel__result-line.is-error {
  color: #f53f3f;
}

.data-check-panel__result-icon {
  flex: 0 0 auto;
  margin-right: 10px;
  font-size: 13px;
}
</style>
