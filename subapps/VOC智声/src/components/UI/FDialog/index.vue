<template>
  <el-dialog
    v-model="innerVisible"
    v-bind="$attrs"
    class="app-dialog"
    :header-class="APP_DIALOG_HEADER_CLASS"
    :width="dialogWidth"
    align-center
    :destroy-on-close="destoryOnClose"
    @close="handleClose"
  >
    <!-- 主内容插槽 -->
    <slot />
    <!-- 头部插槽（可选） -->
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    <!-- 统一底部区包装，默认渲染取消/确定；若传入具名 footer 插槽则以插槽为准 -->
    <template v-if="$slots.footer || props.showFooter" #footer>
      <div class="app-dialog__footer">
        <div class="app-dialog__footer-btns">
          <slot name="footer">
            <el-button class="app-dialog__btn-cancel" @click="onCancelClick">{{
              props.cancelText
            }}</el-button>
            <el-button
              class="app-dialog__btn-confirm"
              type="primary"
              :loading="confirming"
              @click="onConfirmClick"
              >{{ props.confirmText }}</el-button
            >
          </slot>
        </div>
      </div>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import { computed, ref, useAttrs } from 'vue'

defineOptions({ name: 'FDialog' })

interface Props {
  // 对话框显示状态，由外部控制
  visible: boolean
  // 是否展示默认底部（取消/确定）。当具名插槽 footer 存在时，以插槽为准
  showFooter?: boolean
  // 关闭时是否销毁组件
  destoryOnClose?: boolean
  // 取消按钮文案
  cancelText?: string
  // 确认按钮文案
  confirmText?: string
  // 自定义确定处理函数，存在时接管默认行为
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

// 与外部 v-model:visible 保持同步
const innerVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val)
})

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 统一头部类名（内部固定）
const APP_DIALOG_HEADER_CLASS = 'app-dialog__header'

// 默认宽度 680px，可被外部同名属性覆盖
const attrs = useAttrs()
const dialogWidth = computed(() => (attrs.width as any) ?? '680px')

const onCancelClick = () => {
  emit('cancel')
  emit('update:visible', false)
}

// 确定按钮：
// - 若提供 confirm 函数，则调用该函数并在其内部控制关闭时机；
// - 否则保持原有行为，仅触发 confirm 事件。
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

/* 强制移除对话框头部下内边距，避免库内 padding 简写覆盖 */
.app-dialog .el-dialog__header {
  padding-bottom: 0 !important;
}

.app-dialog__footer {
  height: 80px;
  border-top: 1px solid #ebedf0;
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

/* 平分空间：所有直接子元素等宽分布 */
.app-dialog__footer-btns > * {
  flex: 1 1 0;
}

/* 按钮样式约定：默认按钮与主按钮统一圆角，移除相邻 margin */
.app-dialog__footer-btns .el-button + .el-button {
  margin-left: 0 !important;
}

/* 普通按钮（非 primary） */
.app-dialog__footer-btns .el-button {
  background: #f2f3f5;
  border-radius: 2px;
}

/* 主按钮 */
.app-dialog__footer-btns .el-button--primary {
  background: #165dff;
  border-radius: 2px;
}

/* 统一去除对话框内按钮边框（所有变体与状态） */
.app-dialog .el-button,
.app-dialog .el-button:hover,
.app-dialog .el-button:focus,
.app-dialog .el-button:active {
  border: none !important;
}

/* 去除 hover 态的视觉变化（保持背景不变） */
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

/* 内容区默认留白 */
.app-dialog.el-dialog .el-dialog__body,
.app-dialog .el-dialog__body {
  padding: 24px 32px !important;
}

/* 在对话框作用域内回填按钮状态变量，消除 hover/active 视觉差异 */
.app-dialog .el-button {
  --el-button-hover-text-color: var(--el-button-text-color);
  --el-button-hover-bg-color: var(--el-button-bg-color);
  --el-button-hover-border-color: var(--el-button-border-color);
  --el-button-active-text-color: var(--el-button-text-color);
  --el-button-active-bg-color: var(--el-button-bg-color);
  --el-button-active-border-color: var(--el-button-border-color);
  --el-button-outline-color: transparent;
}

/* 主内容区域表单标签样式 */
.app-dialog .el-dialog__body .el-form-item__label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 400;
  // line-height: 22px;
  text-align: right;
  font-style: normal;
  text-transform: none;
}

/* 重置底部容器上内边距，避免顶部空隙 */
.app-dialog.el-dialog .el-dialog__footer,
.app-dialog .el-dialog__footer {
  padding-top: 0 !important;
}
</style>
