<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import type { HDialogEmits, HDialogProps } from './types'

defineOptions({
  name: 'HDialog'
})

const props = withDefaults(defineProps<HDialogProps>(), {
  title: '',
  width: '335px',
  showFooter: true,
  cancelText: '取消',
  confirmText: '确定',
  closeOnClickOverlay: true,
  destroyOnClose: false,
  confirm: undefined
})

const emit = defineEmits<HDialogEmits>()

const confirming = shallowRef(false)

const innerVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const popupStyle = computed(() => ({
  width: props.width,
  maxWidth: 'calc(100vw - 40px)'
}))

const showBodyContent = computed(() => {
  return !props.destroyOnClose || innerVisible.value
})

/**
 * 关闭弹窗，并统一向外同步关闭事件。
 */
const closeDialog = () => {
  emit('update:visible', false)
}

/**
 * 点击取消按钮。
 */
const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}

/**
 * 点击确认按钮。
 * 当传入 confirm 函数时，由调用方决定何时调用 close。
 */
const handleConfirm = async () => {
  if (props.confirm) {
    confirming.value = true
    try {
      await Promise.resolve(props.confirm({ close: closeDialog }))
    } finally {
      confirming.value = false
    }
    return
  }

  emit('confirm')
  emit('update:visible', false)
}
</script>

<template>
  <van-popup
    v-model:show="innerVisible"
    class="h-dialog-popup"
    teleport="body"
    :lock-scroll="true"
    :close-on-click-overlay="props.closeOnClickOverlay"
    :style="popupStyle"
    @closed="emit('close')"
  >
    <div class="h-dialog">
      <div class="h-dialog__header">
        <div class="h-dialog__title van-ellipsis">
          <slot name="header">
            {{ props.title }}
          </slot>
        </div>
        <button class="h-dialog__close" type="button" @click="closeDialog">
          <van-icon name="cross" size="20" color="#8C98A8" />
        </button>
      </div>

      <div v-if="showBodyContent" class="h-dialog__body">
        <slot />
      </div>

      <div v-if="$slots.footer || props.showFooter" class="h-dialog__footer">
        <slot name="footer">
          <button
            class="h-dialog__button h-dialog__button--cancel"
            type="button"
            @click="handleCancel"
          >
            {{ props.cancelText }}
          </button>
          <button
            class="h-dialog__button h-dialog__button--confirm"
            type="button"
            :disabled="confirming"
            @click="handleConfirm"
          >
            {{ props.confirmText }}
          </button>
        </slot>
      </div>
    </div>
  </van-popup>
</template>

<style scoped lang="scss">
.h-dialog-popup {
  overflow: hidden;
  border-radius: 8px;
  background: transparent;
}

.h-dialog {
  overflow: hidden;
  border-radius: 8px;
  background: #ffffff;
}

.h-dialog__header {
  height: 48px;
  padding: 0 14px 0 16px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #edf7ff 0%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.h-dialog__title {
  flex: 1;
  min-width: 0;
  padding-right: 12px;
  font-weight: 600;
  font-size: 14px;
  line-height: 22px;
  color: #1f2733;
}

.h-dialog__close {
  flex: none;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
}

.h-dialog__body {
  min-height: 88px;
  padding: 12px 16px 14px;
}

.h-dialog__footer {
  height: 64px;
  padding: 14px 24px 16px;
  border-top: 1px solid #ebeef2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  column-gap: 16px;
}

.h-dialog__button {
  flex: 1;
  height: 36px;
  border: 0;
  border-radius: 2px;
  font-weight: 400;
  font-size: 16px;
  line-height: 22px;

  &:disabled {
    opacity: 0.65;
  }
}

.h-dialog__button--cancel {
  background: #f2f3f5;
  color: #5f6a7a;
}

.h-dialog__button--confirm {
  background: #1f6bff;
  color: #ffffff;
}
</style>
