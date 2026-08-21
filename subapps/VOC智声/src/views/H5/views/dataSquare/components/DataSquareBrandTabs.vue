<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { H5DataSquareBrandItem } from '@h5/api/dataSquare'

defineOptions({
  name: 'DataSquareBrandTabs'
})

const props = defineProps<{
  list: H5DataSquareBrandItem[]
  defaultBrandCode?: string
  defaultBrandReady: boolean
}>()

const emit = defineEmits<{
  change: [brand: H5DataSquareBrandItem]
}>()

const currentBrandCode = ref('')
// 默认品牌尚未准备好时，不立即回退到首项，避免先发一次错误的首页请求
const hasManualSelected = ref(false)

const sortedList = computed(() => {
  return [...(props.list || [])].sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
})

/**
 * 根据默认值或首项初始化选中品牌。
 * - 默认品牌来自菜单 jsonObject，与首页/任务页保持同源
 * - 默认品牌不在广场品牌列表中时，回退到接口首项
 */
const initCurrentBrand = () => {
  const list = sortedList.value
  if (!list.length) {
    currentBrandCode.value = ''
    hasManualSelected.value = false
    return
  }

  if (!props.defaultBrandReady) {
    return
  }

  if (hasManualSelected.value) {
    return
  }

  const matched = props.defaultBrandCode
    ? list.find(item => item.brandCode === props.defaultBrandCode)
    : undefined
  const next = matched || list[0]
  if (currentBrandCode.value === next.brandCode) {
    return
  }

  currentBrandCode.value = next.brandCode
  emit('change', next)
}

/**
 * 处理品牌切换。
 * @param brand 当前点击品牌
 */
const handleBrandClick = (brand: H5DataSquareBrandItem) => {
  if (!brand?.brandCode || brand.brandCode === currentBrandCode.value) return
  hasManualSelected.value = true
  currentBrandCode.value = brand.brandCode
  emit('change', brand)
}

watch(
  () => [props.list, props.defaultBrandCode, props.defaultBrandReady],
  () => {
    initCurrentBrand()
  },
  { immediate: true }
)
</script>

<template>
  <div v-if="sortedList.length" class="brand-tabs">
    <button
      v-for="item in sortedList"
      :key="item.brandCode"
      class="brand-tabs__item"
      :class="{ 'brand-tabs__item--active': item.brandCode === currentBrandCode }"
      type="button"
      @click="handleBrandClick(item)"
    >
      {{ item.brandName }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.brand-tabs {
  height: 40px;
  padding: 0 12px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 21px;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }

  &__item {
    position: relative;
    height: 40px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #1d252f;
    font-size: 14px;
    line-height: 40px;
    font-weight: 600;
    white-space: nowrap;
    flex: 0 0 auto;

    &--active {
      color: #1677ff;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 2px;
        border-radius: 2px;
        background: #1677ff;
      }
    }
  }
}
</style>
