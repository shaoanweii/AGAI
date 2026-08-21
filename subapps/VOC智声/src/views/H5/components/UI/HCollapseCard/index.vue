<script setup lang="ts">
import { ref, watch } from 'vue'

defineOptions({
  name: 'HCollapseCard'
})

interface HCollapseCardProps {
  /** 标题文案 */
  title: string
  /** 是否默认展开内容 */
  defaultExpanded?: boolean
  /** 是否启用折叠功能，默认不启用 */
  collapsible?: boolean
}

const props = withDefaults(defineProps<HCollapseCardProps>(), {
  title: '',
  defaultExpanded: true,
  collapsible: false
})

// 当前展开状态，默认跟随 defaultExpanded
const isExpanded = ref<boolean>(props.defaultExpanded)

// 当外部默认值变化时，同步一次内部状态，避免重复创建组件时状态不一致
watch(
  () => props.defaultExpanded,
  value => {
    isExpanded.value = value
  }
)

// 点击头部切换展开 / 收起
const handleToggle = () => {
  if (!props.collapsible) return
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <div
    class="h-collapse-card"
    :class="{
      'h-collapse-card--expanded': collapsible ? isExpanded : true,
      'h-collapse-card--collapsed': collapsible && !isExpanded
    }"
  >
    <div
      class="h-collapse-card__header"
      :class="{ 'h-collapse-card__header--collapsible': collapsible }"
      @click="collapsible && handleToggle()"
    >
      <div class="h-collapse-card__title-wrap">
        <span class="h-collapse-card__title-bar" />
        <span class="h-collapse-card__title">{{ title }}</span>
      </div>
      <div v-if="collapsible" class="h-collapse-card__icon">
        <van-icon :name="isExpanded ? 'arrow-up' : 'arrow-down'" color="#C0C4CC" size="16" />
      </div>
    </div>

    <transition name="h-collapse-card">
      <div v-show="collapsible ? isExpanded : true" class="h-collapse-card__body">
        <slot />
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.h-collapse-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 4px 12px rgba(31, 39, 51, 0.04);
}

.h-collapse-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.h-collapse-card__header--collapsible {
  cursor: pointer;
}

.h-collapse-card__title-wrap {
  display: flex;
  align-items: center;
  min-width: 0;
}

.h-collapse-card__title-bar {
  width: 2px;
  height: 16px;
  border-radius: 2px;
  background: #1677ff;
  margin-right: 8px;
}

.h-collapse-card__title {
  font-weight: 500;
  font-size: 14px;
  line-height: 20px;
  color: #1f2733;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.h-collapse-card__icon {
  display: flex;
  align-items: center;
}

.h-collapse-card__body {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef2;
}

/* 折叠内容简单过渡动画 */
.h-collapse-card-enter-active,
.h-collapse-card-leave-active {
  transition: all 0.2s ease;
}

.h-collapse-card-enter-from,
.h-collapse-card-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
