<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

defineOptions({
  name: 'FCollapseSection'
})

const props = withDefaults(
  defineProps<{
    title?: string
    showToggle?: boolean
  }>(),
  {
    title: '',
    showToggle: true
  }
)

defineSlots<{
  default?: () => any
  title?: () => any
  titleExtra?: () => any
}>()

const isExpanded = defineModel<boolean>({ default: true })

/**
 * 切换容器展开态。
 * 统一承接 PC 端“标题 + 展开收起”场景，外层可通过 v-model 接管状态。
 */
const toggleExpanded = () => {
  if (!props.showToggle) return
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <div class="f-collapse-section">
    <div class="f-collapse-section__header">
      <div class="f-collapse-section__left">
        <div class="f-collapse-section__title-wrap">
          <span class="f-collapse-section__title-bar"></span>
          <div class="f-collapse-section__title">
            <slot name="title">{{ props.title }}</slot>
          </div>
        </div>

        <slot name="titleExtra"></slot>
      </div>

      <button
        v-if="props.showToggle"
        type="button"
        class="f-collapse-section__toggle"
        @click="toggleExpanded"
      >
        <span>{{ isExpanded ? '收起' : '展开' }}</span>
        <el-icon class="f-collapse-section__toggle-icon">
          <component :is="isExpanded ? ArrowUp : ArrowDown" />
        </el-icon>
      </button>
    </div>

    <el-collapse-transition>
      <div v-show="isExpanded" class="f-collapse-section__body">
        <slot></slot>
      </div>
    </el-collapse-transition>
  </div>
</template>

<style scoped lang="scss">
.f-collapse-section {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.f-collapse-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.f-collapse-section__left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.f-collapse-section__title-wrap {
  display: flex;
  align-items: center;
  min-width: 0;
}

.f-collapse-section__title-bar {
  width: 2px;
  height: 15px;
  margin-right: 8px;
  background: #1677ff;
  border-radius: 99px;
  flex: none;
}

.f-collapse-section__title {
  min-width: 0;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: rgba(0, 0, 0, 0.9);
  word-break: break-word;
}

.f-collapse-section__toggle {
  padding: 0;
  border: none;
  background: transparent;
  color: #1f2733;
  font-size: 14px;
  line-height: 22px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  flex: none;
}

.f-collapse-section__toggle-icon {
  font-size: 14px;
  color: #86909c;
}

.f-collapse-section__body {
  margin-top: 16px;
  min-width: 0;
}
</style>
