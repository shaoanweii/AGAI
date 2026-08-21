<script setup lang="ts">
import { shallowRef, watch } from 'vue'

defineOptions({
  name: 'BatchProcessSectionCard'
})

interface ProcessSectionCardProps {
  /** 模块标题 */
  title: string
  /** 是否支持折叠 */
  collapsible?: boolean
  /** 默认展开状态 */
  defaultExpanded?: boolean
}

const props = withDefaults(defineProps<ProcessSectionCardProps>(), {
  title: '',
  collapsible: true,
  defaultExpanded: true
})

const isExpanded = shallowRef(props.defaultExpanded)

watch(
  () => props.defaultExpanded,
  value => {
    isExpanded.value = value
  }
)

/**
 * 切换卡片展开状态，仅在开启 collapsible 时生效。
 */
const toggleExpanded = () => {
  if (!props.collapsible) return
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <section class="process-section-card">
    <div
      class="process-section-card__header"
      :class="{ 'is-clickable': props.collapsible }"
      @click="toggleExpanded"
    >
      <div class="process-section-card__title-wrap">
        <span class="process-section-card__title-bar"></span>
        <span class="process-section-card__title">{{ props.title }}</span>
        <slot name="title-extra" />
      </div>

      <van-icon
        v-if="props.collapsible"
        :name="isExpanded ? 'arrow-up' : 'arrow-down'"
        color="#8C98A8"
        size="16"
      />
    </div>

    <transition name="process-section-card">
      <div v-show="props.collapsible ? isExpanded : true" class="process-section-card__body">
        <slot />
      </div>
    </transition>
  </section>
</template>

<style scoped lang="scss">
.process-section-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 12px rgba(31, 39, 51, 0.04);
}

.process-section-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 20px;
}

.process-section-card__header.is-clickable {
  cursor: pointer;
}

.process-section-card__title-wrap {
  display: flex;
  align-items: center;
  min-width: 0;
}

.process-section-card__title-bar {
  width: 2px;
  height: 16px;
  margin-right: 8px;
  border-radius: 2px;
  background: #1677ff;
}

.process-section-card__title {
  flex: none;
  font-weight: 500;
  font-size: 16px;
  line-height: 22px;
  color: #1f2733;
}

.process-section-card__body {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef2;
}

.process-section-card-enter-active,
.process-section-card-leave-active {
  transition: all 0.2s ease;
}

.process-section-card-enter-from,
.process-section-card-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
