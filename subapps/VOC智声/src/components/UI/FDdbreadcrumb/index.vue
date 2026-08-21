<template>
  <div class="reser-card-title">
    <span class="line-height">
      【
      <span
        v-for="(item, index) in breadcrumbList"
        :key="index"
        @click="handleBreadcrumbClick(item, index)"
      >
        <span v-if="index > 0">
          <span v-if="isSameLevel(index)">，</span>
          <span v-else> > </span>
        </span>
        <span class="cursor-point">{{ item.name }}</span>
      </span>
      】
    </span>
    <span>{{ suffix }}</span>
  </div>
</template>

<script setup lang="ts">
import type { BreadcrumbItem, FDdbreadcrumbProps, FDdbreadcrumbEmits } from './types'

defineOptions({
  name: 'FDdbreadcrumb'
})

const props = withDefaults(defineProps<FDdbreadcrumbProps>(), {
  breadcrumbList: () => [],
  suffix: '场景分析'
})

const emit = defineEmits<FDdbreadcrumbEmits>()

// 判断当前项和上一项是否是同级
const isSameLevel = (index: number): boolean => {
  if (index === 0) {
    return false
  }
  const currentItem = props.breadcrumbList[index]
  const previousItem = props.breadcrumbList[index - 1]

  // 如果两个项都有 level 属性，且 level 相同，则认为是同级
  if (currentItem?.level !== undefined && previousItem?.level !== undefined) {
    return currentItem.level === previousItem.level
  }

  // 默认使用 > 分隔
  return false
}

const handleBreadcrumbClick = (item: BreadcrumbItem, index: number) => {
  emit('breadcrumb-click', item, index)
}
</script>

<style scoped lang="scss">
.reser-card-title {
  .line-height {
    line-height: 1.5;
  }

  .cursor-point {
    cursor: pointer;

    &:hover {
      color: var(--el-color-primary);
    }
  }
}
</style>
