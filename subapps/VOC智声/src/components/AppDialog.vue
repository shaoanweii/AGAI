<template>
  <ElDialog
    v-model="innerVisible"
    v-bind="$attrs"
    class="app-dialog"
    :header-class="APP_DIALOG_HEADER_CLASS"
    :width="dialogWidth"
    align-center
    :append-to-body="true"
    @close="handleClose"
  >
    <slot />
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    <template v-if="$slots.footer || props.showFooter" #footer>
      <div class="app-dialog__footer">
        <div class="app-dialog__footer-btns">
          <slot name="footer">
            <ElButton class="app-dialog__btn-cancel" @click="onCancelClick">{{
              props.cancelText
            }}</ElButton>
            <ElButton
              class="app-dialog__btn-confirm"
              type="primary"
              :loading="confirming"
              @click="onConfirmClick"
              >{{ props.confirmText }}</ElButton
            >
          </slot>
        </div>
      </div>
    </template>
  </ElDialog>
</template>
<script setup lang="ts">
import { computed, ref, useAttrs } from 'vue'
import { ElButton, ElDialog } from 'element-plus'

defineOptions({ name: 'AppDialog' })

interface Props {
  visible: boolean
  showFooter?: boolean
  cancelText?: string
  confirmText?: string
  confirm?: ConfirmHandler
}

type ConfirmHandler = (ctx: { close: () => void }) => unknown | Promise<unknown>

const props = withDefaults(defineProps<Props>(), {
  showFooter: true,
  cancelText: '取消',
  confirmText: '确定'
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'cancel'): void
  (e: 'confirm'): void
}>()

const innerVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val)
})

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const APP_DIALOG_HEADER_CLASS = 'app-dialog__header'

const attrs = useAttrs()
const dialogWidth = computed(() => (attrs.width as any) ?? '680px')

const onCancelClick = () => {
  emit('cancel')
  emit('update:visible', false)
}

const confirming = ref(false)
const onConfirmClick = async () => {
  if (props.confirm) {
    confirming.value = true
    try {
      const close = () => emit('update:visible', false)
      await Promise.resolve(props.confirm({ close }))
    } finally {
      confirming.value = false
    }
    return
  }
  emit('confirm')
  emit('update:visible', false)
}
</script>
<style lang="scss">
.app-dialog {
  padding: 0 !important;
  border-radius: 12px 12px 12px 12px !important;
  max-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-dialog .el-dialog__header,
.app-dialog .el-dialog__footer {
  flex: 0 0 auto;
}

.app-dialog .el-dialog__body {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
}

.app-dialog__message {
  font-size: 14px;
  line-height: 22px;
  color: #1d2129;
  white-space: pre-wrap;
}

.app-dialog__header {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #fff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
  padding-bottom: 0;
}

.app-dialog .el-dialog__header {
  padding-bottom: 0 !important;
  border-bottom: 0 !important;
}

.app-dialog__footer {
  height: 80px;
  display: flex;
  align-items: center;
}

.app-dialog__footer-btns {
  gap: 16px;
  width: 100%;
  padding: 0 40px;
  display: flex;
  align-items: center;
}

.app-dialog__footer-btns > * {
  flex: 1 1 0;
}

.app-dialog__footer-btns .el-button + .el-button {
  margin-left: 0 !important;
}

.app-dialog__footer-btns .el-button {
  background: #f2f3f5;
  border-radius: 2px;
}

.app-dialog__footer-btns .el-button--primary {
  background: #165dff;
  border-radius: 2px;
}

.app-dialog .el-button,
.app-dialog .el-button:hover,
.app-dialog .el-button:focus,
.app-dialog .el-button:active {
  border: none !important;
}

.app-dialog .el-button.el-butto__btn-keep-border {
  border: 1px solid var(--el-button-border-color) !important;
}

.app-dialog .el-button.el-butto__btn-keep-border:hover {
  border-color: var(--el-button-hover-border-color) !important;
}

.app-dialog .el-button.el-butto__btn-keep-border:focus {
  border-color: var(--el-button-hover-border-color) !important;
}

.app-dialog .el-button.el-butto__btn-keep-border:active {
  border-color: var(--el-button-active-border-color) !important;
}

.app-dialog__footer-btns .el-button:not(.is-disabled):hover,
.app-dialog__footer-btns .el-button:not(.is-disabled):focus,
.app-dialog__footer-btns .el-button:not(.is-disabled):active {
  background: #f2f3f5 !important;
}
.app-dialog__footer-btns .el-button--primary:not(.is-disabled):hover,
.app-dialog__footer-btns .el-button--primary:not(.is-disabled):focus,
.app-dialog__footer-btns .el-button--primary:not(.is-disabled):active {
  background: #165dff !important;
  border-color: #165dff !important;
}

.app-dialog.el-dialog .el-dialog__body,
.app-dialog .el-dialog__body {
  padding: 24px 32px !important;
}

.app-dialog .el-button {
  --el-button-hover-text-color: var(--el-button-text-color);
  --el-button-hover-bg-color: var(--el-button-bg-color);
  --el-button-hover-border-color: var(--el-button-border-color);
  --el-button-active-text-color: var(--el-button-text-color);
  --el-button-active-bg-color: var(--el-button-bg-color);
  --el-button-active-border-color: var(--el-button-border-color);
  --el-button-outline-color: transparent;
}

.app-dialog .el-dialog__body .el-form-item__label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 400;
  text-align: right;
  font-style: normal;
  text-transform: none;
}

.app-dialog.el-dialog .el-dialog__footer,
.app-dialog .el-dialog__footer {
  padding-top: 0 !important;
}
</style>
