<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { usePermissionsStore } from '@h5/store'
import type { BrandItem } from '@h5/api/brand/types.d.ts'

defineOptions({
  name: 'BrandList'
})

const props = defineProps<{
  /**
   * 默认选中品牌编码（由父组件决定）
   * - 不传时兜底选中列表第一项
   */
  defaultBrandCode?: string
}>()

const emit = defineEmits<{
  'brand-click': [brand: BrandItem]
}>()

const permissionsStore = usePermissionsStore()

// 品牌列表来源于 H5 权限 Store
const brandList = computed<BrandItem[]>(() => permissionsStore.getBrandListForHome)
// 当前选中品牌
const currentBrand = ref<BrandItem | null>(null)

/**
 * 初始化当前选中品牌
 * - 优先使用父组件传入的默认品牌（defaultBrandCode）
 * - 否则使用列表第一项
 * - 初始化完成后，通过事件将选中品牌通知父组件
 */
const initCurrentBrand = () => {
  const list = brandList.value || []
  if (!list.length) {
    currentBrand.value = null
    return
  }

  let def = list[0]
  const defCode = props.defaultBrandCode
  if (typeof defCode === 'string' && defCode) {
    const found = list.find(el => el.key === defCode)
    if (found) {
      def = found
    }
  }

  // 如果当前品牌发生变化，则触发一次“品牌点击”事件，驱动父组件加载数据
  const shouldEmit = !currentBrand.value || currentBrand.value.key !== def.key
  currentBrand.value = def
  if (shouldEmit) {
    emit('brand-click', def)
  }
}

// 品牌列表变化时，重新计算默认选中品牌
watch(
  brandList,
  () => {
    initCurrentBrand()
  },
  { immediate: true }
)

// 默认品牌变化时，重新计算默认选中品牌
watch(
  () => props.defaultBrandCode,
  () => {
    initCurrentBrand()
  }
)

/**
 * 处理品牌点击
 */
const handleBrandClick = (brand: BrandItem) => {
  if (!brand || brand.key === currentBrand.value?.key) return
  currentBrand.value = brand
  emit('brand-click', brand)
}

onMounted(() => {
  console.log('BrandList 组件已挂载')
})
</script>
<template>
  <div v-if="brandList.length > 0" class="brand-list pl-12 pr-12">
    <!-- 品牌列表 -->
    <div
      class="flex-y-center brand-grid"
      :class="{ 'brand-five': brandList.length <= 5, 'brand-more': brandList.length > 5 }"
    >
      <div
        v-for="item in brandList"
        :key="item.key"
        @click="handleBrandClick(item)"
        class="brand-item flex-center"
        :class="{ active: item.key === currentBrand?.key }"
      >
        <div class="brand-name mt-4">{{ item.value }}</div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
/* 隐藏整个滚动条 */
::-webkit-scrollbar {
  width: 0;
  height: 0;
  background-color: transparent;
}

.brand-list {
  height: 40px;
  display: flex;
  align-items: center;
  //border-bottom: 1px solid #EBEDF0;
  background: white;

  .brand-grid {
    flex-wrap: nowrap;
  }

  .brand-more {
    gap: 21px;
    overflow-y: hidden;
    overflow-x: auto;
  }

  .brand-five {
    width: 100%;
    justify-content: space-between;
  }

  .brand-item {
    flex: 0 0 auto;

    &.active {
      position: relative;

      .brand-name {
        font-weight: 600;
        font-size: 14px;
        color: #1677FF;
      }

      &:before {
        position: absolute;
        content: "";
        bottom: -12px;
        left: 50%;
        transform: translateX(-50%);
        width: 100%;
        height: 2px;
        background-color: #1677FF;
      }
    }

    .brand-name {
      font-weight: 600;
      font-size: 14px;
      color: #1D252F;
    }
  }
}
</style>
