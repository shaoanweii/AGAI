<script setup lang="ts">
import { onMounted } from 'vue'
import type { BrandItem } from '@h5/api/brand/types.d.ts'

defineOptions({
  name: 'BrandList'
})

const props = defineProps<{
  currentBrand: BrandItem | null
  brandList: BrandItem[]
}>()

const emit = defineEmits<{
  'brand-click': [brand: BrandItem]
}>()

/**
 * 处理品牌点击
 */
const handleBrandClick = (brand: BrandItem) => {
  emit('brand-click', brand)
}

onMounted(() => {
  console.log('BrandList mounted')
})

</script>
<template>
  <div v-if="brandList.length > 0" class="brand-list pt-10 pb-10 pl-12 pr-12">
    <!-- 品牌列表 -->
    <div class="flex-y-center brand-grid overflow-auto">
      <div
        v-for="item in brandList"
        :key="item.key"
        @click="handleBrandClick(item)"
        class="brand-item flex-center"
        :class="{'active': item.key === currentBrand?.key }"
      >
        <van-image
          width="20"
          height="20"
          :src="item.img"
          fit="cover"
          radius="4"
          :alt="item.value"
          loading-icon="photo"
          error-icon="photo-fail"
        />
        <div class="brand-name mt-4">{{ item.value }}</div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
.brand-list {
  background: #1677FF;
  .loading-state {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .brand-grid {
    flex-wrap: nowrap;
    gap: 8px;
  }

  .brand-item {
    flex: 0 0 auto;
    background: #ffffff;
    border-radius: 8px;
    min-width: 63px;
    padding: 0 4px;
    height: 57px;
    cursor: pointer;
    flex-direction: column;
    opacity: 0.5;
    color: #1D252F;

    &.active {
      opacity: 1;
      color: #1677FF !important;
    }

    .brand-name {
      font-weight: 600;
      font-size: 12px;
      white-space: nowrap;
    }
  }
}
</style>
